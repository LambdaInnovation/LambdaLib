package cn.lambdalib.cgui

import cn.lambdalib.cgui.gui.Widget
import cn.lambdalib.cgui.gui.component.Component
import cn.lambdalib.cgui.gui.event.{IGuiEventHandler, GuiEvent}

import scala.reflect.ClassTag

class SWidgetWrapper(w: Widget) {

  def listens[T <: GuiEvent](handler: (Widget, T) => Any, priority: Int = 0)(implicit tag: ClassTag[T]): Unit = {
    println("Listen " + tag.runtimeClass)
    w.listen[T](tag.runtimeClass.asInstanceOf[Class[T]], new IGuiEventHandler[T] {
      override def handleEvent(w: Widget, event: T) = {
        handler(w, event)
      }
    }, priority)
  }

  def listens[T <: GuiEvent](handler: T => Any)(implicit tag: ClassTag[T]): Unit =
    listens((_, e: T) => handler(e))

  def listens[T <: GuiEvent](handler: () => Any)(implicit tag: ClassTag[T]): Unit =
    listens((_, _: T) => handler())

  def :+(add: Widget): Widget = {
    w.addWidget(add)
    w
  }

  def :+(pair: (String, Widget)): Widget = {
    w.addWidget(pair._1, pair._2)
    w
  }

  def :+(c: Component): Widget = {
    w.addComponent(c)
    w
  }

}

/**
  * CGUI scala extensions to reduce syntax burden.
  */
object ScalaExtensions {

  implicit def toWrapper(w: Widget): SWidgetWrapper = new SWidgetWrapper(w)

}