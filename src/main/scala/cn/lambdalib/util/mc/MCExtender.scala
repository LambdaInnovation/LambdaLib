package cn.lambdalib.util.mc

import net.minecraft.entity.Entity
import net.minecraft.util.{EnumFacing, MovingObjectPosition, Vec3}

object MCExtender {
  implicit def toTraceResult(mop: MovingObjectPosition): TraceResult = TraceResult(mop)

  implicit def toRichEntity(entity: Entity): RichEntity = new RichEntity(entity)

  implicit def toRichVec3(vec3: Vec3): RichVec3 = new RichVec3(vec3)

  implicit def Tuple2Vec3(tuple: (Double, Double, Double)): Vec3 = new Vec3(tuple._1, tuple._2, tuple._3)

  def sideToOrientation(dir: EnumFacing): EntityLook = dir match {
    case EnumFacing.DOWN => new EntityLook(0, 90)
    case EnumFacing.UP   => new EntityLook(0, -90)
    case EnumFacing.EAST => new EntityLook(90, 0)
    case EnumFacing.WEST => new EntityLook(-90, 0)
    case EnumFacing.SOUTH => new EntityLook(0, 0)
    case EnumFacing.NORTH => new EntityLook(180, 0)
  }
}
