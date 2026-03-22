# Solid

## SRP：单一职责原则

- **概念**：一个类只有一个被改变的理由，这个类要改代码的原因，最好只来自同一类需求/同一个人群/同一个业务点
- **违反**：比如 PDF 里的 Rectangle，Rectangle 既负责几何计算（area），又负责GUI 绘制（draw）。这意味着它会因为两种完全不同的需求改动而改：**面积/几何规则变了**要改，**界面渲染方式变了**也要改，此时就有两个改变理由。后果则是GUI 一改，几何库也得跟着重测/重编译，产生不该有的耦合
  - **解决**：把会因为不同原因变化的东西拆开，让每个类只对一种变化负责，也就是把**几何**跟**绘制**拆开

## OCP：开闭原则

- **概念**：需求增加、规则改变时，通过加代码进行扩展，而不是修改原有代码
- **违反**：
  - **`switch(type)` / `if-else`**：PDF 中 `canvas.paint()` 对 `shape.type` 做 `switch(circle/square/triangle)`，此时每次增加一个图形就要在 `switch` 处增加更多的图形，修改了原有的代码，违反 OCP 原则
    - **解决**：先做一个抽象的接口 Shape 的 interface，里面实现抽象的 `draw()`，然后把所有的形状都做成类，类里面单独实现画法 `draw()`，这些类继承 Shape 的 interface。`paint()` 函数直接接受形状参数，用形状参数的类调用 `draw()` 就行，interface 会自动匹配对应的类的 `draw()`。比如我要增加一个五边形，我就实现一个 pantagon 的类，然后在 pantagon 里面实现 `draw()` 函数。这样我 main 只需要调用 `canvas.paint(new pantagon())`，Canvas 里面的 `paint()` 就会自动调用 `pantagon.draw()`，不需要修改原有代码
  - **`到处 new 的问题`**：PDF 中直接在 Client 的 `dowork()` 里面 new 了一个 HttpServer 的对象，但如果要改变协议比如换成 File, STMP 等等就需要在这里把 new 的 HttpServer 对象修改成 FileServer 对象等等，违反 OCP 原则
    - **解决**：用 DI 的思想解决，DI 不需要 new，只需要外部的对象，而这个对象可以在 main 选择性地初始化成任何子类的类型。做法是加一个抽象的 Server 的 interface，把每个协议都提取成单独的类然后继承 interface，然后在 main 种 new 类里面引用 Server 这个 interface 的对象再用依赖注入调用 Server 的抽象方法。这样要加新的协议，比如加一个 FileServer 就只需要创建一个 FileServer 类，在 FileServer 里面实现 `dowork()`，再在 main 创建 Client 的对象，把 FileServer 类传入构造函数，Client 里面的 `dowork()` 就能自动调用 `FileServer.dowork()`

## LSP：里氏替换

- **概念**：子类不能把父类的行为破坏
- **违反**：比如 Rectangle 有单独设置 height 和 width 的能力，但是子类 Square 把 height 和 width 设置成一样的了，调用方如果设置了 `r.setHeight(5)`, `r.setWidth(4)`，并判断面积是否为20。在对象是 Rectangle 时不会有错，但因为子类 Square 会把 height 和 width 设置成一样的，此时返回的面积为16，破坏了父辈的行为
  - **解决**：父辈要是考虑到有 Square 这种子类，就不应该提供单独设置 height 和 width 的功能，而是一个方法统一设置 height 和 width `setSize(int height, int width)`，然后在子类把函数改成 `setSize(int length)`，这样子类怎么传都不会破坏父类的行为

## DIP：依赖倒置

- **概念**：高层模块不该依赖低层模块，两者都应依赖抽象；抽象不该依赖细节，细节应该依赖抽象
- **违反**：比如我底层的 MysqlDatabase 类实现了一个 `save()` 方法，我直接在高层的 OrderService 类里面 new 了一个 MysqlDatabase 的对象 db 然后调用了 `db.save()`，没有依赖抽象而是直接依赖底层模块，违反了 DIP 原则
  - **解决**：用一个 Database interface 把 MysqlDatabase 的接口抽出去，MysqlDatabase 继承这个接口，OrderService 里面用 DI 引用 Database 的对象，然后调用 Database 的 `save()`。至于具体使用哪种数据库，直接在 main 创建对象的时候控制就行 `Database db = new MysqlDatabase()`。此时 OrderService 和 MysqlDatabase 都依赖抽象，Database 不知道 Mysql，但 Mysql 实现接口，依赖 Database

## ISP：接口隔离

- **概念**：主要是说 interface，不要让客户端实现自己不该实现的东西
- **违反**：比如我一个 Worker interface，里面有 `work()`, `eat()`, `sleep()`，但是我一个 Robot 类继承 Worker 接口之后只需要实现 `work()`，不用吃饭也不用睡觉，这些方法对 Robot 没有用，违反了 ISP 原则
  - **解决**：隔离这些方法，拆成小接口，比如 Workable, Eatable, Sleepable，里面实现对应的方法。HumanWorker 就直接继承三个，RobotWorker 就只继承 Workable，这样就不需要实现不需要的方法了