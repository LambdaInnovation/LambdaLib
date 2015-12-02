package cn.lambdalib.vis.refactor

import java.awt.Font
import java.util

import cn.lambdalib.annoreg.core.Registrant
import cn.lambdalib.annoreg.mc.RegInitCallback
import cn.lambdalib.cgui.ScalaExtensions.SWidget
import cn.lambdalib.cgui.gui.{Widget, LIGuiScreen}
import cn.lambdalib.cgui.gui.component.Transform.{WidthAlign, HeightAlign}
import cn.lambdalib.cgui.gui.component.{DrawTexture, TextBox, Tint}
import cn.lambdalib.cgui.gui.event.{DragEvent, LeftClickEvent, LostFocusEvent}
import cn.lambdalib.util.client.font.IFont.{FontAlign, FontOption}
import cn.lambdalib.util.client.font.TrueTypeFont
import cn.lambdalib.util.helper.Color
import cn.lambdalib.util.key.{KeyHandler, KeyManager}
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard

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
  private class MenuBar extends SWidget {
    val len = 30
    val ht = 12
    var count = 0

    transform.height = ht

    private val dt = new DrawTexture().setTex(null)
    dt.color = pure(0.1)
    addComponent(dt)

    addComponent(newText(new FontOption(9, FontAlign.RIGHT, pure(0.4))).setContent("VisEditor 0.0 dev   "))

    def addButton(name: String, func: (SWidget) => Unit) = {
      val button = new SWidget
      button.transform.setSize(len, ht).setPos(5 + (len + 5) * count, 0)

      val tint = new Tint
      tint.idleColor = pure(0.1)
      tint.hoverColor = pure(0.3)

      val text = newText(new FontOption(12, FontAlign.CENTER))
      text.heightAlign = HeightAlign.CENTER
      text.setContent(name)

      button.addComponents(tint, text)
      button.listen(classOf[LeftClickEvent], (w, e: LeftClickEvent) => {
        func(button)
      })

      addWidget(button)

      count += 1
    }

    def addMenu(name: String, creator: (SWidget) => SubMenu) = {
      addButton(name, w => {
        val sub: SubMenu = creator(w)
        // Set menu to pos of button
        sub.transform.setPos(w.transform.x, w.transform.y + w.transform.height)
        addWidget(sub)
        getGui.gainFocus(sub)
      })
    }
  }

  private val menuBar = new MenuBar

  val root: SWidget = new SWidget
  root.transform.doesListenKey = false
  root.transform.y = menuBar.ht

  menuBar.addButton("Test1", w => { println("Test1!") })
  menuBar.addMenu("Test2", w => {
    val testMenu = new SubMenu
    testMenu.addItem("AAA", () => {})
    testMenu.addItem("BBB", () => {})
    testMenu.addItem("CCC", () => {})
    testMenu
  })

  gui.addWidget(root)
  gui.addWidget(menuBar)

  root.addWidget(new Window("Testt", 50, 50, 200, 160, Window.DEFAULT))

  val tab = new HierarchyTab(20, 30, 150)
  val element0 = new Element("SomeTrans", elemTexture("comp_transform"))
  element0 :+ new Element("x", elemTexture("float"))
  element0 :+ new Element("y", elemTexture("float"))
  element0 :+ new Element("z", elemTexture("float"))

  val element1 = new Element("Plain", elemTexture("string"))

  tab.addElement(element0)
  tab.addElement(element1)

  root.addWidget(tab)

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

class SubMenu extends SWidget {

  val len = 30
  val ht = 10

  val itemList = new util.ArrayList[SWidget]
  def items = itemList.size

  listen(classOf[LostFocusEvent], (w, e: LostFocusEvent) => {
    dispose()
  })

  def addItem(name: String, func: () => Unit) = {
    val itemWidget = new SWidget

    itemWidget.transform.setSize(len, ht)

    val text = newText(new FontOption(9))
    text.content = name
    text.heightAlign = HeightAlign.CENTER

    val tint = new Tint
    tint.idleColor = pure(0.1)
    tint.hoverColor = pure(0.3)

    itemWidget :+ tint :+ text
    itemWidget.listen(classOf[LeftClickEvent], (w, e: LeftClickEvent) => {
      func()
    })
    itemWidget.transform.setPos(0, items * ht)

    this :+ itemWidget

    itemList.add(itemWidget)
  }

}

class Window(val name: String, defX: Double, defY: Double, width: Double, height: Double, style: Int = Window.DEFAULT)
  extends SWidget(defX, defY, width, height) {

  import Window._

  // Header
  val header = new SWidget(0, -10, width, 10)

  private val tex = new DrawTexture().setTex(null)
  tex.color = pure(0.15)
  header :+ tex

  private val text = new TextBox(new FontOption(10)).setContent(" " + name)
  text.heightAlign = HeightAlign.CENTER
  header :+ text

  header.listen(classOf[DragEvent], (w, e: DragEvent) => {
    val gui = w.getGui
    val ax = gui.mouseX - e.offsetX
    val ay = gui.mouseY - e.offsetY
    this.transform.setPos(ax, ay)
    this.dirty = true
  })

  // Body (Add sub elements into body)
  val body = new SWidget(0, 0, width, height)

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

    val btn = new SWidget(-step * buttons - 1, 0.5, sz, sz)
    btn.transform.alignWidth = WidthAlign.RIGHT
    btn.listen(classOf[LeftClickEvent], (w, e: LeftClickEvent) => {
      callback(w)
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