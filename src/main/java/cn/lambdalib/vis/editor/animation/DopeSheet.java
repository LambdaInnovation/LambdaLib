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
package cn.lambdalib.vis.editor.animation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.lwjgl.opengl.GL11;

import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import cn.lambdalib.cgui.gui.Widget;
import cn.lambdalib.cgui.gui.component.DrawTexture;
import cn.lambdalib.cgui.gui.component.ElementList;
import cn.lambdalib.cgui.gui.component.TextBox;
import cn.lambdalib.cgui.gui.component.Transform.HeightAlign;
import cn.lambdalib.cgui.gui.event.DragEvent;
import cn.lambdalib.cgui.gui.event.DragStopEvent;
import cn.lambdalib.cgui.gui.event.FrameEvent;
import cn.lambdalib.cgui.gui.event.GainFocusEvent;
import cn.lambdalib.cgui.gui.event.GuiEvent;
import cn.lambdalib.cgui.gui.event.MouseDownEvent;
import cn.lambdalib.core.LambdaLib;
import cn.lambdalib.util.client.HudUtils;
import cn.lambdalib.util.client.RenderUtils;
import cn.lambdalib.util.generic.MathUtils;
import cn.lambdalib.util.helper.Font;
import cn.lambdalib.vis.curve.CubicCurve;
import cn.lambdalib.vis.curve.IFittedCurve;
import cn.lambdalib.vis.editor.common.EditBox;
import cn.lambdalib.vis.editor.common.VEVars;
import cn.lambdalib.vis.editor.common.widget.Window;
import net.minecraft.util.ResourceLocation;

/**
 * @author WeAthFolD
 */
public class DopeSheet extends Window {
	
	// EVENTS
	public static class TimelineChangeEvent implements GuiEvent {
		
		public final Timeline target;
		
		public TimelineChangeEvent(Timeline _target) {
			target = _target;
		}
		
	}
	
	private static class TimePtrChangeEvent implements GuiEvent {}
	
	private static ResourceLocation tex(String loc) {
		return VEVars.tex("anim/" + loc);
	}
	
	private static final ResourceLocation
		TEX_TIMEPTR = tex("timeptr"),
		TEX_KEYFRAME_ON = tex("keyframe_enabled"),
		TEX_KEYFRAME_OFF = tex("keyframe_disabled");
	
	private int fps = 40;
	private double length = 3;
	
	private double time_l = 0, time_r = 3;
	
	private TLWidget focusTL;
	
	private static final double TIMEAREA_L = 70, TIMEAREA_R = 295, TIMEAREA_S = TIMEAREA_R - TIMEAREA_L;
	
	public static class Frame {
		int frame;
		double value;
		
		Frame(int _frame, double _value) {
			frame = _frame;
			value = _value;
		}
	}
	
	public static abstract class Timeline {
		private DopeSheet dopesheet;
		
		private List<Frame> list_ = new ArrayList();
		private IFittedCurve curve_ = createCurve();
		
		public void add(int frame, double value) {
			list_.add(new Frame(frame, value));
			list_.sort((f1, f2) -> ((Integer)f1.frame).compareTo(f2.frame));
		}
		
		public void remove(int index) {
			list_.remove(index);
		}
		
		public void remove(Frame obj) {
			list_.remove(obj);
		}
		
		public Frame frame(int index) { 
			return list_.get(index); 
		}
		
		public Frame frameByF(int frame) {
			for(Frame f : list_) {
				if(f.frame == frame)
					return f;
				if(f.frame > frame)
					return null;
			}
			return null;
		}
		
		public int size() { 
			return list_.size(); 
		}
		
		public IFittedCurve getCurve() {
			return curve_;
		}
		
		public void rebuildCurve() {
			curve_.reset();
			for(Frame f : list_) {
				curve_.addPoint((double) f.frame / dopesheet.fps, f.value);
			}
		}
		
		protected abstract IFittedCurve createCurve();
	}
	
	private class CubicTimeline extends Timeline {

		@Override
		protected IFittedCurve createCurve() {
			return new CubicCurve();
		}
		
	}
	
	private class TimePtr extends Widget {
		
		private int frame;
		
		public TimePtr() {
			listen(DragEvent.class, (w, event) -> 
			{
				Widget parent = DopeSheet.this;
				double ax = getGui().mouseX - event.offsetX;
				double lx = (ax - parent.x) / parent.scale + 5;
				lx = MathUtils.wrapd(TIMEAREA_L, TIMEAREA_R, lx);
				
				setFrame(x2f(lx));
			});
			
			listen(FrameEvent.class, (w, event) ->
			{
				GL11.glColor4d(1, 1, 1, 1);
				GL11.glLineWidth(3f);
				GL11.glBegin(GL11.GL_LINES);
				GL11.glVertex2f(5, 5);
				GL11.glVertex2f(5, 68);
				GL11.glEnd();
				
				RenderUtils.loadTexture(TEX_TIMEPTR);
				if(event.hovering)
					GL11.glColor4f(1, 1, 1, 1);
				else
					GL11.glColor4f(.7f, .7f, .7f, 1);
				HudUtils.rect(0, 0, 10, 10);
			});
			
			transform.setSize(10, 10);
			transform.x = TIMEAREA_L;
			transform.y = 27;
		}
		
