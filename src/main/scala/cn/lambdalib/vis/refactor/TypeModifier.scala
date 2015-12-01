package cn.lambdalib.vis.refactor

import java.lang.reflect.Field

import cn.lambdalib.cgui.ScalaExtensions.SWidget
import cn.lambdalib.cgui.gui.component.TextBox.{ConfirmInputEvent, ChangeContentEvent}
import cn.lambdalib.cgui.gui.component.Transform.HeightAlign
import cn.lambdalib.cgui.gui.component.{Tint, TextBox, DrawTexture}
import cn.lambdalib.cgui.gui.event.{GuiEvent, LeftClickEvent, LostFocusEvent}
import cn.lambdalib.core.LambdaLib
import cn.lambdalib.util.client.font.IFont.FontOption

import Styles._

abstract class EditBox extends SWidget {
  protected val drawer: DrawTexture = new DrawTexture
  drawer.texture = null
  drawer.color = pure(0.2)
  addComponent(drawer)

  protected val text: TextBox = new TextBox(new FontOption(9))
  text.allowEdit = true
  addComponent(text)

  transform.setSize(35, 10)

  listen(classOf[LostFocusEvent], (w, e: LostFocusEvent) => {
    val parent = w.getWidgetParent
    if (parent != null)
      parent.post(e)
  })

  listen(classOf[ChangeContentEvent], (w, e: ChangeContentEvent) => {
    drawer.color = cModified
  })

  listen(classOf[TextBox.ConfirmInputEvent], (w, e: ConfirmInputEvent) => {
    try {
      setValue(text.content)
      drawer.color = pure(0.3)
      updateRepr();
    } catch {
      case e: NumberFormatException =>
        drawer.color = cErrored
      case e: Exception =>
        drawer.color = cErrored
        LambdaLib.log.error("ModifierBase.confirmInput()", e)
    }
  })

  override def onAdded() = {
    super.onAdded()
    updateRepr()
  }

  private def updateRepr() = {
    try {
      text.setContent(repr)
    } catch {
      case e: Exception =>
        LambdaLib.log.error("ModifierBase.onAdded()", e)
        text.setContent("<error>")
    }
  }

  @throws(classOf[Exception])
  protected def repr: String

  @throws(classOf[Exception])
  protected def setValue(content: String)
}

abstract class ModifierBase(val field: Field, val instance: AnyRef) extends EditBox {

  field.setAccessible(true)

  @throws(classOf[Exception])
  protected def repr: String = field.get(instance).toString

  @throws(classOf[Exception])
  protected def setValue(content: String)

}

class IntModifier(field: Field, instance: AnyRef) extends ModifierBase(field, instance) {
  override def setValue(content: String) = field.set(instance, Integer.valueOf(content))
}

class RealModifier(field: Field, instance: AnyRef) extends ModifierBase(field, instance) {
  val isFloat = field.getType == java.lang.Float.TYPE || field.getType == classOf[java.lang.Float] ||
    field.getType == classOf[Float]

  override def setValue(content: String) = field.set(instance,
    if(isFloat) content.toFloat else content.toDouble)
}

class StringModifier(field: Field, instance: AnyRef) extends ModifierBase(field, instance) {
  override def setValue(content: String) = field.set(instance, content)
}

class EnumModifier(field: Field, instance: AnyRef) extends SWidget {
  transform.setSize(35, 10)

  val enumType = field.getType
  require(enumType.isEnum)

  val constants = enumType.getEnumConstants.toList

  val tint = new Tint
  tint.idleColor = pure(0.2)
  tint.hoverColor = pure(0.5)
  this :+ tint

  val text = new TextBox(new FontOption(10, pure(0.8)))
  text.heightAlign = HeightAlign.CENTER
  text.setContent(field.get(instance).toString)
  this :+ text

  this.listen(classOf[LeftClickEvent], (w, e: LeftClickEvent) => {
    // Show the hover list
    val menu = new SubMenu
    val current = field.get(instance)
    constants filter (_ != current) foreach (elem => {
      menu.addItem(elem.toString, () => {
        field.set(instance, elem)
        text.setContent(elem.toString)
      })
    })

    menu.transform.setPos(0, transform.height)
    this :+ menu
  })

}