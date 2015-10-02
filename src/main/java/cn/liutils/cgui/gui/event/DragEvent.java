/**
 * 
 */
package cn.liutils.cgui.gui.event;


/**
 * Solely a notification event, fired when a widget was dragged.
 * @author WeAthFolD
 */
public class DragEvent implements GuiEvent { 
	 public DragEvent() {}
	 
	 public static abstract class DragEventHandler extends GuiEventHandler<DragEvent> {
		public DragEventHandler() {
			super(DragEvent.class);
		}
	 }
}
