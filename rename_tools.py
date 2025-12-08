#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
工具重命名脚本
1. Thornmark 工具（剑、镐、铲、斧、锄）→ Lumistone（微光石）
2. Thornmark 弩和弩匣 → Red Cherry（红樱木）
"""

import os
from pathlib import Path

# 项目根目录
PROJECT_ROOT = Path(__file__).parent

def rename_java_files():
    """步骤1: 重命名 Java 类文件"""
    print("=" * 60)
    print("步骤1: 重命名 Java 类文件")
    print("=" * 60)
    
    renames = [
        # Lumistone 工具（原 Thornmark 工具）
        ("Common/src/main/java/com/cahcap/herbalcurative/item/ThornmarkSwordItem.java",
         "Common/src/main/java/com/cahcap/herbalcurative/item/LumistoneSwordItem.java"),
        ("Common/src/main/java/com/cahcap/herbalcurative/item/ThornmarkPickaxeItem.java",
         "Common/src/main/java/com/cahcap/herbalcurative/item/LumistonePickaxeItem.java"),
        ("Common/src/main/java/com/cahcap/herbalcurative/item/ThornmarkAxeItem.java",
         "Common/src/main/java/com/cahcap/herbalcurative/item/LumistoneAxeItem.java"),
        ("Common/src/main/java/com/cahcap/herbalcurative/item/ThornmarkShovelItem.java",
         "Common/src/main/java/com/cahcap/herbalcurative/item/LumistoneShovelItem.java"),
        ("Common/src/main/java/com/cahcap/herbalcurative/item/ThornmarkHoeItem.java",
         "Common/src/main/java/com/cahcap/herbalcurative/item/LumistoneHoeItem.java"),
        
        # Red Cherry 弩和弩匣（原 Thornmark）
        ("Common/src/main/java/com/cahcap/herbalcurative/item/ThornmarkCrossbowItem.java",
         "Common/src/main/java/com/cahcap/herbalcurative/item/RedCherryCrossbowItem.java"),
        ("Common/src/main/java/com/cahcap/herbalcurative/item/ThornmarkBoltMagazineItem.java",
         "Common/src/main/java/com/cahcap/herbalcurative/item/RedCherryBoltMagazineItem.java"),
        
        # Handler
        ("NeoForge/src/main/java/com/cahcap/herbalcurative/neoforge/handler/ThornmarkToolHandler.java",
         "NeoForge/src/main/java/com/cahcap/herbalcurative/neoforge/handler/LumistoneToolHandler.java"),
    ]
    
    for old_path, new_path in renames:
        old_file = PROJECT_ROOT / old_path
        new_file = PROJECT_ROOT / new_path
        
        if old_file.exists():
            old_file.rename(new_file)
            print(f"✅ 重命名: {old_path} -> {new_path}")
        else:
            print(f"⚠️  文件不存在: {old_path}")
    
    print()

def replace_in_file(file_path, replacements):
    """在单个文件中执行替换"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        for find, replace in replacements:
            content = content.replace(find, replace)
        
        if content != original_content:
            with open(file_path, 'w', encoding='utf-8', newline='\n') as f:
                f.write(content)
            return True
        return False
    except Exception as e:
        print(f"❌ 错误处理文件 {file_path}: {e}")
        return False

