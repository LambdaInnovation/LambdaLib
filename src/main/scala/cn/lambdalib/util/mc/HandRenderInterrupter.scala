package cn.lambdalib.util.mc

import cn.lambdalib.annoreg.core.Registrant
import cn.lambdalib.annoreg.mc.RegEventHandler
import cn.lambdalib.annoreg.mc.RegEventHandler.Bus
import cn.lambdalib.util.datapart.{EntityData, RegDataPart, DataPart}
import cn.lambdalib.util.generic.RegistryUtils
import cn.lambdalib.vis.model.CompTransform
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.relauncher.Side
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.{EntityRenderer, ItemRenderer}
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.client.event.{RenderHandEvent, RenderWorldLastEvent}
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.Project

object HandRenderer {
  private val _fFarPlaneDistance = RegistryUtils.getObfField(classOf[EntityRenderer], "farPlaneDistance", "field_78530_s")
  private val _mGetFOVModifier = RegistryUtils.getMethod(classOf[EntityRenderer], "getFOVModifier", "func_78481_a",
    java.lang.Float.TYPE, java.lang.Boolean.TYPE)
  private val _mViewBobbing = RegistryUtils.getMethod(classOf[EntityRenderer], "setupViewBobbing", "func_78475_f",
    java.lang.Float.TYPE)

  def renderHand(partialTicks: Float, transform: CompTransform = CompTransform.identity) = {
    val mc = Minecraft.getMinecraft
    val renderer = mc.entityRenderer

    GL11.glMatrixMode(GL11.GL_PROJECTION)
    GL11.glLoadIdentity()

    GL11.glEnable(GL11.GL_BLEND)
    GL11.glColor4f(1, 0.2f, 0.2f, 0.6f)

    Project.gluPerspective(
      _mGetFOVModifier.invoke(renderer, partialTicks.asInstanceOf[AnyRef],
        false.asInstanceOf[AnyRef]).asInstanceOf[Float],
      mc.displayWidth.toFloat / mc.displayHeight.toFloat, 0.05F,
      _fFarPlaneDistance.get(renderer).asInstanceOf[Float] * 2.0F)

    GL11.glMatrixMode(GL11.GL_MODELVIEW)
    GL11.glLoadIdentity()
    GL11.glDisable(GL11.GL_DEPTH_TEST)

    transform.doTransform()

    if (mc.gameSettings.viewBobbing) {
      _mViewBobbing.invoke(renderer, partialTicks.asInstanceOf[AnyRef]) // setupViewBobbing(partialTicks)
    }

    renderer.itemRenderer.renderItemInFirstPerson(partialTicks)

    GL11.glEnable(GL11.GL_DEPTH_TEST)
  }
}

trait HandRenderer {
  import HandRenderer._

  def render(partialTicks: Float) = renderHand(partialTicks)
}

@Registrant
@RegEventHandler(Array(Bus.Forge))
class __HREvents {

  @SubscribeEvent
  def onRenderHand(evt: RenderHandEvent) = {
    val player = Minecraft.getMinecraft.thePlayer
    val data = HandRenderInterrupter(player)
    val mc = Minecraft.getMinecraft

    if (data.isPresent) {
      evt.setCanceled(true)

      if (mc.gameSettings.thirdPersonView == 0 &&
        !mc.renderViewEntity.isPlayerSleeping &&
        !mc.gameSettings.hideGUI &&
        !mc.playerController.enableEverythingIsScrewedUpMode) {
        mc.entityRenderer.enableLightmap(evt.partialTicks)
        data.get.render(evt.partialTicks)
        mc.entityRenderer.disableLightmap(evt.partialTicks)
      }
    }
  }
}

object HandRenderInterrupter {
  def apply(player: EntityPlayer) = EntityData.get(player).getPart(classOf[HandRenderInterrupter])
}

@Registrant
@RegDataPart(value=classOf[EntityPlayer], side=Array(Side.CLIENT))
class HandRenderInterrupter extends DataPart[EntityPlayer] {

  private var current: Option[HandRenderer] = None

  def addInterrupt(renderer: HandRenderer) = {
    current = Some(renderer)
  }

  def stopInterrupt(renderer: HandRenderer) = {
    if (Option(renderer) == current) {
      current = None
    }
  }

  def isPresent = current.isDefined
  def get = current.get

}
