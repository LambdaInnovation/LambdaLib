/**
 * 
 */
package cn.lambdalib.cgui.gui.event;

/**
 * Solely a notification event, fired on current focus when it was dragged.
 * 
 * @author WeAthFolD
 */
public class DragEvent implements GuiEvent {
	
	/**
	 * Offset coordinates from dragging widget origin to current mouse position, in global scale level.
	 */
	public final double offsetX, offsetY;

	public DragEvent(double _offsetX, double _offsetY) {
		offsetX = _offsetX;
		offsetY = _offsetY;
	}

}
