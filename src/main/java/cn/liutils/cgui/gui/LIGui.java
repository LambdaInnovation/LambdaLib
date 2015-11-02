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
package cn.liutils.cgui.gui;

import java.util.Iterator;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import cn.liutils.cgui.gui.component.Transform;
import cn.liutils.cgui.gui.event.DragEvent;
import cn.liutils.cgui.gui.event.FrameEvent;
import cn.liutils.cgui.gui.event.GainFocusEvent;
import cn.liutils.cgui.gui.event.GuiEvent;
import cn.liutils.cgui.gui.event.GuiEventBus;
import cn.liutils.cgui.gui.event.KeyEvent;
import cn.liutils.cgui.gui.event.LostFocusEvent;
import cn.liutils.cgui.gui.event.MouseDownEvent;
import cn.liutils.cgui.gui.event.RefreshEvent;
import cn.liutils.cgui.gui.event.global.AddWidgetEvent;
import cn.liutils.cgui.gui.event.global.GlobalMouseEvent;
import cn.liutils.core.LIUtils;
import cn.liutils.util.client.HudUtils;
import cn.liutils.util.helper.GameTimer;

/**
 * @author WeathFolD
 */
public class LIGui extends WidgetContainer {
	
	double width, height; //Only useful when calculating 'CENTER' align preference
	
	//Absolute mouse position.
	public double mouseX, mouseY;
	
	Widget focus; //last input focus
	
	public final GuiEventBus eventBus = new GuiEventBus();

	public LIGui() {}
	
	public LIGui(double width, double height) {
		this.width = width;
		this.height = height;
	}
	
	public void dispose() {
	}
	
	/**
	 * Called when screen is being resized.
	 * @param w new width
	 * @param h new height
	 */
	public void resize(double w, double h) {
		boolean diff = width != w || height != h;
		
		this.width = w;
		this.height = h;
		
		if(diff) {
			for(Widget widget : this) {
				widget.dirty = true;
			}
		}
	}
	
	//---Event callback---
	
	public void draw() {
		draw(-1, -1);
	}
	
	/**
	 * Go down the hierarchy tree and draw each widget node.
	 */
	public void draw(double mx, double my) {
		frameUpdate();
		updateMouse(mx, my);
		
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		drawTraverse(mx, my, null, this, getTopWidget(mx, my));
		
		GL11.glEnable(GL11.GL_ALPHA_TEST);
	}
	
	@Override
	public boolean addWidget(String name, Widget w) {
		if(this.hasWidget(name))
			return false;
		super.addWidget(name, w);
		this.postEvent(new AddWidgetEvent(w));
		return true;
	}
	
	static final long DRAG_TIME_TOLE = 100;
	long lastStartTime, lastDragTime;
	Widget draggingNode;
	double xOffset, yOffset;
	/**
	 * Standard GUI class callback.
	 * @param mx
	 * @param my
	 * @param btn the mouse button ID.
	 * @param dt how long is this button being pressed(ms)
	 */
    public boolean mouseClickMove(int mx, int my, int btn, long dt) {
    	updateMouse(mx, my);
    	if(btn == 0) {
    		long time = GameTimer.getAbsTime();
        	if(Math.abs(time - dt - lastStartTime) > DRAG_TIME_TOLE) {
        		lastStartTime = time;
        		draggingNode = getTopWidget(mx, my);
        		if(draggingNode == null)
        			return false;
        		xOffset = mx - draggingNode.x;
        		yOffset = my - draggingNode.y;
        	}
        	if(draggingNode != null) {
        	    lastDragTime = time;
        		draggingNode.postEvent(new DragEvent());
        		return true;
        	}
    	}
    	return false;
    }
    
    /**
	 * Standard GUI mouseClicked callback.
	 * @param mx
	 * @param my
	 * @param btn the mouse button ID.
	 * @return if any action was performed on a widget.
	 */
	public boolean mouseClicked(int mx, int my, int bid) {
		updateMouse(mx, my);
		eventBus.postEvent(null, new GlobalMouseEvent(mx, my, bid));
		
		if(bid == 0) {
			Widget node = getTopWidget(mx, my);
			if(node != null) {
				gainFocus(node);
				node.postEvent(new MouseDownEvent((mx - node.x) / node.scale, (my - node.y) / node.scale));
				return true;
			} else {
				removeFocus();
			}
		}
		
		return false;
	}
	
