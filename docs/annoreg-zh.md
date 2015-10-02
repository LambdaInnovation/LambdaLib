Annotation Registry 中文使用说明
===

简介
---
Annotation Registry是一个辅助型的Coremod。这个Mod可以让其他Mod使用一种全新的基于Annotation的注册方式。

在Minecraft的Mod开发中，需要注册很多不同种类的对象，包括物品(Item)、方块(Block)、实体(Entity)等等。对于小型Mod，这些对象的定义和注册可以很方便地放在主类中。但对于大型Mod而言，集中放置的大量的注册信息会使代码难以维护。而Annotation Registry就是为了解决这个集中注册的问题。使用Annotation Registry，各个物品、方块可以用更加简短的代码进行注册，并且可以分散在多个class中。

使用Annotation Registry的好处包括：
* 短而清晰的代码。
* 优化的注册接口（如Block可以和OreDict一同注册）。
* 注册可以分散在不同的class中而无需集中处理，使得大型Mod真正实现模块化。
* 可以使用一些MC和Forge没有直接提供的接口。

安装
---
Annotation Registry只是一个普通的Coremod，因此可以按照一般的安装方法。如果有编译好的jar文件希望在Minecraft游戏中使用，只需要将jar放进mods文件夹。如果希望在eclipse环境中使用这个Mod的代码，需要将src/main/java文件夹链接到工程中，并在eclipse/mods文件夹中添加Coremod加载项(jar/AnnotationRegistry.jar)。

开发
---
Annotation Registry的注册完全基于Annotation。在所有Mod加载前会通过forge的AsmTable获取包含注册Annotation的待注册对象，并在该Mod请求注册时完成全部对象的注册。具体来讲，使用Annotation Registry开发Mod需要按照如下步骤：

1 添加Mod声明

在Mod的主类class上使用```@RegistrationMod```，并提供Mod的包名、资源前缀、注册名前缀。如：
```java
package cn.example;

@Mod(modid = "examplemod", version = "1.0")
@RegistrationMod(pkg = "cn.example", res = "example", prefix = "ex_")
class ExampleMod {
```
其中

包名：这个Mod的所有class的公有包名前缀。用于识别类的归属。可以使用上述"cn.example"，也可以使用"cn.example."这样的格式。但不要使用"cn"，否则如果其他Mod的类也在cn包中会出现类归属错误。

资源前缀：resources/assets中这个Mod使用的文件夹的名字。用于自动设置Block和Item的纹理等。

注册名前缀：可选参数，缺省时使用空字符串。在某些注册时需要指定String类型的名字的情况下使用。如类的注册会使用前缀+类名的名字。通常使用默认值并不会出现问题，这里是提供一种方式避免这个Mod的对象和其他Mod重名。（为了避免重名，还可以使用```@RegWithName```来强制指定名字，这种情况下前缀也会被忽略。）注意这里提到的名字仅在无关紧要的情况下使用，注册时用到的关乎逻辑的字符串不会通过这种方式生成。

2 创建包括注册信息的class

注册信息可以包括在任意多个class中，但是这些class必须使用```@Registrant```注解，这样在扫描阶段就能获得这些类中的注册信息了。在包括子类的情况下，只需要在最顶层class使用即可，所有的子类会自动被扫描。例如使用一个单独的class来管理机器的方块：

```java
package cn.example;
//在cn.example包中，可以顺利归属到ExampleMod中。

@Registrant //这个类包含注册信息
class MachineBlocks {
    //这里是注册信息
}
```

3 创建待注册的对象

不同的注册类型有不同的使用方法。例如要注册一个Block，可以使用下面的代码：
```java
@Registrant
class MachineBlocks {
    @RegBlock
    public static BlockTestMachine testMachine = new BlockTestMachine();
}
```
假设已经有BlockTestMachine类，上述代码可以向ExampleMod添加一条注册信息，注册这个testMachine对象。

4 在Mod加载时进行注册

Annotation Registry不会自动注册任何对象（除了NetworkCall之外，详见后），Mod必须在自己的加载阶段向RegistrationManager请求注册。由于不同类型的注册信息需要在不同阶段（preInit、init、postInit、startServer，见LoadStage类）进行，请求需要一个字符串来指定注册那些类型的对象。例如Block是在init阶段注册的，因此需要在主类的init函数中添加如下代码：
```java
RegistrationManager.INSTANCE.registerAll(this, "Init");
```
注意字符串的大小写，必须和LoadStage类里的声明一致。

注：此外，也可以使用
```java
RegistrationManager.INSTANCE.registerAll(this, "Block");
```
来单独请求对方块（对应与RegBlock）的注册，但这种方式不推荐。所有的注册类型（除了NetworkCall）都已经归属于一个LoadStage，请使用LoadStage的名字进行注册。内部这种归属通过依赖（dependency）实现，Init依赖于Block，因此在注册Init时会自动对Block和其他一些类型的注册信息进行注册。

各注册类型及用法
---
参见[RegTypes-zh.md](RegTypes-zh.md)。
