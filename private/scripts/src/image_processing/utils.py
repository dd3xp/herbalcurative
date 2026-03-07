#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
工具函数模块
提供文件查找、路径处理等辅助功能
"""

import os
from .config import RESOURCE_DIR


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

