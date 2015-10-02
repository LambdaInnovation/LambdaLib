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
package cn.liutils.cgui.gui.component;

import java.util.ArrayList;
import java.util.List;

import cn.liutils.cgui.gui.Widget;
import cn.liutils.cgui.gui.event.FrameEvent;
import cn.liutils.cgui.gui.event.FrameEvent.FrameEventHandler;
import cn.liutils.cgui.gui.event.GuiEvent;
import cn.liutils.cgui.gui.event.GuiEventHandler;
import cn.liutils.util.generic.MathUtils;

/**
 * Component that can hold widgets itself and display them as a list. Only Widgets fully in the area will be shown.
 * You MUST specify the widgets BEFORE the component was added into the Widget.
 * @author WeAthFolD
 */
public class ElementList extends Component {
	
	List<Widget> subWidgets = new ArrayList();
	
	/**
	 * The fixed vertical spacing between widgets.
	 */
	public double spacing = 0.0;
	
	private int progress;
	
	private boolean loaded = false;

	public ElementList() {
		super("ElementList");
		
		this.addEventHandler(new FrameEventHandler() {
			@Override
			public void handleEvent(Widget w, FrameEvent event) {
				if(!loaded) {
					loaded = true;
					for(Widget ww : subWidgets) {
						w.addWidget(ww);
					}
					updateList();
				}
			}
		});
	}
	
	public static ElementList get(Widget w) {
		return w.getComponent("ElementList");
	}
	
	public int getProgress() {
		return progress;
	}
	
	public int getMaxProgress() {
		return subWidgets.size() - 1;
	}
	
	public void progressNext() {
		setProgress(progress + 1);
	}
	
	public void progressLast() {
		setProgress(progress - 1);
	}
	
	public void setProgress(int p) {
		p = MathUtils.wrapi(0, getMaxProgress(), p);
		boolean u = progress != p;
		progress = p;
		if(u)
			updateList();
	}
	
	private void updateList() {
		double sum = 0.0;
		for(Widget w : subWidgets) {
			w.transform.doesDraw = false;
		}
		
		for(int i = progress; i < subWidgets.size() && sum <= this.widget.transform.height; ++i) {
			Widget w = subWidgets.get(i);
			
			w.transform.doesDraw = true;
			w.transform.x = 0;
			w.transform.y = sum;
			w.dirty = true;
			
			sum += w.transform.height + spacing;
		}
	}
	
	public void disposeAll() {}
	
	@Override
	public void onRemoved() {
		for(Widget w : subWidgets) {
			w.dispose();
		}
	}
	
	public static class ProgressChangedEvent implements GuiEvent  {}
	
	public abstract static class ProgressChangeHandler 
		extends GuiEventHandler<ProgressChangedEvent> {

		public ProgressChangeHandler() {
			super(ProgressChangedEvent.class);
		}
		
	}
	
	public void addWidget(Widget w) {
		if(loaded) return;
		w.needCopy = false;
		subWidgets.add(w);
	}
	
	public double getFullHeight() {
		return sumHeight(0, subWidgets.size());
	}
	
	/**
	 * from inclusive, to exclusive
	 */
	private double sumHeight(int from, int to) {
		double ret = 0.0;
		for(int i = from; i < to; ++i) {
			ret += subWidgets.get(i).transform.height;
		}
		return ret;
	}
	
	public ElementList copy() {
		ElementList el = (ElementList) super.copy();
		for(Widget w : subWidgets) {
			el.addWidget(w);
		}
		return el;
	}

}