def replace_in_java_files():
    """步骤2: 替换 Java 文件内容"""
    print("=" * 60)
    print("步骤2: 替换 Java 文件内容")
    print("=" * 60)
    
    # 定义替换规则（顺序很重要！从最具体到最一般）
    replacements = [
        # 类名替换
        ("ThornmarkToolHandler", "LumistoneToolHandler"),
        ("ThornmarkSwordItem", "LumistoneSwordItem"),
        ("ThornmarkPickaxeItem", "LumistonePickaxeItem"),
        ("ThornmarkAxeItem", "LumistoneAxeItem"),
        ("ThornmarkShovelItem", "LumistoneShovelItem"),
        ("ThornmarkHoeItem", "LumistoneHoeItem"),
        ("ThornmarkCrossbowItem", "RedCherryCrossbowItem"),
        ("ThornmarkBoltMagazineItem", "RedCherryBoltMagazineItem"),
        
        # 常量名替换（全大写）
        ("THORNMARK_SWORD", "LUMISTONE_SWORD"),
        ("THORNMARK_PICKAXE", "LUMISTONE_PICKAXE"),
        ("THORNMARK_AXE", "LUMISTONE_AXE"),
        ("THORNMARK_SHOVEL", "LUMISTONE_SHOVEL"),
        ("THORNMARK_HOE", "LUMISTONE_HOE"),
        ("THORNMARK_CROSSBOW", "RED_CHERRY_CROSSBOW"),
        ("THORNMARK_BOLT_MAGAZINE", "RED_CHERRY_BOLT_MAGAZINE"),
        
        # 注册 ID 替换（小写下划线）
        ("thornmark_sword", "lumistone_sword"),
        ("thornmark_pickaxe", "lumistone_pickaxe"),
        ("thornmark_axe", "lumistone_axe"),
        ("thornmark_shovel", "lumistone_shovel"),
        ("thornmark_hoe", "lumistone_hoe"),
        ("thornmark_crossbow", "red_cherry_crossbow"),
        ("thornmark_bolt_magazine", "red_cherry_bolt_magazine"),
        
        # 显示名称替换
        ("Thornmark Sword", "Lumistone Sword"),
        ("Thornmark Pickaxe", "Lumistone Pickaxe"),
        ("Thornmark Axe", "Lumistone Axe"),
        ("Thornmark Shovel", "Lumistone Shovel"),
        ("Thornmark Hoe", "Lumistone Hoe"),
        ("Thornmark Crossbow", "Red Cherry Crossbow"),
        ("Thornmark Bolt Magazine", "Red Cherry Bolt Magazine"),
        
        # 注释中的替换
        ("Thornmark 工具", "Lumistone 工具"),
        ("Thornmark tools", "Lumistone tools"),
        ("Thornmark tool", "Lumistone tool"),
    ]
    
    count = 0
    for java_file in PROJECT_ROOT.rglob("*.java"):
        if replace_in_file(java_file, replacements):
            count += 1
            print(f"✅ 更新: {java_file.relative_to(PROJECT_ROOT)}")
    
    print(f"\n共更新 {count} 个 Java 文件")
    print()

def replace_in_json_files():
    """步骤3: 替换 JSON 文件内容"""
    print("=" * 60)
    print("步骤3: 替换 JSON 文件内容")
    print("=" * 60)
    
    replacements = [
        # 注册 ID
        ("thornmark_sword", "lumistone_sword"),
        ("thornmark_pickaxe", "lumistone_pickaxe"),
        ("thornmark_axe", "lumistone_axe"),
        ("thornmark_shovel", "lumistone_shovel"),
        ("thornmark_hoe", "lumistone_hoe"),
        ("thornmark_crossbow", "red_cherry_crossbow"),
        ("thornmark_bolt_magazine", "red_cherry_bolt_magazine"),
        
        # 显示名称
        ("Thornmark Sword", "Lumistone Sword"),
        ("Thornmark Pickaxe", "Lumistone Pickaxe"),
        ("Thornmark Axe", "Lumistone Axe"),
        ("Thornmark Shovel", "Lumistone Shovel"),
        ("Thornmark Hoe", "Lumistone Hoe"),
        ("Thornmark Crossbow", "Red Cherry Crossbow"),
        ("Thornmark Bolt Magazine", "Red Cherry Bolt Magazine"),
    ]
    
    count = 0
    for json_file in PROJECT_ROOT.rglob("*.json"):
        if replace_in_file(json_file, replacements):
            count += 1
            print(f"✅ 更新: {json_file.relative_to(PROJECT_ROOT)}")
    
    print(f"\n共更新 {count} 个 JSON 文件")
    print()

