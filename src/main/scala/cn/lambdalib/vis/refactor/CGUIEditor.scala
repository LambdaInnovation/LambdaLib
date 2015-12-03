package cn.lambdalib.vis.refactor

import cn.lambdalib.cgui.gui.{Widget, WidgetContainer}
import cn.lambdalib.cgui.gui.component.ElementList
import cn.lambdalib.cgui.gui.event.FrameEvent
import cn.lambdalib.cgui.ScalaExtensions._

object CGUIEditor extends VisPlugin {

  import scala.collection.JavaConversions._

  var canvas: Widget = null

  private def newElements(w: WidgetContainer, old: List[WidgetElement], t: IHierarchy): List[WidgetElement] = {
    w.getDrawList.map(w => {
      val res = old.find(_.w == w)
      res match {
        case Some(x) => x
        case None =>
          val ret =new WidgetElement(w)
          ret.addedInto(t)
          ret
      }
    }).toList
  }

  class WidgetElement(val w: Widget) extends Element(w.getName, null) {

    override def foldable = w.getDrawList.nonEmpty

    if(foldable) {
      initFoldButton()
    }

    override def onRebuild(list: ElementList): Unit = {
      elements = newElements(w, elements.asInstanceOf[List[WidgetElement]], this)
      super.onRebuild(list)
    }

  }

  class WidgetHierarchy extends HierarchyTab(0, 20, 120, 100) {
    override def rebuild() = {
      elements = newElements(canvas, elements.asInstanceOf[List[WidgetElement]], this)
      super.rebuild()
    }
  }

  override def onActivate(editor: Editor) = {
    val hierarchy = new WidgetHierarchy()
    canvas = new Widget
    canvas.transform.doesListenKey = false

    canvas.listens[FrameEvent](() => {
      val t = canvas.transform
      if(t.width != editor.width || t.height != editor.height) {
        t.setSize(editor.width, editor.height)
        canvas.dirty = true
      }
    })

    editor.getMenuBar.addButton("+Widget", w => {
      val container: WidgetContainer =
        if(hierarchy.selected == null) canvas else hierarchy.selected.asInstanceOf[WidgetElement].w
      container.addWidget(new Widget())

      hierarchy.rebuild()
    })

    editor.getRoot.addWidget(hierarchy)
    editor.getRoot.addWidget(canvas)
  }

}
