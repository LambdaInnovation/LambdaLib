package cn.lambdalib.test

import cn.lambdalib.annoreg.core.Registrant
import cn.lambdalib.annoreg.mc.{RegInitCallback, RegEventHandler}
import cn.lambdalib.networkcall.s11n.RegSerializable.SerializeField
import cn.lambdalib.s11n.SerializeIncluded
import cn.lambdalib.util.datapart.{EntityData, RegDataPart, DataPart}
import cn.lambdalib.util.key.{KeyHandler, KeyManager}
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.TickEvent.{PlayerTickEvent, ClientTickEvent}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import org.lwjgl.input.Keyboard

// @Registrant
object BakaScala {
  @RegInitCallback
  def init() = {
    KeyManager.dynamic.addKeyHandler("233", Keyboard.KEY_J, new KeyHandler {
      override def onKeyDown() = {
        val data = EntityData.get(getPlayer).getPart(classOf[TestSyncData])
        println("Incrementer " + data.incrementer)
        data.incrementer += 1
        data.sync()
      }
    })
  }
}

// @Registrant
@RegEventHandler()
class PlayerDataTest {

  @SubscribeEvent
  def onPlayerTick(evt: PlayerTickEvent) = {
    val part = EntityData.get(evt.player).getPart(classOf[TestSyncData])
  }

}

// @Registrant
@RegDataPart(classOf[EntityPlayer])
class TestSyncData extends DataPart[EntityPlayer] {

  setTick(true)
  setNBTStorage()
  setClientNeedSync()
  setServerSyncRange(20)
  setClearOnDeath()
  println("Constructed!")

  @SerializeIncluded
  var incrementer = 0

  var ticker = 0

  override def tick() = {
    ticker += 1
    if (!isClient && ticker % 20 == 0) {
      println("Server incrementor: " + incrementer)
    }
  }

  override def toNBT(tag: NBTTagCompound) = {
    tag.setString("aaa", "aaaa")
    tag.setInteger("incr", incrementer)
    test("toNBT")
  }

  override def fromNBT(tag: NBTTagCompound) = {
    test("fromNBT " + tag)
    incrementer = tag.getInteger("incr")
  }

  private def test(msg: String) = {
    println("[TSD] " + msg + " " + (if (isClient) "Client" else "Server"))
  }

}
