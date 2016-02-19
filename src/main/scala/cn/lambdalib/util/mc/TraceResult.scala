package cn.lambdalib.util.mc

import net.minecraft.entity.Entity
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.MovingObjectPosition.MovingObjectType
import net.minecraft.world.World

object TraceResult {
  def apply(mop: MovingObjectPosition) = {
    if (mop == null || mop.typeOfHit == MovingObjectType.MISS) {
      EmptyResult()
    } else if (mop.typeOfHit == MovingObjectType.BLOCK) {
      BlockResult((mop.blockX, mop.blockY, mop.blockZ), mop.sideHit)
    } else { // Entity
      EntityResult(mop.entityHit)
    }
  }
}

trait TraceResult

case class EmptyResult() extends TraceResult

case class EntityResult(target: Entity) extends TraceResult

case class BlockResult(pos: (Int, Int, Int), side: Int) extends TraceResult {
  def getBlock(implicit world: World) = world.getBlock(pos._1, pos._2, pos._3)
  def getTileEntity(implicit world: World) = world.getTileEntity(pos._1, pos._2, pos._3)
}

