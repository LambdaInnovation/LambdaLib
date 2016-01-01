package cn.lambdalib.test

import java.awt.Font

import cn.lambdalib.annoreg.core.Registrant
import cn.lambdalib.util.client.HudUtils
import cn.lambdalib.util.client.auxgui.AuxGui
import cn.lambdalib.util.client.auxgui.AuxGuiRegistry.RegAuxGui
import cn.lambdalib.util.client.font.TrueTypeFont
import cn.lambdalib.util.markdown.{GLMarkdownRenderer, MarkdownParser}
import net.minecraft.client.gui.ScaledResolution

@Registrant
@RegAuxGui
class MarkdownRendererTest extends AuxGui {

  val markdown =
    """
      |#12341234
      |
      |# 234444
      |
      |This is a test of __markdown__ formatting.
      |
      |Markdown is a awesome thing yet has **so*me** serious limitations.
      |Test of some contents in same line
      |
      |1999年，他以《杯中窥人》一文获得首届全国新概念作文比赛一等奖[1]  。2000年，在上高一的韩寒退学，后出版首部长篇小说《三重门》[2]  。2001年，他出版文集《零下一度》，该书获得当年全国图书畅销排行榜第一名。2002年，他出版小说《像少年啦飞驰》。2003开始职业赛车生涯。2004年3月，他出版文集《韩寒五年》。2005年开通博客，开始博客写作[3]  。2006年9月，发行个人首张唱片书《寒·十八禁》[4]  。2009年，他主编《独唱团》。2010年，韩寒登上美国《时代周刊》封面[5]  。2011年，他出版杂文集《青春》。2012年6月，韩寒发布了APP阅读应用“ONE·一个”[6]  。2013年，他出版《一个：很高兴见到你》。2014年7月导演的《后会无期》在中国内地上映[7]  。2015年担任青春电影《沙漏》的监制。
      |
      |Test of cross line
      |
      |* List elem
      |* List elem 2
      |* List elem 3
      |
      |> Reference text
      |> dafsdfasdf
      |
      |> asa2dfdfdf
    """.stripMargin

  val font = TrueTypeFont.defaultFont
  val fontBold = new TrueTypeFont(font.font.deriveFont(Font.BOLD))
  val fontItalic = new TrueTypeFont(font.font.deriveFont(Font.ITALIC))

  val renderer = new GLMarkdownRenderer
  renderer.boldFont = fontBold
  renderer.italicFont = fontItalic
  renderer.fontSize = 8
  renderer.widthLimit = 180

  MarkdownParser.accept(markdown, renderer)

  override def draw(sr : ScaledResolution) = {
    renderer.render()
    HudUtils.colorRect(180, 0, 2, 200)
  }

  override def isForeground = false

}
