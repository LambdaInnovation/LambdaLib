/**
 * 
 */
package cn.liutils.cgui.gui.event;

/**
 * Solely a notification event, fired when a widget was dragged.
 * 
 * @author WeAthFolD
 */
public class DragEvent implements GuiEvent {
	
	/**
	 * Offset coordinates from dragging widget origin to current mouse position, in GLOBAL scale level.
	 */
	public final double offsetX, offsetY;

	public DragEvent(double _offsetX, double _offsetY) {
		offsetX = _offsetX;
		offsetY = _offsetY;
	}

}
