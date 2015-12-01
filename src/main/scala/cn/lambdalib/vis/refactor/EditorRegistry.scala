package cn.lambdalib.vis.refactor

import java.util

trait VisPlugin {



}

object EditorRegistry {

  private val editors = new util.HashMap[String, VisPlugin]

  def register(id: String, plugin: VisPlugin) = {
    assert(!editors.containsKey(id))
    editors.put(id, plugin)
  }

}
