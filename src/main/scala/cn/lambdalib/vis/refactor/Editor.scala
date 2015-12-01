package cn.lambdalib.vis.refactor

import java.awt.Font
import java.util

import cn.lambdalib.annoreg.core.Registrant
import cn.lambdalib.annoreg.mc.RegInitCallback
import cn.lambdalib.cgui.ScalaExtensions.SWidget
import cn.lambdalib.cgui.gui.LIGuiScreen
import cn.lambdalib.cgui.gui.component.Transform.HeightAlign
import cn.lambdalib.cgui.gui.component.{DrawTexture, TextBox, Tint}
import cn.lambdalib.cgui.gui.event.{LeftClickEvent, LostFocusEvent}
import cn.lambdalib.util.client.font.IFont.{FontAlign, FontOption}
import cn.lambdalib.util.client.font.TrueTypeFont
import cn.lambdalib.util.helper.Color
import cn.lambdalib.util.key.{KeyHandler, KeyManager}
import net.minecraft.client.Minecraft
import org.lwjgl.input.Keyboard

object Styles {
  val font = TrueTypeFont(new Font("Consolas", Font.PLAIN, 32), new Font("Arial", Font.PLAIN, 32))

  def rgb(hex: Int) = new Color(hex | 0xFF000000)
  def pure(lum: Double) = new Color(lum, lum, lum, 1)

  def newText(option: FontOption = new FontOption) = {
    val ret = new TextBox(option)
    ret.font = font
    ret
  }
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

  menuBar.addButton("Test1", w => { println("Test1!") })
  menuBar.addMenu("Test2", w => {
    val testMenu = new SubMenu
    testMenu.addItem("AAA", () => {})
    testMenu.addItem("BBB", () => {})
    testMenu.addItem("CCC", () => {})
    testMenu
  })

  gui.addWidget(menuBar)

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