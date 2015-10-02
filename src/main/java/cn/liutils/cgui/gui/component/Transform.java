package cn.liutils.cgui.gui.component;

import cn.liutils.cgui.gui.Widget;

public class Transform extends Component {
	
	public enum WidthAlign { LEFT, CENTER, RIGHT };
	
	public enum HeightAlign { TOP, CENTER, BOTTOM };
	
	public double width = 50.0, height = 50.0;
	
	public double x = 0, y = 0;
	
	public double pivotX = 0, pivotY = 0;
	
	public double scale = 1.0;
	
	public boolean doesDraw = true, doesListenKey = true;
	
	public WidthAlign alignWidth = WidthAlign.LEFT;
	
	public HeightAlign alignHeight = HeightAlign.TOP;

	public Transform() {
		super("Transform");
	}
	
	//Helper set methods
	public Transform setPos(double _x, double _y) {
		x = _x;
		y = _y;
		return this;
	}
	
	public Transform setSize(double _width, double _height) {
		width = _width;
		height = _height;
		return this;
	}
	
	public Transform setCenteredAlign() {
		alignWidth = WidthAlign.CENTER;
		alignHeight = HeightAlign.CENTER;
		return this;
	}
	
}
