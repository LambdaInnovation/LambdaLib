package cn.lambdalib.vis.refactor

import cn.lambdalib.cgui.gui.component.TextBox.ConfirmInputEvent
import cn.lambdalib.cgui.gui.component.Transform.{WidthAlign, HeightAlign}
import cn.lambdalib.cgui.gui.{Widget, WidgetContainer}
import cn.lambdalib.cgui.gui.component._
import cn.lambdalib.cgui.gui.event.{LeftClickEvent, FrameEvent}
import cn.lambdalib.cgui.ScalaExtensions._
import cn.lambdalib.util.client.HudUtils
import cn.lambdalib.util.client.font.IFont.{FontAlign, FontOption}
import cn.lambdalib.util.helper.Color
import cn.lambdalib.vis.refactor.ObjectEditor.ElementEditEvent
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11

object CGUIPlugin extends VisPlugin {

  override def onActivate(editor: Editor) = {
    new CGUIEditor(editor)
  }

}

object CGUIEditor {
  private val components: List[Component] = List(
    new DrawTexture(),
    new Tint(),
    new VerticalDragBar(),
    new ProgressBar(),
    new TextBox(),
    new Outline()
  )

  lazy val fo_createPosHint = new FontOption(9, FontAlign.CENTER)
}

class CGUIEditor(editor: Editor) {
  import CGUIEditor._

  abstract class Tool(name: String, hint: String) {
    toolbar.addButton(hint, texture("tl_" + name), () => {
      if (this != currentTool) {
        currentTool.onDeactivate()
        currentTool = this
        this.onActivate()
      }
    })

    def onSelectionChange(prev: Widget, next: Widget) = {}
    def onActivate() = {}
    def onDeactivate() = {}
  }

  private def texture(loc: String) = new ResourceLocation("lambdalib:textures/vis/cgui/" + loc + ".png")

  editor.getMenuBar.addMenu("View", ret => {
    ret.addItem("Hierarchy", () => hierarchy.transform.doesDraw = true)
    ret.addItem("Inspector", () => inspector.transform.doesDraw = true)
  })

  type Vec2D = (Double, Double)

  import scala.collection.JavaConversions._

  class SelectionOutline extends Component("SelectionOutline") {

    this.listens[FrameEvent](() => {
      GL11.glColor4d(1, 1, 1, 0.7)
      HudUtils.drawRectOutline(0, 0, widget.transform.width, widget.transform.height, 2)
    })

  }

  val root = editor.getRoot
  val canvas = new Widget
  /**
    * Layer that places auxillary widgets such as "resizer
    */
  val editingHelper = new Widget
  val tabs = new Widget

  val inspector = new WidgetInspector
  val hierarchy = new WidgetHierarchy
  val toolbar = new Toolbar

  canvas.transform.doesListenKey = false

  canvas.listens[FrameEvent](() => {
    val t = canvas.transform
    if(t.width != editor.width || t.height != editor.height) {
      t.setSize(editor.width, editor.height)
      canvas.dirty = true
    }
  })

  val toolNothing = new Tool("nothing", "Nothing") {}
  val toolResize = new Tool("resize", "Rectangle Tool") {
    val theRect = new Widget
    theRect :+ new DrawTexture().setTex(null).setColor4d(1, 1, 1, 0.5)
    theRect.listens[FrameEvent](() => {
      println((theRect.x, theRect.y, theRect.transform.width, theRect.transform.height))
    })

    private def moveRect(target: Widget) = {
      theRect.transform.setPos(target.x, target.y)
        .setSize(target.scale * target.transform.width, target.scale * target.transform.height)
      theRect.dirty = true

      if (theRect.disposed || theRect.getAbstractParent == null) {
        editingHelper :+ theRect
      }

      println(editingHelper.getHierarchyStructure)
    }
    override def onSelectionChange(prev: Widget, next: Widget) = {
      Option(next) match {
        case Some(w) =>
          moveRect(next)
        case _ =>
          theRect.dispose()
      }
    }
    override def onActivate() = actionOnSelected(moveRect)
    override def onDeactivate() = theRect.dispose()
  }

  var currentTool = toolNothing
  currentTool.onActivate()

  tabs :+ hierarchy
  tabs :+ inspector
  tabs :+ toolbar

  root.addWidget("Canvas", canvas)
  root.addWidget(editingHelper)
  root.addWidget(tabs)

