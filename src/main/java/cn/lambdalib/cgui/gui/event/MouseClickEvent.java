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
package cn.lambdalib.cgui.gui.event;

/**
 * Fired on both LIGui and current focus when any mouse button except for LMB and RMB is clicked.
 * (For convenience reasons, they are handled in {@link LeftClickEvent} and {@link RightClickEvent}.)
 */
public class MouseClickEvent implements GuiEvent {

	/**
	 * Mouse position in local coordinate space.
	 */
	public final double mx, my;
	
	/**
	 * Pressed button id.
	 */
	public final int button;
	
	public MouseClickEvent(double _mx, double _my, int bid) {
		mx = _mx;
		my = _my;
		button = bid;
	}

}
