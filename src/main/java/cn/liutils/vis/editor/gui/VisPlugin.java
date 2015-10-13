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
package cn.liutils.vis.editor.gui;

import cn.liutils.cgui.gui.Widget;

/**
 * @author WeAthFolD
 */
public abstract class VisPlugin {
	
	VisEditor editor;
	
	public VisPlugin() {}
	
	public void addModifier(String id, Widget modifier) {
		
	}
	
	public void addToolbarButton(String buttonName, ICallback callback) {
		
	}
	
	public void addStorageType(String type) {
		
	}
	
	public void onEditorInit(VisEditor editor) {}
	
	protected VisEditor getEditor() {
		return editor;
	}
	
	public abstract void onSave(String type);
	
	public interface ICallback {
		void invoke();
	}
	
}
