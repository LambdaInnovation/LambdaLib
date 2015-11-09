# CGUI Documentation

Introduction
---

CGUI is a GUI framework designed to fit in the needs of creating GUI in Minecraft. Compared to Minecraft's plain widget layout (A list of ``GUIButtons``), CGUI uses position&scale hierarchy, component pattern and dynamic event handlers to describe a GUI. 

CGUI also provides you with a powerful in-game visualized editor, which can greatly change the workflow of GUI creation because artists can get involved. The created GUIs are stored as XML files, and can be de-serialized at runtime to be displayed.

CGUI is already being used extensively in [AcademyCraft](https://github.com/LambdaInnovation/AcademyCraft) to achive some complicated display effects.

Basic Concepts
---

``[Widget](#Widget Hierarchy)`` is the basic building block of CGUI. A widget represents an entity with position and size (width, height) lying in a 'canvas'. 
Widgets are also allowed to be *added into Widgets*, by doing this, the child widget will move and rescale together with its parent widget. This allows a great deal of flexibility for actions like window dragging and resizing.

``LIGui`` is the basic "canvas" of CGUI. Widgets are added into LIGui and LIGui handles everything for them, including drawing, dragging, keyboard clicking and many other stuffs. 
LIGui does so by taking event delegations from somewhere else. Most typically, ``GuiScreen`` and its subclasses. If you have question about event delegation, see section [Delegation](#Delegation).

You can treat ``Widget`` as what ``GameObject`` is in Unity. If you add an empty widget it does nothing. To make it be able to do things such as mouse event handling and picture drawing, you have to use ``GuiEventBus`` and possibly ``Component``.

Every action in CGUI is represented as a ``GuiEvent``. When drawing each frame, widget receives ``FrameEvent``, when mouse is clicked, the clicked widget receives ``MouseDownEvent``, when its dragged it receives ``DragEvent``...

To make a widget response for such events, just call ```Widget#listen(...)```. The code is very intuitive and simple, if combined with the use of Java8 lambda expression. For example, the following code snippet will send chat to player when player clicks button:

```java
EntityPlayer player = ...;
Widget w = new Widget();
// Setup the widget ...
// Listens to LeftClickEvent and do things on clicking
w.listen(LeftClickEvent.class, (__, event) -> 
{
	player.addChatMessage(new ChatComponentTranslation("You have clicked this! ;w;"));
});
```

As simple as that.

While event listening provides simple action processing on the fly, you might also want to reuse common widget templates, such as the behaviour of drawing a texture in widget area, or the behaviour of button ...

Such requirements are supported via the ```Component```. Component defines a set of states and a set of event handlers, and it can be copied simply to be added into multiple widgets.

Consider the following code that makes the widget display a tinted picture:

```java
Widget w = new Widget();

DrawTexture drawer = new DrawTexture().setTex(new ResourceLocation("mymod:textures/test.png"));

Tint tint = new Tint();
tint.affectTexture = true; // Make the Tint affect DrawTexture.

w.addComponent(drawer);
w.addComponent(tint);
```

Components are usually well-defined and contains many custom attributes for you to tweak.

- [ ] TODO: Finish animation briefing

That's basically the structure of CGUI. It's flexible and extensible, almost allows any kind of GUI creation. One last thing worth mentioning: The visualized editing system!

- [ ] TODO: Image of the new editor

After editing the GUI, it is stored as XML document, and can be restored any time by reading it back as a ``LIGui`` instance. ``Widget`` supports prototype pattern natively.
That is, you can copy widgets from the loaded ``LIGui`` arbitarily and forge them into any shape you want!

```java
LIGui loadedGui = CGUIDocLoader.load(new ResourceLocation("mymod:gui.xml"));
Widget mainWidget = loadedGui.getWidget("main").copy(); // Copies the main widget and all of its childs!

mainWidget.getWidget("button").listen(....);

gui.addWidget(mainWidget); // Add into runtime gui for real usage
```

Widget Hierarchy
---


Delegation
---


Event handlers
---


Components
---


Animation
---


Visualized Editing
---


Serialization&Deserialization
---


References
---

[Bob Nystrom. Game Programming Patterns - Prototype Pattern](http://gameprogrammingpatterns.com/prototype.html)