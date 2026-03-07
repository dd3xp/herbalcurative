#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
颜色映射模块
提供多种颜色映射算法：RGB、亮度、HSL色相感知等
"""

import colorsys
from .config import (
    COLOR_PALETTE, COLOR_MAPPING_MODE, BLACK_THRESHOLD, ALPHA_THRESHOLD,
    HUE_WEIGHT, LUMINANCE_WEIGHT, COLOR_WEIGHT, APPLY_CONTRAST_STRETCH
)


def hex_to_rgb(hex_color):
    """
    将十六进制颜色代码转换为RGB元组
    
    Args:
        hex_color: 十六进制颜色代码（例如: "#FF0000" 或 "FF0000"）
    
    Returns:
        RGB元组 (r, g, b)
    """
    hex_color = hex_color.lstrip('#')
    return tuple(int(hex_color[i:i+2], 16) for i in (0, 2, 4))


def rgb_to_hsl(r, g, b):
    """
    将RGB转换为HSL
    
    Args:
        r, g, b: RGB值 (0-255)
    
    Returns:
        (h, l, s) 元组，值范围都是0-1
    """
    h, l, s = colorsys.rgb_to_hls(r/255, g/255, b/255)
    return (h, l, s)


def get_luminance(color):
    """
    计算颜色的感知亮度（使用标准权重）
    
    Args:
        color: RGB元组 (r, g, b)
    
    Returns:
        亮度值 (0-255)
    """
    r, g, b = color[:3]
    # 使用感知亮度公式
    return 0.299 * r + 0.587 * g + 0.114 * b


def color_distance(color1, color2):
    """
    计算两个颜色之间的欧几里得距离
    
    Args:
        color1: RGB元组 (r, g, b)
        color2: RGB元组 (r, g, b)
    
    Returns:
        颜色距离（浮点数）
    """
    r1, g1, b1 = color1[:3]
    r2, g2, b2 = color2[:3]
    return ((r1 - r2) ** 2 + (g1 - g2) ** 2 + (b1 - b2) ** 2) ** 0.5


def get_hue_distance(hue1, hue2):
    """
    计算两个色相之间的距离（考虑环形特性）
    
    Args:
        hue1, hue2: 色相值 (0-1)
    
    Returns:
        色相距离 (0-0.5)
    """
    diff = abs(hue1 - hue2)
    # 因为色相是环形的，取较短的那条路径
    return min(diff, 1 - diff)


def normalize_luminance(value, min_val, max_val):
    """
    将亮度值归一化到0-1范围
    
    Args:
        value: 当前亮度值
        min_val: 最小亮度值
        max_val: 最大亮度值
    
    Returns:
        归一化后的值 (0-1)
    """
    if max_val == min_val:
        return 0.5
    return (value - min_val) / (max_val - min_val)


def is_black_or_transparent(pixel, black_threshold=None, alpha_threshold=None):
    """
    判断像素是否为黑色或透明
    
    Args:
        pixel: RGBA元组 (r, g, b, a)
        black_threshold: 黑色阈值（如果为None，使用全局配置）
        alpha_threshold: 透明度阈值（如果为None，使用全局配置）
    
    Returns:
        布尔值
    """
    if black_threshold is None:
        black_threshold = BLACK_THRESHOLD
    if alpha_threshold is None:
        alpha_threshold = ALPHA_THRESHOLD
    
    r, g, b, a = pixel
    
    # 检查是否透明
    if a < alpha_threshold:
        return True
    
    # 检查是否接近黑色
    if (r + g + b) < black_threshold:
        return True
    
    return False


def analyze_image_luminance(img):
    """
    分析图片的亮度范围
    
    Args:
        img: PIL Image对象
    
    Returns:
        (min_lum, max_lum) 元组
    """
    pixels = img.load()
    width, height = img.size
    
    luminances = []
    for y in range(height):
        for x in range(width):
            pixel = pixels[x, y]
            if not is_black_or_transparent(pixel):
                lum = get_luminance(pixel)
                luminances.append(lum)
    
    if not luminances:
        return (0, 255)
    
    return (min(luminances), max(luminances))


def apply_contrast_stretch(img, lum_range):
    """
    对图片应用对比度拉伸
    
    Args:
        img: PIL Image对象
        lum_range: 当前亮度范围 (min, max)
    
    Returns:
        处理后的PIL Image对象
    """
    min_lum, max_lum = lum_range
    if max_lum == min_lum:
        return img
    
    pixels = img.load()
    width, height = img.size
    
    for y in range(height):
        for x in range(width):
            pixel = pixels[x, y]
            if not is_black_or_transparent(pixel):
                r, g, b, a = pixel
                
                # 拉伸每个通道
                r = int((r - min_lum / 3) * 255 / (max_lum - min_lum))
                g = int((g - min_lum / 3) * 255 / (max_lum - min_lum))
                b = int((b - min_lum / 3) * 255 / (max_lum - min_lum))
                
                # 限制在0-255范围内
                r = max(0, min(255, r))
                g = max(0, min(255, g))
                b = max(0, min(255, b))
                
                pixels[x, y] = (r, g, b, a)
    
    return img


def find_closest_color_rgb(pixel, palette):
    """
    标准RGB距离匹配
    
    Args:
        pixel: RGBA元组 (r, g, b, a)
        palette: RGB颜色列表
    
    Returns:
        最接近的RGB颜色元组
    """
    pixel_rgb = pixel[:3]
    min_distance = float('inf')
    closest_color = palette[0]
    
    for color in palette:
        dist = color_distance(pixel_rgb, color)
        if dist < min_distance:
            min_distance = dist
            closest_color = color
    
    return closest_color


def collect_all_colors(img):
    """
    收集图片中所有非黑色、非透明的颜色及其亮度
    
    Args:
        img: PIL Image对象
    
    Returns:
        颜色亮度列表（已排序，从暗到亮）
    """
    pixels = img.load()
    width, height = img.size
    
    luminances = []
    for y in range(height):
        for x in range(width):
            pixel = pixels[x, y]
            if not is_black_or_transparent(pixel):
                lum = get_luminance(pixel)
                luminances.append(lum)
    
    # 从暗到亮排序
    luminances.sort()
    return luminances


def build_quantile_mapping(all_luminances, palette, palette_luminances):
    """
    构建分位数映射：将图片亮度分成n段，每段对应调色板中的一个颜色
    
    Args:
        all_luminances: 图片中所有像素的亮度值（已排序）
        palette: RGB调色板列表
        palette_luminances: 调色板的亮度值列表
    
    Returns:
        分段阈值列表
    """
    if not all_luminances:
        return []
    
    n = len(palette)
    total_pixels = len(all_luminances)
    
    # 按亮度排序调色板（从暗到亮）
    sorted_palette = sorted(zip(palette_luminances, palette), key=lambda x: x[0])
    
    # 计算分位点
    thresholds = []
    for i in range(n):
        if i == n - 1:
            # 最后一段到最大值
            thresholds.append((all_luminances[-1] + 1, sorted_palette[i][1]))
        else:
            # 计算分位点位置
            quantile_pos = int((i + 1) * total_pixels / n)
            if quantile_pos >= total_pixels:
                quantile_pos = total_pixels - 1
            threshold_lum = all_luminances[quantile_pos]
            thresholds.append((threshold_lum, sorted_palette[i][1]))
    
    return thresholds


def find_closest_color_luminance_quantile(pixel, quantile_mapping):
    """
    使用分位数映射找到对应的颜色
    
    Args:
        pixel: RGBA元组 (r, g, b, a)
        quantile_mapping: 分位数映射表 [(threshold, color), ...]
    
    Returns:
        对应的RGB颜色元组
    """
    pixel_lum = get_luminance(pixel)
    
    # 找到第一个阈值大于等于像素亮度的段
    for threshold, color in quantile_mapping:
        if pixel_lum <= threshold:
            return color
    
    # 如果没找到（不应该发生），返回最亮的颜色
    return quantile_mapping[-1][1]


def find_closest_color_luminance(pixel, palette, palette_luminances, img_lum_range):
    """
    通过亮度映射找到最接近的颜色（简单模式：直接匹配最近亮度）
    
    Args:
        pixel: RGBA元组 (r, g, b, a)
        palette: RGB调色板列表（已按亮度排序）
        palette_luminances: 调色板的亮度值列表
        img_lum_range: 图片的亮度范围 (min, max)
    
    Returns:
        最接近的RGB颜色元组
    """
    pixel_lum = get_luminance(pixel)
    
    # 归一化像素亮度到0-1
    normalized_lum = normalize_luminance(pixel_lum, img_lum_range[0], img_lum_range[1])
    
    # 在调色板中找到亮度最接近的颜色
    min_diff = float('inf')
    closest_color = palette[0]
    
    min_pal_lum = min(palette_luminances)
    max_pal_lum = max(palette_luminances)
    
    for i, pal_lum in enumerate(palette_luminances):
        # 调色板亮度也归一化到0-1
        pal_normalized = normalize_luminance(pal_lum, min_pal_lum, max_pal_lum)
        diff = abs(normalized_lum - pal_normalized)
        
        if diff < min_diff:
            min_diff = diff
            closest_color = palette[i]
    
    return closest_color


def find_closest_color_hsl(pixel, palette, palette_hsl, palette_luminances, img_lum_range):
    """
    HSL色相感知映射（推荐用于多色调调色板）
    
    Args:
        pixel: RGBA元组 (r, g, b, a)
        palette: RGB调色板列表
        palette_hsl: 调色板的HSL值列表
        palette_luminances: 调色板的亮度值列表
        img_lum_range: 图片的亮度范围 (min, max)
    
    Returns:
        最接近的RGB颜色元组
    """
    pixel_rgb = pixel[:3]
    pixel_hsl = rgb_to_hsl(*pixel_rgb)
    pixel_hue, pixel_light, pixel_sat = pixel_hsl
    pixel_lum = get_luminance(pixel_rgb)
    
    # 归一化像素亮度
    normalized_pixel_lum = normalize_luminance(pixel_lum, img_lum_range[0], img_lum_range[1])
    
    min_pal_lum = min(palette_luminances)
    max_pal_lum = max(palette_luminances)
    
    # 找到最匹配的颜色
    min_distance = float('inf')
    closest_color = palette[0]
    
    for i, (pal_hue, pal_light, pal_sat) in enumerate(palette_hsl):
        # 色相距离（0-0.5）
        hue_dist = get_hue_distance(pixel_hue, pal_hue)
        
        # 亮度距离（归一化到0-1）
        normalized_pal_lum = normalize_luminance(palette_luminances[i], min_pal_lum, max_pal_lum)
        lum_dist = abs(normalized_pixel_lum - normalized_pal_lum)
        
        # 组合距离（色相距离需要 *2 因为其范围是0-0.5）
        combined_dist = HUE_WEIGHT * (hue_dist * 2) + LUMINANCE_WEIGHT * lum_dist
        
        if combined_dist < min_distance:
            min_distance = combined_dist
            closest_color = palette[i]
    
    return closest_color


def find_closest_color_hybrid(pixel, palette, palette_luminances, img_lum_range):
    """
    混合模式：同时考虑RGB距离和亮度
    
    Args:
        pixel: RGBA元组 (r, g, b, a)
        palette: RGB颜色列表
        palette_luminances: 调色板的亮度值列表
        img_lum_range: 图片的亮度范围 (min, max)
    
    Returns:
        最接近的RGB颜色元组
    """
    pixel_rgb = pixel[:3]
    pixel_lum = get_luminance(pixel)
    normalized_lum = normalize_luminance(pixel_lum, img_lum_range[0], img_lum_range[1])
    
    min_distance = float('inf')
    closest_color = palette[0]
    
    min_pal_lum = min(palette_luminances)
    max_pal_lum = max(palette_luminances)
    
    luminance_weight = 1.0 - COLOR_WEIGHT
    
    for i, color in enumerate(palette):
        # RGB距离（归一化到0-1）
        rgb_dist = color_distance(pixel_rgb, color) / 441.67  # 441.67 是最大可能距离
        
        # 亮度差异（已经是0-1）
        pal_normalized = normalize_luminance(palette_luminances[i], min_pal_lum, max_pal_lum)
        lum_diff = abs(normalized_lum - pal_normalized)
        
        # 加权组合
        combined_distance = COLOR_WEIGHT * rgb_dist + luminance_weight * lum_diff
        
        if combined_distance < min_distance:
            min_distance = combined_distance
            closest_color = color
    
    return closest_color


def apply_color_mapping(img, palette=None, mode=None):
    """
    将图片中的颜色映射到指定调色板
    
    Args:
        img: PIL Image对象
        palette: 十六进制颜色列表（如果为None，使用配置）
        mode: 映射模式（如果为None，使用配置）
    
    Returns:
        处理后的PIL Image对象
    """
    if palette is None:
        palette = COLOR_PALETTE
    if mode is None:
        mode = COLOR_MAPPING_MODE
    
    # 将十六进制颜色转换为RGB
    rgb_palette = [hex_to_rgb(color) for color in palette]
    
    print(f"调色板颜色数量: {len(rgb_palette)}")
    print(f"映射模式: {mode}")
    print(f"黑色阈值: {BLACK_THRESHOLD}")
    print(f"透明度阈值: {ALPHA_THRESHOLD}")
    
    # 分析图片亮度范围
    print("分析图片亮度范围...")
    img_lum_range = analyze_image_luminance(img)
    print(f"图片亮度范围: {img_lum_range[0]:.1f} - {img_lum_range[1]:.1f}")
    
    # 对比度拉伸（如果启用）
    if APPLY_CONTRAST_STRETCH:
        print("应用对比度拉伸...")
        img = apply_contrast_stretch(img, img_lum_range)
        img_lum_range = analyze_image_luminance(img)
        print(f"拉伸后亮度范围: {img_lum_range[0]:.1f} - {img_lum_range[1]:.1f}")
    
    # 预处理调色板
    palette_luminances = [get_luminance(color) for color in rgb_palette]
    
    # 分位数映射表（用于luminance模式）
    quantile_mapping = None
    
    # 如果是亮度模式，构建分位数映射
    if mode == 'luminance':
        print("构建亮度分位数映射...")
        all_luminances = collect_all_colors(img)
        print(f"收集到 {len(all_luminances)} 个有效像素")
        quantile_mapping = build_quantile_mapping(all_luminances, rgb_palette, palette_luminances)
        print(f"已将图片分为 {len(quantile_mapping)} 个亮度段")
        # 显示分段信息
        for i, (threshold, color) in enumerate(quantile_mapping):
            lum = get_luminance(color)
            print(f"  段 {i+1}: 亮度 <= {threshold:.1f} -> 调色板亮度 {lum:.1f}")
    
    # 如果是HSL模式，计算HSL值
    palette_hsl = None
    if mode == 'hsl':
        palette_hsl = [rgb_to_hsl(*color) for color in rgb_palette]
        print(f"HSL权重 - 色相: {HUE_WEIGHT:.2f}, 亮度: {LUMINANCE_WEIGHT:.2f}")
    
    print(f"调色板亮度范围: {min(palette_luminances):.1f} - {max(palette_luminances):.1f}")
    
    # 获取像素数据
    pixels = img.load()
    width, height = img.size
    
    # 统计数据
    total_pixels = width * height
    processed = 0
    mapped_count = 0
    skipped_count = 0
    
    # 逐个像素处理
    for y in range(height):
        for x in range(width):
            pixel = pixels[x, y]
            
            if is_black_or_transparent(pixel):
                skipped_count += 1
            else:
                # 根据模式选择颜色
                if mode == 'luminance':
                    # 使用分位数映射
                    closest_color = find_closest_color_luminance_quantile(pixel, quantile_mapping)
                elif mode == 'hsl':
                    closest_color = find_closest_color_hsl(
                        pixel, rgb_palette, palette_hsl, palette_luminances, img_lum_range
                    )
                elif mode == 'hybrid':
                    closest_color = find_closest_color_hybrid(
                        pixel, rgb_palette, palette_luminances, img_lum_range
                    )
                else:  # 'rgb' 模式
                    closest_color = find_closest_color_rgb(pixel, rgb_palette)
                
                pixels[x, y] = (closest_color[0], closest_color[1], closest_color[2], pixel[3])
                mapped_count += 1
            
            processed += 1
            
            if total_pixels >= 10 and processed % (total_pixels // 10) == 0:
                progress = (processed / total_pixels) * 100
                print(f"进度: {progress:.1f}%", end='\r')
    
    print(f"进度: 100.0%")
    print(f"映射像素数: {mapped_count}")
    print(f"跳过像素数: {skipped_count} (黑色/透明)")
    
    return img

