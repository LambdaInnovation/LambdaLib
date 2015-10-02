# Ripple LIUtils实现
# API文档

Ripple版本：0.1

注意：本文档介绍的是[Ripple脚本语言](ripple_cn.md)在java下的实现。关于语言规范请参考其他文档。


综述
---

LIUtils在cn.liutils.ripple包中实现了Ripple脚本语言。这个编译器会将Ripple在运行时编译到JVM运行，并且支持多脚本文件的合并。
同时，该编译器也提供了通过”原生函数“让Ripple访问java的方法。


脚本的加载
---

首先，创建一个```ScriptProgram```类的实例。然后调用```ScriptProgram#loadScript(Reader)```或者```ScriptProgram#loadScript(ResourceLocation)```
来加载一个脚本。加载脚本时，该脚本的所有命名空间、函数、值都会被合并入之前的加载结果。如果存在重复的函数或者值，则会抛出一个异常。

注意：两个函数是重复的，当且仅当他们的全名相同，并且它们的参数个数相同。


脚本的调用
---

脚本的调用通过```ScriptNamespace```类进行。每个```ScriptNamespace```类代表了脚本中的命名空间。你可以从```ScriptProgram```
获取其根命名(via ```ScriptProgram#root```)空间以及其他子命名空间(via ```ScriptProgram#at(path)```)。

ScriptNamespace类可以通过以下方法获得脚本函数或者值：

* ``getInt(path)``
* ``getFloat(path)``
* ``getDouble(path)``
* ``getFunction(path)``

它们都支持命名空间的嵌套。


标准库
---
该实现完全实现了文档中所述的标准库函数。此外还提供了如下函数：

* print(x)：将x在java控制台打印出来。等价于System.out.println(x);

Know issue
---

* 暂时不支持值的查找。

