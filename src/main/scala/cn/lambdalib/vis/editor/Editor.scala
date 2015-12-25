package cn.lambdalib.vis.editor

import java.awt.Font
import java.io.File
import java.util

import cn.lambdalib.annoreg.core.Registrant
import cn.lambdalib.annoreg.mc.RegInitCallback
import cn.lambdalib.cgui.gui.component.TextBox.ConfirmInputEvent
import cn.lambdalib.cgui.gui.{CGuiScreen, Widget}
import cn.lambdalib.cgui.gui.component.Transform.{WidthAlign, HeightAlign}
import cn.lambdalib.cgui.gui.component._
import cn.lambdalib.cgui.gui.event._
import cn.lambdalib.util.client.font.IFont.{FontAlign, FontOption}
import cn.lambdalib.util.client.font.TrueTypeFont
import cn.lambdalib.util.helper.Color
import cn.lambdalib.util.key.{KeyHandler, KeyManager}
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard

import cn.lambdalib.cgui.ScalaCGUI._
import scala.collection.JavaConversions._

object Styles {
  val font = TrueTypeFont.defaultFont

  def rgb(hex: Int) = new Color(hex | 0xFF000000)
  def pure(lum: Double) = new Color(lum, lum, lum, 1)

  def texture(path: String) = new ResourceLocation("lambdalib:textures/vis/" + path + ".png")
  def elemTexture(path: String) = texture("elements/" + path)
  def buttonTexture(path: String) = texture("buttons/" + path)

  def newText(option: FontOption = new FontOption) = {
    val ret = new TextBox(option)
    ret.font = font
    ret
  }

  def newButton(x: Double, y: Double, w: Double, h: Double, text: String) = {
    val ret = new Widget(x, y, w, h)
    ret :+ pureTint(0.25, 0.35, false)
    ret :+ new Outline(pure(0.45))

    val tbox = new TextBox(new FontOption(8, FontAlign.CENTER)).setContent(text)
    ret :+ tbox

    ret
  }

  def pureTint(idle: Double, hover: Double, affectTex: Boolean) = {
    val ret = new Tint
    ret.idleColor = pure(idle)
    ret.hoverColor = pure(hover)
    ret.affectTexture = affectTex
    ret
  }

  val cErrored = rgb(0xee2222)
  val cModified = rgb(0x9f5a00)
}

import cn.lambdalib.vis.editor.Styles._

class Editor extends CGuiScreen {

  class MenuBar extends Widget {
    type MenuCallback = SubMenu => Any

    val len = 30
    val ht = 12
    var count = 0

    transform.height = ht

    private val dt = new DrawTexture().setTex(null)
    dt.color = pure(0.1)
    addComponent(dt)

    addComponent(newText(new FontOption(9, FontAlign.RIGHT, pure(0.4))).setContent("VisEditor 0.0 dev   "))

    def addButton(name: String, func: (Widget) => Unit) = {
      val button = new Widget
      button.transform.setSize(len, ht).setPos(5 + (len + 5) * count, 0)

      val tint = new Tint
      tint.idleColor = pure(0.1)
      tint.hoverColor = pure(0.3)

      val text = newText(new FontOption(12, FontAlign.CENTER))
      text.heightAlign = HeightAlign.CENTER
      text.setContent(name)

      button.addComponents(tint, text)
      button.listens[LeftClickEvent](() => func(button))

      addWidget(name, button)

      count += 1
    }

    var menuCallbacks = Map[String, List[MenuCallback]]()

    def addMenu(name: String, creator: MenuCallback) = {
      if (!menuCallbacks.contains(name)) {
        menuCallbacks = menuCallbacks updated (name, List(creator))
        addButton(name, w => {
          val sub = new SubMenu
          menuCallbacks(name) foreach (f => f(sub))
          // Set menu to pos of button
          sub.transform.setPos(w.transform.x, w.transform.y + w.transform.height)
          addWidget(sub)
          // getGui.gainFocus(sub)
        })
      } else {
        menuCallbacks = menuCallbacks updated (name, creator :: menuCallbacks(name))
      }
    }
  }

  private var menuContainer: Widget = null
  private var menuHover: Widget = null
  private var menuBar: MenuBar = null

  private var root: Widget = null

  def getRoot = root
  def getMenuBar = menuBar

