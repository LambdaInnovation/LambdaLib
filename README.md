# LambdaLib
Common library and utils behind Lambda Innovation mods.

Introduction
---

LambdaLib is the code base for many Lambda Innovation mods. It provides many utilities for Minecraft modding and can boost modder's developement speed dramatically.

LambdaLib is licensed under [GPLv3](http://www.gnu.org/licenses/gpl.html), and currently works on Minecraft 1.7.10. We probably won't update to newer MC versions recently (until mojang make their codes better, which is unlikely to happen).

LambdaLib is merged from two previous projects:

* [LIUtils](https://github.com/LambdaInnovation/LIUtils) provides various functionalities and utilities for common modding.
* [AnnotationRegistry](https://github.com/LambdaInnovation/AnnotationRegistry) focuses on a series of Registration-friendly methods based on Annotations.

Their functionalities are pretty distinct, so the package names of the project will stay the same: ```cn.liutils``` and ```cn.annoreg```.

The project is under heavy developement and is currently not stable. There will be a formal release after we finished cleaning up the code.

Workspace setup
---

1. Link the ```src/main/java``` and  ```src/main/resources``` into your workspace.
2. Put the [coremod loading hook](jars/LambdaLib.jar) into your ``eclipse/mods`` folder.
3. Make sure you have installed JDK8 and uses 1.8 compilation level in your IDE. ForgeGradle default uses 1.6. 

Compilation
---

The project uses Java8 in developement and we officially uses [RetroLambda](https://github.com/evant/gradle-retrolambda) to compile. Therefore, any use of stream API is forbiddened.

You can use the following command to build locally:
```
gradle clean install
```

Contents
---

* CGUI - GUI Library with Event Factory, Component Pattern, Visualized Edit
* Vis - Animation and model utilities
* RecipeRegistry - Write recipes in a script and stay away from the trouble of ```GameRegistry.addRecipe```
* AnnotationRegistry - Use annotation to decouple modules and game elements
* NetworkCall - Send packets by directly calling methods
* Raytrace - Better wrapup for ray-tracing, allows Entity and Block Type filtering.

More to discover...

Contribution
---

If you are a talented coder and want to contribute to the project, feel free to submit a pull request! We would love the help.