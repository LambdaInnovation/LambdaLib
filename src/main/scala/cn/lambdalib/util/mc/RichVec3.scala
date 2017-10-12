package cn.lambdalib.util.mc

import cn.lambdalib.util.generic.MathUtils
import net.minecraft.util.{MathHelper, Vec3}
import MathUtils._

class EntityLook(val yaw: Float, val pitch: Float) {

  def toVec3 = {
    val yawRad = toRadians(yaw)
    val pitchRad = toRadians(pitch)
    new Vec3(
      -MathHelper.sin(yawRad) * MathHelper.cos(pitchRad),
      -MathHelper.sin(pitchRad),
      MathHelper.cos(yawRad) * MathHelper.cos(pitchRad))
  }

}

class RichVec3(var self: Vec3) extends AnyRef {
  private implicit def cvt(v: Vec3): RichVec3 = new RichVec3(v)

  def set(x: Double, y: Double, z: Double) = {
    self=new Vec3(x,y,z)
  }
  def set(vec: Vec3) = {
    self=vec.copy()
  }

  @inline def x: Double = self.xCoord
  @inline def y: Double = self.yCoord
  @inline def z: Double = self.zCoord

  def +(other: Vec3) = new Vec3(self.xCoord + other.xCoord, self.yCoord + other.yCoord, self.zCoord + other.zCoord)
  def unary_-() = new Vec3(-self.xCoord, -self.yCoord, -self.zCoord)
  def -(other: Vec3): Vec3 = self + (-other)
  def *(other: Vec3): Vec3 = new Vec3(self.xCoord * other.xCoord, self.yCoord * other.yCoord, self.zCoord * other.zCoord)

  def *(s: Double): Vec3 = new Vec3(self.xCoord * s, self.yCoord * s, self.zCoord * s)

  def +=(other: Vec3): Vec3 = {
    self = self.add(other)
    self
  }
  def -=(other: Vec3): Vec3 = {
    self=self.subtract(other)
    self
  }
  def *=(s: Double): Vec3 = {
    self=new Vec3(self.xCoord * s, self.yCoord * s, self.zCoord * s)
    self
  }

  /**
    * @return The yaw-pitch representation of this vector.
    */
  def toLook: EntityLook = new EntityLook(
    -toDegrees(Math.atan2(self.xCoord, self.zCoord)).toFloat,
    -toDegrees(Math.atan2(self.yCoord, Math.sqrt(self.xCoord * self.xCoord + self.zCoord * self.zCoord))).toFloat)

  def copy(): Vec3 = self.copy()

}
