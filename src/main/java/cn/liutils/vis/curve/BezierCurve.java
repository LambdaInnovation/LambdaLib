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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.liutils.util.generic.MathUtils;

/**
 * TODO: Better over-the-end handling
 * @author WeAthFolD
 */
public class BezierCurve implements IFittedCurve {
	
	/**
	 * Average distance between draw points
	 */
	public double divideStep = 1.0;
	
	private List<Point> drawpts = new ArrayList();
	private List<Pt> pts = new ArrayList();
	private boolean baked = false;
	private double minX = Double.NaN, maxX = Double.NaN;

	@Override
	public void addPoint(double x, double y) {
		pts.add(new Pt(x, y));
		if(maxX == Double.NaN || maxX < x)
			maxX = x;
		if(minX == Double.NaN || minX > x)
			minX = x;
		
		baked = false;
		Collections.sort(pts, (Pt a, Pt b) -> ((Double) a.pos.x).compareTo(b.pos.x));
	}
	
	public void bake() {
		drawpts.clear();
		for(int i = 1; i < pts.size(); ++i) {
			Pt pt1 = pts.get(i - 1), pt2 = pts.get(i);
			Point 
				p0 = pt1.pos,
				p1 = pt1.ctrl == null ? pt1.pos : pt1.ctrl,
				p3 = pt2.pos,
				p2 = pt2.ctrl == null ? pt2.pos : pt2.ctrl;
			double step = (p3.x - p1.x) / divideStep;
			drawpts.add(p0);
			double t = 1 / step;
			for(int j = 0; j < step - 1; ++j, t += 1 / step) {
				double 
					u = 1 - t,
					u2 = u * u,
					u3 = u2 * u,
					t2 = t * t,
					t3 = t2 * t;
				double x = u3 * p0.x + 3 * u2 * t * p1.x + 3 * u * t2 * p2.x + t3 * p3.x,
						y = u3 * p0.y + 3 * u2 * t * p1.y + 3 * u * t2 * p2.y + t3 * p3.y;
				drawpts.add(new Point(x, y));
			}
			drawpts.add(p3);
		}
		baked = true;
		Collections.sort(drawpts, (Point a, Point b) -> ((Double) a.x).compareTo(b.x));
	}
	
	public BezierCurve setCtrlPoint(int ind, double x, double y) {
		pts.get(ind).emplaceCtrl(x, y);
		return this;
	}
	
	public int sizePoints() {
		return pts.size();
	}

	/**
	 * Piecewise linear interop
	 */
	@Override
	public double valueAt(double x) {
		if(!baked)
			bake();
		if(x <= drawpts.get(0).x) {
			return drawpts.get(0).y;
		}
		if(x >= drawpts.get(drawpts.size() - 1).x) {
			return drawpts.get(drawpts.size() - 1).y;
		}
		int i = 0;
		for(; drawpts.get(i).x < x; ++i);
		Point p0 = drawpts.get(i - 1), p1 = drawpts.get(i);
		return MathUtils.lerp(p0.y, p1.y, (x - p0.x) / (p1.x - p0.x));
	}
	
	private static class Pt {
		Point pos;
		Point ctrl;
		
		public Pt(double _x, double _y) {
			pos = new Point(_x, _y);
		}
		
		public void emplaceCtrl(double _x, double _y) {
			ctrl = new Point(_x, _y);
		}
	}
	
	private static class Point {
		double x, y;
		
		Point(double _x, double _y) {
			x = _x;
			y = _y;
		}
		Point() {
			this(0, 0);
		}
	}

}
