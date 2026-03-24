# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 模型更新

私有资产目录里面的json文件需要改一下纹理路径

## 多方块光照格子问题 - 已修复

**根因**：Blockbench 导出的模型 JSON 中每个 element 都带有 `rotation: {angle: 0, axis: y, origin: ...}`。虽然 angle=0 无实际旋转，但 Minecraft 对有 `rotation` 属性的 element 走不同的 AO 渲染路径，导致相邻方块 AO 插值不一致 → 格子。

**修复**：ModelSplitProvider 裁剪时，`rotation.angle == 0` 的 rotation 属性直接删除不写入 _part_ 模型。

**注意**：不要在 _part_ 模型的 element 中添加 `rotation: {angle: 0}` 属性。
