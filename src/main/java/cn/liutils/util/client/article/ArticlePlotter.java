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
package cn.liutils.util.client.article;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import cn.liutils.core.LIUtils;
import cn.liutils.util.client.HudUtils;
import cn.liutils.util.client.RenderUtils;
import cn.liutils.util.helper.Color;
import cn.liutils.util.helper.Font;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

/**
 * @author WeAthFolD
 */
public class ArticlePlotter {
	
	public static ArticlePlotter fromLang(String lang) {
		try {
			return new ArticleCompiler("unknown", new ByteArrayInputStream(lang.getBytes())).compile();
		} catch (Exception e) {
			LIUtils.log.error("Exception compiling lang, string: " + lang, e);
			return null;
		}
	}
	
	static final ResourceLocation missing = new ResourceLocation("minecraft:missing");
	
	// Render parameters
	public double fontSize = 10;
	public double fontSize_h1 = 18;
	public double fontSize_h2 = 16;
	public double fontSize_h3 = 14;
	public double lineSpacing = 2;
	
	public Color color = Color.WHITE();
	
	private List<Instruction> instructions = new ArrayList();
	
	private boolean redraw = true;
	private int listId = -1;
	
	public ArticlePlotter() {}
	
	public void draw() {
		if(redraw) {
			redraw = false;
			rebuildList();
		} else {
			GL11.glCallList(listId);
		}
	}
	
	public void markDirty() {
		redraw = true;
	}
	
	private void rebuildList() {
		if(listId == -1)
			listId = GL11.glGenLists(1);
		else
			GL11.glDeleteLists(listId, 1);
		
		double x = 0, y = 0, lastLineHeight = fontSize;
		
		GL11.glNewList(listId, GL11.GL_COMPILE);
		
		for(Instruction ins : instructions) {
			String insr = ins.insr;
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
						size = fontSize_h1;
						break;
					case "h2":
						size = fontSize_h2;
						break;
					case "h3":
						size = fontSize_h3;
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
				builder.append(tag.getString("content"));
				String content = builder.toString();
				
				Font.font.draw(content, x, y, size, color.asHexColor());
				x += Font.font.strLen(content, size);
				lastLineHeight = size;
				
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
				
				HudUtils.rect(x, y, width, height);
				
				y += height;
				x = 0;
				break;
			}
		}
		
		GL11.glEndList();
		RenderUtils.loadTexture(missing);
	}
	
	public ArticlePlotter insr(String insr, NBTTagCompound tag) {
		instructions.add(new Instruction(insr, tag));
		return this;
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
