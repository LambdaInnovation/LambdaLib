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
package cn.liutils.cgui.gui.event;

import cn.liutils.cgui.gui.Widget;

/**
 * Indicate that a widget has lost input focus.
 * @author WeAthFolD
 */
public class LostFocusEvent implements GuiEvent {
	
	public Widget newFocus;
	
	public LostFocusEvent(Widget _newFocus) {
		newFocus = _newFocus;
	}
	
	public static abstract class LostFocusHandler extends GuiEventHandler<LostFocusEvent> {
		public LostFocusHandler() {
			super(LostFocusEvent.class);
		}
	}
}
