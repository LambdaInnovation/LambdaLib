/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.vis.editor

import net.minecraft.client.Minecraft

abstract class VisPlugin(editor: Editor) {

  def onActivate() = {}

  def onDeactivate(quitEditor: Boolean) = if (quitEditor) {
    Minecraft.getMinecraft.displayGuiScreen(null)
  }

}

object EditorRegistry {

  private var editors = Map[String, Editor => VisPlugin]()

  def register(id: String, pluginFactory: Editor => VisPlugin) = {
    assert(!editors.contains(id))
    editors = editors updated (id, pluginFactory)
  }

  def getEditors = editors

  register("CGUI", new CGUIEditor(_))

}
