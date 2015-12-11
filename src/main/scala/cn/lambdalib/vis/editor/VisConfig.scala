package cn.lambdalib.vis.editor

import cn.lambdalib.core.LambdaLib

object VisConfig {

  val K_WORKDIR = "working_dirs"
  val K_CURDIR = "current_working_dir"

  lazy val config = LambdaLib.getConfig

  def getWorkDirs = config.getStringList(K_WORKDIR, "vis", Array[String](), "Working directories")
  def updateWorkDirs(seq: Seq[String]) = config.get("vis", K_WORKDIR, Array[String]()).set(seq.toArray)

  def getCurrentDir: Option[String] = Option(config.get("vis", K_CURDIR, null.asInstanceOf[String])) match {
    case Some(p) => Some(p.getString)
    case None => None
  }
  def setCurrentDir(dir: String) = config.get("vis", K_CURDIR, dir).set(dir)

}