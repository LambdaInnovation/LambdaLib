package cn.liutils.cgui.gui.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import cn.liutils.cgui.gui.Widget;

public final class GuiEventBus {
	
	public GuiEventBus() {}
	
	Map< Class<? extends GuiEvent>, LinkedList<GuiHandlerNode> > eventHandlers = new HashMap();
	
	public final void postEvent(Widget widget, GuiEvent event) {
		List<GuiHandlerNode> list = eventHandlers.get(event.getClass());
		if(list != null) {
			for(GuiHandlerNode n : list) {
				n.handler.handleEvent(widget, event);
			}
		}
	}
	
	public void regEventHandler(GuiEventHandler handler) {
		reg(handler.getEventClass(), handler);
	}
	
	public void regAtBeginning(GuiEventHandler handler) {
		regAtBeginning(handler.getEventClass(), handler);
	}
	
	public void reg(Class<? extends GuiEvent> clazz, IGuiEventHandler handler) {
		getRawList(clazz).add(new GuiHandlerNode(handler));
	}
	
	public void regAtBeginning(Class<? extends GuiEvent> clazz, IGuiEventHandler handler) {
		getRawList(clazz).addFirst(new GuiHandlerNode(handler));
	}
	
	public void remove(GuiEventHandler handler) {
		getRawList(handler.getEventClass()).remove(new GuiHandlerNode(handler));
	}
	
	public void remove(Class<? extends GuiEvent> clazz, IGuiEventHandler handler) {
		getRawList(clazz).remove(new GuiHandlerNode(handler));
	}
	
	private LinkedList<GuiHandlerNode> getRawList(Class<? extends GuiEvent> clazz) {
		LinkedList<GuiHandlerNode> ret = eventHandlers.get(clazz);
		if(ret == null) {
			eventHandlers.put(clazz, ret = new LinkedList());
		}
		
		return ret;
	}
	
	public GuiEventBus copy() {
		GuiEventBus ret = new GuiEventBus();
		
		for(Entry< Class<? extends GuiEvent>, LinkedList<GuiHandlerNode>> ent : eventHandlers.entrySet()) {
			ret.getRawList(ent.getKey()).addAll(ent.getValue());
		}
		return ret;
	}
	
	private class GuiHandlerNode {
		IGuiEventHandler handler;
		
		public GuiHandlerNode(IGuiEventHandler handler) {
			this.handler = handler;
		}
		
		@Override
		public boolean equals(Object another) {
			return another instanceof GuiHandlerNode && ((GuiHandlerNode) another).handler == handler;
		}
		
		@Override
		public int hashCode() {
			return handler.hashCode();
		}
	}
	
}
