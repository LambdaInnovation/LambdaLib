package cn.liutils.cgui.gui.event;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

import cn.liutils.cgui.gui.Widget;

public final class GuiEventBus {
	
	private class NodeCollection extends LinkedList<GuiHandlerNode> {
		
		boolean dirty;
		
	}
	
	public GuiEventBus() {}
	
	Map< Class<? extends GuiEvent>, NodeCollection > eventHandlers = new HashMap();
	
	public final void postEvent(Widget widget, GuiEvent event) {
		NodeCollection list = eventHandlers.get(event.getClass());
		if(list != null) {
			if(list.dirty) {
				list.dirty = false;
				list.sort((GuiHandlerNode g0, GuiHandlerNode g1) -> 
					((Integer) g0.priority).compareTo(g1.priority)
				);
			}
			for(GuiHandlerNode n : list) {
				n.handler.handleEvent(widget, event);
			}
		}
	}
	
	public <T extends GuiEvent> void reg(Class<? extends T> clazz, IGuiEventHandler<T> handler) {
		reg(clazz, handler, 0);
	}
	
	public <T extends GuiEvent> void reg(Class<? extends GuiEvent> clazz, IGuiEventHandler handler, int priority) {
		NodeCollection list = getRawList(clazz);
		list.add(new GuiHandlerNode(handler, priority));
		list.dirty = true;
	}
	
	public <T extends GuiEvent> void remove(Class<? extends GuiEvent> clazz, IGuiEventHandler<T> handler) {
		getRawList(clazz).remove(new GuiHandlerNode(handler, 0));
	}
	
	private NodeCollection getRawList(Class<? extends GuiEvent> clazz) {
		NodeCollection ret = eventHandlers.get(clazz);
		if(ret == null) {
			eventHandlers.put(clazz, ret = new NodeCollection());
		}
		
		return ret;
	}
	
	public GuiEventBus copy() {
		GuiEventBus ret = new GuiEventBus();
		
		for(Entry< Class<? extends GuiEvent>, NodeCollection> ent : eventHandlers.entrySet()) {
			ret.getRawList(ent.getKey()).addAll(ent.getValue());
		}
		return ret;
	}
	
	private class GuiHandlerNode {
		final IGuiEventHandler handler;
		final int priority;
		
		public GuiHandlerNode(IGuiEventHandler _handler, int _priority) {
			handler = _handler;
			priority = _priority;
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