		public void setFrame(int f) {
			frame = f;
			transform.x = f2x(frame) - 5;
			dirty = true;
			DopeSheet.this.post(new TimePtrChangeEvent(), true);
		}
		
	}
	
	private class TLWidget extends Widget {
		
		final String path;
		final Timeline timeline;
		final List<FrameWidget> widgets = new ArrayList();
		
		Widget addFrame;
		
		TLWidget(String _path, Timeline _timeline) {
			path = _path;
			timeline = _timeline;
			
			addFrame = new Widget() {
				{
					DrawTexture dt = new DrawTexture();
					dt.texture = TEX_KEYFRAME_OFF;
					addComponent(dt);
					
					transform.alignHeight = HeightAlign.CENTER;
					transform.setSize(6, 6).setPos(30, 0);
					
					listen(TimePtrChangeEvent.class, (__, event) -> 
					{
						for(int i = 0; i < timeline.size(); ++i) {
							Frame f = timeline.frame(i);
							if(f.frame > pointer.frame) break;
							if(f.frame == pointer.frame) {
								dt.setTex(TEX_KEYFRAME_ON);
								return;
							}
						}
						dt.setTex(TEX_KEYFRAME_OFF);
					});
					
					listen(MouseDownEvent.class, (__, event) -> 
					{
						Frame f = timeline.frameByF(pointer.frame);
						if(f == null) {
							timeline.add(pointer.frame, timeline.getCurve().valueAt(f2t(pointer.frame)));
						} else {
							timeline.remove(f);
						}
						rebuild();
						DopeSheet.this.post(new TimePtrChangeEvent(), true);
					});
				}
			};
			addWidget(addFrame);
			
			addWidget(new EditBox() {
				
				{
					transform.setPos(37, 1).setSize(29, 8);
					
					listen(TimePtrChangeEvent.class, (__, event) -> 
					{
						text.setContent(r());
					});
				}

				@Override
				protected String repr() throws Exception {
					return r();
				}
				
				private String r() {
					return String.format("%.3f", timeline.curve_.valueAt(f2t(pointer.frame)));
				}

				@Override
				protected void setValue(String content) throws Exception {
					double val = Double.valueOf(content);
					Frame f = timeline.frameByF(pointer.frame);
					if(f != null) {
						 f.value = val;
						 rebuild();
					} else throw new RuntimeException();
				}
				
			});
			
			listen(FrameEvent.class, (w, event) -> 
			{
				float amul = this == focusTL ? 0.9f : 1f;
				float c1 = .15f * amul, c2 = .12f * amul;
				
				GL11.glColor4d(c1, c1, c1, 1);
				HudUtils.colorRect(3, 0, 67, 10);
				
				GL11.glColor4d(c2, c2, c2, 1);
				HudUtils.colorRect(70, 0, TIMEAREA_S, 10);
				
				Font.font.draw(path, 4, 0, 9, 0xffffff);
			});
			
			listen(GainFocusEvent.class, (w, event) -> 
			{
				if(focusTL != this) {
					focusTL = this;
					DopeSheet.this.post(new TimelineChangeEvent(focusTL.timeline));
				}
			});
			
			transform.setPos(0, 0).setSize(300, 10);
		}
		
		@Override
		public void onAdded() {
			super.onAdded();
			rebuild();
		}
		
		public void rebuild() {
			for(Widget w : widgets)
				this.removeWidget(w);
			widgets.clear();
			
			for(int i = 0; i < timeline.size() - 1; ++i) {
				if(timeline.frame(i).frame == timeline.frame(i+1).frame) {
					timeline.remove(i);
					--i;
				}
			}
			
			timeline.rebuildCurve();
			
			for(int i = 0; i < timeline.size(); ++i) {
				Frame f = timeline.frame(i);
				double x = f2t(f.frame);
				if(x >= time_l && x <= time_r) {
					FrameWidget fw = new FrameWidget(f);
					addWidget(fw);
					widgets.add(fw);
				}
			}
		}
		
		private class FrameWidget extends Widget {
			
			final Frame frame;
			final DrawTexture texture;
			
			public FrameWidget(Frame _frame) {
				frame = _frame;
				repose();
				transform.alignHeight = HeightAlign.CENTER;
				
				texture = new DrawTexture();
				texture.setTex(TEX_KEYFRAME_OFF);
				addComponent(texture);
				
				transform.setSize(10, 10);
				
				listen(DragEvent.class, (w, e) -> 
				{
					if(focusTL != TLWidget.this) {
						focusTL = TLWidget.this;
						DopeSheet.this.post(new TimelineChangeEvent(focusTL.timeline));
					}
					
					double ax = getGui().mouseX - e.offsetX;
					double lx = (ax - TLWidget.this.x) / TLWidget.this.scale + 5;
					lx = MathUtils.wrapd(TIMEAREA_L, TIMEAREA_R, lx);
					
					int f = x2f(lx);
					frame.frame = f;
					w.transform.x = f2x(f) - 5;
					w.dirty = true;
					
					texture.setTex(TEX_KEYFRAME_ON);
				});
				
				listen(DragStopEvent.class, (w, e) ->
				{
					texture.setTex(TEX_KEYFRAME_OFF);
					rebuild();
				});
			}
			
