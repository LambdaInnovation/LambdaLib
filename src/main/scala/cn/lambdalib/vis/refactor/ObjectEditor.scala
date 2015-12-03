package cn.lambdalib.vis.refactor

import java.lang.reflect.{Modifier, Field}

import cn.lambdalib.cgui.gui.Widget
import cn.lambdalib.cgui.gui.component.{DrawTexture, ElementList}
import cn.lambdalib.cgui.gui.component.Transform.HeightAlign
import cn.lambdalib.core.LambdaLib
import cn.lambdalib.vis.editor.VisProperty

import cn.lambdalib.cgui.ScalaExtensions._
import cn.lambdalib.vis.model.CompTransform
import net.minecraft.util.Vec3

class ObjectPanel(bar: Boolean, x: Double, y: Double, width: Double, height: Double, name: String)
  extends HierarchyTab(bar, x, y, width, height, name) {

  val editpanel = new Widget(transform.width, 0, 60, transform.height)
  editpanel :+ new DrawTexture().setTex(null).setColor4d(.08, .08, .08, 1)
  this.addWidgetBefore("EditPanel", editpanel, null)

  // override def requireSelection = false

}

object ObjectEditor {

  private def nameOf(field: Field) = {
    val anno = field.getAnnotation(classOf[VisProperty])
    if(anno == null || anno.name().equals("")) field.getName else anno.name()
  }

  def objElements(obj: AnyRef) = {
    val klass = obj.getClass
    klass.getFields filter (f => {
      val anno = f.getAnnotation(classOf[VisProperty])
      (anno == null || !anno.exclude()) && (f.getModifiers & (Modifier.FINAL | Modifier.STATIC)) == 0
    }) map (f => new FieldElement(f, obj))
  }

  private def fieldIcon(field: Field) = {
    Styles.elemTexture(field.getType() match {
      case t if t == classOf[Int] || t == classOf[Integer] => "integer"
      case t if t == classOf[Float] || t == classOf[java.lang.Float] => "float"
      case t if t == classOf[Double] || t == classOf[java.lang.Double] => "double"
      case t if t == classOf[Vec3] => "vec3"
      case t if t == classOf[CompTransform] => "comp_transform"
      case t if t.isEnum => "enum"
      case _ => "folder"
    })
  }

  class FieldElement(val field: Field, val instance: AnyRef)
    extends Element(nameOf(field), fieldIcon(field)) {
    val editWidth = 60

    val klass = field.getType
    val supported = TypeModifier.isSupported(klass)

    private var subelem : List[Element] = null

    private var init = false

    override def onAdded() = {
      if(!init) {
        val tab = getTab
        transform.width = tab.transform.width

        init = true
        if (supported) {
          val modifier = TypeModifier.create(field, instance)
          modifier.transform.alignHeight = HeightAlign.CENTER
          modifier.transform.x = transform.width + 3
          this :+ modifier
        } else {
          initFoldButton()
        }
      }

      super.onAdded()
    }

    override def onRebuild(list: ElementList) = {
      if(!supported && !folded) {
        if(subelem == null) {
          try {
            subelem = objElements(field.get(instance)).toList
            subelem foreach (_.addedInto(this))
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