package cn.lambdalib.util.mc

import net.minecraft.entity.Entity
import net.minecraft.util.{Vec3, MovingObjectPosition}
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

trait TraceResult {
  def hasPosition: Boolean
  def position: Vec3
}

case class EmptyResult() extends TraceResult {
  override def hasPosition = false
  override def position = throw new UnsupportedOperationException()
}

case class EntityResult(target: Entity) extends TraceResult {
  import MCExtender._

  override def hasPosition = true
  override def position = target.position
}

case class BlockResult(pos: (Int, Int, Int), side: Int) extends TraceResult {
  import MCExtender._

  def getBlock(implicit world: World) = world.getBlock(pos._1, pos._2, pos._3)
  def getTileEntity(implicit world: World) = world.getTileEntity(pos._1, pos._2, pos._3)

  override def hasPosition = true
  override def position    = (pos._1 + .5, pos._2 + .5, pos._3 + .5)
}

