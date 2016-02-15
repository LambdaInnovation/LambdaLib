package cn.lambdalib.util.mc

import net.minecraft.entity.Entity
import net.minecraft.util.{Vec3, MovingObjectPosition}
import net.minecraft.util.MovingObjectPosition.MovingObjectType

object MCExtender {
  implicit def toTraceResult(mop: MovingObjectPosition): TraceResult = {
    if (mop == null || mop.typeOfHit == MovingObjectType.MISS) {
      EmptyResult()
    } else if (mop.typeOfHit == MovingObjectType.BLOCK) {
      BlockResult((mop.blockX, mop.blockY, mop.blockZ), mop.sideHit)
    } else { // Entity
      EntityResult(mop.entityHit)
    }
  }

  implicit def toRichEntity(entity: Entity): RichEntity = new RichEntity(entity)

  implicit def toRichVec3(vec3: Vec3): RichVec3 = new RichVec3(vec3)

  implicit def Tuple2Vec3(tuple: (Double, Double, Double)): Vec3 = Vec3(tuple._1, tuple._2, tuple._3)
}
