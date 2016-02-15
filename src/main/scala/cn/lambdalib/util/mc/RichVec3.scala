package cn.lambdalib.util.mc

import cn.lambdalib.util.generic.{MathUtils, VecUtils}
import net.minecraft.util.{MathHelper, Vec3}
import MathUtils._

object Vec3 {
  def apply(x: Double, y: Double, z: Double): Vec3 = net.minecraft.util.Vec3.createVectorHelper(x, y, z)
  def apply(other: Vec3): Vec3 = Vec3(other.xCoord, other.yCoord, other.zCoord)
  def lerp(a: Vec3, b: Vec3, t: Double) = VecUtils.lerp(a, b, t)
}

class EntityLook(val yaw: Float, val pitch: Float) {

  def toVec3 = {
    val yawRad = toRadians(yaw)
    val pitchRad = toRadians(pitch)
    Vec3(
      -MathHelper.sin(yawRad) * MathHelper.cos(pitchRad),
      MathHelper.cos(yawRad) * MathHelper.cos(pitchRad),
      -MathHelper.sin(pitchRad))
  }

}

class RichVec3(val self: Vec3) extends AnyVal {
  implicit def cvt(v: Vec3): RichVec3 = new RichVec3(v)

  def set(x: Double, y: Double, z: Double) = {
    self.xCoord = x
    self.yCoord = y
    self.zCoord = z
  }
  def set(vec: Vec3) = {
    self.xCoord = vec.xCoord
    self.yCoord = vec.yCoord
    self.zCoord = vec.zCoord
  }

  def x = self.xCoord
  def y = self.yCoord
  def z = self.zCoord

  def +(other: Vec3) = Vec3(self.xCoord + other.xCoord, self.yCoord + other.yCoord, self.zCoord + other.zCoord)
  def unary_-() = Vec3(-self.xCoord, -self.yCoord, -self.zCoord)
  def -(other: Vec3) = self + (-other)
  def *(other: Vec3): Vec3 = Vec3(self.xCoord * other.xCoord, self.yCoord * other.yCoord, self.zCoord * other.zCoord)

  def *(s: Double): Vec3 = Vec3(self.xCoord * s, self.yCoord * s, self.zCoord * s)

  def +=(other: Vec3) = {
    self.xCoord += other.xCoord
    self.yCoord += other.yCoord
    self.zCoord += other.zCoord
    self
  }
  def -=(other: Vec3) = {
    self.xCoord -= other.xCoord
    self.yCoord -= other.yCoord
    self.zCoord -= other.zCoord
    self
  }
  def *=(other: Vec3): Vec3 = {
    self.xCoord *= other.xCoord
    self.yCoord *= other.yCoord
    self.zCoord *= other.zCoord
    self
  }
  def *=(s: Double): Vec3 = {
    self.xCoord *= s
    self.yCoord *= s
    self.zCoord *= s
    self
  }

  def toLook: EntityLook = new EntityLook(
    -toDegrees(Math.atan2(self.xCoord, self.zCoord)).toFloat,
    -toDegrees(Math.atan2(self.yCoord, Math.sqrt(self.xCoord * self.xCoord + self.zCoord * self.zCoord))).toFloat)

}
