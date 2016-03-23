/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.vis.editor

import java.lang.reflect.Field

import cn.lambdalib.cgui.ScalaCGUI
import cn.lambdalib.cgui.gui.Widget
import cn.lambdalib.cgui.gui.component.TextBox.{ChangeContentEvent, ConfirmInputEvent}
import cn.lambdalib.cgui.gui.component.Transform.HeightAlign
import cn.lambdalib.cgui.gui.component.{DrawTexture, TextBox, Tint}
import cn.lambdalib.cgui.gui.event.{GuiEvent, LeftClickEvent, LostFocusEvent}
import cn.lambdalib.core.LambdaLib
import cn.lambdalib.util.client.font.IFont.FontOption
import ScalaCGUI._
import Styles._
import cn.lambdalib.util.client.font.{Fonts, IFont}
import net.minecraft.util.ResourceLocation

object TypeModifier {

  type ClassPred = Class[_] => Boolean
  type Creator = (Field, AnyRef) => Widget

  private var supports: List[(ClassPred, Creator)] = List()

  def addSupportPred(creator: Creator, classPred: ClassPred) = {
    supports = supports :+ (classPred, creator)
  }

  def addSupport(creator: Creator, classes: Class[_]*) = {
    addSupportPred(creator, c => classes exists (c2 => c2.isAssignableFrom(c)))
  }

  def isSupported(c: Class[_]) = supports.exists(_._1(c))

  def create(f: Field, instance: AnyRef) = supports.filter(_._1(f.getType)).head._2(f, instance)

  addSupport(new IntModifier(_, _), classOf[Int], classOf[Integer])
  addSupport(new RealModifier(_, _), classOf[Float], classOf[java.lang.Float],
    classOf[Double], classOf[java.lang.Double])
  addSupport(new StringModifier(_, _), classOf[String])
  addSupportPred(EnumModifier(_, _), _.isEnum)
  addSupport(new BooleanModifier(_, _), classOf[Boolean], classOf[java.lang.Boolean])
  addSupport(new ResLocModifier(_, _), classOf[ResourceLocation])
  addSupport(FontModifier(_, _), classOf[IFont])

}

/**
  * Fired inside any Modifier Widget whenever its editing target was modified by it.
  */
class EditEvent extends GuiEvent

trait IModifier {
  def updateRepr(): Unit
}

abstract class EditBox extends Widget with IModifier {

  protected def backColor = pure(0.2)

  private var editing = false

  protected val drawer: DrawTexture = new DrawTexture
  drawer.texture = null
  drawer.color = backColor
  addComponent(drawer)

  protected val text: TextBox = new TextBox(new FontOption(9))
  text.allowEdit = true
  addComponent(text)

  transform.setSize(35, 10)

  this.listens[ChangeContentEvent](() => {
    drawer.color = Styles.cModified
    editing = true
  })

  this.listens[ConfirmInputEvent](() => {
    try {
      setValue(text.content)
      drawer.color = backColor
      updateRepr()
      this.post(new EditEvent)
    } catch {
      case e: Exception =>
        drawer.color = cErrored
        LambdaLib.log.error("ModifierBase.confirmInput()", e)
    }
    editing = false
  })

  override def onAdded() = {
    super.onAdded()
    updateRepr()
  }

  final def updateRepr() = {
    try {
      text.setContent(repr)
    } catch {
      case e: Exception =>
        LambdaLib.log.error("ModifierBase.onAdded()", e)
        text.setContent("<error>")
    }
  }

  def isEditing = editing

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

class BooleanModifier(field: Field, instance: AnyRef) extends Widget with IModifier {

  transform.setSize(18, 6.3)

  val tex_on = Styles.texture("buttons/checkbox_on")
  val tex_off = Styles.texture("buttons/checkbox_off")

  val dt = new DrawTexture
  this :+ dt

  this :+ pureTint(0.8, 1, true)

  updateTexture()

  this.listens[LeftClickEvent](() => {
    field.set(instance, !field.getBoolean(instance))
    updateTexture()
  })

  private def updateTexture() = {
    dt.setTex(if(field.getBoolean(instance)) tex_on else tex_off)
  }

  override def updateRepr() = updateTexture()

}

private object EnumModifier {

  def apply(field: Field, instance: AnyRef): ListModifier[Enum[_]] = {
    new ListModifier[Enum[_]](field, instance).setValues(field.getType.getEnumConstants.asInstanceOf[Array[Enum[_]]])
  }

}

private object FontModifier {
  import scala.collection.JavaConversions._

  def apply(field: Field, instance: AnyRef): ListModifier[IFont] = {
    new ListModifier[IFont](field, instance) {
      setValues(Fonts.getFonts.toList)

      override def repr(font: IFont) = Fonts.getName(font)
    }
  }

}

private class ListModifier[T](field: Field, instance: AnyRef) extends Widget with IModifier {

  class EditEvent(val value: T) extends GuiEvent

  transform.setSize(35, 10)

  val tint = new Tint
  tint.idleColor = pure(0.2)
  tint.hoverColor = pure(0.5)
  this :+ tint

  val text = new TextBox(new FontOption(10, pure(0.8)))
  text.heightAlign = HeightAlign.CENTER
  text.setContent(field.get(instance).toString)
  this :+ text

  private var values: Seq[T] = Nil

  this.listens((e: LeftClickEvent) => {
    // Show the hover list
    val menu = new SubMenu
    val current = currentValue

    values filter (_ != current) foreach (elem => {
      menu.addItem(repr(elem), () => {
        this.post(new EditEvent(elem))
        text.setContent(repr(elem))
        menu.dispose()
      })
    })

    menu.transform.setPos(x, y + transform.height * scale)
    val gui = getGui
    gui.addWidget(menu)
  })

  this.listens((e: EditEvent) => {
    field.set(instance, e.value)
  })

  def setValues(e: Seq[T]) = {
    values = e
    this
  }

  def repr(value: T) = value.toString

  def currentValue: T = field.get(instance).asInstanceOf[T]

  override def updateRepr() = text.setContent(repr(currentValue))

}

class ResLocModifier(field: Field, instance: AnyRef) extends ModifierBase(field, instance) {
  val NULL_TAG = "<null>"

  override def repr: String = Option(field.get(instance).asInstanceOf[ResourceLocation]) match {
    case Some(r: ResourceLocation) => r.toString
    case _ => NULL_TAG
  }

  protected override def setValue(content: String) = field.set(instance,
    if(content == NULL_TAG) null else new ResourceLocation(content))
}