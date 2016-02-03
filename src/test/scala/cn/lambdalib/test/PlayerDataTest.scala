package cn.lambdalib.test

import cn.lambdalib.annoreg.core.Registrant
import cn.lambdalib.annoreg.mc.RegEventHandler
import cn.lambdalib.util.datapart.{EntityData, RegDataPart, DataPart}
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.TickEvent.{PlayerTickEvent, ClientTickEvent}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound

@Registrant
@RegEventHandler()
class PlayerDataTest {

  @SubscribeEvent
  def onPlayerTick(evt: PlayerTickEvent) = {
    val part = EntityData.get(evt.player).getPart(classOf[TestSyncData])
  }

}

@Registrant
@RegDataPart(classOf[EntityPlayer])
class TestSyncData extends DataPart[EntityPlayer] {

  setTick(true)
  setNBTStorage()
  setClientNeedSync()
  setServerSyncRange(20)

  var ticker = 0

  override def tick() = {
    ticker += 1
    if (isClient && ticker % 20 == 0) {
      sync()
    }
  }

  override def toNBT(tag: NBTTagCompound) = {
    tag.setString("aaa", "aaaa")
    test("toNBT")
  }

  override def fromNBT(tag: NBTTagCompound) = {
    test("fromNBT " + tag)
  }

  override def toNBTSync(tag: NBTTagCompound) = {
    tag.setString("aaa", "bbbb")
    test("toNBTSync")
  }

  override def fromNBTSync(tag: NBTTagCompound) = {
    test("fromNBTSync " + tag)
  }

  private def test(msg: String) = {
    println("[TSD] " + msg + " " + (if (isClient) "Client" else "Server"))
  }

}