			public void repose() {
				transform.x = f2x(frame.frame) - 5;
				dirty = true;
			}
			
		}
		
	}
	
	private TimePtr pointer;
	
	private Map<String, Timeline> table = new TreeMap();
	
	private ElementList elements;
	
	private Widget timeArea;

	public DopeSheet() {
		super("Dope Sheet");
		initTopButton(TopButtonType.MINIMIZE);
		transform.setSize(300, 100);
		
		pointer = new TimePtr();
		addWidget(pointer);
		
		try {
			boxWithName(10, 2, "FPS", new EditBox() {

				@Override
				protected String repr() throws Exception {
					return String.valueOf(fps);
				}

				@Override
				protected void setValue(String content) throws Exception {
					int value = Integer.valueOf(content);
					if(value <= 0 || value > 200)
						throw new RuntimeException();
					fps = value;
				}
				
			});
			boxWithName(70, 2, "Length", new EditBox() {

				@Override
				protected String repr() throws Exception {
					return String.valueOf(length);
				}

				@Override
				protected void setValue(String content) throws Exception {
					double value = Double.valueOf(content);
					if(value < 1e-2)
						throw new RuntimeException();
					length = value;
				}
				
			});
			
			body.listen(FrameEvent.class, (w, e) -> {
				Font.font.draw(pointer.frame + " frame", 200, 2, 10, 0xffffff);
			});
		} catch(Exception e) {
			LambdaLib.log.error(e);
		}
		
		{
			Widget w = new Widget();
			w.transform.setSize(TIMEAREA_S, 7).setPos(TIMEAREA_L, 16);
			w.addComponent(new DrawTexture().setTex(null).setColor4i(50, 50, 50, 255));
			w.listen(MouseDownEvent.class, (__, event) -> {
				pointer.setFrame(x2f(TIMEAREA_L + event.x));
			});
			body.addWidget(w);
		}
		
		
		timeArea = new Widget();
		timeArea.transform.setSize(300, 60).setPos(0, 23);
		timeArea.listen(FrameEvent.class, (w, event) -> 
		{
			GL11.glColor4d(.2, .2, .2, 1);
			HudUtils.colorRect(3, 0, 67, 60);
			GL11.glColor4d(.15, .15, .15, 1);
			HudUtils.colorRect(70, 0, 225, 60);
		});
		body.addWidget(timeArea);
		
	}
	
	public void addTarget(String path, Timeline timeline) {
		if(table.containsKey(path))
			throw new RuntimeException();
		table.put(path, timeline);
		timeline.dopesheet = this;
		if(this.getGui() != null) {
			rebuild();
		}
	}
	
	/**
	 * Convert current timeline data (all timeline) into a json object containing {path:curve} mapping.
	 */
	public JsonObject toJson() {
		return null;
	}
	
	/**
	 * Construct the timeines from the given json object
	 */
	public void fromJson(JsonObject obj) {
		
	}
	
	@Override
	public void onAdded() {
		rebuild();
		super.onAdded();
	}
	
	private void rebuild() {
		if(elements != null)
			timeArea.removeComponent(elements);
		
		elements = new ElementList();
		for(Map.Entry<String, Timeline> entry : table.entrySet()) {
			elements.addWidget(new TLWidget(entry.getKey(), entry.getValue()));
		}
		timeArea.addComponent(elements);
	}
	
	private void boxWithName(double x, double y, String name, Widget box) {
		Widget textwid = new Widget();
		TextBox text = new TextBox();
		text.content = name;
		text.size = 10;
		textwid.addComponent(text);
		textwid.transform.setPos(x, y);
		textwid.transform.setSize(0, 10);
		
		box.transform.setPos(x + Font.font.strLen(name,10) + 3, y);
		
		body.addWidget(textwid);
		body.addWidget(box);
	}
	
	// Convert macros
	// f: frame, t: time, x: x_position
	
	private double f2t(int frame) {
		return (double) frame / fps;
	}
	
	private int t2f(double time) {
		return (int) (time * fps);
	}
	
	private double x2t(double x) {
		return time_l + (x - TIMEAREA_L) / TIMEAREA_S * (time_r - time_l);
	}
	
	private double t2x(double time) {
		return TIMEAREA_L + (time - time_l) / (time_r - time_l) * TIMEAREA_S;
	}
	
	private double f2x(int frame) {
		return t2x(f2t(frame));
	}
	
	private int x2f(double x) {
		return t2f(x2t(x));
	}
	
	private void log(Object obj) {
		LambdaLib.log.info("[DopeSheet]" + obj);
	}
	
	private static class TimelineAdapter extends TypeAdapter<Timeline> {

		@Override
		public void write(JsonWriter out, Timeline value) throws IOException {
			
		}

		@Override
		public Timeline read(JsonReader in) throws IOException {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
}
