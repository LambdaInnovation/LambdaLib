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
package cn.liutils.vis.curve;

/**
 * This interface represents a Fitted Curve (拟合曲线).
 * Add control points into the curve and the interface generates a reasonable curve for it.
 * @author WeAthFolD
 */
public interface IFittedCurve {
	
	public static class Point implements Comparable<Point> {
		public double x, y;
		public Point() {}
		public Point(double _x, double _y) {
			x = _x;
			y = _y;
		}
		
		@Override
		public int compareTo(Point arg0) {
			return ((Double) x).compareTo(arg0.x);
		}
	}
	
	/**
	 * Add a point to fit for the curve.
	 * Note that normally you shouldn't add the same point twice. Bad things like zero division might occur.
	 */
	void addPoint(double x, double y);
	
	/**
	 * Get the yval of the curve at given x coordinate.
	 */
	double valueAt(double x);
	
	/**
	 * @return The count of control points
	 */
	int pointCount();
	
	/**
	 * @return The control point with index i
	 * @throws IndexOutofBoundsException
	 */
	Point getPoint(int i);
	
}
