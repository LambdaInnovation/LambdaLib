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
 * Class that handles a single gui event.
 * A GuiEventHandler should be stateless. Otherwise, use Component.
 * @author WeAthFolD
 */
public abstract class GuiEventHandler<T extends GuiEvent> implements IGuiEventHandler<T> {
	
	private final Class<? extends GuiEvent> eventClass;
	
	public GuiEventHandler(Class<? extends GuiEvent> _eventClass) {
		eventClass = _eventClass;
		Widget w;
	}
	
	public abstract void handleEvent(Widget w, T event);
	
	public Class <? extends GuiEvent> getEventClass() {
		return eventClass;
	}
	
}
