#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
图片加深模块
提供逐像素加深功能
"""

from .config import IMAGE_DARKEN_DEPTH


def darken_pixel(pixel, depth):
    """
    加深单个像素
    
    Args:
        pixel: RGB元组 (r, g, b) 或 RGBA元组 (r, g, b, a)
        depth: 加深深度 (0.0 - 1.0)
    
    Returns:
        加深后的像素元组
    """
    if len(pixel) == 4:  # RGBA
        r, g, b, a = pixel
        r = max(0, int(r * (1 - depth)))
        g = max(0, int(g * (1 - depth)))
        b = max(0, int(b * (1 - depth)))
        return (r, g, b, a)
    else:  # RGB
        r, g, b = pixel
        r = max(0, int(r * (1 - depth)))
        g = max(0, int(g * (1 - depth)))
        b = max(0, int(b * (1 - depth)))
        return (r, g, b)


def apply_darken(img, depth=None):
    """
    加深图片
    
    Args:
        img: PIL Image对象
        depth: 加深深度（如果为None，使用全局配置）
    
    Returns:
        处理后的PIL Image对象
    """
    if depth is None:
        depth = IMAGE_DARKEN_DEPTH
    
    print(f"加深深度: {depth * 100:.1f}%")
    
    # 获取像素数据
    pixels = img.load()
    width, height = img.size
    
    # 逐个像素处理
    total_pixels = width * height
    processed = 0
    
    for y in range(height):
        for x in range(width):
            pixel = pixels[x, y]
            darkened_pixel = darken_pixel(pixel, depth)
            pixels[x, y] = darkened_pixel
            processed += 1
            
            # 显示进度（每10%显示一次）
            if total_pixels >= 10 and processed % (total_pixels // 10) == 0:
                progress = (processed / total_pixels) * 100
                print(f"进度: {progress:.1f}%", end='\r')
    
    print(f"进度: 100.0%")
    
    return img

