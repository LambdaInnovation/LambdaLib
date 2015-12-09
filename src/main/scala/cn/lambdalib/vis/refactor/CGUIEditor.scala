package cn.lambdalib.vis.refactor

import javax.vecmath.Vector2d

import cn.lambdalib.cgui.gui.component.TextBox.ConfirmInputEvent
import cn.lambdalib.cgui.gui.component.Transform.{WidthAlign, HeightAlign}
import cn.lambdalib.cgui.gui.{Widget, WidgetContainer}
import cn.lambdalib.cgui.gui.component._
import cn.lambdalib.cgui.gui.event._
import cn.lambdalib.cgui.ScalaExtensions._
import cn.lambdalib.util.client.{RenderUtils, HudUtils}
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
  lazy val fo_resizeHint = new FontOption(9, FontAlign.CENTER)

  lazy val icon_DragVT = Styles.buttonTexture("drag_vt")
  lazy val icon_DragHR = Styles.buttonTexture("drag_hr")
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

  abstract class ToolWithCompanion(name: String, hint: String) extends Tool(name, hint) {
    val companion = new Widget
    val compTrans = companion.transform // Fast alias
    var selected: Widget = null
    companion.listens[FrameEvent](() => {
      if (shouldUpdateWithSelected && selected != null) {
        if (selected.x != companion.x || selected.y != companion.y ||
          selected.transform.width * selected.scale != companion.transform.width ||
          selected.transform.height * selected.scale != companion.transform.height) {
          companion.dirty = true
          updatePos()
        }
      }
    })

    protected def shouldUpdateWithSelected = true

    private def updatePos() = companion.transform.setPos(selected.x, selected.y).
      setSize(selected.scale * selected.transform.width, selected.scale * selected.transform.height)

    private def moveRect(target: Widget) = {
      selected = target
      updatePos()
      companion.dirty = true

      if (companion.disposed || companion.getAbstractParent == null) {
        editingHelper :+ companion
      }
    }
    override def onSelectionChange(prev: Widget, next: Widget) = {
      Option(next) match {
        case Some(w) =>
          moveRect(next)
        case _ =>
          selected = null
          companion.dispose()
      }
    }
    override def onActivate() = actionOnSelected(moveRect)
    override def onDeactivate() = companion.dispose()
  }

  private def texture(loc: String) = new ResourceLocation("lambdalib:textures/vis/cgui/" + loc + ".png")

  editor.getMenuBar.addMenu("View", ret => {
    ret.addItem("Hierarchy", () => hierarchy.transform.doesDraw = true)
    ret.addItem("Inspector", () => inspector.transform.doesDraw = true)
  })

  type Vec2D = Vector2d
  implicit def toPair(vec: Vec2D): (Double, Double) = (vec.x, vec.y)
  implicit def toVec(pair: (Double, Double)): Vector2d = new Vector2d(pair._1, pair._2)
  def transformToRect(t: Transform): (Vec2D, Vec2D) = ((t.x, t.y), (t.width, t.height))

  import scala.collection.JavaConversions._

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

  val toolNothing = new ToolWithCompanion("nothing", "Nothing") {
    companion.listens[FrameEvent](() => {
      GL11.glColor4d(1, 1, 1, 0.7)
      HudUtils.drawRectOutline(0, 0, companion.transform.width, companion.transform.height, 2)
    })
  }

  val toolResize = new ToolWithCompanion("resize", "Rectangle Tool") {
    private var draggingLine: Widget = null
    private def dragging = draggingLine != null
    private val outline = new Outline(new Color(1, 1, 1, 0.5))
    outline.lineWidth = 0.8f
    companion :+ outline
    companion.listens((e: FrameEvent) => {
      if (dragging) {
        val font = Styles.font
        font.draw(s"(${compTrans.x}, ${compTrans.y})", 0, -5, fo_resizeHint)
        font.draw(s"width:${compTrans.width}", compTrans.width / 2, compTrans.height + 2, fo_resizeHint)
        font.draw(s"height:${compTrans.height}", compTrans.width, compTrans.height / 2, fo_resizeHint)
      }
    })

    override def shouldUpdateWithSelected = !dragging

    def createDragLn(walign: WidthAlign, halign: HeightAlign, resizer: (Double, Double) => (Vec2D, Vec2D)) = {

      val line = new Widget

      val csrSize = 10
      def drawsCursor(icon: ResourceLocation) = {
        line.listens((e: FrameEvent) => {
          if (e.hovering || draggingLine == line) {
            val gui = line.getGui
            val x = gui.mouseX - line.x - csrSize / 2
            val y = gui.mouseY - line.y - csrSize / 2
            RenderUtils.loadTexture(icon)
            HudUtils.rect(x, y, csrSize, csrSize)
          }
        })
      }

      if (walign == WidthAlign.CENTER) { // Resize width
        line.listens[FrameEvent](() => {
          line.transform.width = companion.transform.width
        })
        line.transform.height = 1
        drawsCursor(icon_DragVT)
      }
      if (halign == HeightAlign.CENTER) { // Resize height
        line.listens[FrameEvent](() => {
          line.transform.height = companion.transform.height
        })
        line.transform.width = 1
        drawsCursor(icon_DragHR)
      }
      line.transform.setAlign(walign, halign)

      line.listens[DragEvent](() => {
        val gui = editor.getGui
        draggingLine = line
        resizer(gui.mouseX, gui.mouseY) match {
          case (pos, size) =>
            compTrans.setPos(pos.x, pos.y).setSize(size.x, size.y)
        }
        gui.updateWidget(companion)
      })

      line.listens[DragStopEvent](() => {
        updateSize(selected,
          (compTrans.x, compTrans.y),
          (compTrans.width, compTrans.height)
        )
        draggingLine = null
      })

      companion :+ line
    }

    createDragLn(WidthAlign.LEFT, HeightAlign.CENTER, (mx, my) => {
      val nmx = math.min(mx, compTrans.x + compTrans.width)
      ((nmx, compTrans.y),
      (compTrans.x + compTrans.width - nmx, compTrans.height))
    })

    createDragLn(WidthAlign.RIGHT, HeightAlign.CENTER, (mx, my) => {
      val nmx = math.max(compTrans.x, mx)
      ((compTrans.x, compTrans.y),
      (nmx - compTrans.x, compTrans.height)
      )
    })

    createDragLn(WidthAlign.CENTER, HeightAlign.TOP, (mx, my) => {
      val nmy = math.min(my, compTrans.y + compTrans.height)
      ((compTrans.x, nmy),
      (compTrans.width, compTrans.y + compTrans.height - nmy))
    })

    createDragLn(WidthAlign.CENTER, HeightAlign.BOTTOM, (mx, my) => {
      val nmy = math.max(compTrans.y, my)
      ((compTrans.x, compTrans.y),
      (compTrans.width, nmy - compTrans.y))
    })
  }

  var currentTool: Tool = toolNothing
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

    def toPosAndSize(a: Vec2D, b: Vec2D) = ((math.min(a.x, b.x), math.min(a.y, b.y)),
      (math.abs(a.x - b.x), math.abs(a.y - b.y)))

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
    val ret = new Widget
    updateSize(ret, pos, size)
    ret
  }

  private def updateSize(w: Widget, pos: Vec2D, size: Vec2D) = {
    val parent = w.getWidgetParent
    val wscale = w.transform.scale
    var invscale: Double = wscale
    var x = pos.x
    var y = pos.y
    if (parent != null) {
      invscale *= parent.scale
      x = (x - parent.x) / parent.scale
      y = (y - parent.y) / parent.scale
    }
    w.transform.setPos(x / wscale, y / wscale).setSize(size.x / invscale, size.y / invscale)
    w.dirty = true
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

