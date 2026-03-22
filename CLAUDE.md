# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 模型更新

私有资产目录里面的json文件需要改一下纹理路径

## 多方块光照格子问题 - 已修复

**根因**：Blockbench 导出的模型 JSON 中每个 element 都带有 `rotation: {angle: 0, axis: y, origin: ...}`。虽然 angle=0 无实际旋转，但 Minecraft 对有 `rotation` 属性的 element 走不同的 AO 渲染路径，导致相邻方块 AO 插值不一致 → 格子。

**修复**：ModelSplitProvider 裁剪时，`rotation.angle == 0` 的 rotation 属性直接删除不写入 _part_ 模型。

**注意**：不要在 _part_ 模型的 element 中添加 `rotation: {angle: 0}` 属性。

## 代码规范检查结果

参照 `private/documents/codestyle/` 中的 Refactory.md 和 Solid.md 进行审查。

### 1. 代码重复（Duplicated Code）— 高优先级

#### 1.1 草药列表/映射硬编码重复
6 种草药（scaleplate, dewpetal_shard, golden_lilybell, cryst_spine, burnt_node, heart_of_stardream）在多处重复定义：
- `HerbCabinetBlockEntity.initializeStorage()` / `getAllHerbItems()` / `getHerbFromKey()` / `getHerbIndex()`
- `HerbVaultBlockEntity.initializeStorage()`（完全复制）
- `FlowweaveRingItem.getHerbFromKey()` / `getHerbKeyForItem()`
- `HerbBoxItem.HERB_KEYS` 数组 + `fillFromCabinet()` / `fillFromVault()` 硬编码字符串

**建议**：提取 `HerbRegistry` 工具类，集中管理草药列表、key 映射、index 映射。

#### 1.2 多方块解体逻辑重复
`KilnBlockEntity.disassemble()`、`HerbCabinetBlockEntity.disassemble()`、`HerbVaultBlockEntity.disassemble()`、`ObeliskBlockEntity.disassemble()` 有高度相似的两趟遍历（第一趟标记 formed=false + suppressDrops，第二趟恢复原方块）。

**建议**：在 `MultiblockPartBlockEntity` 中实现模板方法，子类只覆写 `dropStoredItems()` 和 `getStructurePositions()`。

#### 1.3 Tooltip Handler 重复（7 个文件）
所有 7 个 TooltipHandler 共享几乎相同的结构：
- 方块命中检测 + BlockEntity 类型检查（~15 行 × 7）
- `TooltipAnimator` 初始化 + reset/update 调用（~10 行 × 7）
- 屏幕中心计算 + PoseStack 缩放动画（~8 行 × 7）
- 物品+数量渲染循环（~25 行 × 3）

**建议**：提取基类或工具类，用模板方法统一流程，子类只实现 `renderContent()`。

#### 1.4 网格命中检测重复
`HerbCabinetBlock.isHitInGridCell()` 和 `HerbVaultBlock.isHitInGridCell()` 逻辑几乎相同（含 Direction switch 坐标转换）。

**建议**：提取到工具方法，两个类传入各自的 `GRID_CELLS` 数组。

#### 1.5 双击批量添加循环重复
`HerbCabinetBlock`、`HerbVaultBlock`、`HerbBasketBlock` 的双击遍历玩家背包添加草药的循环体高度相似。

**建议**：提取公共方法 `transferHerbsFromInventory(player, storage, herb)`。

#### 1.6 药水效果工具方法重复
`FlowweaveRingItem.getEffectForType()` / `isInstantEffect()` 和 `PotItem.getEffectForType()` / `isInstantEffect()` 完全一致。
`FlowweaveRingItem.getBoundPotionTypes()` 和 `PotItem.getPotionTypes()` 逻辑也几乎相同。

**建议**：提取到 `PotionHelper` 工具类。

#### 1.7 `syncToClient()` 重复
`HerbBasketBlockEntity`、`HerbPotBlockEntity`、`IncenseBurnerBlockEntity`、`WorkbenchBlockEntity` 各自实现了和 `MultiblockPartBlockEntity.syncToClient()` 完全相同的方法。

**建议**：提取到公共基类或工具方法。

#### 1.8 `isDoubleClick()` 重复
`HerbCabinetBlockEntity`、`HerbVaultBlockEntity`、`HerbBasketBlockEntity` 有完全相同的双击检测逻辑。

**建议**：提取到工具方法或 mixin interface。

### 2. 方法过长（Long Method）

