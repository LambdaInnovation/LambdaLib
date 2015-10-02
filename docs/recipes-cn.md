# LIUtils 文档
## 合成表配置文件

为了实现方便的修改和定义合成表，我们实现了一套自定义的专门用来解析合成表的配置文件格式。
代码实现可以在`cn.liutils.crafting.*`找到。

RecipeRegistry
---
``RecipeRegistry`是注册表脚本的核心系统。它负责处理以下事务：

* 载入默认的合成类型和自定义合成类型
* 加载对mod中物品/方块实例的名称映射
* 根据以上信息解析合成表文件

也就是说，每一个RecipeRegistry对象为合成表文件提供了一个环境。
合成表文件可以使用这个环境所定义的自定义合成类型和物品/方块来指定合成表。



合成表文件
---
合成表文件通常应该放置在``assets/<modid>/``中，后缀名为.recipe。
合成表由一系列的RecipeElement(合成对象)组成。每个RecipeElement指定了一个单独的合成，例如从泥土熔炼得到金坷垃（雾）。
每一个合成对象之间不需要明确的分隔符。但是你应该用回车来分开不同的合成对象。
每一个合成对象的语法如下：
```
recipe_type(<recipe_output>)[experience] {
	[<element>, <element>, <element>]
	[<element>, <element>, <element>]
	....
} ; a comment

; some comments
```

其中，用`<>`包裹的字段代表一个ItemStack或oreDict字段，在之后会详细说明。

各部分解释如下：

* `recipe_type`： 该合成表的合成表类型。所能指定的类型由RecipeRegistry对象的信息所限定。默认拥有的类型有shaped, shapeless, shaped_s。
* `experience`: 该合成表执行时所给予玩家的经验值。可以忽略。仅对特定类型的合成有效（如熔炼）
* `<recipe_output>`： 该合成表的输出。
* `{ [...] }`: 该合成表的合成网格。每个网格应该由一系列的列表( [ elem, elem, elem, ... ] )所组成，并且每个列表的长度必须一致。列表之间无需分隔符。
* `; comment`: 注♀释

在设置好RecipeRegistry的状态以后，使用```RecipeRegistry.addRecipeFromXXX```系列方法来解析并且添加合成表文件



声明ItemStack或oreDict
---
可以用以下方法声明一个ItemStack或oreDict字段：

* `name#data*amount`
* `name#data`
* `name*amount`
* `name`

如果省略data，data默认为0; 如果省略amount, amount默认为1。
每一个声明所对应的对象由RecipeRegistry的环境所决定。在解析一个name时，RecipeRegistry会顺序做如下搜索：

* 如果自定义映射列表中含有这个name，返回该映射列表所对应的ItemStack（一定是从Item或者Block所创建）。
* 如果oredict中含有这个name，返回该oredict名称。
* 如果Item中有严格以该name为名称的（包括命名空间，如果是minecraft命名空间可省略）方块，返回该物品所对应的ItemStack
* 如果Block中有严格以该name为名称的（包括命名空间，如果是minecraft命名空间可省略）方块，返回该方块所对应的ItemStack

如果返回的结果是一个oredict名称，data和amount会被忽略。

在合成表的输出处不能够指定一个oredict名称。如果你这么做，将会得到一个错误。

**nil**是一个保留字符，用来说明该位置的内容为空。它也只能在输入部分被使用。



自定义名称映射
---
通常来说，你会想对自己mod中的物品和方块起一个简短的名字用在合成表中。RecipeRegistry提供了这样的名称映射方法。

你可以通过
```java
RecipeRegistry.map(String key, String obj);
```
方法来指定一个自定义名称映射。
该自定义名称映射必须是以下三种对象中的一个：

* Item: 从一个物品创建ItemStack
* Block: 从一个方块创建ItemStack
* String: oredict名称

关于名称的搜索方法详见**声明ItemStack或oredict**一节。



CustomMappingHelper
---
当然，如果要从每个地方都这么调用实在太过繁琐，你可以通过```CustomMappingHelper```中的```@RecipeName```注解来帮助加载自定义名称。
它的用法如下所示：

```java
public class ModItemRegistry {
	@RecipeName("jinkela")
	public static Item itemJinkela = new ItemJinkela();
	
	@RecipeName("iron_plate")
	public static Item itemIronPlate = new ItemIronPlate();
	
	@RecipeName("schrodinger_cat_box")
	public static Block blockCatbox = new BlockSchrodingerCatBox();
	
	/**
	* 这个方法应该在init阶段被调用。为了演示方便直接传入RR作为参数。
	*/
	public static void init(RecipeRegistry recipes) {
		// 注册物品 .....
		
		CustomMappingHelper.addMapping(recipes, ModItemRegistry.class); 
		//现在"jinkela", "schrodinger_cat_box"和"ks"被加入了recipes对象的自定义映射表中。
	}
}
```


自定义合成
---
你可以通过
```java
RecipeRegistry.registerRecipeType(String, IRecipeRegistry);
```
来添加一种自定义合成类型。自定义合成类型的写法详见原生类型。



样例
---
以下是一个合成表的样例，我们假设它的环境在前面的样例已经声明。
```
shaped(jinkela) {
	[diamond, dirt, diamond]
	[dirt, glass, dirt]
	[diamond, dirt, diamond]
}

shapeless(iron_plate) {
	[iron_ingot, iron_ingot, iron_ingot]
}

shaped(schrodinger_cat_box#0*5) {
	[glass, nil, glass]
	[iron_plate, fish, iron_plate]
	[glass, jinkela, glass]
}
```
