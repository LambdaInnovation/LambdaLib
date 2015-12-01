package cn.lambdalib.cgui

import cn.lambdalib.cgui.gui.Widget
import cn.lambdalib.cgui.gui.component.Component
import cn.lambdalib.cgui.gui.event.{IGuiEventHandler, GuiEvent}

/**
  * CGUI scala extensions to reduce syntax burden.
  */
object ScalaExtensions {

  class SWidget extends Widget {

    def listen[T <: GuiEvent](event: Class[T], handler: (Widget, T) => Unit): Unit = {
      listen(event, new IGuiEventHandler[T] {
        override def handleEvent(w: Widget, e: T) = {
          handler(w, e)
        }
      })
    }

    /**
      * Equivalent to addComponent(c).
      */
    def :+(c: Component): SWidget = {
      addComponent(c)
      this
    }

    /**
      * Equivalent to addWidget(w).
      */
    def :+(w: Widget): SWidget = {
      addWidget(w)
      this
    }

    /**
      * Equivalent to addWidget(name, w).
      */
    def :+(pair: (String, Widget)): SWidget = {
      addWidget(pair._1, pair._2)
      this
    }

  }

}