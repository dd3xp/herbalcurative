# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 模型更新

私有资产目录里面的json文件需要改一下纹理路径

## AO 光照格子问题 - 已修复

**根因**：Blockbench 导出的模型 JSON 中每个 element 都带有 `rotation: {angle: 0, axis: y, origin: ...}`。虽然 angle=0 无实际旋转，但 Minecraft 在 bake 模型时对有 `rotation` 属性的 element 走不同的渲染代码路径，影响 AO（Ambient Occlusion）的计算 —— 相邻 element 之间、以及多方块的相邻方块之间都会出现亮度不连续的格子。

### 规则：所有会被 Minecraft 直接渲染的 block 模型 JSON 里，element 都不能带 `rotation: {angle: 0, ...}`

"直接渲染"指被 blockstate 或 BlockEntityRenderer 引用、交给 Minecraft model baker 的那些。具体来说：

- **多方块 `_part_` 模型**（由 ModelSplitProvider 输出）：**自动保证干净**。ModelSplitProvider 在 [line 329-343](neoforge/src/main/java/com/cahcap/neoforge/common/datagen/models/ModelSplitProvider.java#L329-L343) 的裁剪逻辑里显式跳过 `angle == 0` 的 rotation，不写入 `_part_` 文件。所以**不要手动去 `_part_` 模型里加 `rotation: {angle: 0}`**（会让 ModelSplitProvider 的防护失效）。
- **单块自定义形状模型**（herb_basket_floor / herb_basket_wall / herb_pot / incense_burner / red_cherry_shelf）：这些是 Minecraft 直接加载的最终文件，**必须手动保证清洁**。从 Blockbench 导出或同步到 `common/.../models/block/` 之前需要剥离一遍。
- **BlockEntityRenderer 直接加载的独立模型**（如 [WorkbenchRenderer](common/src/main/java/com/cahcap/client/renderer/WorkbenchRenderer.java) 里的 `workbench_tool_*`）：同上，必须手动保证清洁。
- **多方块源文件**（cauldron / herb_cabinet / herb_vault / kiln / obelisk / workbench）：虽然只作为 ModelSplitProvider 的输入、不被直接渲染，但 `MultiblockStateProvider` 的 `unformedModel` 字段偶尔会指回源文件（例如 herb_cabinet），那时就会变成直接渲染。**保持源文件清洁**比记住每个方块的 unformed 状态来自哪里更稳。

### 怎么剥离

单行 Blockbench compact 和多行 pretty-print 两种格式都要能处理。Python 正则一把梭：

```python
import re
pattern = re.compile(
    r'^[ \t]*"rotation":\s*\{[^{}]*?"angle":\s*0\b[^{}]*?\},?\s*\n',
    re.MULTILINE
)
# 对每个目标 .json 文件：
#   content = pattern.sub('', content)
```

`[^{}]*?` 保证不会跨越嵌套的大括号（rotation 对象里没有嵌套 `{}`，安全）。只会删 `angle == 0` 的 rotation，真旋转（±22.5、45°等）全部保留。

### 不要踩的坑

- `display.thirdperson_righthand` 等里面的 `"rotation": [75, 45, 0]` 是 item 变换的 3 元素数组，**不是** element rotation，不能删。上面的正则不会误伤它们（匹配的是 `{"angle": ...}` 对象形式）。
- VoxelShapeProvider 里 [computeElementAabb](neoforge/src/main/java/com/cahcap/neoforge/common/datagen/models/VoxelShapeProvider.java) 把 `angle == 0` 视为无旋转、直接走 from/to 的 AABB —— 这和"没有 rotation 字段"完全等价，剥离源文件 **不会影响**碰撞箱生成。

## 自定义模型的 datagen 加载流程

### 三个输出目录

所有自定义模型都通过 datagen 生成到 `neoforge/src/main/generated/resources/assets/herbalcurative/models/` 下的**三个子目录**，互不冲突：

```
generated/.../models/
├── custom/   ← BlockModelProvider 写入（剥 rotation:angle=0 后的清洁版本）
├── split/    ← ModelSplitProvider 写入（多方块裁切出的 _part_ 模型）
└── (block/ 不存在，避免与 common 源文件同名冲突)
```

### 完整数据流

```
common/.../models/block/<name>.json   ← Blockbench 导出的源文件（dirty，可能带 angle:0）
                 ↓
       BlockModelProvider 读取 + 剥离 angle:0
                 ↓
generated/.../models/custom/<name>.json   ← 清洁版本
                 ↓
        ┌────────┴────────┐
        ↓                  ↓
  单方块直接加载         多方块再切一刀
        ↓                  ↓
 blockstate 引用      ModelSplitProvider
 custom/<name>        从 common 源读取，自己也剥 angle:0
        ↓                  ↓
 Minecraft 渲染      generated/.../models/split/<name>_part_X_Y_Z.json
                           ↓
                    MultiblockStateProvider
                    生成 blockstate → split/<name>_part_X_Y_Z
                           ↓
                    Minecraft 渲染
```

### 为什么用 `custom/` 而不是直接写 `block/`

Architectury 在 dev 环境会把 `common` 模块打包成虚拟 mod（日志里的 `mod/generated_XXXXXX`），这个虚拟 mod 在 ResourceManager 中**优先级高于** `neoforge` 模块。如果 BlockModelProvider 写到 `models/block/<name>.json`，会和 `common/.../models/block/<name>.json` 在同一路径上产生两个版本，且 common 的 dirty 版本胜出 → AO 格子。

`models/custom/` 这条路径**只存在于 generated/**，common 模块下没有这个目录，所以不存在"两个 mod 提供同名文件"的冲突。Minecraft 只能从 generated/ 加载 → 永远是清洁版本。

### blockstate 和 item model 的引用方式

- **单方块 blockstate**（手写在 `common/.../blockstates/`）：引用 `herbalcurative:custom/<name>`
- **多方块 blockstate**（datagen 生成在 `generated/.../blockstates/`）：
  - `FORMED=true` 引用 `herbalcurative:split/<name>_part_X_Y_Z`
  - `FORMED=false` 引用 `herbalcurative:block/<占位方块>`（如 `block/lumistone`、`block/red_cherry_log`）
- **item model**（手写在 `common/.../models/item/`）：`parent: "herbalcurative:custom/<name>"`
- **WorkbenchRenderer 工具模型**：加载路径 `custom/workbench_tool_*`

### 新增一个自定义形状方块时需要做的事

1. 在 Blockbench 里建模，导出 `.json` 到 `common/src/main/resources/assets/herbalcurative/models/block/<name>.json`
2. 修纹理路径（加 `herbalcurative:block/` 命名空间前缀）
3. 在 [BlockModelProvider.MODELS](neoforge/src/main/java/com/cahcap/neoforge/common/datagen/models/BlockModelProvider.java) 列表里加一行 `"<name>"`
4. 在 [VoxelShapeProvider](neoforge/src/main/java/com/cahcap/neoforge/common/datagen/models/VoxelShapeProvider.java) 列表里加一行 `processModel(cache, "<name>")`
5. 写 blockstate JSON，model 引用用 `herbalcurative:custom/<name>`
6. 写 item model JSON，parent 用 `herbalcurative:custom/<name>`
7. Block 类里 `MultiblockShapes.load("/assets/herbalcurative/voxelshapes/<name>.json")` + `getByIndex(facing, 0, false)`
8. 跑 `./gradlew neoforge:runData`