	public void removeFocus() {
		if(focus != null) {
			focus.postEvent(new LostFocusEvent());
			focus = null;
		}
	}
	
	/**
	 * Gain a widget's focus with force.
	 */
	public void gainFocus(Widget node) {
		if(node == focus) {
			return;
		}
		if(focus != null) {
			removeFocus();
		}
		focus = node;
		focus.postEvent(new GainFocusEvent());
	}
	
	public void keyTyped(char ch, int key) {
		if(focus != null) {
			focus.postEvent(new KeyEvent(ch, key));
		}
	}
    
    //---Helper Methods---
	public Widget getTopWidget(double x, double y) {
		return gtnTraverse(x, y, null, this);
	}
	
	public Widget getHoveringWidget() {
		return getTopWidget(mouseX, mouseY);
	}
	
    public void updateDragWidget() {
    	if(draggingNode != null) {
    		moveWidgetToAbsPos(draggingNode, mouseX - xOffset, mouseY - yOffset);
    	}
    }
    
    /**
     * Inverse calculation. Move this widget to the ABSOLUTE window position (x0, y0).
     * Note that the widget's position may be further changed because of its parent widget's position change.
     */
    public void moveWidgetToAbsPos(Widget widget, double x0, double y0) {
    	Transform transform = widget.transform;
		double tx, ty;
		double tw, th;
		double parentScale;
		
		if(widget.isWidgetParent()) {
			Widget p = widget.getWidgetParent();
			tx = p.x;
			ty = p.y;
			tw = p.transform.width * p.scale;
			th = p.transform.height * p.scale;
			parentScale = p.scale;
			
			widget.scale = transform.scale * p.scale;
		} else {
			tx = ty = 0;
			tw = width;
			th = height;
			parentScale = 1;
			
			widget.scale = transform.scale;
		}
		
		double xx = 0;
		switch(transform.alignWidth) {
		case CENTER:
			xx = (x0 - tx  - (tw - transform.width * transform.scale) / 2) / parentScale;
			break;
		case LEFT:
			xx = (x0 - tx) / parentScale;
			break;
		case RIGHT:
			xx = (x0 - tx  - (tw - transform.width * transform.scale)) / widget.scale;
			break;
		}
		transform.x = xx;
		
		double yy = 0;
		switch(transform.alignHeight) {
		case CENTER:
			yy = (y0 - ty  - (tw - transform.width * transform.scale) / 2) / parentScale;
			break;
		case TOP:
			yy = (y0 - ty) / parentScale;
			break;
		case BOTTOM:
			yy = (y0 - ty  - (tw - transform.width * transform.scale)) / parentScale;
			break;
		}
		transform.y = yy;
		
		widget.x = x0;
		widget.y = y0;
		
		widget.dirty = true;
    }
    
    public Widget getDraggingWidget() {
        return Math.abs(GameTimer.getAbsTime() - lastDragTime) > DRAG_TIME_TOLE || 
        		draggingNode == null ? null : draggingNode;
    }
	
	public Widget getFocus() {
		return focus;
	}
	
	//---Key Handling
	
	//---Internal Processing
	public void updateWidget(Widget widget) {
		widget.gui = this;
		
		Transform transform = widget.transform;
		
		double tx, ty;
		double tw, th;
		double parentScale;
		if(widget.isWidgetParent()) {
			Widget p = widget.getWidgetParent();
			tx = p.x;
			ty = p.y;
			tw = p.transform.width * p.scale;
			th = p.transform.height * p.scale;
			parentScale = p.scale;
			
			widget.scale = transform.scale * p.scale;
		} else {
			tx = ty = 0;
			tw = width;
			th = height;
			
			parentScale = 1;
			widget.scale = transform.scale;
		}
		
		double x0 = 0;
		switch(transform.alignWidth) {
		case CENTER:
			x0 = tx + (tw - transform.width * widget.scale) / 2 + transform.x * parentScale;
			break;
		case LEFT:
			x0 = tx + transform.x * parentScale;
			break;
		case RIGHT:
			x0 = tx + (tw - transform.width * widget.scale) + transform.x * parentScale;
			break;
		}
		widget.x = x0;
		
		double y0 = 0;
		switch(transform.alignHeight) {
		case CENTER:
			y0 = ty + (th - transform.height * widget.scale) / 2 + transform.y * parentScale;
			break;
		case TOP:
			y0 = ty + transform.y * parentScale;
			break;
		case BOTTOM:
			y0 = ty + (th - transform.height * widget.scale) + transform.y * parentScale;
			break;
		}
		widget.y = y0;
		
		widget.dirty = false;
		
		//Check sub widgets
		for(Widget w : widget) {
			updateWidget(w);
		}
	}
	
