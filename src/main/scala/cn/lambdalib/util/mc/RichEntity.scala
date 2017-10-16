package cn.lambdalib.util.mc

import cn.lambdalib.util.generic.VecUtils
import net.minecraft.entity.Entity
import net.minecraft.util.Vec3

class RichEntity(val entity: Entity) extends AnyVal {

  def position: Vec3 = {
    if (isThePlayer)
      new Vec3(entity.posX, entity.posY - 1.6, entity.posZ)
    else new Vec3(entity.posX, entity.posY, entity.posZ)
  }
  def velocity: Vec3 = new Vec3(entity.motionX, entity.motionY, entity.motionZ)
  def lookVector: Vec3 = VecUtils.toDirVector(entity.getRotationYawHead, entity.rotationPitch)
  def headPosition: Vec3 = {
    val ret = position
    ret.addVector(0,entity.getEyeHeight,0)
    ret
  }

  def setPos(p: Vec3): Unit = {
    val y = if (isThePlayer) p.yCoord + 1.6 else p.yCoord
    entity.setPosition(p.xCoord, y, p.zCoord)
  }

  def setVel(p: Vec3): Unit = {
    entity.motionX = p.xCoord
    entity.motionY = p.yCoord
    entity.motionZ = p.zCoord
  }

  def setLook(look: EntityLook) = {
    entity.rotationYaw = look.yaw
    entity.rotationPitch = look.pitch
  }

  def setLookHead(look: EntityLook) = {
    entity.setRotationYawHead(look.yaw)
    entity.rotationPitch = look.pitch
  }

  private def isThePlayer = SideHelper.getThePlayer == entity

}
