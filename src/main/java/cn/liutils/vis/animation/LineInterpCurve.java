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
package cn.liutils.vis.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.liutils.util.generic.MathUtils;

/**
 * @author WeAthFolD
 */
public class LineInterpCurve implements ICurve {
	
	private List<Point> points = new ArrayList();
	
	public LineInterpCurve() {}
	
	public void addPoint(double x, double y) {
		points.add(new Point(x, y));
		Collections.sort(points, (Point a, Point b) -> {
			return ((Double) a.x).compareTo(b.x);
		});
	}

	@Override
	public double valueAt(double x) {
		int index = 0;
		for(; index < points.size() && points.get(index).x < x; ++index);
		if(index > 0) --index;
		Point p0, p1;
		if(index == points.size() - 1) {
			p0 = points.get(index - 1);
			p1 = points.get(index);
		} else {
			p0 = points.get(index);
			p1 = points.get(index + 1);
		}
		double lambda = (x - p0.x) / (p1.x - p0.x);
		return MathUtils.lerp(p0.y, p1.y, lambda);
	}
	
	private class Point {
		double x, y;
		Point(double _x, double _y) {
			x = _x;
			y = _y;
		}
	}

}
