/**
 * Copyright (c) Lambda Innovation, 2013-2015
 * 本作品版权由Lambda Innovation所有。
 * http://www.li-dev.cn/
 *
 * This project is open-source, and it is distributed under
 * the terms of GNU General Public License. You can modify
 * and distribute freely as long as you follow the license.
 * 本项目是一个开源项目，且遵循GNU通用公共授权协议。
 * 在遵照该协议的情况下，您可以自由传播和修改。
 * http://www.gnu.org/licenses/gpl.html
 */
package cn.lambdalib.util.client.article;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import cn.lambdalib.core.LambdaLib;
import cn.lambdalib.util.client.HudUtils;
import cn.lambdalib.util.client.RenderUtils;
import cn.lambdalib.util.helper.Color;
import cn.lambdalib.util.helper.Font;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

import javax.vecmath.Vector2d;

/**
 * @author WeAthFolD
 */
public class ArticlePlotter {
	
	public static ArticlePlotter fromLang(String lang) {
		try {
			return new ArticleCompiler("unknown", new ByteArrayInputStream(lang.getBytes())).compile();
		} catch (Exception e) {
			LambdaLib.log.error("Exception compiling lang, string: " + lang, e);
			return null;
		}
	}
	
	static final ResourceLocation missing = new ResourceLocation("minecraft:missing");
	
	// Render parameters
	public double dx = 0, dy = 0;
	public double widthLimit = 2333;
	public double heightLimit = 2333;

	public double fontSize = 10;
	public double fontSize_h[] = new double[] { 0, 17, 16, 15, 14, 12, 11 };
	public double lineSpacing = 3;

	public double scale = 1.0;
	
	public Color color = Color.WHITE();
	
	private List<Instruction> instructions = new ArrayList();

	private int listId = -1;
	private double x, y, lastLineHeight;
	
	public ArticlePlotter() {}
	
	public void draw() {
		x = 0;
		y = 0;
		lastLineHeight = fontSize;
		GL11.glPushMatrix();
		GL11.glTranslated(dx, dy, 0);
		GL11.glScaled(scale, scale, 1);
		for(Instruction ins : instructions) {
			NBTTagCompound tag = ins.tag;
			switch(ins.insr) {
				case Opcodes.TEXT:
					double size = fontSize;
					boolean bold = false;

					NBTTagList tags = (NBTTagList) tag.getTag("tags");
					for(int i = 0; i < tags.tagCount(); ++i) {
						String partag = tags.getStringTagAt(i);
						switch(partag) {
							case "h1":
								bold = true;
								size = fontSize_h[1];
								break;
							case "h2":
								size = fontSize_h[2];
								break;
							case "h3":
								size = fontSize_h[3];
								break;
							case "h4":
								size = fontSize_h[4];
								break;
							case "h5":
								size = fontSize_h[5];
								break;
							case "h6":
								size = fontSize_h[6];
								break;
							case "bold":
								bold = true;
								break;
							default:
								throw new RuntimeException("Tag with name " + partag + " invalid");
						}
					}

					StringBuilder builder = new StringBuilder();
					if(bold)
						builder.append("§l");
					String dec = builder.toString();
					String content = tag.getString("content");

					drawMultiline(content, dec, size, widthLimit / scale);
					lastLineHeight = size + lineSpacing;

					break;

				case Opcodes.NEWLINE:
					x = 0;
					y += (lastLineHeight + lineSpacing);
					lastLineHeight = fontSize;
					break;

				case Opcodes.IMAGE:
					ResourceLocation texture = new ResourceLocation(tag.getString("src"));
					int width = tag.getInteger("width"),
							height = tag.getInteger("height");
					RenderUtils.loadTexture(texture);
					int twidth = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH),
							theight = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
					if(width == -1 && height == -1) {
						width = twidth;
						height = theight;
					} else if(width == -1) {
						width = (int) (height * ((double) twidth / theight));
					} else if(height == -1) {
						height = (int) (width * ((double) theight / twidth));
					}

					if(y + height < heightLimit / scale)
						HudUtils.rect(x, y, width, height);

					y += height;
					x = 0;
					break;
			}

			if(y >= heightLimit / scale)
				break;
		}
		GL11.glPopMatrix();
	}
	
	public ArticlePlotter insr(String insr, NBTTagCompound tag) {
		instructions.add(new Instruction(insr, tag));
		return this;
	}

	private boolean isWordCharacter(char ch) {
		return ('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z') || (ch == '_' && ch == '-') ||
				Character.isWhitespace(ch) || Character.isDigit(ch);
	}

	static final char puncts[] = { ',', '.', ':', '，', '。', '、', '；', '：' };

	private boolean isPunct(char ch) {
		for(char c : puncts)
			if(c == ch)
				return true;
		return false;
	}

	private void drawMultiline(String str, String dec, double size, double width) {
		Font font = Font.font;
		StringBuilder sbt = new StringBuilder();
		boolean word = false;
		double length = 0.0;
		//debug("{ " + str);
		for(int i = 0; i < str.length(); ++i) {
			char ch = str.charAt(i);
			if(sbt.length() == 0 && Character.isSpaceChar(ch))
				continue;

			boolean nword = isWordCharacter(ch);

			boolean exceeded = x + length >= width;
			boolean end = str.length() - 1 == i;
			boolean punct = isPunct(ch);
			boolean flush = false;
			if(end) {
				sbt.append(ch);
				length += font.charLen(ch, size);
			}
			if(((word ^ nword || exceeded) && sbt.length() != 1) || end) {
				flush = true;
				// Flush the buffer.
				if(punct) {
					sbt.append(ch);
					length += font.charLen(ch, size);
				}
				String content = sbt.toString();
				String flushed = dec + content;
				if(word) {
					// English word
					if(exceeded) {
						x = 0;
						y += size + lineSpacing;
					}
					font.draw(flushed, x, y, size, 0xffffff);
					x += length;
				} else {
					// Other content
					font.draw(flushed, x, y, size, 0xffffff);
					x += length;
					if(exceeded) {
						x = 0;
						y += size + lineSpacing;
					}
				}

				//debug("\t Flush [%s] [%d%d][%.2f,%.2f]", content, exceeded ? 1 : 0, nword ? 1 : 0,
				//debug("\tFlush " + content + " " + exceeded + " " + x + "," + length);
				sbt = new StringBuilder();
				length = 0.0;

				if(y >= heightLimit / scale)
					return;
			}
			if(!punct || !flush) {
				sbt.append(ch);
				length += font.charLen(ch, size);
				word = nword;
			}
		}
		//debug("}");
	}

	private void debug(Object o) {
		LambdaLib.log.info("[AP] " + o);
	}

	private class Instruction {
		final String insr;
		final NBTTagCompound tag;
		
		Instruction(String _insr, NBTTagCompound _tag) {
			insr = _insr;
			tag = _tag;
		}
	}
	
}
