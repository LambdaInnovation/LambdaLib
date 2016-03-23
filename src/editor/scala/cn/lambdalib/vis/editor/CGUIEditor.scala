/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.vis.editor

import java.io.{File, FileInputStream, FileOutputStream, IOException}
import javax.vecmath.Vector2d

import cn.lambdalib.cgui.gui.component.TextBox.ConfirmInputEvent
import cn.lambdalib.cgui.gui.component.Transform.{HeightAlign, WidthAlign}
import cn.lambdalib.cgui.gui.{Widget, WidgetContainer}
import cn.lambdalib.cgui.gui.component._
import cn.lambdalib.cgui.gui.event._
import cn.lambdalib.cgui.ScalaCGUI._
import cn.lambdalib.cgui.loader.xml.CGUIDocLoader
import cn.lambdalib.cgui.xml.CGUIDocument
import cn.lambdalib.util.client.font.IFont
import cn.lambdalib.util.client.{HudUtils, RenderUtils}
import cn.lambdalib.util.client.font.IFont.{FontAlign, FontOption}
import cn.lambdalib.util.helper.{Color, GameTimer}
import cn.lambdalib.vis.editor.ObjectEditor.ElementEditEvent
import net.minecraft.util.ResourceLocation
import org.apache.commons.io.IOUtils
import org.lwjgl.opengl.GL11
import org.xml.sax.SAXException

import collection.mutable

object CGUIEditor {
  private val components = mutable.ArrayBuffer[Component](
    new DrawTexture(),
    new Tint(),
    new ProgressBar(),
    new TextBox(),
    new Outline(),
    new DragBar()
  )

  /**
    * Adds a component template to be reused in CGUI Editor.
    */
  def addComponent(template: Component): Unit = {
    components += template
  }

  private lazy val fo_createPosHint = new FontOption(9, FontAlign.CENTER)
  private lazy val fo_resizeHint = new FontOption(9, FontAlign.CENTER)

  private lazy val icon_DragVT = Styles.buttonTexture("drag_vt")
  private lazy val icon_DragHR = Styles.buttonTexture("drag_hr")
}

class CGUIEditor(editor: Editor) extends VisPlugin(editor) {
  import CGUIEditor._
  import scala.collection.JavaConversions._

  /**
    * The file that currently opened path correspond to.
    */
  var path: Option[File] = None

  // Type alias & basic utils
  type Vec2D = Vector2d
  implicit def toPair(vec: Vec2D): (Double, Double) = (vec.x, vec.y)
  implicit def toVec(pair: (Double, Double)): Vec2D = new Vec2D(pair._1, pair._2)
  def transformToRect(t: Transform): (Vec2D, Vec2D) = ((t.x, t.y), (t.width, t.height))

  private def texture(loc: String) = new ResourceLocation("lambdalib:textures/vis/cgui/" + loc + ".png")

  // Utils

  private def updateSize(w: Widget, pos: Vec2D, size: Vec2D) = {
    val wscale = w.transform.scale
    var invscale: Double = wscale
    w.getWidgetParent match {
      case wp: Widget => invscale *= wp.scale
      case _ =>
    }
    editor.getGui.moveWidgetToAbsPos(w, pos.x, pos.y)
    w.transform.setSize(size.x / invscale, size.y / invscale)
  }

  /**
    * Gets the currently selected widget.
    */
  private def getSelectedWidget: Option[Widget] = hierarchy.getSelected match {
    case Some(e: WidgetElement) => Some(e.w)
    case None => None
    case _ => throw new RuntimeException
  }

  /**
    * Performs given operation on currently selected widget. Does nothing if not selecting.
    */
  private def actionOnSelected(fn: Widget => Any) = getSelectedWidget match {
      case Some(target) => fn(target)
      case _ =>
    }

  /**
    * @param w The container of wanted widgets
    * @param t The IHierarchy for the elements to be added into
    * @return A list of WidgetElement, retaining old ones if the corresponding widget is still there.
    */
  private def newElements(w: WidgetContainer,
                          old: List[WidgetElement],
                          t: IHierarchy,
                          supplier: Widget => WidgetElement = w => new WidgetElement(w)): List[WidgetElement] = {
    w.getDrawList.filter(!_.disposed).map(w => {
      val res = old.find(_.w == w)
      res match {
        case Some(x) => x
        case None =>
          val ret = supplier(w)
          ret.addedInto(t)
          ret
      }
    }).toList
  }