	/**
	 * Generic checking.
	 */
	private void frameUpdate() {
		updateTraverse(null, this);
		this.update();
	}
	
	private void updateTraverse(Widget cur, WidgetContainer set) {
		if(cur != null) {
			if(cur.dirty) {
				cur.postEvent(new RefreshEvent());
				this.updateWidget(cur);
			}
		}
		
		Iterator<Widget> iter = set.iterator();
		while(iter.hasNext()) {
			Widget widget = iter.next();
			if(!widget.disposed) {
				updateTraverse(widget, widget);
				widget.update();
			}
		}
	}
	
	private void updateMouse(double mx, double my) {
		this.mouseX = mx;
		this.mouseY = my;
	}
	
	private void drawTraverse(double mx, double my, Widget cur, WidgetContainer set, Widget top) {
		try {
			if(cur != null && cur.isVisible()) {
				GL11.glPushMatrix();
				GL11.glTranslated(cur.x, cur.y, 0);
				
				double px = cur.transform.pivotX * cur.scale, py = cur.transform.pivotY * cur.scale;
				

				GL11.glScaled(cur.scale, cur.scale, 1);
				GL11.glTranslated(-cur.transform.pivotX, -cur.transform.pivotY, 0);
				
				GL11.glColor4d(1, 1, 1, 1); //Force restore color for any widget
				cur.postEvent(new FrameEvent((mx - cur.x) / cur.scale, (my - cur.y) / cur.scale, cur == top));
				GL11.glPopMatrix();
			}
		} catch(Exception e) {
			LIUtils.log.error("Error occured handling widget draw. instance class: " + cur.getClass().getName() + ", name: " + cur.getName());
			e.printStackTrace();
		}
		
		if(cur == null || cur.isVisible()) {
			Iterator<Widget> iter = set.iterator();
			while(iter.hasNext()) {
				Widget wn = iter.next();
				drawTraverse(mx, my, wn, wn, top);
			}
		}
	}
	
	protected Widget gtnTraverse(double x, double y, Widget node, WidgetContainer set) {
		Widget res = null;
		boolean checkSub = node == null || node.transform.doesDraw;
		if(node != null && node.transform.doesDraw
			&& node.transform.doesListenKey 
			&& node.isPointWithin(x, y)) {
			res = node;
		}
		
		if(!checkSub) return res;
		
		Widget next = null;
		for(Widget wn : set) {
			Widget tmp = gtnTraverse(x, y, wn, wn);
			if(tmp != null)
				next = tmp;
		}
		return next == null ? res : next;
	}

	@Override
	protected void onWidgetAdded(String name, Widget w) {
		w.gui = this;
		updateWidget(w);
	}
	
	public static void drawBlackout() {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		GLU.gluOrtho2D(1, 0, 1, 0);
		
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		
		GL11.glColor4d(0, 0, 0, 0.7);
		GL11.glTranslated(0, 0, 0);
		HudUtils.colorRect(0, 0, 1, 1);
		
		GL11.glPopMatrix();
		
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPopMatrix();
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glColor4d(1, 1, 1, 1);
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	}
	
	/**
	 * Event bus delegator, will post every widget inside this LIGui.
	 */
	public void postEvent(GuiEvent event) {
		eventBus.postEvent(null, event);
		for(Widget w : getDrawList()) {
			hierPostEvent(w, event);
		}
	}
	
	private void hierPostEvent(Widget w, GuiEvent event) {
		w.postEvent(event);
		for(Widget ww : w.widgetList) {
			hierPostEvent(ww, event);
		}
	}
}
