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
package cn.liutils.template.block;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cn.liutils.api.render.model.ITileEntityModel;
import cn.liutils.util.client.RenderUtils;

/**
 * Simple model renderer template.
 * @author WeathFolD
 */
public class RenderBlockMultiModel extends RenderBlockMulti {
	
	protected ITileEntityModel mdl;
	protected ResourceLocation tex;
	protected double scale = 1.0;
	protected double rotateY = 0.0;
	
	public RenderBlockMultiModel(ITileEntityModel _mdl, ResourceLocation _tex) {
		mdl = _mdl;
		tex = _tex;
	}
	
	public RenderBlockMultiModel() {}
	
	public RenderBlockMultiModel setScale(double f) {
		scale = f;
		return this;
	}

	@Override
	public void drawAtOrigin(TileEntity te) {
		GL11.glColor4d(1, 1, 1, 1);
		if(tex != null) {
			RenderUtils.loadTexture(tex);
		}
		GL11.glRotated(rotateY, 0, 1, 0);
		GL11.glScaled(scale, scale, scale);
		mdl.render(te, 0, 0);
	}

}