  private var lastCreated: String = null
  private var currentPlugin: Option[VisPlugin] = None

  initWidgets()

  private def initWidgets(): Unit = {
    menuContainer = new Widget
    menuBar = new MenuBar
    menuHover = new Widget(0, 0, width, 2)
    root = new Widget

    menuHover :+ new DrawTexture().setTex(null).setColor(pure(0.1))
    menuHover.transform.doesDraw = false
    menuHover.listens((e: FrameEvent) => if(e.hovering) {
      // Show the menu bar
      menuHover.transform.doesDraw = false
      menuBar.transform.doesDraw = true
      println(menuHover.y + "," + menuHover.transform.y)
    })

    root.transform.doesListenKey = false

    menuContainer :+ menuBar
    menuContainer :+ menuHover

    gui.addWidget(root)
    gui.addWidget(menuContainer)

    menuBar.addMenu("Editor", menu => {
      EditorRegistry.getEditors foreach {
        case (name, factory) if name != lastCreated =>
          menu.addItem(name, () => {
            root.dispose()
            menuBar.dispose()
            initWidgets()

            currentPlugin match {
              case Some(p) => p.onDeactivate(false)
              case _ =>
            }

            val plugin = factory(this)
            plugin.onActivate()
            currentPlugin = Option(plugin)
            lastCreated = name
          })
        case _ =>
      }
    })

    menuBar.addMenu("File", _.addItem("Edit work folder", startEditWorkDirs))

    menuBar.addMenu("View", menu => menu.addItem("Show/Hide Cover", () => drawBack = !drawBack))

    menuBar.addMenu("View", menu => {
      menu.addItem("Hide Menu", () => {
        // Hide the menu bar
        menuHover.transform.doesDraw = true
        menuBar.transform.doesDraw = false
      })
    })
  }

  def addPopup(header: String, msg: String, buttons: (String, () => Any)*) = {
    val fontSize = 8
    val strlen = 10 + Styles.font.getTextWidth(msg, new FontOption(fontSize))
    val buttonLen = 18
    val buttonStep = buttonLen + 5
    val buttonW = buttonStep * buttons.length - 5

    val width = math.max(strlen, math.max(80, buttonW))

    val cover = new ScreenCover(this)
    val not = new Window(header, 0, 0, width, 45, 0)
    not.transform.setCenteredAlign()

    val textArea = new Widget(0, -10, width, 0)
    textArea.transform.setCenteredAlign()

    textArea :+ newText(new FontOption(fontSize, FontAlign.CENTER)).setContent(msg)

    buttons.zipWithIndex.foreach{
      case (buttonspec, index) =>
        val button = newButton(-buttonW/2 + index * buttonStep + buttonLen/2, 10, buttonLen, 7, buttonspec._1)
        button.listens[LeftClickEvent](() => {
          buttonspec._2()
          cover.dispose()
        })
        button.transform.setCenteredAlign()
        not :+ button
    }

    not :+ textArea
    cover :+ not
    root :+ cover
  }

  def notify(msg: String, onConfirmed: () => Any) = {
    addPopup("Notification", msg, ("OK", () => onConfirmed))
  }

  def confirm(msg: String, yesCallback: () => Any, noCallback: () => Any = () => {}) = {
    addPopup("Notification", msg, ("OK", yesCallback), ("Cancel", noCallback))
  }

