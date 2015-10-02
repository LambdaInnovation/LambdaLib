/**
 * 
 */
package cn.liutils.cgui.gui.event;

import cn.liutils.cgui.gui.Widget;

/**
 * @author WeAthFolD
 */
public class KeyEvent implements GuiEvent {
	public final char inputChar;
	public final int keyCode;
	
	public KeyEvent(char _ch, int _key) {
		inputChar = _ch;
		keyCode = _key;
	}
	
	public abstract static class KeyEventHandler extends GuiEventHandler<KeyEvent> {

		public KeyEventHandler() {
			super(KeyEvent.class);
		}
		
	}
}