  /**
    * @return A list of WidgetElement, retaining old ones if the corresponding widget is still there.
    */
  private def newElements(w: WidgetContainer, old: List[WidgetElement], t: IHierarchy): List[WidgetElement] = {
    w.getDrawList.filter(!_.disposed).map(w => {
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
  private def startCreateWidget(createMethod: (Vec2D, Vec2D) => Any) = {
    val coverage = new ScreenCoverage(editor)

    def toPosAndSize(a: Vec2D, b: Vec2D) = ((math.min(a._1, b._1), math.min(a._2, b._2)),
      (math.abs(a._1 - b._1), math.abs(a._2 - b._2)))

    tabs.transform.doesDraw = false

    var state: Int = 0
    var p0 = (0.0, 0.0)

    coverage.listens((e: LeftClickEvent) => {
      if (state == 0) {
        p0 = (e.x, e.y)
        state = 1
      } else {
        val p1 = (e.x, e.y)
        val ps = toPosAndSize(p0, p1)
        createMethod(ps._1, ps._2)

        tabs.transform.doesDraw = true

        onCanvasUpdated()
        coverage.dispose()
      }
    })

    coverage.listens((e: FrameEvent) => {
      def drawPos(x: Double, y: Double) = {
        val font = Styles.font
        font.draw(s"($x, $y)", x - 5, y - 10, fo_createPosHint)
      }

      // Draw the position information
      if(state == 1) {
        drawPos(p0._1, p0._2)

        GL11.glColor4d(1, 1, 1, 0.3)

        toPosAndSize(p0, (e.mx, e.my)) match {
          case ((x, y),(w, h)) => HudUtils.colorRect(x, y, w, h)
        }
      }

      drawPos(e.mx, e.my)
    })

    editor.getRoot :+ coverage
  }

  private def toWidget(pos: Vec2D, size: Vec2D)(implicit parent: Widget = null) = {
    println("Parent: " + parent)
    var scale: Double = 1
    var x = pos._1
    var y = pos._2
    if (parent != null) {
      scale = 1 / parent.scale
      x = (x - parent.x) / parent.scale
      y = (y - parent.y) / parent.scale
    }
    new Widget(x, y, size._1 * scale, size._2 * scale)
  }

  private def onCanvasUpdated() = {
    hierarchy.rebuild()
    inspector.updateTarget()
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

    override def foldable = w.getDrawList.exists(!_.disposed)

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

    this.initButton("Add Widget", "add", button => {
      val selected = getSelectedWidget
      selected match {
        case Some(w) =>
          val menu = new SubMenu
          menu.addItem("Add as child", () => startCreateWidget((pos, size) => {
            w.addWidget(toWidget(pos, size)(w))
          }))
          menu.addItem("Add after", () => startCreateWidget((pos, size) => {
            val par = w.getAbstractParent
            par.addWidgetAfter(toWidget(pos, size)(w.getWidgetParent), w)
          }))
          menu.addItem("Add before", () => startCreateWidget((pos, size) => {
            val par = w.getAbstractParent
            par.addWidgetBefore(toWidget(pos, size)(w.getWidgetParent), w)
          }))
          menu.transform.setPos(button.x - 50, button.y)
          editor.getRoot :+ menu
        case _ =>
          startCreateWidget((pos, size) => {
            canvas.addWidget(toWidget(pos, size))
          })
      }
    })

    this.initButton("Remove Widget", "remove", button => getSelectedWidget match {
        case Some(w) =>
          w.dispose()
          onCanvasUpdated()
        case None =>
      })

    this.initButton("Reparent", "reparent", fn = button => getSelectedWidget match {
      case Some(w) =>
        val coverage = new ScreenCoverage(editor)

        val tab = new HierarchyTab(false, 0, 0, 120, 150, "Select new parent...", Window.CLOSABLE)

        tab.listens[CloseEvent](() => {
          coverage.dispose()
        })

        tab.transform.setCenteredAlign()

        val elem = new WidgetElement(canvas)
        elem.folded = false
        tab :+ elem

        tab.transform.height += 15
        tab.body.transform.height += 15

        val confirmButton = Styles.newButton(0, -6, 10, 6, "OK")
        confirmButton.transform.alignWidth = WidthAlign.CENTER
        confirmButton.transform.alignHeight = HeightAlign.BOTTOM
        confirmButton.listens[LeftClickEvent](() => {
          val selected = tab.getSelected
          selected match {
            case Some(we: WidgetElement) if w == we.w || we.w.isChildOf(w) =>
              editor.notify("Can't reparent to child or self", () => coverage.dispose())
            case Some(we: WidgetElement) =>
              setSelected(null)

              var name = w.getName

              val par = w.getAbstractParent
              par.forceRemoveWidget(w)

              val newpar = we.w

              // Find a suitable name for widget
              if (newpar.hasWidget(name)) {
                var i = 0
                while (newpar.hasWidget(name + " " + i)) {
                  i += 1
                }
                name = name + " " + i
              }
              // Rebase!
              newpar.addWidget(name, w)

              coverage.dispose()
              onCanvasUpdated()
            case None =>
          }
        })

        tab.body :+ confirmButton
        coverage :+ tab
        root :+ coverage
      case None =>
    })

    this.listens((e: SelectionChangeEvent) => {
      inspector.updateTarget()
      currentTool.onSelectionChange(toSelectedWidget(e.previous), toSelectedWidget(e.newSel))

      e.previous match {
        case we: WidgetElement =>
          we.onDeselect()
          we.w.removeComponent("SelectionOutline")
        case _ =>
      }

      e.newSel match {
        case we: WidgetElement => we.w :+ new SelectionOutline
        case _ =>
      }
    })

    override def rebuild() = {
      elements = newElements(canvas, elements.asInstanceOf[List[WidgetElement]], this)
      super.rebuild()
    }

    private def toSelectedWidget(e: Element) = e match {
      case we: WidgetElement => we.w
      case _  => null
    }
  }

  class WidgetInspector extends HierarchyTab(true, 200, 100, 140, 100, "Inspector") {

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
      getSelectedWidget match {
        case Some(target) =>
          if (refresh || target != lastSelected) {
            elements = target.getComponentList.filter(_.canEdit).map(c => {
              val elem = new ComponentElement(c)
              ObjectEditor.default.addToHierarchy(elem, c)
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
  private def getSelectedWidget: Option[Widget] = hierarchy.getSelected match {
      case Some(e: WidgetElement) => Some(e.w)
      case None => None
      case _ => throw new RuntimeException
  }

}

