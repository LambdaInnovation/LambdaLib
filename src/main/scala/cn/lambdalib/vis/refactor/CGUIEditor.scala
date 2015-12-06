package cn.lambdalib.vis.refactor

import cn.lambdalib.cgui.gui.component.TextBox.ConfirmInputEvent
import cn.lambdalib.cgui.gui.{Widget, WidgetContainer}
import cn.lambdalib.cgui.gui.component._
import cn.lambdalib.cgui.gui.event.{LeftClickEvent, FrameEvent}
import cn.lambdalib.cgui.ScalaExtensions._
import cn.lambdalib.util.client.HudUtils
import cn.lambdalib.util.helper.Color
import cn.lambdalib.vis.refactor.ObjectEditor.ElementEditEvent
import org.lwjgl.opengl.GL11

object CGUIEditor extends VisPlugin {

  private val components: List[Component] = List(
    new DrawTexture(),
    new Tint(),
    new VerticalDragBar(),
    new ProgressBar(),
    new TextBox(),
    new Outline()
  )

  import scala.collection.JavaConversions._

  class SelectionOutline extends Component("SelectionOutline") {

    this.listens[FrameEvent](() => {
      GL11.glColor4d(1, 1, 1, 0.7)
      HudUtils.drawRectOutline(0, 0, widget.transform.width, widget.transform.height, 2)
    })

  }

  var canvas: Widget = null
  var inspector: WidgetInspector = null
  var hierarchy: WidgetHierarchy = null

  /**
    * @return A list of WidgetElement, retaining old ones if the corresponding widget is still there.
    */
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

    this.listens[LeftClickEvent](() => {
      if (Option(w) == getSelectedWidget) {
        setTextEditable(true)
      }
    })

    textArea.listens[ConfirmInputEvent](() => {
      w.rename(TextBox.get(textArea).content)
      setTextEditable(false)
    })

    if(foldable) {
      initFoldButton()
    }

    setTextEditable(false)

    /**
      * Callback when corresponding widget was deselected.
      */
    def onDeselect() = {
      setTextEditable(false)
    }

    override def foldable = w.getDrawList.nonEmpty

    override def createText() = {
      val ret = new EditBox {
        override def backColor = if(editing) new Color(1, 1, 1, 0.1) else new Color(1, 1, 1, 0)
        override def repr = w.getName
        override def setValue(content: String) = w.rename(content)
      }
      ret
    }

    private def editing = if(textArea != null) textArea.transform.doesListenKey else false

    private def setTextEditable(value: Boolean) = {
      textArea.transform.doesListenKey = value
      TextBox.get(textArea).allowEdit = value

      if (!value) {
        DrawTexture.get(textArea).setColor4d(0, 0, 0, 0)
      }
    }

    override def onRebuild(list: ElementList): Unit = {
      elements = newElements(w, elements.asInstanceOf[List[WidgetElement]], this)
      super.onRebuild(list)
    }

    override def equals(other: Any) = other match {
      case we: WidgetElement => w == we.w
      case _ => false
    }
  }

  // (Currently) WidgetHierarchy's selection determins which Widget user is selecting globally.
  class WidgetHierarchy extends HierarchyTab(true, 0, 20, 120, 100) {

    this.listens((e: SelectionChangeEvent) => {
      inspector.updateTarget()
      e.previous match {
        case we: WidgetElement =>
          we.onDeselect()
          we.w.removeComponent("SelectionOutline")
        case _ => // NOPE
      }

      e.newSel.asInstanceOf[WidgetElement].w :+ new SelectionOutline
    })

    override def rebuild() = {
      elements = newElements(canvas, elements.asInstanceOf[List[WidgetElement]], this)
      super.rebuild()
    }
  }

  class WidgetInspector extends ObjectPanel(true, 200, 100, 120, 100, "Inspector") {

    private var lastSelected: Widget = null

    initButton("Add Component", "add", w => actionOnSelected(selected => {
      val menu = new SubMenu
      menu.transform.x = -60
      components.filter(x => selected.getComponent(x.name) == null).foreach(
        (x: Component) => {
          menu.addItem(x.name, () => {
            selected.addComponent(x.copy())
            updateTarget(true)
          })
        })
      w :+ menu
    }))

    initButton("Remove Component", "remove", w => {
      getSelected match {
        case Some(ComponentElement(c)) if c.name != "Transform" =>
          actionOnSelected(w =>{
            w.removeComponent(c.name)
            updateTarget(true)
          })
        case _ =>
      }
    })

    this.listens[ElementEditEvent](() => actionOnSelected(_.dirty = true))

    def updateTarget(refresh: Boolean = false) = {
      println("Selected: " + getSelectedWidget)
      getSelectedWidget match {
        case Some(target) =>
          if (refresh || target != lastSelected) {
            elements = target.getComponentList.filter(_.canEdit).map(c => {
              val elem = new ComponentElement(c)
              ObjectEditor.addToHierarchy(elem, c)
              elem
            }).toList
            lastSelected = target
          }
        case None =>
          elements = Nil
          lastSelected = null
      }
      rebuild()
    }

    case class ComponentElement(c: Component) extends Element(c.name, Styles.elemTexture("cgui_component"))

  }

  /**
    * Performs given operation on currently selected widget. Does nothing if not selecting.
    */
  private def actionOnSelected(fn: Widget => Any) = {
    getSelectedWidget match {
      case Some(target) => fn(target)
      case _ =>
    }
  }

  /**
    * Gets the currently selected widget.
    */
  private def getSelectedWidget: Option[Widget] =
    hierarchy.getSelected match {
      case Some(e: WidgetElement) => Some(e.w)
      case None => None
      case _ => throw new RuntimeException
    }

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
      val container: WidgetContainer = hierarchy.getSelected match {
        case Some(e: WidgetElement) => e.w
        case None => canvas
        case _ => throw new RuntimeException
      }
      container.addWidget(new Widget())

      hierarchy.rebuild()
    })

    val root = editor.getRoot
    root.addWidget(hierarchy)
    root.addWidget(canvas)
    root.addWidget(inspector)
  }

}
