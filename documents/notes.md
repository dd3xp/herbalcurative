# 别忘了

## jvav相关

- 把™的 Extension Pack for Jvav 给禁用掉，sb东西一个
- .\gradlew.bat clean build 构建模组
- .\gradlew.bat runClient 启动客户端
- gradlew 的警告千万别修，修了会影响逻辑，能跑的情况下这些警告全是氛围灯，心情好再修

## 材质相关

- blockbench右键拖住可以平移
- blockbench如果要导出1.12.2的Jvav模型，需要先新建1.12.2的Jvav Entity模型，然后导入现有的bbmodel文件
- blockbench里面的各个部位的枢轴点记得要调对，不然盔甲位置会错乱，枢轴点参考Armor(main)和Armor(leggings)里面自带的，这样导出后的setRotationPoint方法里面的参数才是正确适配mc的
- 如果模型位置很奇怪可能是json和渲染文件的旋转重叠作用
- 需要搞水池那种方块的话需要用Jvav版物品/方块

## md语句相关

- 3*3的chart模板
    <table class="board">
    <tr>
        <td>null</td><td>null</td><td>null</td>
    </tr>
    <tr>
        <td>null</td><td>null</td><td>null</td>
    </tr>
    <tr>
        <td>null</td><td>null</td><td>null</td>
    </tr>
    </table>

## 模组实现相关

- 做相同的东西的时候记得抽象出一个基类
- cursor 闲置的时候记得检查一下代码质量，硬编码之类的
- wthit 就是个傻逼模组，有问题别管