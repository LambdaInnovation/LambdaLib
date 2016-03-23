/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.client.font

import java.awt.{GraphicsEnvironment, Color, RenderingHints, Font}
import java.awt.image.{DataBufferByte, DataBufferInt, BufferedImage}
import java.nio.{ByteOrder, ByteBuffer}
import java.util

import cn.lambdalib.util.client.font.IFont.FontOption
import net.minecraft.client.renderer.Tessellator
import net.minecraft.util.MathHelper
import org.lwjgl.opengl.{GL30, GL11}
import org.lwjgl.opengl.GL11._

/**
  * This class wraps a java AWT font and make it gl-drawable. It generates font texture procedurally
  *  so that any unicode character can be supported.
  *  @param font: The underlying AWT font
  */
class TrueTypeFont(val font: Font) extends IFont {

  import TrueTypeFont._

  class CachedChar(val ch: Int, val width: Int, val index: Int, val u: Float, val v: Float)

  val TEXTURE_SZ_LIMIT = Math.min(2048, GL11.glGetInteger(GL_MAX_TEXTURE_SIZE))
  val charSize = (font.getSize * 1.4).toInt

  private val maxPerCol = MathHelper.floor_float(TEXTURE_SZ_LIMIT / charSize.toFloat)
  private val maxStep = maxPerCol * maxPerCol

  private val generated = new util.ArrayList[Int]
  private val dirty = new util.BitSet()
  private val lookup = new util.HashMap[Int, CachedChar]

  private def currentTexture = generated.get(generated.size - 1)
  private var step = 0

  val texStep = 1.0 / maxPerCol

  newTexture()

  /**
    * Resolve the font used to draw with given code point.
    */
  protected def resolve(codePoint: Int) = font

  override def draw(str: String, px: Double, y: Double, option: FontOption) = {
    val len = getTextWidth(str, option) // Which will call updateCache()

    (0 until dirty.size()).filter(dirty.get).foreach(index => {
      glBindTexture(GL_TEXTURE_2D, generated.get(index))
      GL30.glGenerateMipmap(GL_TEXTURE_2D)
    })
    dirty.clear()

    var x = px
    val t = Tessellator.instance
    val sz = option.fontSize
    val scale = option.fontSize / charSize

    option.color.bind()
    x = px - len * option.align.lenOffset

    val preEnabled = glIsEnabled(GL_ALPHA_TEST)
    val preFunc = glGetInteger(GL_ALPHA_TEST_FUNC)
    val preRef = glGetFloat(GL_ALPHA_TEST_REF)
    glDisable(GL_ALPHA_TEST)
    // glAlphaFunc(GL_GEQUAL, 0.1f)
    glEnable(GL_TEXTURE_2D)
    // TODO group by texture to reduce draw calls?
    for(i <- codePoints(str)) yield {
      val info = lookup.get(i)
      val u = info.u
      val v = info.v
      glBindTexture(GL_TEXTURE_2D, generated.get(info.index))
      t.startDrawingQuads()
      t.addVertexWithUV(x,      y,      0, u,           v          )
      t.addVertexWithUV(x,      y + sz, 0, u,           v + texStep)
      t.addVertexWithUV(x + sz, y + sz, 0, u + texStep, v + texStep)
      t.addVertexWithUV(x + sz, y,      0, u + texStep, v          )
      t.draw()

      x += info.width * scale
    }
    if (preEnabled) {
      glEnable(GL_ALPHA_TEST)
    }
    glAlphaFunc(preFunc, preRef)
  }

  override def getCharWidth(chr: Int, option: FontOption): Double = {
    if(!lookup.containsKey(chr)) {
      writeImage(chr)
    }
    lookup.get(chr).width * option.fontSize / charSize
  }

  override def getTextWidth(str: String, option: FontOption): Double = {
    updateCache(str)
    codePoints(str).map(lookup.get(_).width).sum * option.fontSize / charSize
  }

