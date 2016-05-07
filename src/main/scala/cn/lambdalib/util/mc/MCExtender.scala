package cn.lambdalib.util.mc

import net.minecraft.entity.Entity
import net.minecraft.util.{Vec3, MovingObjectPosition}
import net.minecraft.util.MovingObjectPosition.MovingObjectType
import net.minecraftforge.common.util.ForgeDirection

object MCExtender {
  implicit def toTraceResult(mop: MovingObjectPosition): TraceResult = TraceResult(mop)

  implicit def toRichEntity(entity: Entity): RichEntity = new RichEntity(entity)

  implicit def toRichVec3(vec3: Vec3): RichVec3 = new RichVec3(vec3)

  implicit def Tuple2Vec3(tuple: (Double, Double, Double)): Vec3 = Vec3(tuple._1, tuple._2, tuple._3)

  def sideToOrientation(dir: ForgeDirection): EntityLook = dir match {
    case ForgeDirection.DOWN => new EntityLook(0, 90)
    case ForgeDirection.UP   => new EntityLook(0, -90)
    case ForgeDirection.EAST => new EntityLook(90, 0)
    case ForgeDirection.WEST => new EntityLook(-90, 0)
    case ForgeDirection.SOUTH => new EntityLook(0, 0)
    case ForgeDirection.NORTH => new EntityLook(180, 0)
  }
}
