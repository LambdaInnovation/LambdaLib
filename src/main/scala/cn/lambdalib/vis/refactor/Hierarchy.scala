package cn.lambdalib.vis.refactor

import cn.lambdalib.cgui.ScalaExtensions.SWidget
import cn.lambdalib.cgui.gui.Widget
import cn.lambdalib.cgui.gui.component.{TextBox, DrawTexture, ElementList}
import cn.lambdalib.cgui.gui.component.Transform.{WidthAlign, HeightAlign}
import cn.lambdalib.cgui.gui.event.LeftClickEvent
import cn.lambdalib.util.client.font.IFont.{FontOption, FontAlign}
import net.minecraft.util.ResourceLocation

object Hierarchy {
  val width = 100
  val elemIconSz = 8
  val elementHt = 10
}

import Hierarchy._

class HierarchyTab(defX: Double, defY: Double, height: Double)
  extends Window("Hierarchy", defX, defY, width, height) {

  private var elements = List[Element]()

  private val listArea = new SWidget(0, 10, width, height - 10)
  listArea :+ new DrawTexture().setTex(null).setColor4d(0.1, 0.1, 0.1, 1)
  body :+ listArea

  private var eList: ElementList = null

  def added = getGui != null

  def addElement(elem: Element) = {
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
    if (eList != null) {
      listArea.removeComponent(eList)
    }
    eList = new ElementList
    elements.foreach(e => {
      e.disposed = false
      eList.addWidget(e)
      e.onRebuild(eList)
    })
    listArea :+ eList
  }

}

trait IHierarchy {
  def :+(element: Element)
  def level = 0
}

class Element(val name: String, val icon: ResourceLocation) extends SWidget with IHierarchy {

  transform.setSize(width, elementHt)

  protected var indent = 0
  private var hierarchy: IHierarchy = null

  private var folded = true

  private var elements = List[Element]()

  private val texMin = Styles.texture("buttons/minimize")
  private val texMax = Styles.texture("buttons/maximize")

  private def indentOffset = indent * 2

  this :+ Styles.pureTint(0.12, 0.4, false)

  override def onAdded() = {
    val iconArea = new SWidget
    iconArea.transform.setPos(5 + indentOffset, 0).setSize(elemIconSz, elemIconSz)
    iconArea.transform.doesListenKey = false
    iconArea.transform.alignHeight = HeightAlign.CENTER
    iconArea :+ new DrawTexture().setTex(icon)
    this :+ iconArea

    val textArea = new Widget
    textArea.transform.setPos(18 + indentOffset, 0).setSize(0, elementHt)
    textArea.transform.doesListenKey = false
    val tbox: TextBox = new TextBox(new FontOption(9)).setContent(name)
    textArea.addComponent(tbox)
    this :+ textArea
  }

  def addedInto(hier: IHierarchy) = {
    indent = hier.level + 1
    hierarchy = hier
  }

  def foldable = elements.nonEmpty

  override def :+(element: Element) = {
    if(!foldable) {
      val button = new SWidget(-2, 0, 8, 8)
      button.transform.alignHeight = HeightAlign.CENTER
      button.transform.alignWidth = WidthAlign.RIGHT

      val tex = new DrawTexture().setTex(texMax)
      button :+ tex

      val tint = Styles.pureTint(0.7, 0.9, true)
      button :+ tint

      button listen (classOf[LeftClickEvent], (w, e: LeftClickEvent) => {
        folded = !folded
        tex.setTex(if(folded) texMax else texMin)
        getTab.rebuild()
      })

      this :+ button
    }
    element.addedInto(this)
    elements = elements :+ element
  }

  override def level = indent

  def onRebuild(list: ElementList) = {
    if(!folded) {
      elements foreach (e => {
        e.disposed = false
        list.addWidget(e)
      })
    }
  }

  /**
    * Get the window holding this element. Yields NullPtrException when not added into one.
    */
  def getTab = {
    var par = getWidgetParent
    while(!par.isInstanceOf[HierarchyTab]) par = par.getWidgetParent
    par.asInstanceOf[HierarchyTab]
  }

  def getHierarchy = hierarchy

}