  private def startEditWorkDirs() = {
    val cover = new ScreenCover(Editor.this)
    val window:HierarchyTab = new HierarchyTab(true, 0, 0, 150, 100, "Working Dirs", Window.CLOSABLE) {
      this.listens[CloseEvent](() => cover.dispose())
      override def rebuild(): Unit = {
        elements = VisConfig.getWorkDirs.map(new Element(_, Styles.elemTexture("folder"))).toList
        super.rebuild()
      }
    }

    window.initButton("Add directory", "add", w => {
      val cover = new ScreenCover(Editor.this)
      val askPath = new Window("Enter new path...", 0, 0, 100, 30, Window.CLOSABLE)

      askPath.listens[askPath.CloseEvent](() => cover.dispose())
      askPath.transform.setCenteredAlign()

      val textArea = new Widget(0, 15, 80, 12)
      val textbox = Styles.newText(new FontOption(9, FontAlign.CENTER)).setContent("ENTER: Confirm")
      textArea :+ textbox

      val input: EditBox = new EditBox {
        var str = ""

        transform.y = -5
        transform.width = 70
        override def repr = str
        override def setValue(str: String) = {
          this.str = str
          val file = new File(str)
          if (file.isDirectory && !VisConfig.getWorkDirs.contains(str)) {
            VisConfig.updateWorkDirs(str :: VisConfig.getWorkDirs.toList)
            window.rebuild()
            cover.dispose()
          } else {
            textbox.option.color.setColor4d(1, 0.2, 0.2, 1)
            textbox.content = "Invalid path"
          }
        }
      }
      input.transform.setCenteredAlign()

      askPath :+ textArea
      askPath :+ input

      cover :+ askPath
      root :+ cover
    })

    window.initButton("Remove directory", "remove", w => {
      window.getSelected match {
        case Some(elem) =>
          VisConfig.updateWorkDirs(VisConfig.getWorkDirs.filter(_ != elem.name))
          window.rebuild()
        case _ =>
      }
    })

    window.transform.setCenteredAlign()
    cover :+ window
    root :+ cover
  }

  private def createFileWindow(allowNewFile: Boolean)(name: String, buttonName: String,
                               buttonCallback: File => Boolean, abortCallback: () => Any) = {
    val cover = new ScreenCover(Editor.this)
    val alldirs = VisConfig.getWorkDirs map (d => new File(d))

    var currentPath: Option[File] = VisConfig.getCurrentDir match {
      case Some(str) => Some(new File(str))
      case None => None
    }

    def pathStr: String = currentPath match {
      case Some(file) => file.getAbsolutePath
      case None => "<no active path>"
    }

    val pathTeller = new Widget(2, 1, 146, 10)
    val pathTellerText = new TextBox()
    pathTeller :+ pureTint(0.3, 0.4, false)
    pathTeller :+ pathTellerText

    val pathInput = new Widget(2, -1, 130, 10)
    pathInput.transform.alignHeight = HeightAlign.BOTTOM
    val pathInput_t = new TextBox().allowEdit()
    pathInput :+ new DrawTexture().setTex(null).setColor(pure(0.3))
    pathInput :+ pathInput_t

    def updatePath(newPath: File) = {
      currentPath = Option(newPath)
      pathTellerText.setContent(pathStr)
    }

    updatePath(VisConfig.getCurrentDir match {
      case Some(dir) => new File(dir)
      case _ => null
    })

    def go_(tab: HierarchyTab) = currentPath match {
      case Some(f) =>
        val path = f.getPath + "\\" + pathInput_t.content
        val file = new File(path)
        if (file.isFile || // Is file
          allowNewFile) { // Or need to create a new one
          if (buttonCallback(file)) {
            cover.dispose()
          }
        } else if(file.isDirectory) {
          updatePath(file.getAbsoluteFile)
          pathInput_t.setContent("")
          tab.rebuild()
        } else {
          notify("Invalid file path " + pathInput_t.content, () => {})
        }
      case _ =>
        notify("You must select a valid path", () => {})
    }

    val tab: HierarchyTab = new HierarchyTab(true, 0, 0, 150, 86, name, Window.CLOSABLE) {
      this.listens[CloseEvent](() => {
        cover.dispose()
        abortCallback()
      })

      case class NoPathElem() extends Element("Path doesn't exist", Styles.elemTexture("folder_open"))
      case class PrevFolderElem() extends Element("..", Styles.elemTexture("folder")) {
        this.listens((w, e: LeftClickEvent) => {
          if (getTab.getSelected == Option(this)) {
            updatePath(currentPath.get.getParentFile)
            rebuild()
          }
        }, 1)
      }

      listArea.transform.x += 2
      listArea.transform.width -= 2

      var selectedPath = ""

      transform.setCenteredAlign()
      body.transform.height += 12

      override lazy val top = 12

      override def rebuild(): Unit = {
        elements = currentPath match {
          case Some(file) =>
            if (file.isDirectory) {
              val list = file.listFiles().map(f => {
                val elem = new Element(f.getName,
                  if(f.isDirectory) Styles.elemTexture("folder") else Styles.elemTexture("file"))
                elem.listens((w, e: LeftClickEvent) => {
                  if (f.isDirectory && getSelected == Option(elem)) {
                    updatePath(f)
                    rebuild()
                  }
                  if (!f.isDirectory) {
                    if (pathInput_t.content == f.getName) {
                      go_(this)
                    }
                    pathInput_t.setContent(f.getName)
                  }
                }, 1)
                elem
              }).toList
              if (alldirs.exists(f => f.equals(file))) list else new PrevFolderElem :: list
            } else List(new NoPathElem)
          case _ =>
            List(new NoPathElem)
        }
        super.rebuild()
      }
    }

    def go() = go_(tab)

    pathTeller.listens[LeftClickEvent](() => {
      val sn = new SubMenu
      alldirs.filter(!_.equals(currentPath.orNull)).foreach(dir => {
        sn.addItem(dir.getAbsolutePath, () => {
          currentPath = Some(dir)
          tab.rebuild()
          pathTellerText.setContent(dir.getAbsolutePath)
        })
      })
      sn.transform.y = 10
      sn.transform.alignWidth = WidthAlign.RIGHT
      pathTeller :+ sn
    })

    pathInput.listens[ConfirmInputEvent](() => go())

    val buttonAct = Styles.newButton(-1, -2, 15, 8, buttonName)
    buttonAct.transform.alignHeight = HeightAlign.BOTTOM
    buttonAct.transform.alignWidth = WidthAlign.RIGHT
    buttonAct.listens[LeftClickEvent](() => go())

    val body = tab.body
    body :+ pathTeller
    body :+ pathInput
    body :+ buttonAct
    cover :+ tab
    root :+ cover
  }

