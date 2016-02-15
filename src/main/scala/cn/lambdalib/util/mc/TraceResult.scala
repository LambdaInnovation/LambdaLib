package cn.lambdalib.util.mc

import net.minecraft.entity.Entity
import net.minecraft.world.World

trait TraceResult

case class EmptyResult() extends TraceResult

case class EntityResult(target: Entity) extends TraceResult

case class BlockResult(pos: (Int, Int, Int), side: Int) extends TraceResult {
  def getBlock(implicit world: World) = world.getBlock(pos._1, pos._2, pos._3)
  def getTileEntity(implicit world: World) = world.getTileEntity(pos._1, pos._2, pos._3)
}

