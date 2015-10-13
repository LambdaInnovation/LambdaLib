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
package cn.liutils.vis.test;

import cn.annoreg.core.Registrant;
import cn.liutils.cgui.gui.LIGui;
import cn.liutils.vis.editor.IVisPluginCommand;
import cn.liutils.vis.editor.gui.VisEditor;
import cn.liutils.vis.editor.gui.VisPlugin;
import cn.liutils.vis.editor.gui.widget.Window;
import cn.liutils.vis.editor.gui.widget.Window.TopButtonType;
import cn.liutils.vis.editor.registry.RegVisPluginCommand;
import net.minecraft.command.ICommandSender;

/**
 * @author WeAthFolD
 */
@Registrant
@RegVisPluginCommand("test")
public class EditorTest implements IVisPluginCommand {

	@Override
	public VisPlugin createPlugin(ICommandSender ics, String[] args) {
		return new ThePlugin();
	}
	
	public static class ThePlugin extends VisPlugin {

		public void onEditorInit(VisEditor editor) {
			System.out.println("OnEditorInit");
			LIGui gui = editor.getGui();
			Window window = new Window("233333");
			window.transform.setPos(100, 100).setSize(200, 200);
			window.initTopButton(TopButtonType.MINIMIZE);
			
			gui.addWidget(window);
		}
		
		@Override
		public void onSave(String type) {
			System.out.println("OnSave!");
		}
		
	}

}
