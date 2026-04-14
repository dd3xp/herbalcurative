# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 模型更新

私有资产目录里面的json文件需要改一下纹理路径

## AO 光照格子问题 - 已修复

**根因**:Blockbench 导出的模型 JSON 中每个 element 都带有 `rotation: {angle: 0, axis: y, origin: ...}`。虽然 angle=0 无实际旋转,但 Minecraft 在 bake 模型时对有 `rotation` 属性的 element 走不同的渲染代码路径,影响 AO(Ambient Occlusion)的计算 —— 相邻 element 之间、以及多方块的相邻方块之间都会出现亮度不连续的格子。

### 规则:所有会被 Minecraft 直接渲染的 block 模型 JSON 里,element 都不能带 `rotation: {angle: 0, ...}`

由 [`:common:processModels`](buildSrc/src/main/java/com/cahcap/tools/models/BlockModelProcessor.java) 自动剥离,**不需要手工维护**。Blockbench 导出脏 JSON → 跑一次 task → 原地清洁。

## 自定义模型的处理流程(不用 runData)

**重要变更**:模型处理已从 `runData`(NeoForge datagen)迁到 buildSrc 的独立 Gradle task。不需要启动 Minecraft,毫秒级完成,跨平台共享产物。

### 触发方式

```bash
./gradlew :common:processModels
```

改了 Blockbench 源模型后跑这一条。task 定义在 [common/build.gradle](common/build.gradle),实现在 [buildSrc/src/main/java/com/cahcap/tools/models/](buildSrc/src/main/java/com/cahcap/tools/models/)。

### 四个处理器

| 处理器 | 输入 | 输出 |
|---|---|---|
| [BlockModelProcessor](buildSrc/src/main/java/com/cahcap/tools/models/BlockModelProcessor.java) | `common/.../models/block/<name>.json` (脏) | 原地覆写(清洁) |
| [ModelSplitProcessor](buildSrc/src/main/java/com/cahcap/tools/models/ModelSplitProcessor.java) | `common/.../models/block/<multiblock>.json` | `common/.../models/split/<name>_part_X_Y_Z.json` + `_mirrored` 变体 |
| [VoxelShapeProcessor](buildSrc/src/main/java/com/cahcap/tools/models/VoxelShapeProcessor.java) | `common/.../models/block/<name>.json` | `common/.../voxelshapes/<name>.json` |
| [MultiblockStateProcessor](buildSrc/src/main/java/com/cahcap/tools/models/MultiblockStateProcessor.java) | `common/.../models/block/<multiblock>.json` | `common/.../blockstates/<name>.json` (覆写) |

### 完整数据流

```
common/.../models/block/<name>.json         ← Blockbench 脏导出
                 ↓
         :common:processModels
         ├─ BlockModelProcessor 原地清洁 ────┐
         ├─ ModelSplitProcessor              │
         ├─ VoxelShapeProcessor              │
         └─ MultiblockStateProcessor         │
                 ↓                           │
common/.../models/block/<name>.json  ←──────┘ (清洁版)
common/.../models/split/<name>_part_X_Y_Z.json
common/.../voxelshapes/<name>.json
common/.../blockstates/<multiblock>.json  (覆写)
                 ↓
       Minecraft 直接从 common 资源加载
```

### 为什么是 buildSrc task 而不是 DataProvider

之前用 NeoForge 的 `DataProvider` 走 `runData`,问题:
1. 启动完整 MC 运行时要分钟级
2. 输出落在 `neoforge/src/main/generated/`,Fabric 加进来后要么重新跑一次要么复制过去,都别扭
3. 逻辑本质是纯 JSON in/out,根本不需要 MC

迁到 buildSrc 之后:
- 秒级完成,不启 MC
- 输出落在 common,所有平台自动共享
- 加 Fabric 时完全不需要重复配置这块

### blockstate 和 item model 的引用方式

custom 这条伪路径已废弃。现在一律用 vanilla 的 `block/` 前缀:

- **单方块 blockstate**(手写在 `common/.../blockstates/`):`"model": "herbalcurative:block/<name>"`
- **多方块 blockstate**(processModels 覆写生成):
  - `formed=true` → `"herbalcurative:split/<name>_part_X_Y_Z"`
  - `formed=false` → `"herbalcurative:block/<占位方块>"`(如 `block/lumistone`)
- **item model**(手写在 `common/.../models/item/`):`"parent": "herbalcurative:block/<name>"`
- **WorkbenchRenderer 工具模型**:加载路径 `block/workbench_tool_*`

### 新增一个自定义形状方块时需要做的事

1. 在 Blockbench 里建模,导出 `.json` 到 `common/src/main/resources/assets/herbalcurative/models/block/<name>.json`
2. 修纹理路径(加 `herbalcurative:block/` 命名空间前缀)
3. 在 [BlockModelProcessor.MODELS](buildSrc/src/main/java/com/cahcap/tools/models/BlockModelProcessor.java) 列表里加一行 `"<name>"`
4. 在 [VoxelShapeProcessor](buildSrc/src/main/java/com/cahcap/tools/models/VoxelShapeProcessor.java) 的 `run()` 里加一条 `process("<name>", "<name>", Set.of())`
5. 写 blockstate JSON,model 引用用 `herbalcurative:block/<name>`
6. 写 item model JSON,parent 用 `herbalcurative:block/<name>`
7. Block 类里 `MultiblockShapes.load("/assets/herbalcurative/voxelshapes/<name>.json")` + `getByIndex(facing, 0, false)`
8. 跑 `./gradlew :common:processModels`(多方块需要再进 [MultiblockStateProcessor.CONFIGS](buildSrc/src/main/java/com/cahcap/tools/models/MultiblockStateProcessor.java) 加一行)

### 什么还需要 runData

- loot 表、recipe、block/item/biome tag、worldgen(biome modifier)—— 都还在 NeoForge datagen 里,跑 `./gradlew :neoforge:runData`
- 模型相关的四件事**不再需要** runData
