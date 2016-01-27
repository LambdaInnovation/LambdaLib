/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.vis.curve;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.lambdalib.util.generic.MathUtils;

/**
 * @author WeAthFolD
 */
public class LineInterpCurve implements IFittedCurve {
    
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

    @Override
    public int pointCount() {
        return points.size();
    }

    @Override
    public Point getPoint(int i) {
        return points.get(i);
    }

    @Override
    public void reset() {
        points.clear();
    }

}
