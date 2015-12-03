package cn.lambdalib.vis.refactor

import java.awt.Font
import java.util

import cn.lambdalib.annoreg.core.Registrant
import cn.lambdalib.annoreg.mc.RegInitCallback
import cn.lambdalib.cgui.gui.{Widget, LIGuiScreen}
import cn.lambdalib.cgui.gui.component.Transform.{WidthAlign, HeightAlign}
import cn.lambdalib.cgui.gui.component.{Transform, DrawTexture, TextBox, Tint}
import cn.lambdalib.cgui.gui.event.{DragEvent, LeftClickEvent, LostFocusEvent}
import cn.lambdalib.util.client.font.IFont.{FontAlign, FontOption}
import cn.lambdalib.util.client.font.TrueTypeFont
import cn.lambdalib.util.helper.Color
import cn.lambdalib.util.key.{KeyHandler, KeyManager}
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard

import cn.lambdalib.cgui.ScalaExtensions._

object Styles {
  val font = TrueTypeFont(new Font("Consolas", Font.PLAIN, 32), new Font("Arial", Font.PLAIN, 32))

  def rgb(hex: Int) = new Color(hex | 0xFF000000)
  def pure(lum: Double) = new Color(lum, lum, lum, 1)

  def texture(path: String) = new ResourceLocation("lambdalib:textures/vis/" + path + ".png")
  def elemTexture(path: String) = texture("elements/" + path)

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

      addWidget(button)

      count += 1
    }

    def addMenu(name: String, creator: Widget => SubMenu) = {
      addButton(name, w => {
        val sub: SubMenu = creator(w)
        // Set menu to pos of button
        sub.transform.setPos(w.transform.x, w.transform.y + w.transform.height)
        addWidget(sub)
        // getGui.gainFocus(sub)
      })
    }
  }

  private var menuBar: MenuBar = null
  private var root: Widget = null

  def getRoot = root
  def getMenuBar = menuBar

  private def initWidgets(): Unit = {
    menuBar = new MenuBar
    root = new Widget

    root.transform.doesListenKey = false

    gui.addWidget(root)
    gui.addWidget(menuBar)

    menuBar.addMenu("Editor", w => {
      val menu = new SubMenu
      EditorRegistry.getEditors foreach {
        case (name, plugin) =>
          menu.addItem(name, () => {
            root.dispose()
            menuBar.dispose()
            initWidgets()

            plugin.onActivate(this)
          })
      }
      menu
    })
  }

  initWidgets()

  override def drawScreen(mx: Int, my: Int, w: Float) = {
    if(width != menuBar.transform.width) {
      menuBar.transform.width = width
      root.transform.width = width
      menuBar.dirty = true
      root.dirty = true
    }

    val bodyHt: Double = height - menuBar.transform.height
    if(bodyHt != root.transform.height) {
      root.transform.height = bodyHt
      root.dirty = true
    }

    super.drawScreen(mx, my, w)
  }

}

// COMMONLY USED WIDGETS

class SubMenu extends Widget {

  val len = 30
  val ht = 10

  val itemList = new util.ArrayList[Widget]
  def items = itemList.size

  this.listens[LostFocusEvent](() => dispose())

  def addItem(name: String, func: () => Unit) = {
    val itemWidget = new Widget

    itemWidget.transform.setSize(len, ht)

    val text = newText(new FontOption(9))
    text.content = name
    text.heightAlign = HeightAlign.CENTER

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
      dispose()
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

object Window {
  // Style constants
  val CLOSABLE    = 1 << 0
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