  private def startSaving(onAbort: () => Any = () => {}, onSuccess: () => Any = ()=>{}) =
    editor.saveFile(file => {
      var successful: Boolean = false
      try {
        CGUIDocument.write(canvas, file)
        path = Some(file)
        successful = true
      } catch {
        case e: IOException =>
          editor.notify(s"Saving to ${file.getName} failed.", () => {})
          e.printStackTrace()
      }
      if (successful) {
        onSuccess()
      }
      successful
    }, onAbort)

  // Events
  private def onCanvasUpdated() = {
    hierarchy.rebuild()
    inspector.updateTarget()
  }

  override def onDeactivate(quit: Boolean) = {
    editor.confirm("Save before close?",
      () => {
        startSaving(
          () => {}, // Do nothing when quitted saving
          () => super.onDeactivate(quit)) // Quit if saved
      },
      () => super.onDeactivate(quit)) // Quit and not save
  }
  //

  // Gui actions
  /**
    * Starts to create a new widget. makes the user supply (pos, size) and do the actual creation with the
    * ``createMethod`` passed in.
    */
  private def startCreateWidget(createMethod: (Vec2D, Vec2D) => Any) = {
    val cover = new ScreenCover(editor)

    def toPosAndSize(a: Vec2D, b: Vec2D) = ((math.min(a.x, b.x), math.min(a.y, b.y)),
      (math.abs(a.x - b.x), math.abs(a.y - b.y)))

    tabs.transform.doesDraw = false

    var state: Int = 0
    var p0 = (0.0, 0.0)

    cover.listens((e: LeftClickEvent) => {
      if (state == 0) {
        p0 = (e.x, e.y)
        state = 1
      } else {
        val p1 = (e.x, e.y)
        val ps = toPosAndSize(p0, p1)
        createMethod(ps._1, ps._2)

        tabs.transform.doesDraw = true

        onCanvasUpdated()
        cover.dispose()
      }
    })

    cover.listens((e: FrameEvent) => {
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

    editor.getRoot :+ cover
  }
  //

  // Widgets
  val root = editor.getRoot

  val canvas = new Widget
  /**
    * Layer that places auxillary widgets such as "resizer"
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
  //

  // Misc Init
  private val menubar = editor.getMenuBar

  {
    lazy val option = new FontOption(10, new Color(0xdddddd))
    val widget = new Widget().pos(3, 10).listens[FrameEvent](() => {
      val text = path match {
        case Some(file) => "File: " + file.getName
        case None       => "No file opened"
      }
      Styles.font.draw(text, 0, 0, option)
    })
    root :+ widget
  }

  editor.getMenuBar.addMenu("View", ret => {
    ret.addItem("Hierarchy", () => hierarchy.transform.doesDraw = true)
    ret.addItem("Inspector", () => inspector.transform.doesDraw = true)
    ret.addItem("Toolbar", () => toolbar.transform.doesDraw = true)
  })

  editor.getMenuBar.addMenu("File", menu => {
    menu.addItem("Open Legacy...", () => editor.openFile(file => {
      var successful: Boolean = false
      try {
        val container = CGUIDocLoader.load(IOUtils.toString(
          new FileInputStream(file)))
        canvas.clear()
        canvas.addAll(container)
        onCanvasUpdated()
        successful = true
      } catch {
        case e @ (_:IOException | _:SAXException) =>
          editor.notify(s"Opening ${file.getName} failed.", () => {})
      }
      successful
    }))
    menu.addItem("Open", () => editor.openFile(file => {
      var successful: Boolean = false
      try {
        val container = CGUIDocument.read(file)
        canvas.clear()
        canvas.addAll(container)
        onCanvasUpdated()
        path = Some(file)
        successful = true
      } catch {
        case e @ (_:IOException | _:SAXException) =>
          editor.notify(s"Opening ${file.getName} failed.", () => {})
      }
      successful
    }))
    menu.addItem("Save", () => path match {
      case Some(file) =>
        var stream: FileOutputStream = null
        try {
          stream = new FileOutputStream(file)
          CGUIDocument.write(canvas, stream)
        } catch {
          case e: Exception =>
            editor.addPopup("Error", "Save failed: " + e.getClass.getSimpleName, ("OK", () => {}))
        } finally {
          if (stream != null)
            stream.close()
        }
      case None => startSaving()
    })
    menu.addItem("Save As", () => startSaving())
  })


  tabs :+ hierarchy
  tabs :+ inspector
  tabs :+ toolbar

  root.addWidget("Canvas", canvas)
  root.addWidget(editingHelper)
  root.addWidget(tabs)
  //

  // Subclasses
  case class WidgetElement(val w: Widget,
                           elementSupplier: Widget => WidgetElement = w => new WidgetElement(w))
    extends Element(w.getName, Styles.elemTexture("widget")) {

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
      elements = newElements(w, elements.asInstanceOf[List[WidgetElement]], this, elementSupplier)
      super.onRebuild(list)
    }

    override def equals(other: Any) = other match {
      case we: WidgetElement => w == we.w
      case _ => false
    }
  }

  // (Currently) WidgetHierarchy's selection determines which Widget user is selecting globally.
  class WidgetHierarchy extends HierarchyTab(true, 0, 20, 120, 100) {

    this.initButton("Add Widget", "add", button => {
      def toWidget(pos: Vec2D, size: Vec2D, adder: Widget => Any) = {
        val ret = new Widget
        adder(ret)
        updateSize(ret, pos, size)
        ret
      }

      val selected = getSelectedWidget
      selected match {
        case Some(w) =>
          val menu = new SubMenu
          menu.addItem("Add as child", () => startCreateWidget((pos, size) => toWidget(pos, size, w.addWidget)))
          menu.addItem("Add after", () => startCreateWidget((pos, size) => {
            toWidget(pos, size, (w2) => w.getAbstractParent.addWidgetAfter(w2, w))
          }))
          menu.addItem("Add before", () => startCreateWidget((pos, size) => {
            val par = w.getAbstractParent
            toWidget(pos, size, (w2) => par.addWidgetBefore(w2, w))
          }))
          menu.transform.setPos(button.x - 50, button.y)
          editor.getRoot :+ menu
        case _ =>
          startCreateWidget((pos, size) => toWidget(pos, size, canvas.addWidget))
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
        val cover = new ScreenCover(editor)

        val tab = new HierarchyTab(false, 0, 0, 120, 150, "Select new parent...", Window.CLOSABLE)

        tab.listens[CloseEvent](() => {
          cover.dispose()
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
              editor.notify("Can't reparent to child or self", () => cover.dispose())
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

              cover.dispose()
              onCanvasUpdated()
            case None =>
          }
        })

        tab.body :+ confirmButton
        cover :+ tab
        root :+ cover
      case None =>
    })

    this.initButton("Duplicate", "duplicate", button => getSelectedWidget match {
      case Some(widget) =>
        val parent = widget.getAbstractParent
        val name = widget.getName
        val dup = widget.copy()
        var idx = 0
        while (parent.hasWidget(name + idx)) {
          idx += 1
        }

        parent.addWidget(name + idx, dup)
        onCanvasUpdated()
      case _ =>
    })

    this.initButton("Move up", "up", _ => getSelectedWidget match {
      case Some(w) =>
        val par = w.getAbstractParent
        val dlist = par.getDrawList
        par.reorder(w, math.max(0, dlist.indexOf(w)-1))
        onCanvasUpdated()
      case _ =>
    })

    this.initButton("Move down", "down", _ => getSelectedWidget match {
      case Some(w) =>
        val par = w.getAbstractParent
        val dlist = par.getDrawList
        par.reorder(w, math.min(dlist.size, dlist.indexOf(w) + 2))
        onCanvasUpdated()
      case _ =>
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

    private def supply(w: Widget): WidgetElement = {
      new WidgetElement(w, supply) {
        def updateAlpha() = DrawTexture.get(this.iconArea).color.a = if (w.hidden) 0.1 else 1.0
        this.iconArea.listens[LeftClickEvent](() => {
          w.hidden = !w.hidden
          updateAlpha()
        })
        updateAlpha()
      }
    }

    override def rebuild() = {
      elements = newElements(canvas, elements.asInstanceOf[List[WidgetElement]], this, supply)

      super.rebuild()
    }

    private def toSelectedWidget(e: Element) = e match {
      case we: WidgetElement => we.w
      case _  => null
    }
  }

  /**
    * Component & Meta properties inspector of currently selected widget.
    */
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

    var lastUpdated = GameTimer.getAbsTime

    this.listens[ElementEditEvent](() => actionOnSelected(_.dirty = true))
    this.listens[FrameEvent](() => { // Schedule to update the value
      val time = GameTimer.getAbsTime
      if (time - lastUpdated > 1000L) {
        lastUpdated = time
        // Update not-editing elements only
        elements foreach update
      }
    })

    private def update(e: Element): Unit = {
      ObjectEditor.getModifier(e) match {
        case Some(m: EditBox) if m.isEditing || !e.isFocused =>
        case Some(m) => m.updateRepr()
        case _ =>
      }
      e.elements foreach update
    }

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
  //

  // Tools
  val c_toolSelected = Styles.pure(1)
  val c_toolOther = Styles.pure(0.6)

  abstract class Tool(name: String, hint: String) {

    val button = toolbar.addButton(hint, texture("tl_" + name), () => {
      if (this != currentTool) {
        currentTool.onDeactivate()
        currentTool = this
        this.onActivate()
      }
    })

    def onSelectionChange(prev: Widget, next: Widget) = {}
    def onActivate():Unit = {
      DrawTexture.get(button).color = c_toolSelected
    }
    def onDeactivate():Unit = {
      DrawTexture.get(button).color = c_toolOther
    }
  }

  abstract class ToolWithCompanion(name: String, hint: String) extends Tool(name, hint) {
    val companion = new Widget
    val compTrans = companion.transform // Fast alias
    var selected: Widget = null

    private val outline = new Outline(new Color(1, 1, 1, 0.5))
    outline.lineWidth = 0.8f
    companion :+ outline

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
    override def onSelectionChange(prev: Widget, next: Widget) = Option(next) match {
        case Some(w) =>
          moveRect(next)
        case _ =>
          selected = null
          companion.dispose()
      }
    override def onActivate() = {
      super.onActivate()
      actionOnSelected(moveRect)
    }
    override def onDeactivate() = {
      super.onDeactivate()
      companion.dispose()
    }
  }

  var currentTool: Tool = new ToolWithCompanion("nothing", "Nothing") {}

  currentTool.onActivate()

  new ToolWithCompanion("resize", "Resize widget") {
    private var draggingLine: Widget = null
    private def dragging = draggingLine != null

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

  new ToolWithCompanion("reposition", "Move widget") {

    val minLen = 30
    val aspect = 54 / 200.0

    var dragging = false

    override def shouldUpdateWithSelected = !dragging

    private def decorateBar(w: Widget, tex: String) = {
      w :+ new DrawTexture().setTex(Styles.elemTexture(tex))
      w :+ Styles.pureTint(0.8, 1, true)
      w.listens[DragStopEvent](() => {
        updateSize(selected, (compTrans.x, compTrans.y), (compTrans.width, compTrans.height))
      })
      w.listens[DragEvent](() => dragging = true)
    }

    val xbar = new Widget
    decorateBar(xbar, "axis_x")
    xbar.listens((e: DragEvent) => {
      val gui = editor.getGui
      val nx = (gui.mouseX - e.offsetX) - companion.transform.width / 2
      companion.transform.x = nx
      companion.dirty = true
    })

    val ybar = new Widget
    decorateBar(ybar, "axis_y")
    ybar.listens((e: DragEvent) => {
      val gui = editor.getGui
      val ny = (gui.mouseY - e.offsetY) - (companion.transform.height / 2 - ybar.transform.height)
      companion.transform.y = ny
      companion.dirty = true
    })

    companion :+ xbar
    companion :+ ybar

    private def update(next: Widget) = {
      if (next != null) {
        val nlength = math.max(minLen,
          math.max(next.transform.width * next.scale * 0.5,
            next.transform.height * next.scale * 0.5))
        val nwidth = nlength * aspect
        val nhwidth = nwidth / 2
        val x0 = companion.transform.width / 2
        val y0 = companion.transform.height / 2

        xbar.transform.setSize(nlength, nwidth).setPos(x0, y0 - nhwidth)
        ybar.transform.setSize(nwidth, nlength).setPos(x0 - nhwidth, y0 - nlength)
        xbar.dirty = true
        ybar.dirty = true
      }
    }

    override def onSelectionChange(prev: Widget, next: Widget) = {
      super.onSelectionChange(prev, next)
      update(next)
    }

    override def onActivate() = {
      super.onActivate()
      update(getSelectedWidget.orNull)
    }

  }

}

