Annotation Registry API document
===

Intro
---
Annotation Registry is a coremod API that allows other mods to use a brand new (also elegant and fast) registration method based on annotation.

When developing minecraft mods, a great many kinds and quantities of entities needs to be registered, including Item, Block, Entity. For small mods, you can easily put the registration method in your mod class. For big mods, putting all the registration in one place increases coupling between classes and makes your mod hard to be maintained. And AnnotationRegistry is built to solve this problem. Using AnnoReg, you can register many kinds of entities using much simpler code, and you can distribute the registration call in many classes.

Using Annotation Registry is good in:
* Short and readable code.
* Optimized registration interfaces. (e.g. Block and OreDict can be registered at the same place)
* Distribute registration in different classes rather than processing them all at once, making mods very easily modularizable.
* Enables some other interfaces MC and Forge didn't directly provide. (e.g. Cross-side function call)

Installation
---
Annotation Registry is a normal coremod, which follows the standard mod installation procedure. If you have compiled version of AnnoReg, just put it in "mods" folder. If you want to directly use the mod source code in eclipse, you will have to link "src/main/java" into the workspace, and put the coremod loading hook (jar/AnnotationRegistry.jar) in eclipse/mods folder.

Developement
---
The registration method Annotation Registry provided is completely based on Annotation. Before any mod was loaded, AnnoReg will analyze the entities to be registered through Forge's AsmTable, and load them after then. In concrete, you need to do the following things in order to use AnnoReg:

1 Add mod declaration
Use ```@RegistrationMod``` in your mod main class (The one annotated with @Mod), and provide the mod's registration package, resource prefix, and registration name prefix. For example: 
```java
package cn.example;

@Mod(modid = "examplemod", version = "1.0")
@RegistrationMod(pkg = "cn.example", res = "example", prefix = "ex_")
class ExampleMod {
```
Let's explain the ```@RegistrationMod``` annotation's parameters.

pkg: The common package name of all classes, used to identify the class's belonged mod. This field should never collide with other mods that uses AnnoReg. (a.k.a, use unique package name for your mod, not globally-used ones)

res：The resource namespace that this mod uses. Used to setup Item and Block's textures automatically.

prefix: emittable, using empty string for default. Used in some cases that consists of a automatically-generated ID. You can use this to avoid name collision with other mods. Notice that the names mentioned here only will be used when they doesn't matter much. Important discriminators and other won't be generated this way.

2 Creat a class with stuffs to register
You can contain registration information in arbitary many classes, but these classes must all be populated with ```@Registant```, so AnnoReg can acquire the registration information in begin loading stages. When you want to reg stuffs in internal classes, you just have to annotate the topmost class, all subclasses will be automatically scanned.
e.g. Use a single class to handle all the machine block instances within your mod:

```java
package cn.example;
//in cn.example package, this belongs to the ExampleMod mentioned above.

@Registrant //This class needs to be scanned!
class MachineBlocks {
    //Contains registration info...
}
```

3 Register Instances/classes

Different registration annotations are used differently. E.g. the following code registers a BlockTestMachine instance:
```java
@Registrant
class MachineBlocks {
    @RegBlock
    public static BlockTestMachine testMachine = new BlockTestMachine();
}
```

4 Registration Query

Annotation Registry does not reg anything automatically(Except for the NetworkCall mentioned later), and mod itself must query the AnnoReg to reg different kinds of registration types. To be specific, you must specify the load stage of this load query: 
```java
RegistrationManager.INSTANCE.registerAll(this, "Init");
```
This is typically done in your Mod's FMLInitializationEvent handler method. Other stages include "PreInit" "PostInit" "ServerStarting" and so on. This function is case-sensitive, so be aware of your formatting.

P.S. You can also use something like
```java
RegistrationManager.INSTANCE.registerAll(this, "Block");
```
To query a specific type of registration, but it is not recommended. All registration type belongs to a load stage, and use that to load is much more clean.

Various Registration Type and Their Usage
---
See [RegTypes-en.md](RegTypes-en.md)。
