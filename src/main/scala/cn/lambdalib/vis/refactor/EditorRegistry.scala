package cn.lambdalib.vis.refactor

trait VisPlugin {

  def onActivate(editor: Editor)

}

object EditorRegistry {

  private var editors = Map[String, VisPlugin]()

  def register(id: String, plugin: VisPlugin) = {
    assert(!editors.contains(id))
    editors = editors updated (id, plugin)
  }

  def getEditors = editors

  register("CGUI", CGUIPlugin)

}
