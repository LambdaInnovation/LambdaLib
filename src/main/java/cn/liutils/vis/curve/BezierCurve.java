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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import cn.liutils.util.generic.MathUtils;

/**
 * Adapt bezier curve into the IFittedCurve pattern. 
 * Use <code>setControlPoint</code> to set the control point for each index.
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
			Point p0 = drawpts.get(0), c0 = pts.get(0).ctrl;
			double dx = c0 == null ? 0 : p0.x - c0.x;
			double k = dx == 0 ? 0 : (p0.y - c0.y) / dx;
			return p0.y + k * (x - p0.x);
		}
		if(x >= drawpts.get(drawpts.size() - 1).x) {
			Point pn = drawpts.get(drawpts.size() - 1), cn = pts.get(pts.size() - 1).ctrl;
			double dx = cn == null ? 0 : cn.x - pn.x;
			double k = dx == 0 ? 0 : (cn.y - pn.y) / dx;
			return pn.y + k * (x - pn.x);
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
	
	public static TypeAdapter<BezierCurve> adapter = new TypeAdapter<BezierCurve>() {

		@Override
		public void write(JsonWriter out, BezierCurve value) throws IOException {
			out.beginObject();
			
			//out.name("type");
			//out.value("bezier");
			
			out.name("points");
			out.beginArray();
			for(Pt pt : value.pts) {
				out.beginArray();
				out.value(pt.pos.x);
				out.value(pt.pos.y);
				out.endArray();
			}
			out.endArray();
			
			out.name("ctrl");
			out.beginArray();
			for(Pt pt : value.pts) {
				if(pt.ctrl != null) {
					out.beginArray();
					out.value(pt.ctrl.x);
					out.value(pt.ctrl.y);
					out.endArray();
				} else {
					out.nullValue();
				}
			}
			out.endArray();
			out.endObject();
		}

		@Override
		public BezierCurve read(JsonReader in) throws IOException {
			BezierCurve ret = new BezierCurve();
			in.beginObject();
			JsonToken token0 = null;
			while((token0 = in.peek()) != JsonToken.END_OBJECT) {
				JsonToken token1 = null;
				switch(in.nextName()) {
				case "points":
					in.beginArray();
					while((token1 = in.peek()) != JsonToken.END_ARRAY) {
						in.beginArray();
						ret.addPoint(in.nextDouble(), in.nextDouble());
						in.endArray();
					}
					in.endArray();
					break;
				case "ctrl":
					in.beginArray();
					int i = 0;
					while((token1 = in.peek()) != JsonToken.END_ARRAY) {
						if(in.peek() == JsonToken.NULL) {
							in.nextNull();
						} else {
							in.beginArray();
							ret.setCtrlPoint(i, in.nextDouble(), in.nextDouble());
							in.endArray();
						}
						++i;
					}
					in.endArray();
					break;
				default:
					in.nextString();
				}
			}
			in.endObject();
			return ret;
		}
		
	};

	@Override
	public int pointCount() {
		return pts.size();
	}

	@Override
	public Point getPoint(int i) {
		return pts.get(i).pos;
	}

}
