package cn.lambdalib.vis.refactor

import java.awt.Font
import java.util

import cn.lambdalib.annoreg.core.Registrant
import cn.lambdalib.annoreg.mc.RegInitCallback
import cn.lambdalib.cgui.gui.{Widget, LIGuiScreen}
import cn.lambdalib.cgui.gui.component.Transform.{WidthAlign, HeightAlign}
import cn.lambdalib.cgui.gui.component.{Transform, DrawTexture, TextBox, Tint}
import cn.lambdalib.cgui.gui.event._
import cn.lambdalib.util.client.font.IFont.{FontAlign, FontOption}
import cn.lambdalib.util.client.font.TrueTypeFont
import cn.lambdalib.util.helper.Color
import cn.lambdalib.util.key.{KeyHandler, KeyManager}
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard

import cn.lambdalib.cgui.ScalaExtensions._
import scala.collection.JavaConversions._

object Styles {
  val font = TrueTypeFont(new Font("Consolas", Font.PLAIN, 32), new Font("Arial", Font.PLAIN, 32))

  def rgb(hex: Int) = new Color(hex | 0xFF000000)
  def pure(lum: Double) = new Color(lum, lum, lum, 1)

  def texture(path: String) = new ResourceLocation("lambdalib:textures/vis/" + path + ".png")
  def elemTexture(path: String) = if(path == null) null else texture("elements/" + path)

  def newText(option: FontOption = new FontOption) = {
    val ret = new TextBox(option)
    ret.font = font
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

import cn.lambdalib.vis.refactor.Styles._

class Editor extends LIGuiScreen {

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
        case (name, plugin) =>
          menu.addItem(name, () => {
            root.dispose()
            menuBar.dispose()
            initWidgets()

            plugin.onActivate(this)
          })
      }
    })

    menuBar.addMenu("View", menu => {
      menu.addItem("Hide Menu", () => {
        // Hide the menu bar
        menuHover.transform.doesDraw = true
        menuBar.transform.doesDraw = false
      })
    })
  }

  initWidgets()

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
    getGui.gainFocus(this)
  }

}

class Window(val name: String, defX: Double, defY: Double, width: Double, height: Double, style: Int = Window.DEFAULT)
  extends Widget(defX, defY, width, height) {

  import Window._

  // Header
  val header = new Widget(0, -10, width, 10)

  private val tex = new DrawTexture().setTex(null)
  tex.color = pure(0.15)
  header :+ tex

  private val text = new TextBox(new FontOption(10)).setContent(" " + name)
  text.heightAlign = HeightAlign.CENTER
  header :+ text

  header.listens((e: DragEvent) => {
    val gui = header.getGui
    val ax = gui.mouseX - e.offsetX
    val ay = gui.mouseY - e.offsetY
    gui.moveWidgetToAbsPos(this, ax, ay + 10)
    this.dirty = true
  })

  // Body (Add sub elements into body)
  val body = new Widget(0, 0, width, height)

  private val tex2 = new DrawTexture().setTex(null)
  tex2.color = pure(0.1)
  body :+ tex2

  this :+ body
  this :+ header

  transform.doesListenKey = false

  private var buttons = 0

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

}

class ScreenCoverage(env: LIGuiScreen, blackout: Boolean = true) extends Widget {

  private def updateSize() = transform.setSize(env.width, env.height)

  if (blackout) {
    this :+ new DrawTexture().setTex(null).setColor4d(0, 0, 0, 0.3)
  }

  updateSize()

  this.listens[RefreshEvent](() => updateSize())

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

  val DEFAULT = CLOSABLE | MINIMIZABLE
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
    println("Inittttttttttttttt")
    KeyManager.dynamic.addKeyHandler("wtf", Keyboard.KEY_L, new TestKey)
  }
}