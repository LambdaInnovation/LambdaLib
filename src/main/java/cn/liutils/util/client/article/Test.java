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

import cn.annoreg.core.Registrant;
import cn.liutils.api.gui.AuxGui;
import cn.liutils.registry.AuxGuiRegistry.RegAuxGui;
import net.minecraft.client.gui.ScaledResolution;

/**
 * @author WeAthFolD
 */
//@Registrant
//@RegAuxGui
public class Test extends AuxGui {
	
	String str = "[h1]This is a test[/h1][ln][bold]This is another test[/bold]line[ln][img src=\"liutils:textures/cgui/missing.png\" width=123 height=123][ln]I tell you this is good!";

	ArticlePlotter plotter;
	
	public Test() {
		plotter = ArticlePlotter.fromLang(str);
	}

	@Override
	public void draw(ScaledResolution sr) {
		plotter.draw();
	}
	
	@Override
	public boolean isForeground() {
		return false;
	}

}
