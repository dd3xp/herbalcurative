# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 模型更新

私有资产目录里面的json文件需要改一下纹理路径

## 多方块模型:运行时 split loader

### 思想

Blockbench 导出的 oversized 多方块模型(元素坐标跨 `[−16, 32]`)不再预先切成 per-position JSON。改为在 bake 时由自定义 NeoForge geometry loader `herbalcurative:split` 运行时切片,缓存 `[mirror][position][side]` 的 `BakedQuad` 表,按 block state 的 `position` / `mirrored` 属性分派。同一份 base model JSON 同时喂渲染、VoxelShape、物品显示,**无任何预生成产物**。

### 数据流

```
common/.../models/block/<name>.json   ← Blockbench 导出,根部带 "loader": "herbalcurative:split"
             │
             ├─► 客户端:SplitGeometryLoader → SplitUnbakedGeometry.bake()
             │      剥 rotation:{angle:0} → 计算 grid bounds → clipElement + mirrorElementX
             │      每 (mirror,pos) 一份 clipped element 子集 → vanilla BlockModel.bake
             │      结果塞进 BakedSplitModel 的 quad 表
             │
             └─► 服务端/两端:CustomVoxelShapes.loadFromModel(path)
                    读同一 JSON → 按 element 中心点归格 → 合成 per-cell VoxelShape
```

### 关键类

| 类 | 位置 | 作用 |
|---|---|---|
| [`SplitGeometryLoader`](neoforge/src/main/java/com/cahcap/neoforge/client/model/split/SplitGeometryLoader.java) | neoforge | 注册入口,key = `herbalcurative:split` |
| [`SplitUnbakedGeometry`](neoforge/src/main/java/com/cahcap/neoforge/client/model/split/SplitUnbakedGeometry.java) | neoforge | 运行时 clip / mirror / bake,全部逻辑 |
| [`BakedSplitModel`](neoforge/src/main/java/com/cahcap/neoforge/client/model/split/BakedSplitModel.java) | neoforge | `getQuads(state, side, rand)` 按 state 查表,item 渲染时返回全模型 |
| [`CustomVoxelShapes.loadFromModel`](common/src/main/java/com/cahcap/common/util/CustomVoxelShapes.java) | common | 读同一 model JSON 产出 VoxelShape;支持 `excludeGroups` 跳过 Blockbench 组 |

Loader 在 [`HerbalCurativeNeoForgeClient.registerGeometryLoaders`](neoforge/src/main/java/com/cahcap/neoforge/HerbalCurativeNeoForgeClient.java) 里注册。

### 哪些多方块走这条路

6 个: `cauldron`、`herb_cabinet`、`herb_vault`、`kiln`、`obelisk`、`workbench`。这 6 个 model JSON 根部有 `"loader": "herbalcurative:split"` 标记。

另外 4 个 (`herb_pot`、`herb_basket`、`incense_burner`、`shelf`) 的 block model 不跨格,走 vanilla bake;它们仅通过 `CustomVoxelShapes.loadFromModel` 获取 VoxelShape。

### Blockstate JSON 形式

6 个走 split loader 的多方块 blockstate 退化成 4-5 条 variant:

```json
{
  "variants": {
    "formed=false": { "model": "herbalcurative:block/lumistone" },
    "facing=north,formed=true": { "model": "herbalcurative:block/cauldron" },
    "facing=south,formed=true": { "model": "herbalcurative:block/cauldron", "y": 180 },
    "facing=east,formed=true":  { "model": "herbalcurative:block/cauldron", "y": 90 },
    "facing=west,formed=true":  { "model": "herbalcurative:block/cauldron", "y": 270 }
  }
}
```

Position / mirrored / lit 等属性由 `BakedSplitModel.getQuads` 从 state 读,不进 blockstate 文件。`workbench` 无 `formed` 属性,只有 4 条 facing variant。

### 新增一个多方块要做的事

1. Blockbench 建模,导出到 `common/src/main/resources/assets/herbalcurative/models/block/<name>.json`
2. 修纹理路径(加 `herbalcurative:block/` 命名空间前缀)
3. **根部加** `"loader": "herbalcurative:split"` 一行(在 `format_version` 同级)
4. 写 blockstate JSON(模板见上,换模型名和占位方块)
5. 写 item model JSON:`{ "parent": "herbalcurative:block/<name>" }`
6. Block 类 `private static final CustomVoxelShapes SHAPES = CustomVoxelShapes.loadFromModel("/assets/herbalcurative/models/block/<name>.json");`
7. 如果模型带 "配饰" 组(例如 incense 香条、rope)需排除出碰撞,给 `loadFromModel` 传第二参数 `Set.of("GroupName")`

**没有 Gradle task 要跑。** 改完 Blockbench 模型,F3+T 或重启客户端即可看到改动。

### 什么还需要 runData

- loot 表、recipe、block/item/biome tag、worldgen (biome modifier) —— 都还在 NeoForge datagen 里,跑 `./gradlew :neoforge:runData`
- 模型相关的四件事(剥 rotation、split、voxelshape、blockstate)**全部运行时完成**,无需 datagen 也无需 buildSrc task

### AO 光照格子历史

Blockbench 导出的 elements 含 `rotation: {angle: 0, ...}` 会让 Minecraft 走不同 bake 路径,产生 AO 不连续。loader 在读入 JSON 后自动剥离这种 zero-angle rotation(见 `SplitUnbakedGeometry.cleanElements`),源 JSON 不需要手工清理。
