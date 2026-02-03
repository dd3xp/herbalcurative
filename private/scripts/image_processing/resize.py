#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
图片缩放模块
提供图片大小调整功能
"""

from PIL import Image
from .config import IMAGE_RESIZE_WIDTH, IMAGE_RESIZE_HEIGHT, IMAGE_RESIZE_SAMPLING


def get_sampling_method(method_name):
    """
    获取PIL的采样方法
    
    Args:
        method_name: 采样方法名称字符串
    
    Returns:
        PIL采样方法常量
    """
    sampling_methods = {
        'NEAREST': Image.NEAREST,
        'BILINEAR': Image.BILINEAR,
        'BICUBIC': Image.BICUBIC,
        'LANCZOS': Image.LANCZOS,
    }
    return sampling_methods.get(method_name.upper(), Image.NEAREST)


def resize_image(img, target_width=None, target_height=None, keep_aspect_ratio=True, sampling=None):
    """
    调整图片大小
    
    Args:
        img: PIL Image对象
        target_width: 目标宽度（如果为None，使用配置值）
        target_height: 目标高度（如果为None，使用配置值）
        keep_aspect_ratio: 是否保持宽高比
        sampling: 采样方法（如果为None，使用配置值）
    
    Returns:
        调整大小后的PIL Image对象
    """
    if target_width is None:
        target_width = IMAGE_RESIZE_WIDTH
    if target_height is None:
        target_height = IMAGE_RESIZE_HEIGHT
    if sampling is None:
        sampling = IMAGE_RESIZE_SAMPLING
    
    original_width, original_height = img.size
    
    # 如果需要保持宽高比
    if keep_aspect_ratio:
        # 计算宽高比
        aspect_ratio = original_width / original_height
        target_aspect_ratio = target_width / target_height
        
        # 根据宽高比调整目标尺寸
        if aspect_ratio > target_aspect_ratio:
            # 图片更宽，以宽度为准
            new_width = target_width
            new_height = int(target_width / aspect_ratio)
        else:
            # 图片更高，以高度为准
            new_height = target_height
            new_width = int(target_height * aspect_ratio)
    else:
        new_width = target_width
        new_height = target_height
    
    print(f"调整大小: {original_width}x{original_height} -> {new_width}x{new_height}")
    print(f"采样方法: {sampling}")
    
    # 获取采样方法
    resampling = get_sampling_method(sampling)
    
    # 调整图片大小
    resized_img = img.resize((new_width, new_height), resampling)
    
    return resized_img