def rename_resource_files():
    """步骤4: 重命名资源文件"""
    print("=" * 60)
    print("步骤4: 重命名资源文件")
    print("=" * 60)
    
    # 查找所有 thornmark 相关的资源文件
    patterns = ["*thornmark*"]
    resource_dirs = [
        "Common/src/main/resources/assets/herbalcurative/models/item",
        "Common/src/main/resources/assets/herbalcurative/textures/item",
        "Common/src/main/generated/resources",
    ]
    
    found_files = []
    for resource_dir in resource_dirs:
        dir_path = PROJECT_ROOT / resource_dir
        if dir_path.exists():
            for pattern in patterns:
                for file in dir_path.rglob(pattern):
                    if file.is_file():
                        found_files.append(file)
    
    if not found_files:
        print("✅ 没有找到需要重命名的资源文件")
        print()
        return
    
    print(f"找到 {len(found_files)} 个资源文件需要重命名：")
    
    count = 0
    for old_file in found_files:
        # 生成新文件名
        new_name = old_file.name
        
        # Lumistone 工具
        new_name = new_name.replace("thornmark_sword", "lumistone_sword")
        new_name = new_name.replace("thornmark_pickaxe", "lumistone_pickaxe")
        new_name = new_name.replace("thornmark_axe", "lumistone_axe")
        new_name = new_name.replace("thornmark_shovel", "lumistone_shovel")
        new_name = new_name.replace("thornmark_hoe", "lumistone_hoe")
        
        # Red Cherry 弩
        new_name = new_name.replace("thornmark_crossbow", "red_cherry_crossbow")
        new_name = new_name.replace("thornmark_bolt_magazine", "red_cherry_bolt_magazine")
        
        new_file = old_file.parent / new_name
        
        if old_file != new_file:
            if new_file.exists():
                print(f"⚠️  目标文件已存在，跳过: {new_file.name}")
                continue
            
            old_file.rename(new_file)
            count += 1
            print(f"✅ 重命名: {old_file.name} -> {new_file.name}")
    
    print(f"\n共重命名 {count} 个资源文件")
    print()

def verify_results():
    """步骤5: 验证结果"""
    print("=" * 60)
    print("步骤5: 验证是否有遗漏")
    print("=" * 60)
    
    import re
    
    # 注意：现在只检查工具相关的，不检查弩（因为弩已经改成 red_cherry）
    patterns = [
        r'\bthornmark_sword\b',
        r'\bthornmark_pickaxe\b',
        r'\bthornmark_axe\b',
        r'\bthornmark_shovel\b',
        r'\bthornmark_hoe\b',
        r'\bThornmarkSword',
        r'\bThornmarkPickaxe',
        r'\bThornmarkAxe',
        r'\bThornmarkShovel',
        r'\bThornmarkHoe',
        r'\bTHORNMARK_SWORD\b',
        r'\bTHORNMARK_PICKAXE\b',
        r'\bTHORNMARK_AXE\b',
        r'\bTHORNMARK_SHOVEL\b',
        r'\bTHORNMARK_HOE\b',
    ]
    
    found_issues = []
    
    for pattern in patterns:
        regex = re.compile(pattern)
        for file_path in list(PROJECT_ROOT.rglob("*.java")) + list(PROJECT_ROOT.rglob("*.json")):
            # 跳过 build 目录和 othermods 目录
            if 'build' in file_path.parts or 'othermods' in file_path.parts:
                continue
            try:
                with open(file_path, 'r', encoding='utf-8') as f:
                    for line_num, line in enumerate(f, 1):
                        if regex.search(line):
                            found_issues.append((file_path, line_num, line.strip()))
            except:
                pass
    
    if found_issues:
        print("⚠️  发现以下位置可能需要手动检查：")
        for file_path, line_num, line in found_issues[:20]:
            print(f"  {file_path.relative_to(PROJECT_ROOT)}:{line_num}")
            print(f"    {line[:100]}")
        if len(found_issues) > 20:
            print(f"  ... 还有 {len(found_issues) - 20} 个")
    else:
        print("✅ 没有发现遗漏，替换完成！")
    
    print()

def main():
    print("\n" + "=" * 60)
    print("工具重命名脚本")
    print("1. Thornmark 工具 → Lumistone（微光石）")
    print("2. Thornmark 弩/弩匣 → Red Cherry（红樱木）")
    print("=" * 60)
    print()
    
    # 确认执行
    print("⚠️  警告：此操作将修改大量文件！")
    print("请确保已经备份或提交了 Git！")
    print()
    
    response = input("确定要继续吗？(yes/no): ").strip().lower()
    if response not in ['yes', 'y']:
        print("❌ 操作已取消")
        return
    
    print()
    
    # 执行步骤
    rename_java_files()
    replace_in_java_files()
    replace_in_json_files()
    rename_resource_files()
    verify_results()
    
    print("=" * 60)
    print("🎉 重命名完成！")
    print("=" * 60)
    print()
    print("后续步骤：")
    print("1. 检查 Git 差异，确认修改正确")
    print("2. 运行 gradlew clean")
    print("3. 运行 gradlew runData")
    print("4. 运行 gradlew build")
    print()

if __name__ == "__main__":
    main()
