# -*- coding: utf-8 -*-
"""
配置文件 - 图片处理模块
在这里修改处理参数
"""

# ========== 资源目录配置 ==========
# 资源目录路径（相对于脚本所在目录）
RESOURCE_DIR = "../../Common/src/main/resources"

# ========== 图片处理设置 ==========
# 是否覆盖原文件
# True = 覆盖原文件
# False = 创建新文件（自动添加时间戳后缀，如：image_20231209_143025.png）
IMAGE_OVERWRITE_ORIGINAL = False

# ========== 图片加深配置 ==========
# 加深深度 (0.0 - 1.0)
# 0.0 = 不加深, 1.0 = 完全变黑
IMAGE_DARKEN_DEPTH = 0.2

# ========== 图片调整大小配置 ==========
# 目标宽度（像素）
IMAGE_RESIZE_WIDTH = 16

# 目标高度（像素）
IMAGE_RESIZE_HEIGHT = 16

# 是否保持宽高比
# True = 保持宽高比，False = 拉伸到指定尺寸
IMAGE_RESIZE_KEEP_ASPECT_RATIO = True

# 调整大小时使用的采样方法
# 'NEAREST' = 最近邻采样（像素风格，适合像素艺术）
# 可选值: 'NEAREST', 'BILINEAR', 'BICUBIC', 'LANCZOS'
IMAGE_RESIZE_SAMPLING = 'BICUBIC'

# ========== 颜色映射配置 ==========
# 目标调色板（十六进制颜色代码列表）
# 图片中的每个非黑色、非透明像素将被替换为此列表中最接近的颜色
COLOR_PALETTE = [
    "#31210b",
    "#503a12",
    "#4a3411",
    "#442f0f",
    "#3e2a0e",
    "#37260c"
]

# 颜色映射模式
# 'rgb': 标准RGB距离匹配（适合颜色分布均匀的调色板）
# 'luminance': 亮度分位数映射（适合单色调调色板，将图片亮度分段映射到调色板）
# 'hsl': HSL色相感知映射（推荐用于多色调调色板，既保持色调又保持亮度）
# 'hybrid': RGB+亮度混合模式
COLOR_MAPPING_MODE = 'luminance'

# HSL模式权重（仅在 COLOR_MAPPING_MODE = 'hsl' 时使用）
# 色相权重：控制颜色相似度的重要性 (0-1)
HUE_WEIGHT = 0.7

# 亮度权重：控制明暗保持的重要性 (0-1)
LUMINANCE_WEIGHT = 0.3

# 混合模式权重（仅在 COLOR_MAPPING_MODE = 'hybrid' 时使用）
# 颜色权重 (0-1)
COLOR_WEIGHT = 0.5

# 是否在映射前进行对比度拉伸
# True = 自动拉伸图片亮度范围到最大值（有助于暗图）
APPLY_CONTRAST_STRETCH = False

# 黑色阈值（0-255）
# RGB值之和小于此阈值的像素将被视为黑色，不会被替换
BLACK_THRESHOLD = 30

# 透明度阈值（0-255）
# Alpha值小于此阈值的像素将被视为透明，不会被替换
ALPHA_THRESHOLD = 10

