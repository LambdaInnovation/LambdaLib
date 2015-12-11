package cn.lambdalib.vis.editor

import cn.lambdalib.core.LambdaLib

object VisConfig {

  val KEY = "working_dirs"

  def getWorkDirs = LambdaLib.getConfig.getStringList(KEY, "vis", Array[String](), "Working directories")
  def updateWorkDirs(seq: Seq[String]) = LambdaLib.getConfig.get("vis", KEY, Array[String]()).set(seq.toArray)

}