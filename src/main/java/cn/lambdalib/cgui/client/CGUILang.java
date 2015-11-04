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
package cn.lambdalib.cgui.client;

import net.minecraft.util.StatCollector;

/**
 * 
 * @author KSkun
 */
public class CGUILang {
	
	public static String butBackground() {
		return local("cgui.gui.background");
	}
	
	public static String guiComeditor() {
		return local("cgui.gui.componentEditor");
	}
	
	public static String txtBackground() {
		return local("cgui.text.background");
	}
	
	public static String txtSelection() {
		return local("cgui.text.selection");
	}
	
	public static String butRename() {
		return local("cgui.button.rename");
	}
	
	public static String butDuplicate() {
		return local("cgui.button.duplicate");
	}
	
	public static String butRemove() {
		return local("cgui.button.remove");
	}
	
	public static String butMoveDown() {
		return local("cgui.button.moveDown");
	}
	
	public static String butMoveUp() {
		return local("cgui.button.moveUp");
	}
	
	public static String butChild() {
		return local("cgui.button.child");
	}
	
	public static String butDechild() {
		return local("cgui.button.dechild");
	}
	
	public static String butHierarchy() {
		return local("cgui.button.hierarchy");
	}
	
	public static String butAdd() {
		return local("cgui.button.addWidget");
	}
	
	public static String butSaveAs() {
		return local("cgui.button.saveAs");
	}
	
	public static String butSave() {
		return local("cgui.button.save");
	}
	
	public static String guiHierarchy() {
		return local("cgui.gui.hierarchy");
	}
	
	public static String guiToolbar() {
		return local("cgui.gui.toolbar");
	}
	 
	public static String commFileNotFound() {
		return local("cgui.command.filenotfound");
	}
	
	public static String commSaveFailed() {
		return local("cgui.command.savefailed");
	}
	
	public static String commSaved() {
		return local("cgui.command.saved");
	}
	
	public static String commUsage() {
		return local("cgui.command.usage");
	}
	 
	private static String local(String str) {
		return StatCollector.translateToLocal(str);
	}
	
}
