package cn.lambdalib.util.mc

import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{BlockPos, EnumFacing, MovingObjectPosition}
import net.minecraft.util.MovingObjectPosition.MovingObjectType
import net.minecraft.world.World

object TraceResult {
  def apply(mop: MovingObjectPosition) = {
    if (mop == null || mop.typeOfHit == MovingObjectType.MISS) {
      EmptyResult()
    } else if (mop.typeOfHit == MovingObjectType.BLOCK) {
      BlockResult(mop.getBlockPos, mop.sideHit)
    } else { // Entity
      EntityResult(mop.entityHit)
    }
  }
}

trait TraceResult {
  def hasPosition: Boolean
  def position: BlockPos
}

case class EmptyResult() extends TraceResult {
  override def hasPosition = false
  override def position = throw new UnsupportedOperationException()
}

case class EntityResult(target: Entity) extends TraceResult {
  import MCExtender._

  override def hasPosition = true
  override def position = target.getPosition
}

case class BlockResult(pos:BlockPos, side: EnumFacing) extends TraceResult {
  import MCExtender._

  def getBlock(implicit world: World): Block = world.getBlockState(pos).getBlock
  def getTileEntity(implicit world: World): TileEntity = world.getTileEntity(pos)

  override def hasPosition = true
  def position:BlockPos    = pos.add( .5, .5, .5)
}

