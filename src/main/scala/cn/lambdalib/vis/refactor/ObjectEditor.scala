package cn.lambdalib.vis.refactor

import java.lang.reflect.{Modifier, Field}

import cn.lambdalib.cgui.ScalaExtensions.SWidget
import cn.lambdalib.cgui.gui.component.{DrawTexture, ElementList}
import cn.lambdalib.cgui.gui.component.Transform.HeightAlign
import cn.lambdalib.core.LambdaLib
import cn.lambdalib.vis.editor.VisProperty

class ObjectEditor(val obj: AnyRef) extends HierarchyTab(100, 100, 80, 100) {

  val width = 80
  val klass = obj.getClass

  val editpanel = new SWidget(80, 0, transform.width, transform.height)
  editpanel :+ new DrawTexture().setTex(null).setColor4d(.08, .08, .08, 1)
  body.addWidgetBefore("EditPanel", editpanel, body.getDrawList.get(0))

  private def nameOf(field: Field) = {
    val anno = field.getAnnotation(classOf[VisProperty])
    if(anno == null || anno.name().equals("")) field.getName else anno.name()
  }

  override def requireSelection = false

  private def objElements(obj: AnyRef) = {
    def klass = obj.getClass
    klass.getFields filter (f => {
      val anno = f.getAnnotation(classOf[VisProperty])
      (anno == null || !anno.exclude()) && (f.getModifiers & (Modifier.FINAL | Modifier.STATIC)) == 0
    }) map (f => new FieldElement(f, obj))
  }

  private class FieldElement(val field: Field, val instance: AnyRef)
    extends Element(nameOf(field), null) {
    val editWidth = 60

    val klass = field.getType
    val supported = TypeModifier.isSupported(klass)

    if(supported) {
      val modifier = TypeModifier.create(field, instance)
      modifier.transform.alignHeight = HeightAlign.CENTER
      modifier.transform.x = width + 3
      this :+ modifier
    } else {
      initFoldButton()
    }

    private var subelem : List[Element] = null

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

  objElements(obj) foreach :+

}

object ObjectEditor {

  private var cached = Map[Class[_], ObjectEditor]()

  def apply(obj: AnyRef): ObjectEditor = {
    if(!(cached contains obj.getClass)) {
      cached = cached updated (obj.getClass, new ObjectEditor(obj))
    }
    cached(obj.getClass)
  }

}