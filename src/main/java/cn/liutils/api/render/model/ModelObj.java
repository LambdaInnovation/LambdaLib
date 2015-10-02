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

import net.minecraftforge.client.model.IModelCustom;

public class ModelObj extends Model {
	
	IModelCustom model;

	public ModelObj(IModelCustom _model) {
		model = _model;
	}

	public ModelObj(IModelCustom _model, Object[][] data) {
		super(data);
		model = _model;
	}

	@Override
	public void draw(String part) {
		model.renderPart(part);
	}

	@Override
	public void drawAll() {
		model.renderAll();
	}

}
