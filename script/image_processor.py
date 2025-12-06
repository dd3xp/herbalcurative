#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
Image Processor - 图片处理工具
逐个像素加深图片，深度可调节
"""

from PIL import Image
import os
import sys

# 导入配置
try:
    from config import IMAGE_DARKEN_DEPTH, IMAGE_OVERWRITE_ORIGINAL, IMAGE_OUTPUT_SUFFIX, RESOURCE_DIR
except ImportError:
    print("错误: 找不到 config.py 配置文件")
    sys.exit(1)


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


def process_image(input_path, output_path=None, depth=None):
    """
    处理图片，逐个像素加深
    
    Args:
        input_path: 输入图片路径
        output_path: 输出图片路径（如果为None，自动生成）
        depth: 加深深度（如果为None，使用全局配置）
    """
    if depth is None:
        depth = IMAGE_DARKEN_DEPTH
    
    try:
        # 打开图片
        img = Image.open(input_path)
        print(f"正在处理: {input_path}")
        print(f"图片尺寸: {img.size}")
        print(f"图片模式: {img.mode}")
        print(f"加深深度: {depth * 100:.1f}%")
        
        # 转换为RGBA模式以便处理透明度
        if img.mode != 'RGBA':
            img = img.convert('RGBA')
        
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
                if processed % (total_pixels // 10) == 0:
                    progress = (processed / total_pixels) * 100
                    print(f"进度: {progress:.1f}%", end='\r')
        
        print(f"进度: 100.0%")
        
        # 生成输出路径
        if output_path is None:
            if IMAGE_OVERWRITE_ORIGINAL:
                # 覆盖原文件
                output_path = input_path
            else:
                # 创建新文件
                base_name = os.path.splitext(input_path)[0]
                ext = os.path.splitext(input_path)[1]
                output_path = f"{base_name}{IMAGE_OUTPUT_SUFFIX}{ext}"
        
        # 保存图片
        img.save(output_path)
        print(f"处理完成！输出文件: {output_path}")
        
    except Exception as e:
        print(f"错误: {e}")
        return False
    
    return True


def find_file_in_directory(directory, filename):
    """
    在目录中递归查找文件
    
    Args:
        directory: 搜索目录
        filename: 文件名
    
    Returns:
        找到的文件完整路径，如果没找到返回None
    """
    for root, dirs, files in os.walk(directory):
        if filename in files:
            return os.path.join(root, filename)
    return None


def get_resource_path(filename):
    """
    获取资源文件的完整路径
    
    Args:
        filename: 文件名（可以包含路径，也可以只是文件名）
    
    Returns:
        完整的文件路径
    """
    # 获取脚本所在目录
    script_dir = os.path.dirname(os.path.abspath(__file__))
    
    # 构建资源目录路径
    resource_dir = os.path.join(script_dir, RESOURCE_DIR)
    resource_dir = os.path.normpath(resource_dir)
    
    # 如果文件名已经包含路径，直接使用
    if os.path.sep in filename or '/' in filename:
        # 如果已经是绝对路径，直接返回
        if os.path.isabs(filename):
            return filename
        # 否则相对于资源目录
        return os.path.join(resource_dir, filename)
    
    # 如果只是文件名，在整个资源目录中递归查找
    found_path = find_file_in_directory(resource_dir, filename)
    if found_path:
        return found_path
    
    # 如果没找到，返回默认路径（用于错误提示）
    return os.path.join(resource_dir, filename)


def main():
    """主函数"""
    print("=" * 50)
    print("图片处理工具 - 像素加深功能")
    print("=" * 50)
    print(f"当前加深深度: {IMAGE_DARKEN_DEPTH * 100:.1f}%")
    print(f"覆盖原文件: {'是' if IMAGE_OVERWRITE_ORIGINAL else '否'}")
    if not IMAGE_OVERWRITE_ORIGINAL:
        print(f"输出文件后缀: {IMAGE_OUTPUT_SUFFIX}")
    print(f"资源目录: {RESOURCE_DIR}")
    print("=" * 50)
    print()
    
    # 获取图片文件名
    if len(sys.argv) > 1:
        filename = sys.argv[1]
    else:
        filename = input("请输入图片文件名（例如: forest_heartwood_planks.png）: ").strip()
        if not filename:
            print("错误: 未输入文件名")
            return
    
    # 获取完整路径
    input_path = get_resource_path(filename)
    
    # 检查文件是否存在
    if not os.path.exists(input_path):
        print(f"错误: 文件不存在: {input_path}")
        print(f"请确认文件在以下目录中:")
        print(f"  {os.path.dirname(input_path)}")
        return
    
    print(f"找到文件: {input_path}")
    print()
    
    # 处理图片
    process_image(input_path)


if __name__ == "__main__":
    main()

