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
