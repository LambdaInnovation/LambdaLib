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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.lwjgl.opengl.GL11;

import cn.lambdalib.cgui.gui.Widget;
import cn.lambdalib.cgui.gui.component.TextBox;
import cn.lambdalib.cgui.gui.event.DragEvent;
import cn.lambdalib.cgui.gui.event.FrameEvent;
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
	
	private static ResourceLocation tex(String loc) {
		return VEVars.tex("anim/" + loc);
	}
	
	private static final ResourceLocation
		TEX_TIMEPTR = tex("timeptr"),
		TEX_KEYFRAME_ON = tex("keyframe_enabled"),
		TEX_KEYFRAME_OFF = tex("keyframe_disabled");
	
	private int fps = 40;
	private double length = 3;
	
	private static final double TIMEAREA_L = 60, TIMEAREA_R = 280, TIMEAREA_S = TIMEAREA_R - TIMEAREA_L;
	
	private class Frame {
		int frame;
		double value;
		
		Frame(int _frame, double _value) {
			frame = _frame;
			value = _value;
		}
	}
	
	// Preserved for future use. (More types of curves?)
	private abstract class BaseTimeline {
		private List<Frame> list_ = new ArrayList();
		private IFittedCurve curve_ = createCurve();
		
		public void add(int frame, double value) {
			list_.add(new Frame(frame, value));
			list_.sort((f1, f2) -> ((Integer)f1.frame).compareTo(f2.frame));
		}
		
		public void remove(int index) {
			list_.remove(index);
		}
		
		public Frame frame(int index) { 
			return list_.get(index); 
		}
		
		public int size() { 
			return list_.size(); 
		}
		
		public void rebuildCurve() {
			curve_ = createCurve();
			for(Frame f : list_) {
				curve_.addPoint((double) f.frame / fps, f.value);
			}
		}
		
		protected abstract IFittedCurve createCurve();
	}
	
	private class Timeline extends BaseTimeline {

		@Override
		protected IFittedCurve createCurve() {
			return new CubicCurve();
		}
		
	}
	
	private class TimePtr extends Widget {
		
		int frame;
		
		public TimePtr() {
			listen(DragEvent.class, (w, event) -> 
			{
				Widget parent = DopeSheet.this;
				double ax = getGui().mouseX - event.offsetX;
				double lx = (ax - parent.x) / parent.scale + 5;
				lx = MathUtils.wrapd(TIMEAREA_L, TIMEAREA_R, lx);
				lx = (lx - TIMEAREA_L) / TIMEAREA_S;
				
				frame = (int) (fps * length * lx);
				transform.x = TIMEAREA_L + TIMEAREA_S * frame / (fps * length) - 5;
				dirty = true;
			});
			
			listen(FrameEvent.class, (w, event) ->
			{
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
		
	}
	
	private TimePtr pointer;
	
	private Map<String, Timeline> table = new TreeMap();

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
		
	}
	
	public void addTarget(String path) {
		if(table.containsKey(path))
			throw new RuntimeException();
		table.put(path, new Timeline());
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
	

}