  /**
    * Opens a open file dialogue.
    * @param openedCallback returns: Should the window be closed.
    */
  def openFile(openedCallback: File => Boolean) = {
    createFileWindow(false)("Open...", "Open", openedCallback, () => {})
  }

  /**
    * Opens a save file dialogue.
    * @param saver returns: Should the window be closed.
    */
  def saveFile(saver: File => Boolean, abortedCallback: () => Any = () => {}) = {
    createFileWindow(true)("Save...", "Save", saver, abortedCallback)
  }

  override def drawScreen(mx: Int, my: Int, w: Float) = {
    if(width != menuBar.transform.width) {
      List(menuBar, menuHover, root).foreach(w => {
        w.transform.width = width
        w.dirty = true
      })
    }

    root.transform.setSize(width, height)

    super.drawScreen(mx, my, w)
  }

  override def keyTyped(char: Char, key: Int) = {
    gui.keyTyped(char, key)
    if (key == Keyboard.KEY_ESCAPE) {
      currentPlugin match {
        case Some(plugin) => plugin.onDeactivate(true)
        case _ => mc.displayGuiScreen(null)
      }
    }
  }

}

// COMMONLY USED WIDGETS

class SubMenu extends Widget {

  var len = 30.0
  val ht = 10.0

  val itemList = new util.ArrayList[Widget]
  def items = itemList.size

  this.listens[LostFocusEvent](() => dispose())

  def addItem(name: String, func: () => Unit) = {
    val itemWidget = new Widget

    itemWidget.transform.setSize(len, ht)

    val text = newText(new FontOption(8))
    text.content = name
    text.heightAlign = HeightAlign.CENTER
    len = math.max(len, text.font.getTextWidth(name, text.option))

    val tint = new Tint
    tint.idleColor = pure(0.1)
    tint.hoverColor = pure(0.3)

    itemWidget :+ tint
    itemWidget :+ text
    itemWidget.listens[LeftClickEvent](func)
    itemWidget.transform.setPos(0, items * ht)

    this :+ itemWidget

    itemList.add(itemWidget)
  }

  override def onAdded() = {
    super.onAdded()
    itemList.foreach(w => {
      w.transform.width = len + 3
      w.dirty = true
    })
    transform.width = len + 3
    dirty = true
    getGui.gainFocus(this)
  }

}

