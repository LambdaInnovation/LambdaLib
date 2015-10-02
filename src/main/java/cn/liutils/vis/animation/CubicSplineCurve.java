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
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

/**
 * @author WeAthFolD
 */
public class CubicSplineCurve implements ICurve {

	private List<Pair<Double, Double>> pts = new ArrayList();
	
	public void addPoint(double x, double y) {
		pts.add(Pair.of(x, y));
		pts.sort((Pair<Double, Double> a, Pair<Double, Double> b) -> {
			return a.getLeft().compareTo(b.getLeft());
		});
	}
	
	@Override
	public double valueAt(double x) {
		// Find last point whose value <= x
		int index = 0;
		for(; index < pts.size() && pts.get(index).getLeft() < x; ++index);
		if(index > 0) --index;
		if(index >= pts.size() - 1) index = pts.size() - 2;
		
		// Generate 4 refpoints' vals
		Pair<Double, Double> pt1 = pts.get(index), pt2 = pts.get(index + 1), pt0 = index == 0 ? pt1 : pts.get(index - 1);
		double dist = pt2.getLeft() - pt1.getLeft(), ldist = pt0 == pt1 ? dist : pt1.getRight() - pt0.getLeft();
		double p0 = pt1.getRight(), p1 = pt2.getRight(), m0 = kval(index) * ldist, m1 = kval(index + 1) * dist;
		
		double u = (x - pt1.getLeft()) / dist,
				u2 = u * u,
				u3 = u2 * u;
		
		// Apply calculation
		return (2 * u3 - 3 * u2 + 1) * p0 +
			   (u3 - 2 * u2 + u) * m0 +
			   (-2 * u3 + 3 * u2) * p1 +
			   (u3 - u2) * m1;
	}
	
	private double kval(int index) {
		Pair<Double, Double> pt1 = null, pt2 = null, pt3 = null;
		
		boolean flag = false;
		if(index == 0) {
			pt1 = pts.get(0);
			pt2 = pts.get(1);
			flag = true;
		}
		if(index == pts.size() - 1) {
			pt1 = pts.get(pts.size() - 2);
			pt2 = pts.get(index);
			flag = true;
		}
		if(flag)
			return (pt2.getRight() - pt1.getRight()) / (pt2.getLeft() - pt1.getLeft());
		
		pt1 = pts.get(index - 1);
		pt2 = pts.get(index);
		pt3 = pts.get(index + 1);
		
		return ((pt2.getRight() - pt1.getRight()) / (pt2.getLeft() - pt1.getLeft()) + 
				(pt3.getRight() - pt2.getRight()) / (pt3.getLeft() - pt2.getLeft()) ) / 2;
	}

}
