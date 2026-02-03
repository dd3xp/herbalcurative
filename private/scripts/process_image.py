#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
图片处理工具 - 主程序
提供交互式菜单，支持图片缩放、调色、加深等功能
"""

from PIL import Image
import os
import sys
from datetime import datetime

# 导入图片处理模块
from image_processing import resize_image, apply_color_mapping, apply_darken, get_resource_path
from image_processing.config import (
    IMAGE_OVERWRITE_ORIGINAL,
    IMAGE_RESIZE_WIDTH, IMAGE_RESIZE_HEIGHT, IMAGE_RESIZE_KEEP_ASPECT_RATIO, IMAGE_RESIZE_SAMPLING,
    COLOR_PALETTE, COLOR_MAPPING_MODE, BLACK_THRESHOLD, ALPHA_THRESHOLD,
    HUE_WEIGHT, LUMINANCE_WEIGHT, IMAGE_DARKEN_DEPTH, RESOURCE_DIR
)


def print_banner():
    """打印程序标题"""
    print("=" * 60)
    print("  图片处理工具 v1.0")
    print("  支持: 缩放 | 调色 | 加深")
    print("=" * 60)
    print()


def print_config_info():
    """打印当前配置信息"""
    print("资源目录:", RESOURCE_DIR)
    print("覆盖原文件:", "是" if IMAGE_OVERWRITE_ORIGINAL else "否 (使用时间戳后缀)")
    print()


def show_feature_menu():
    """显示功能选择菜单"""
    print("\n" + "=" * 60)
    print("请选择要使用的功能：")
    print()
    print("  1. 缩放图片")
    print("  2. 调色映射")
    print("  3. 加深图片")
    print("  0. 退出")
    print("=" * 60)
    
    while True:
        try:
            choice = input("\n请输入选项 (0-3): ").strip()
            if choice in ['0', '1', '2', '3']:
                return choice
            else:
                print("[错误] 无效选项，请输入 0-3 之间的数字")
        except KeyboardInterrupt:
            print("\n\n已取消操作")
            return '0'


def show_resize_info():
    """显示缩放功能配置"""
    print("\n【缩放配置】")
    print(f"  目标尺寸: {IMAGE_RESIZE_WIDTH}x{IMAGE_RESIZE_HEIGHT}")
    print(f"  保持宽高比: {'是' if IMAGE_RESIZE_KEEP_ASPECT_RATIO else '否'}")
    print(f"  采样方法: {IMAGE_RESIZE_SAMPLING}")


def show_color_mapping_info():
    """显示调色功能配置"""
    print("\n【调色配置】")
    print(f"  调色板: {len(COLOR_PALETTE)} 种颜色")
    print(f"  颜色预览: {', '.join(COLOR_PALETTE[:3])}{'...' if len(COLOR_PALETTE) > 3 else ''}")
    print(f"  映射模式: {COLOR_MAPPING_MODE}")
    if COLOR_MAPPING_MODE == 'hsl':
        print(f"  权重 - 色相: {HUE_WEIGHT:.2f}, 亮度: {LUMINANCE_WEIGHT:.2f}")
    print(f"  黑色阈值: {BLACK_THRESHOLD}, 透明度阈值: {ALPHA_THRESHOLD}")


def show_darken_info():
    """显示加深功能配置"""
    print("\n【加深配置】")
    print(f"  加深深度: {IMAGE_DARKEN_DEPTH * 100:.1f}%")


def process_image(input_path, output_path, features):
    """
    处理图片
    
    Args:
        input_path: 输入图片路径
        output_path: 输出图片路径
        features: 要执行的功能列表 ['resize', 'color_mapping', 'darken']
    """
    try:
        # 打开图片
        img = Image.open(input_path)
        print(f"\n正在处理: {os.path.basename(input_path)}")
        print(f"原始尺寸: {img.size[0]}x{img.size[1]}")
        print(f"图片模式: {img.mode}")
        
        # 转换为RGBA模式以便处理透明度
        if img.mode != 'RGBA':
            img = img.convert('RGBA')
        
        print(f"\n执行功能: {' -> '.join([f.upper() for f in features])}")
        print()
        
        # 按顺序执行功能
        for i, feature in enumerate(features):
            if i > 0:
                print()
            
            print("─" * 60)
            
            if feature == 'resize':
                print(f">> 步骤 {i+1}: 调整大小")
                print("-" * 60)
                img = resize_image(img, keep_aspect_ratio=IMAGE_RESIZE_KEEP_ASPECT_RATIO)
            
            elif feature == 'color_mapping':
                print(f">> 步骤 {i+1}: 颜色映射")
                print("-" * 60)
                img = apply_color_mapping(img)
            
            elif feature == 'darken':
                print(f">> 步骤 {i+1}: 像素加深")
                print("-" * 60)
                img = apply_darken(img)
        
        print("\n" + "-" * 60)
        
        # 保存图片
        img.save(output_path)
        print(f"\n[完成] 处理完成！")
        print(f"输出文件: {output_path}")
        print(f"最终尺寸: {img.size[0]}x{img.size[1]}")
        
        return True
        
    except Exception as e:
        print(f"\n[错误] {e}")
        import traceback
        traceback.print_exc()
        return False


def get_output_path(input_path):
    """生成输出路径（使用时间戳作为唯一标识符）"""
    if IMAGE_OVERWRITE_ORIGINAL:
        return input_path
    else:
        base_name = os.path.splitext(input_path)[0]
        ext = os.path.splitext(input_path)[1]
        # 生成时间戳：格式为 _YYYYMMDD_HHMMSS
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        return f"{base_name}_{timestamp}{ext}"


def main():
    """主函数"""
    print_banner()
    print_config_info()
    
    # 如果有命令行参数，处理完后退出
    one_shot_mode = len(sys.argv) > 1
    
    while True:
        # 选择功能
        choice = show_feature_menu()
        
        if choice == '0':
            print("\n再见！")
            break
        
        # 根据选择确定要执行的功能
        feature_map = {
            '1': (['resize'], '缩放图片'),
            '2': (['color_mapping'], '调色映射'),
            '3': (['darken'], '加深图片'),
        }
        
        features, feature_name = feature_map[choice]
        
        print(f"\n已选择: {feature_name}")
        
        # 显示相关配置信息
        if 'resize' in features:
            show_resize_info()
        if 'color_mapping' in features:
            show_color_mapping_info()
        if 'darken' in features:
            show_darken_info()
        
        print("\n" + "=" * 60)
        
        # 获取图片文件名
        if one_shot_mode:
            filename = sys.argv[1]
        else:
            print("\n请输入图片文件名（或输入 q 返回菜单）")
            print("提示: 可以是完整路径，或只是文件名（将在资源目录中搜索）")
            filename = input("文件名: ").strip()
            
            if not filename or filename.lower() == 'q':
                print("\n返回主菜单...")
                continue
        
        # 获取完整路径
        input_path = get_resource_path(filename)
        
        # 检查文件是否存在
        if not os.path.exists(input_path):
            print(f"\n[错误] 文件不存在: {input_path}")
            print(f"请确认文件在以下目录中:")
            print(f"  {os.path.dirname(input_path)}")
            
            if one_shot_mode:
                break
            else:
                continue
        
        print(f"\n[OK] 找到文件: {input_path}")
        
        # 生成输出路径
        output_path = get_output_path(input_path)
        
        # 处理图片
        success = process_image(input_path, output_path, features)
        
        if success:
            print("\n" + "=" * 60)
            print("  处理成功！")
            print("=" * 60)
        else:
            print("\n" + "=" * 60)
            print("  处理失败")
            print("=" * 60)
        
        # 如果是命令行模式，处理完就退出
        if one_shot_mode:
            break
        
        print("\n" * 2)


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\n程序已中断")
    except Exception as e:
        print(f"\n[错误] 发生错误: {e}")
        import traceback
        traceback.print_exc()

