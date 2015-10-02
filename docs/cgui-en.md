# CGUI Documentation

Introduction
-----
CGUI is a sub API of LambdaLib. It provides a Unity-like framework for swift GUI programming in Minecraft. 

In CGUI, everything in the GUI is treated as a ```Widget```. Widget is solely a container of ```Component```s and ```EventHandler```s, which provide the concrete function for this Widget, such as drawing, typing and click event handling. All widgets are handled in a class named ```LIGui```. LIGui provides the interface that Minecraft GUI provided generally, and you should delegate all drawing/keyboard/mouse events to the LIGui instance you created in your GUI class. 

What's more, Widgets are allowed to have sub widgets. That is, you can build sophiscated structures using hierachy method. For example, you can make a "window" with many ```InputBox```es and Buttons, with no effort.
CGUI also implemented a visualized editing environment. With the editing UI, you can create Widgets and edit their components, change hierarchy, and get a WYSIWYG feeling. The editing UI stores the gui data using .xml files, and you can manually restore them back to a LIGui at runtime.

For implementation simplicity, a special event registration method called **Annotation Event Registry** is provided. It allows you to create the gui from XML, and bind the events to widgets dynamically. You can simply just write event handlers in one class, specify the hierarchy target widget, and we will do the rest of job for you.


Get Started
-----
To use CGUI, either by hard-coding or by XML loading, you must create a LIGui and delegate Minecraft gui's original events to that gui.
```LIGui gui = new LIGui();``` creates an empty LIGui.
```LIGui gui = CGUIDocLoader.load(xml);``` creates a LIGui from the XML string specified.

Delegation looks like this:
```java
public class MyGui extends GuiScreen {

	public LIGui gui = new LIGui();
	
	//Vanilla draw event
	@Override
	public void drawScreen(int mouseX, int mouseY) {
		gui.draw(mouseX, mouseY);
	}
}
```
But if you simply just want to use one LIGUI, we have already provided the delegation class for you, with some helper methods. Check out ```LIGuiScreen``` and ```LIGuiContainer```.

Widget
-----
## Basic usage
Widget is the core function class in CGUI, so let's first look at it in more detail.

Creating a widget is simple:
```Widget w = new Widget();```
This creates a widget that does nothing except for being a placeholder. You can add some Component to it or attach EventHandlers to get certain functions.
e.g. The following code generates a widget which draws the texture **"academy:textures/guis/hhh.png"**, and exit on mouse hit.
```java
Widget w = new Widget();
w.addComponent(new DrawTexture().setTex(new ResourceLocation("academy:textures/guis/hhh.png"))); //Add the DrawTexture component
w.regEventHandler(new MouseDownHandler() {
	@Override
	public void handleEvent(MouseDownEvent event) {
		exit();
	}
});
```
You might have already noticed that every widget is automatically initialized with a ```Transform``` component. This is because position and size are basic properties for a Widget to be treated in a GUI screen. You are not allowed to remove the ```Transform``` component.

After creating a Widget, you can either add it into a ```LIGui``` or into a ```Widget```. In both ways they are treated as being a 'scene' created by its parent. When the widget is really added (i.e. The widget is directly added into a LIGui or it was added to a widget and the widget is being added or already added into a LIGui), its onAdded() function will get called. You can use this callback to do some initializing job.
If you want to destroy a Widget at runtime, simply call its ```dispose()``` function. It will get removed the next frame.
When the widget's position is updated, you must set ```widget.dirty=true``` so LIGui will update its position information the next frame. (This is because widget's absolute position is pre-calculated)

A widget can **create a copy of itself**. While copying, it copies all the Components and Event Listeners to the new Widget, and go down the hierachy tree to **copy all the sub-widgets** of it. This is particularly useful if you want to build some widgets out of a certain template. But be warned, **avoid usage of inner class event listeners** when copying Widget, or it may cause serious issues (End up handling the wrong widget).

## Widget naming
Widget and LIGui introduced a naming system, where all widgets within them have a this-container-unique id. If you call addWidget(widget), that name is auto generated. You can call ```addWidget(id, widget)``` to specify its id.

The searching of id is hierarchical, for example, you can use this to get widget "c" inside "b", which has the parent someWidget:
```Widget widgetC = someWidget.getWidget("b/c");```
An id should NOT consist of **"/"**, as it was used for namespace dividing.

CGUI Editor
-----
The real thing that makes CGUI powerful is its visualized editor. Within the editor you can create widgets, edit their properties, create hierarchies and save them to a single XML file. The XML file can be later dynamically loaded into a fresh LIGui.
You can use in-game command ```/cgui``` to open a new gui and ```/cgui filename``` to open a existing xml file. Then the UI will pop up. In dev environment(eclipse), the search is done in **"MCP/eclipse/"** folder.
The UI is pretty much self-explaining so I won't say too much about it. You can use **Hierarchy** to view currently existing Widgets and change the current selection. Use the direction buttons provided to change a widget's hierarchy order. Use the **Toolbar** to save or save as, and add new widgets. When you are selecting a widget, you can use **Property** to view&edit&add the Widget's components.

Runtime loading
-----
After saving a XML file, you would want to load it at runtime using:
```java
CGUIDocLoader.load(xml);
```
You will get a ready-to use LIGui instance right away. But You would probably want to add lots of functions to it (e.g. event callbacks, dynamic widget generations...), and we have provided the fast hook for you.
Call ```EventLoader.load(WidgetContainer container, Object eventProvider);```, and it will register all the functions populated with ```@GuiCallback(String widgetName)``` as eventHandlers into the targeting widget. The handler method must have the signature ```(Widget w, <? extends GuiEvent> event)V.``` exactly.
The first param of ```EventLoader.load``` is ```WidgetContainer```, which means you can use this method both on a LIGui and a Widget. If the path provided in the annotation is **""**, the func is registered to the LIGui/Widget itself.

Example code:
```java
public class GalgameGui extends LIGuiScreen {
	
	public GalgameGui() {
		this.gui = CGUIDocLoader.load(readXml());
		EventLoader.load(this.gui, this);
	}
	
	@GuiCallback("quit")
	public void onQuit(Widget w, MouseDownEvent event) {
		this.closeScreen();
	}
	
	@GuiCallback("main/confirm")
	public void onConfirmed(Widget w, MouseDownEvent event) {
		gui.addWidget(new Nikonikoni()); //I was just joking >)
	}
	
}
```
Of course, for clear purpose you might want to seperate the registration of different widget into different classes.
