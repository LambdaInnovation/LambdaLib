package cn.lambdalib.vis.refactor

import java.lang.reflect.{Modifier, Field}

import cn.lambdalib.cgui.gui.Widget
import cn.lambdalib.cgui.gui.component.{TextBox, DrawTexture, ElementList}
import cn.lambdalib.cgui.gui.component.Transform.{WidthAlign, HeightAlign}
import cn.lambdalib.cgui.gui.event.GuiEvent
import cn.lambdalib.core.LambdaLib
import cn.lambdalib.util.helper.Color
import cn.lambdalib.vis.editor.VisProperty

import cn.lambdalib.cgui.ScalaExtensions._
import cn.lambdalib.vis.model.CompTransform
import cn.lambdalib.vis.refactor
import net.minecraft.util.Vec3

object ObjectEditor {
  /**
    * Fired on the editor tab whenever an property on given element was edited.
    */
  class ElementEditEvent(element: Element) extends GuiEvent

  val default = new ObjectEditor
  val specialHandlers = List[((Field, AnyRef) => Boolean, (ObjectEditor, Field, AnyRef) => Element)](
    ((f, i) => f.getType == classOf[Color], (e, f, i) => {
      val ret = e.createElement(f, i)
      val hex = new Element("hex", null)
      DrawTexture.get(hex.iconArea).setColor4d(0, 0, 0, 0)

      def color = f.get(i).asInstanceOf[Color]
      def updateIndicateColor() = {
        val c = color
        DrawTexture.get(ret.iconArea).setColor4d(c.r, c.g, c.b, c.a)
      }

      val hexbox: EditBox = new EditBox {
        override def repr: String = Integer.toHexString(color.asHexColor())
        override def setValue(content: String) = f.set(i, new Color(java.lang.Long.parseLong(content, 16).toInt))
      }

      ret.procesesElement = w => {
        w.getWidget("Modifier").listens((w, e: EditEvent) => {
          if (w != hexbox) {
            hexbox.asInstanceOf[IModifier].updateRepr()
          }
          updateIndicateColor()
        })
      }

      hexbox.listens[EditEvent](() => {
        // TODO: Update r g b a fields when hex was edited
      })

      e.setModifier(hex, hexbox)
      ret :+ hex

      println("Used me")
      ret
    })
  )
}

class ObjectEditor {
  import ObjectEditor._

  private def nameOf(field: Field) = {
    val anno = field.getAnnotation(classOf[VisProperty])
    if(anno == null || anno.name().equals("")) field.getName else anno.name()
  }

  def objElements(obj: AnyRef): Iterable[Element] = {
    val klass = obj.getClass
    klass.getFields filter (f => {
      val anno = f.getAnnotation(classOf[VisProperty])
      (anno == null || !anno.exclude()) && (f.getModifiers & (Modifier.FINAL | Modifier.STATIC)) == 0
    }) map (f => {
      specialHandlers.find(t => t._1(f, obj)) match {
        case Some((pred, handler)) => handler(this, f, obj)
        case _ => createElement(f, obj)
      }
    })
  }

  private def fieldIcon(field: Field) = {
    Styles.elemTexture(field.getType match {
      case t if t == classOf[Int] || t == classOf[Integer]               => "integer"
      case t if t == classOf[Float] || t == classOf[java.lang.Float]     => "float"
      case t if t == classOf[Double] || t == classOf[java.lang.Double]   => "double"
      case t if t == classOf[Vec3]                                       => "vec3"
      case t if t == classOf[CompTransform]                              => "comp_transform"
      case t if t.isEnum                                                 => "enum"
      case t if t == classOf[Boolean] || t == classOf[java.lang.Boolean] => "boolean"
      case t if t == classOf[Color]                                      => null
      case _                                                             => "folder"
    })
  }

  private def setModifier(target: Element, modifier: Widget) = {
    modifier.transform.alignHeight = HeightAlign.CENTER
    modifier.transform.alignWidth = WidthAlign.RIGHT
    modifier.transform.x = modifier.transform.width - 55
    modifier.listens[EditEvent](() => {
      target.getTab.post(new ElementEditEvent(target))
    })
    target :+ ("Modifier", modifier)
  }

  private def getModifier(target: Element): Option[IModifier] = {
    val ret = target.getWidget("Modifier")
    ret match {
      case r: IModifier => Some(r)
      case _ => None
    }
  }

  protected def createElement(field: Field, instance: AnyRef) = new FieldElement(field, instance)

  class FieldElement(val field: Field, val instance: AnyRef, height: Double = 10)
    extends Element(nameOf(field), fieldIcon(field), height) {
    val editWidth = 60

    val klass = field.getType
    val supported = TypeModifier.isSupported(klass)

    private var subelem : List[Element] = null

    private var init = false

    if (supported) {
      val modifier = TypeModifier.create(field, instance)
      setModifier(this, modifier)
    } else {
      initFoldButton()
    }

    override def onAdded() = {
      if(!init) {
        val tab = getTab
        transform.width = tab.transform.width
        init = true
      }

      super.onAdded()
    }

    override def onRebuild(list: ElementList) = {
      super.onRebuild(list)

      if(!supported && !folded) {
        if(subelem == null) {
          try {
            subelem = objElements(field.get(instance)).toList
            subelem foreach (e => { e.addedInto(this); procesesElement(e) })
          } catch {
            case e: Exception => LambdaLib.log.error("Error creating editor for " + field, e)
          }
        }

        if(subelem != null) {
          subelem foreach list.addWidget
        }
      }
    }

  }

  def addToHierarchy(hier: IHierarchy, obj: AnyRef) = {
    objElements(obj) foreach (hier :+ _)
  }

}