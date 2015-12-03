package cn.lambdalib.vis.refactor

import cn.lambdalib.cgui.gui.{Widget, WidgetContainer}
import cn.lambdalib.cgui.gui.component._
import cn.lambdalib.cgui.gui.event.{LeftClickEvent, FrameEvent}
import cn.lambdalib.cgui.ScalaExtensions._

object CGUIEditor extends VisPlugin {

  private val components = List[Component](
    new DrawTexture(),
    new Tint(),
    new VerticalDragBar(),
    new ProgressBar(),
    new TextBox(),
    new Outline()
  )

  import scala.collection.JavaConversions._

  var canvas: Widget = null
  var inspector: WidgetInspector = null
  var hierarchy: WidgetHierarchy = null

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

  class WidgetElement(val w: Widget) extends Element(w.getName, Styles.elemTexture("widget")) {

    override def foldable = w.getDrawList.nonEmpty

    if(foldable) {
      initFoldButton()
    }

    override def onRebuild(list: ElementList): Unit = {
      elements = newElements(w, elements.asInstanceOf[List[WidgetElement]], this)
      super.onRebuild(list)
    }

  }

  class WidgetHierarchy extends HierarchyTab(true, 0, 20, 120, 100) {

    this.listens((e: SelectionChangeEvent) => {
      inspector.updateTarget()
    })

    override def rebuild() = {
      elements = newElements(canvas, elements.asInstanceOf[List[WidgetElement]], this)
      super.rebuild()
    }
  }

  class WidgetInspector extends ObjectPanel(true, 200, 100, 120, 100, "Inspector") {

    initButton("Add Component", "add", w => {
      val selected = getSelectedWidget
      if(selected != null) {
        val menu = new SubMenu
        menu.transform.x = -menu.transform.width
        components filter (x => selected.getComponent(x.name) == null) foreach
          (x => {
            menu.addItem(x.name, () => {
              selected.addComponent(x.copy())
              updateTarget()
            })
          })
        w :+ menu
      }
    })

    initButton("Remove Component", "remove", w => {
      getSelectedWidget match {
        case ce : ComponentElement => w.removeComponent(ce.c.name)
        case _ => // Not selecting a component, NOPE
      }
    })

    def updateTarget() = {
      val target = getSelectedWidget
      elements = target.getComponentList.filter(_.canEdit).map(c => {
        val elem = new ComponentElement(c)
        ObjectEditor.addToHierarchy(elem, c)
        elem
      }).toList
      rebuild()
    }

    case class ComponentElement(c: Component) extends Element(c.name, Styles.elemTexture("cgui_component"))

  }

  private def getSelectedWidget =
    if(hierarchy.getSelected == null) null else hierarchy.getSelected.asInstanceOf[WidgetElement].w

  override def onActivate(editor: Editor) = {
    hierarchy = new WidgetHierarchy()
    canvas = new Widget
    canvas.transform.doesListenKey = false

    canvas.listens[FrameEvent](() => {
      val t = canvas.transform
      if(t.width != editor.width || t.height != editor.height) {
        t.setSize(editor.width, editor.height)
        canvas.dirty = true
      }
    })

    inspector = new WidgetInspector

    editor.getMenuBar.addButton("+Widget", w => {
      val container: WidgetContainer =
        if(hierarchy.getSelected == null) canvas else hierarchy.getSelected.asInstanceOf[WidgetElement].w
      container.addWidget(new Widget())

      hierarchy.rebuild()
    })

    val root = editor.getRoot
    root.addWidget(hierarchy)
    root.addWidget(canvas)
    root.addWidget(inspector)
  }

}
