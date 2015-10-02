各注册类型及用法
===
Annotation Registry中实现了很多注册类型。每个类型包括一个Annotation和一个负责注册的class（包含```@RegistryTypeDecl```注解）两部分。要了解详细的注册机理请参考```cn.annoreg.mc```包的代码。以下只对各类型进行最简单的用法介绍。

Block
---
在以Block或Block的继承类为类型的public static字段上使用```@RegBlock```。最简单的用法为：
```java
@RegBlock
public static BlockXXX xxx = new BlockXXX();
```
可以通过item指定ItemBlock的class。可以添加```@RegBlock.OreDict```来将其注册到矿物字典。可以添加```@RegBlock.BTName```来在注册后设置该Block的unlocalized name和icon name。例如：
```java
@RegBlock
@RegBlock.OreDict("oreTin")
@RegBlock.BTName("tinore")
public static BlockOre oreTin = new BlockOre();
```

ChestContent
---
在WeightedRandomChestContent类型的字段上使用```@RegChestContent```。例如：
```java
@RegChestContent(0, 1, 2, 3)
public static WeightedRandomChestContent record0 = new WeightedRandomChestContent(new ItemStack(MyItems.record0), 1, 1, 5);
```
！注意：这个类型的注册接口还不完善，可能会修改。

Command
---
在实现了ICommand的class上使用```@RegCommand```。注意需要在serverStarting时调用
```java
RegistrationManager.INSTANCE.registerAll(this, "StartServer");
```

Entity
---
在Entity的继承类上使用```@RegEntity```。可以使用```@RegEntity.HasRender```和```@RegEntity.Render```联合指定Renderer，例如：
```java
@Registrant
@RegEntity
@RegEntity.HasRender
public class MyEntity extends Entity {
    @RegEntity.Render
    @SideOnly(Side.CLIENT)
    public static MyRender renderer;
}
```

EventHandler
---
在EventHandler的类或字段中使用```@RegEventHandler```。如果在类上使用，会自动创建一个新实例。如果在字段上使用，则直接使用类的值进行注册。默认情况下会向FML和Forge两个EventBus注册，可以使用value参数指定。

Item
---
参考Block的用法。

MessageHandler
---
在实现了IMessageHandler的类上使用```@RegMessageHandler```。例如：
```java
@RegMessageHandler(msg = MyMessage.class, side = RegMessageHandler.Side.CLIENT)
class MyMessageHandler implements IMessageHandler<MyMessage, IMessage> {
```
通过side指定Handler是在服务器端或是在客户端。要使用这个功能，除了上述声明外，还必须在主类中提供Handler实例，并添加```@RegMessageHandler.WrapperInstance```。例如：
```java
@RegMessageHandler.WrapperInstance
public static SimpleNetworkWrapper netHandler = NetworkRegistry.INSTANCE.newSimpleChannel("examplemod_channle");
```
发送Message时直接发送到这个netHandler即可。另外SimpleNetworkWrapper注册Handler时需要指定一个唯一的数字，这里通过Annotation Registry注册的Handler会从100开始，因此在Mod的其他地方使用通常方法注册Handler如果从0开始的话并不会受到影响。

PreloadTexture
---
强制在注册时加载资源。使用```@ForcePreloadTexture```在包含public static ResourceLocation字段的class上。例如：
```java
@RegistrationClass
@ForcePreloadTexture
public class Resources {
    //这个png会在注册时加载
    public static ResourceLocation texture1 = new ResourceLocation("example:textures/models/texture1.png");
}
```

Init
---
在Mod的init时向其他class发送一个init指令。可以用在类上（调用public static的init函数）或字段上（调用public的init函数）例如：
```java
@RegistrationClass
@RegInit
class MySubmodule {
    public static void init() {
        //这个函数会在init阶段被调用
    }
}
```

TileEntity
---
在继承TileEntity的类上使用```@RegTileEntity```。可以指定Renderer，方法参见Entity。

WorldGen
---
在以实现了IWorldGenerator接口的类为类型的字段上使用```@RegWorldGen```，调用GameRegistry.registerWorldGenerator注册一个IWorldGenerator。

GuiHandler
---
在以GuiHandlerBase的继承类为类型的字段上使用```@RegGuiHandler```。要实现功能，需要实现GuiHandlerBase中getClientGui，或者getClientContainer和getServerContainer函数。前者使GuiHandlerBase可以开启一个基于Container的Gui，后者则可以开启一个仅存在于客户端的Gui。例如用在AcademyCraft中的：
```java
@Registrant
public class GuiHandlers {
	
	@RegGuiHandler
	public static GuiHandlerBase handlerPresetSettings = new GuiHandlerBase() {
		@Override
		@SideOnly(Side.CLIENT)
		protected GuiScreen getClientGui() {
			return new GuiPresetSettings();
		}
	};

}
```
要开启这个Gui，只需要在任意地方（必须在客户端）调用```GuiHandlers.handlerPresetSettings.openClientGui()```即可。

Serializable
---
指定一个class的序列化器。序列化分为实例序列化(Instance)和数据序列化(Data)两种基本模式和更新序列化(Updata)的混合模式。在指定序列化器时，可以分别指定实例和数据的序列化器，更新序列化会自动通过两种基本序列化器进行。

基本来讲，实例序列化用于将一个对象的实例引用进行序列化（例如服务器端和客户端上的Entity对象）。数据序列化用于将一个对象的内容进行序列化（例如一个ItemStack）。更新序列化器则首先通过实例序列化其找到实例，然后通过数据序列化器将数据写入这个实例。

NetworkCall
---
NetworkCall可以允许客户端触发服务器端的一个函数，或者反之。函数的参数通过Serialization模块实现。一个简单的例子如下：
```java
@RegNetworkCall(side = Side.SERVER)
public static void myNetworkCall(@StorageOption.Data Integer i) {
    System.out.print(i);
}
```
在客户端直接调用这个myNetworkCall函数，就会在服务器端输出给定的参数。一个典型的用法是在服务器端响应客户端Gui上的事件。

目前支持客户端和服务器端之间的双向调用，函数可以为static或非static函数（需要指定this的序列化方式）。

在需要得到返回值时，可以使用Future对象进行包装。例如以下代码从ret返回参数a的值加1的结果。
```java
@RegNetworkCall(side = Side.SERVER)
public static void getValue(@Data Integer a, @Data Future ret) {
    ret.setAndSync(a + 1);
}
```
使用时需要
```java
getValue(0, Future.create(new FutureCallback() {
    @Override public void onReady(Object result) {
        //result will contain the value returned
    }
}));
```
Future对象可以使用Data或Instance序列化方式，这个方式将决定Future所传递的内容的序列化方式。
注意一个Future只能同步一次。因此当从Server发往Client时只有一个Client可以向Future中写入值。
