# LambdaLib

A modding library that aims at making modding fluent and enjoyable.

LambdaLib contains many functionalities that is really helpful to Minecraft modding but isn't provided by Minecraft
or Forge. The utilities shipped with LambdaLib include:

* CGUI - Hierarchy based GUI framework, with an **in-game editor**
* AnnoReg - Use annotation to assist registry, achieving great decoupleness
* NetworkS11n - Send objects and messages with no effort through network
* ......

LambdaLib is currently not yet stable and is only for internal usage only,
but will reach a stable state perhaps soon.

Developement Setup
=====

A java8 developement environment with scala compatibility is required. Note that when you include source code, you must
put [jars\LambdaLib-PluginLoader.jar](jars\LambdaLib-PluginLoader.jar) into the `eclipse\mods` folder.

Project structure:

* `src\main\`: Core features shipped with release
* `src\editor\`: Features used only in dev environment
* `src\test\`: Test codes

Use in gradle
=====

Add in your `build.gradle`:
```gradle
repositories {
    ...
    mavenLocal()
}

dependencies {
    compile "cn.lambdalib:LambdaLib:{your wanted version}:dev"
}
```

After then, build LambdaLib with the correct version using `gradle install` first, then build your project.

Misc
=====

License: MIT
