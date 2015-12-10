package cn.lambdalib.vis.editor

import cn.lambdalib.cgui.gui.Widget
import cn.lambdalib.cgui.gui.component._
import cn.lambdalib.cgui.gui.component.Transform.{WidthAlign, HeightAlign}
import cn.lambdalib.cgui.gui.event._
import cn.lambdalib.util.client.font.IFont.{FontAlign, FontOption}
import net.minecraft.util.ResourceLocation

import org.lwjgl.opengl.GL11._

import cn.lambdalib.cgui.ScalaExtensions._
import scala.collection.JavaConversions._
import Styles._

class HierarchyTab(hasButton: Boolean, defX: Double, defY: Double,
                   width: Double, height: Double, name: String = "Hierarchy", style: Int = Window.DEFAULT)
  extends Window(name, defX, defY, width, height, style) with IHierarchy {

  // Hierarchy-unique events
  class SelectionChangeEvent(val previous: Element, val newSel: Element) extends GuiEvent

  protected var elements = List[Element]()

  private var selected : Element = null

  def getSelected: Option[Element] = Option(selected)
  def setSelected(newSel: Element) = if (newSel != selected) {
    val prev = selected
    selected = newSel
    post(new SelectionChangeEvent(prev, newSel))
  }

  private val top = if(hasButton) 10 else 0
  private val listArea = new Widget(0, top, width, height - top)
  listArea :+ new DrawTexture().setTex(null).setColor4d(0.1, 0.1, 0.1, 1)
  body :+ listArea

  body.listens[LeftClickEvent](() => {
    setSelected(null)
  })

  private var eList: ElementList = null
  private var bar: Widget = null

  private var buttonSz = 0

  def requireSelection = true

  def added = getGui != null

  private val hintOption = new FontOption(8, FontAlign.CENTER)

  def initButton(hint: String, icon: String, fn: Widget => Any) = {
    val sz = 8
    val step = sz + 2
    val button = new Widget(2 + step * buttonSz, 1, sz, sz)
    button :+ new DrawTexture().setTex(Styles.texture("buttons/" + icon))
    button :+ new Tint(pure(0.7), pure(0.9), true)
    button.listens((e: FrameEvent) => {
      if(e.hovering) {
        glPushMatrix()
        glTranslated(0, 0, 1)
        Styles.font.draw(hint, button.transform.width / 2, -5, hintOption)
        glPopMatrix()
      }
    })
    button.listens[LeftClickEvent](() => fn(button))

    body :+ button
    buttonSz += 1
  }

  def :+(elem: Element) = {
    elements = elements :+ elem
    if (added) {
      rebuild()
    }
  }

  override def onAdded() = {
    super.onAdded()
    rebuild()
  }

  def rebuild() = {
    // Clear current selection, but retain if still present
    if(!elements.contains(selected)) {
      setSelected(null)
    }

    val oldList = eList
    if (eList != null) {
      listArea.removeComponent(eList)
    }
    if (bar != null) {
      bar.dispose()
    }

    eList = new ElementList
    elements.foreach(e => {
      eList.addWidget(e)
      e.onRebuild(eList)
    })

    listArea :+ eList

    if(oldList != null && eList.shouldScroll()) {
      eList.setProgress(oldList.getProgress)
    }

    // Check scroll bar
    if(eList.shouldScroll()) {
      val barLength = 8

      eList.getSubWidgets foreach (w => {
        w.transform.width = width - barLength
        w.dirty = true
      })

      bar = new Widget(width - barLength, 0, barLength, listArea.transform.height * 0.3)
      bar :+ new Tint(pure(0.3), pure(0.5))
      val cDragBar = new VerticalDragBar(0, listArea.transform.height - bar.transform.height)
      bar :+ cDragBar
      bar.listens[VerticalDragBar.DraggedEvent](() => {
        eList.setProgress((eList.getMaxProgress * cDragBar.getProgress).toInt)
      })
      cDragBar.setProgress(eList.getProgress.toDouble / eList.getMaxProgress)

      listArea :+ bar
    }
  }

}

trait IHierarchy {
  def :+(element: Element)
  def level = 0
}

class Element(val name: String, val icon: ResourceLocation, implicit val height: Double = 10) extends Widget with IHierarchy {

  val elemIconSz = height * 0.8

  protected var indent = 0
  private var hierarchy: IHierarchy = null

  var folded = true

  var elements = List[Element]()

  private val texMin = Styles.texture("buttons/minimize")
  private val texMax = Styles.texture("buttons/maximize")

  protected def indentOffset = indent * 2

  private var init = false

  val iconArea = new Widget
  iconArea :+ new DrawTexture().setTex(icon)

  val textArea = createText()

  protected def createText() = {
    val ret = new Widget
    ret.transform.doesListenKey = false
    val tbox = new TextBox(new FontOption(0.9 * height)).setContent(name)
    ret :+ tbox
    ret
  }

  override def onAdded() = {
    if(!init) {
      init = true

      val tab = getTab
      transform.setSize(tab.transform.width, height)

      iconArea.transform.setPos(5 + indentOffset, 0).setSize(elemIconSz, elemIconSz)
      iconArea.transform.doesListenKey = false
      iconArea.transform.alignHeight = HeightAlign.CENTER
      this :+ iconArea

      textArea.transform.setPos(18 + indentOffset, 0).setSize(50, height)
      this :+ textArea

      val dt = new DrawTexture().setTex(null).setColor4d(0, 0, 0, 0)
      this :+ dt

      val hov = Styles.pure(0.4)
      val idle = Styles.pure(0.12)

      if(tab.requireSelection) {
        this.listens[LeftClickEvent](() => {
          getTab.setSelected(this)
        })
      }

      this.listens[FrameEvent](() => {
        dt.color = getTab.getSelected match {
          case Some(sel) if sel == this => hov
          case _ => idle
        }
      })
    }
  }

  def addedInto(hier: IHierarchy) = {
    indent = hier.level + 1
    hierarchy = hier
  }

  def foldable = elements.nonEmpty

  private var foldButtonInit = false

  protected def initFoldButton(): Unit = {
    if(!foldButtonInit) {
      foldButtonInit = true

      val button = new Widget(-2, 0, elemIconSz, elemIconSz)
      button.transform.alignHeight = HeightAlign.CENTER
      button.transform.alignWidth = WidthAlign.RIGHT

      val tex = new DrawTexture().setTex(texMax)
      button :+ tex

      val tint = Styles.pureTint(0.7, 0.9, true)
      button :+ tint

      button.listens[LeftClickEvent](() => {
        folded = !folded
        tex.setTex(if (folded) texMax else texMin)
        getTab.rebuild()
      })

      this :+ button
    }
  }

  override def :+(element: Element) = {
    element.addedInto(this)
    elements = elements :+ element
    procesesElement(element)
  }

  var procesesElement = (w: Element) => {}

  override def level = indent

  def onRebuild(list: ElementList): Unit = {
    if(foldable) {
      initFoldButton()
    }
    if(!folded) {
      elements foreach (e => {
        list.addWidget(e)
        e.onRebuild(list)
      })
    }
  }

  /**
    * Get the window holding this element. Yields NullPtrException when not added into one.
    */
  def getTab = {
    var par = getWidgetParent
    while(par != null && !par.isInstanceOf[HierarchyTab]) {
      par = par.getWidgetParent
    }
    par.asInstanceOf[HierarchyTab]
  }

  def getHierarchy = hierarchy

}