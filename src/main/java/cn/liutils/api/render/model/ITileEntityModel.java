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
package cn.liutils.api.render.model;

import net.minecraft.tileentity.TileEntity;

/**
 * Interface for models applied on TileEntity.
 * @author WeAthFolD
 */
public interface ITileEntityModel {

	public void render(TileEntity is, float f1, float f);
	
	public void renderPart(TileEntity te, String name , float f1, float f);
	
}
