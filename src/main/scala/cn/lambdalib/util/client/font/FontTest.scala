package cn.lambdalib.util.client.font

import java.awt.Font

import cn.lambdalib.annoreg.core.Registrant
import cn.lambdalib.util.client.auxgui.AuxGui
import cn.lambdalib.util.client.auxgui.AuxGuiRegistry.RegAuxGui
import cn.lambdalib.util.client.font.IFont.FontOption
import net.minecraft.client.gui.ScaledResolution

@Registrant
@RegAuxGui
class FontTest extends AuxGui {

  val font = new TrueTypeFont(new Font("微软雅黑", Font.PLAIN, 32), 40)

  val option = new FontOption(15)

  println("FontTest constructed")

  override def isForeground = false

  override def draw(res: ScaledResolution) = {
    // System.out.println("Drawing...")
    // font.draw("全てはシュタインズゲートの选択だ！！", 0, 0, option)

    option.fontSize = 12
    font.draw("AcademyCraft developer info", 130, 10, option)
    option.fontSize = 10
    font.draw("Ability not acquired", 130, 22, option)
    option.fontSize = 8
    font.draw("[BACKSPACE] Skill info", 130, 33, option)
  }

}