| 文件 | 方法 | 行数 | 问题 |
|------|------|------|------|
| `FlowweaveRingItem.useOn()` | 184 行 | 处理 6 种多方块交互 + 模式切换，职责过多 |
| `CauldronTooltipHandler.onRenderGuiPost()` | 134 行 | 命中检测 + 动画 + 多种状态渲染混在一起 |
| `HerbPotTooltipHandler.onRenderGuiPost()` | 120 行 | 同上 |
| `PotItem.useOn()` | 106 行 | 釜交互逻辑（收集/倒出药水）+ 多种状态判断 |
| `CauldronBlock.useItemOn()` | 105 行 | 水桶/空桶/物品/空手多种交互路径 |
| `IncenseBurnerTooltipHandler.onRenderGuiPost()` | 101 行 | 同上 |
| `FlowweaveRingItem.tryWorkbenchCraft()` | 98 行 | 工作台合成完整流程 |
| `KilnBlockEntity.serverTick()` | 94 行 | 冶炼状态机 + 催化剂 + 自动IO |
| `CauldronItemHandler.insertItem()` | 83 行 | 多种物品插入路径判断 |

### 3. 循环过长 / 嵌套过深（3+ 层）

- `FlowweaveRingItem.useOn()`：for > if > if > while（4 层）
- `PotItem.useOn()`：if > if > if > if（4 层）
- `FlowweaveProjectile.applyEffectToEntitiesInRange()`：for > if > if > for > if（5 层）
- `CauldronBlock.useItemOn()`：if instanceof > if bucket > if addFluid（3-4 层）
- `HerbBasketBlock.useItemOn()`：if doubleClick > for > if isHerb > if isEmpty（4 层）
- 所有 TooltipHandler 的 `onRenderGuiPost()`：hitResult > instanceof > instanceof > 渲染逻辑（3-4 层）

### 4. public 数据成员

`MultiblockPartBlockEntity` 中以下字段为 public 非 final：
- `formed`、`posInMultiblock`、`offset`、`facing`、`mirrored`、`suppressDrops`、`originalBlockState`

这些字段被多个类直接读写（Block、BlockEntity、ItemHandler），破坏封装。

**建议**：改为 private + getter/setter，敏感操作（如 `formed` 变更）通过方法封装。

### 5. 类内聚性差 / SRP 违反

#### `FlowweaveRingItem`（1123 行，11+ 职责）
1. 药水绑定与管理
2. 多方块组装触发（5 种多方块）
3. 釜交互（开始/完成酿造）
4. 草药调和触发
5. 工作台合成（完整配方匹配+执行）
6. 花篮管理（绑定/解绑/弹出）
7. 盆栽管理（移除苗/土）
8. 熏香台管理（移除粉末）
9. 施法模式切换
10. 弹射物发射
11. 草药消耗计算

**建议**：拆分为多个 Handler 类（`MultiblockFormationHandler`、`CraftingHandler`、`BlockInteractionHandler`、`ProjectileHandler`），`FlowweaveRingItem` 只做分发。

### 6. OCP 违反（开闭原则）

#### `FlowweaveRingItem.useOn()` 的多方块类型检查
每加一种新多方块，需要修改 `useOn()` 中的 if-else 链和 `wouldTriggerAction()` 中的平行判断。

**建议**：定义 `MultiblockInteraction` 接口，每种多方块注册自己的交互逻辑，`useOn()` 遍历注册列表。

### 7. 基本类型滥用（Primitive Obsession）

- `MultiblockPartBlockEntity.offset` 用 `int[]` 表示三维偏移，没有语义——应封装为 `BlockPos` 或自定义 `Offset` 类
- 草药用 `Item` 引用 + `String` key 映射，分散在多处——应封装为 `HerbType` 枚举或注册对象

### 8. 相关数据未封装

- `CauldronBlockEntity` 的酿造状态用多个独立 boolean（`isBrewing`、`isStartingBrew`、`isCompletingBrew`）表示——应封装为 `enum BrewingState`
- `HerbPotBlockEntity` 的生长状态类似——`isGrowing` + `growthTicks` 可封装为 `enum GrowthState`
- `FlowweaveRingItem.shootProjectile()` 有 8 个参数——后 5 个（effects, duration, amplifier, color, lingering）应封装为 `ProjectileConfig`

### 9. 命名问题

| 位置 | 名称 | 建议 |
|------|------|------|
| 全局 | `be` | 用具体类型名如 `cauldron`、`cabinet`、`basket` |
| `MultiblockPartBlock` | `buffer[]` | `accumulatedShape` |
| `MultiblockPartBlock` | `mi` | `mirroredIndex` |
| `MultiblockPartBlock` | `nx1, nz1` | `rotatedX1, rotatedZ1` |
| `WorkbenchBlockEntity` | `materialStack` | `materialStackList`（实际是 List） |
| `CauldronBlockEntity` | `materials` | `materialIngredients` |

### 10. 方法更依赖别的类（Feature Envy）

- `HerbBoxItem.fillFromCabinet()` / `fillFromVault()` / `transferToCabinet()` / `transferToVault()`：大量操作 cabinet/vault 的字段，应在 BE 侧提供高层 transfer 方法
- `FlowweaveRingItem.tryWorkbenchCraft()`：15+ 次调用 workbench 方法，应把合成执行逻辑移到 `WorkbenchBlockEntity`
- 所有 Block 类的 `useItemOn()` / `attack()` 大量操作 BlockEntity 字段——这在 MC mod 中常见但可通过在 BE 中封装高层方法改善