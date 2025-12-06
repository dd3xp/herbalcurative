# -*- coding: utf-8 -*-
"""
配置文件 - Resource Processor
在这里修改处理参数
"""

# ========== 资源目录配置 ==========
# 资源目录路径（相对于脚本所在目录）
# 会在整个 resources 目录中搜索文件
RESOURCE_DIR = "../src/main/resources"

# ========== 图片处理参数 ==========
# 加深深度 (0.0 - 1.0)
# 0.0 = 不加深, 1.0 = 完全变黑
IMAGE_DARKEN_DEPTH = 0.1

# 是否覆盖原文件
# True = 覆盖原文件, False = 创建新文件（带后缀）
IMAGE_OVERWRITE_ORIGINAL = True

# 输出文件名后缀（仅在 IMAGE_OVERWRITE_ORIGINAL = False 时使用）
IMAGE_OUTPUT_SUFFIX = "_modified"