class Window(val name: String, defX: Double, defY: Double, width: Double, height: Double, style: Int = Window.DEFAULT)
  extends Widget(defX, defY, width, height) {

  /**
    * Fired when the window was closed.
    */
  class CloseEvent extends GuiEvent

  import Window._

  // Header
  val header = new Widget(0, -10, width, 10)

  private val tex = new DrawTexture().setTex(null)
  tex.color = pure(0.15)
  header :+ tex

  private val text = new TextBox(new FontOption(10)).setContent(" " + name)
  text.heightAlign = HeightAlign.CENTER
  header :+ text

  // Body (Add sub elements into body)
  val body = new Widget(0, 0, width, height)

  private val tex2 = new DrawTexture().setTex(null)
  tex2.color = pure(0.1)
  body :+ tex2

  this :+ body
  this :+ header

  transform.doesListenKey = false

  private var buttons = 0

  /**
    * Resize the window to the given size.
    */
  def resize(width: Double, height: Double) = {
    body.transform.setSize(width, height)
    dirty = true
  }

  private def addButton(name: String, callback: Widget => Unit) = {
    val sz = 9
    val step = sz + 1

    val btn = new Widget(-step * buttons - 1, 0.5, sz, sz)
    btn.transform.alignWidth = WidthAlign.RIGHT
    btn.listens[LeftClickEvent](() => {
      callback(btn)
    })

    val dt = new DrawTexture().setTex(texture("buttons/" + name))
    btn :+ dt

    val tint = new Tint()
    tint.idleColor = pure(0.7)
    tint.hoverColor = pure(1)
    tint.affectTexture = true
    btn :+ tint

    header :+ btn
    buttons += 1
  }

  if ((style & CLOSABLE) != 0) {
    addButton("close", w => {
      transform.doesDraw = false
      post(new CloseEvent)
    })
  }

  if ((style & MINIMIZABLE) != 0) {
    val t1 = texture("buttons/minimize")
    val t2 = texture("buttons/maximize")
    addButton("minimize", w => {
      val dt = DrawTexture.get(w)
      body.transform.doesDraw = !body.transform.doesDraw
      dt.setTex(if(body.transform.doesDraw) t1 else t2)
    })
  }

  if ((style & DRAGGABLE) != 0) {
    header.listens((e: DragEvent) => {
      val gui = header.getGui
      val ax = gui.mouseX - e.offsetX
      val ay = gui.mouseY - e.offsetY
      gui.moveWidgetToAbsPos(this, ax, ay + 10)
      this.dirty = true
    })
  }

}

class ScreenCover(env: CGuiScreen, blackout: Boolean = true) extends Widget {

  private def updateSize() = transform.setSize(env.width, env.height)

  if (blackout) {
    this :+ new DrawTexture().setTex(null).setColor4d(0, 0, 0, 0.3)
  }

  updateSize()

  this.listens[RefreshEvent](() => updateSize())

  override def onAdded() = this.gainFocus()

}

private object Toolbar {
  val height = 10.5
  val iconSz = 9
  val step = iconSz + 2

  lazy val fo_buttonHint = new FontOption(8, FontAlign.CENTER)
}

class Toolbar(name: String = "Toolbar") extends Window(name, 50, 50, 70, Toolbar.height) {
  import Toolbar._

  val buttons = new util.ArrayList[Widget]
  def buttonCount = buttons.size

  def addButton(hint: String, icon: ResourceLocation, callback: () => Any) = {
    val button = new Widget(3 + buttonCount * step, 0, iconSz, iconSz)
    button :+ new DrawTexture().setTex(icon)
    button.transform.alignHeight = HeightAlign.CENTER
    button.listens[LeftClickEvent](callback)
    button.listens((e: FrameEvent) => {
      if (e.hovering) {
        Styles.font.draw(hint, iconSz / 2, 10, fo_buttonHint)
      }
    })
    body :+ button

    resize(math.max(70, button.transform.x + 15), height)
    buttons.add(button)
    button
  }
}

object Window {
  // Style constants
  /**
    * Whether the window can be 'closed'.
    * Currently we just set transform.doesDraw = false, so you can redisplay it later.
    */
  val CLOSABLE    = 1 << 0
  /**
    * Whether the window can be minimized. Minimized window retains only top bar.
    */
  val MINIMIZABLE = 1 << 1
  /**
    * Whether the window can be dragged.
    */
  val DRAGGABLE = 1 << 2

  val DEFAULT = CLOSABLE | MINIMIZABLE | DRAGGABLE
}

// TESTS

class TestKey extends KeyHandler {

  override def onKeyDown() = {
    println("Pressed")
    Minecraft.getMinecraft.displayGuiScreen(new Editor)
  }

}

@Registrant
object Test {
  @RegInitCallback
  def init() = {
    KeyManager.dynamic.addKeyHandler("wtf", Keyboard.KEY_L, new TestKey)
  }
}