  private def newTexture() = {
    val texture = glGenTextures()

    glBindTexture(GL_TEXTURE_2D, texture)
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, TEXTURE_SZ_LIMIT, TEXTURE_SZ_LIMIT, 0, GL_RGBA, GL_FLOAT,
      null.asInstanceOf[ByteBuffer])

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR_MIPMAP_LINEAR)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP)
    glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE)

    glBindTexture(GL_TEXTURE_2D, 0)

    generated.add(texture)
    step = 0
  }

  // Update the cached images to contain the given new characters.
  private def updateCache(str: String) = {
    val newchars = codePoints(str).toSet.filterNot(lookup.containsKey(_))
    newchars.foreach(writeImage)
  }

  // Draw the image into the cached textures at current step position and increment the step by 1.
  private def writeImage(ch: Int) = {
    // Create an image holding the character
    val image = new BufferedImage(charSize, charSize, BufferedImage.TYPE_INT_ARGB)
    val curtex = currentTexture

    val g = image.createGraphics()
    val drawFont = resolve(ch)

    g.setFont(drawFont)
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

    val metrics = g.getFontMetrics
    val width = metrics.charWidth(ch)
    // Draw to the image
    g.setBackground(BACKGRND_COLOR)
    g.clearRect(0, 0, charSize, charSize)
    g.setColor(Color.WHITE)

    g.drawString(new java.lang.StringBuilder(2).appendCodePoint(ch).toString, 3, 1 + metrics.getAscent)

    // Convert awt image to byte buffer
    // Original algorithm credits:
    /*
      * author James Chambers (Jimmy) <br>
      * author Jeremy Adams (elias4444) <br>
      * author Kevin Glass (kevglass) <br>
      * author Peter Korzuszek (genail) <br>
      */

    var byteBuffer: ByteBuffer = null
    val db = image.getData().getDataBuffer
    val bpp = image.getColorModel.getPixelSize.toByte
    if (db.isInstanceOf[DataBufferInt]) {
      val intI = image.getData().getDataBuffer.asInstanceOf[DataBufferInt].getData()
      val newI = new Array[Byte](intI.length * 4)
      for(i <- 0 until intI.length) yield {
        val b = intToByteArray(intI(i))
        val newIndex = i*4

        newI.update(newIndex+0, b(1))
        newI.update(newIndex+1, b(2))
        newI.update(newIndex+2, b(3))
        newI.update(newIndex+3, b(0))
      }

      byteBuffer = ByteBuffer.allocateDirect(
        charSize*charSize*(bpp/8))
        .order(ByteOrder.nativeOrder())
        .put(newI)
    } else {
      byteBuffer = ByteBuffer.allocateDirect(
        charSize*charSize*(bpp/8))
        .order(ByteOrder.nativeOrder())
        .put(image.getData().getDataBuffer.asInstanceOf[DataBufferByte].getData())
    }
    byteBuffer.flip()

    // write the image to texture
    val rasterX = (step % maxPerCol) * charSize
    val rasterY = (step / maxPerCol) * charSize

    glBindTexture(GL_TEXTURE_2D, curtex)
    glTexSubImage2D(GL_TEXTURE_2D, 0, rasterX, rasterY, charSize, charSize, GL_RGBA, GL_UNSIGNED_BYTE, byteBuffer)

    lookup.put(ch, new CachedChar(ch, width, generated.size() - 1, rasterX.toFloat / TEXTURE_SZ_LIMIT,
      rasterY.toFloat / TEXTURE_SZ_LIMIT))

    step += 1
    if (step == maxStep) {
      step = 0
      newTexture()
    }

    dirty.set(generated.size - 1)

    g.dispose()
  }

  private def codePoints(str: String) = (0 until str.length).map(str.codePointAt)

  private def intToByteArray(value: Int) = {
    val ret = new Array[Byte](4)
    ret.update(0, (value >>> 24).toByte)
    ret.update(1, (value >>> 16).toByte)
    ret.update(2, (value >>> 8).toByte)
    ret.update(3, value.toByte)
    ret
  }
}

object TrueTypeFont {

  val defaultFont = withFallback(Font.PLAIN, 32,
    "Microsoft YaHei", "Adobe Heiti Std R", "STHeiti",
    "SimHei", "微软雅黑", "黑体",
    "Consolas", "Monospace", "Arial")

  def withFallback2(style: Int, size: Int, fallbackNames: Array[String]) =
    withFallback(style, size, fallbackNames.toSeq: _*)

  def withFallback(style: Int, size: Int, fallbackNames: String*) = {
    val allfonts = GraphicsEnvironment.getLocalGraphicsEnvironment.getAllFonts
    val used = fallbackNames.filter(n => allfonts.exists(_.getName.equalsIgnoreCase(n))).take(1)
    if (used.isEmpty) TrueTypeFont(new Font(null, style, size))
    else TrueTypeFont(new Font(used.head, style, size))
  }

  def apply(font: Font) = new TrueTypeFont(font)

  private lazy val BACKGRND_COLOR = new Color(255, 255, 255, 0)
}