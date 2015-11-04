package cn.liutils.cgui.gui.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cn.liutils.cgui.gui.Widget;

public final class GuiEventBus {
	
	private class NodeCollection extends LinkedList<GuiHandlerNode> {
		
		List<GuiHandlerNode> toadd = new ArrayList();
		List<Object> toremove = new ArrayList();
		
		boolean iterating;
		
		@Override
		public boolean add(GuiHandlerNode node) {
			if(iterating) {
				toadd.add(node);
				return true;
			} else {
				return super.add(node);
			}
		}
		
		@Override
		public boolean remove(Object o) {
			if(iterating) {
				toremove.add(o);
				return true;
			} else {
				return super.remove(o);
			}
		}
		
		void startIterating() { iterating = true; }
		
		void endIterating() {
			iterating = false;
			addAll(toadd);
			removeAll(toremove);
			toadd.clear();
			toremove.clear();
		}
		
	}
	
	public GuiEventBus() {}
	
	Map< Class<? extends GuiEvent>, NodeCollection > eventHandlers = new HashMap();
	
	public final void postEvent(Widget widget, GuiEvent event) {
		NodeCollection list = eventHandlers.get(event.getClass());
		if(list != null) {
			list.startIterating();
			for(GuiHandlerNode n : list) {
				n.handler.handleEvent(widget, event);
			}
			list.endIterating();
		}
	}
	
	public <T extends GuiEvent> void listen(Class<? extends T> clazz, IGuiEventHandler<T> handler) {
		listen(clazz, handler, 0);
	}
	
	public <T extends GuiEvent> void listen(Class<? extends GuiEvent> clazz, IGuiEventHandler handler, int priority) {
		listen(clazz, handler, priority, true);
	}
	
	public <T extends GuiEvent> void listen(Class<? extends GuiEvent> clazz, IGuiEventHandler handler, int priority, boolean copyable) {
		NodeCollection list = getRawList(clazz);
		// Perform gracefully if handler is duplicated. This allows safe copy.
		for(GuiHandlerNode n : list) {
			if(n.handler == handler)
				return;
		}
		list.add(new GuiHandlerNode(handler, priority, copyable));
	}
	
	public <T extends GuiEvent> void unlisten(Class<? extends GuiEvent> clazz, IGuiEventHandler<T> handler) {
		getRawList(clazz).remove(new GuiHandlerNode(handler, 0, false));
	}
	
	private NodeCollection getRawList(Class<? extends GuiEvent> clazz) {
		NodeCollection ret = eventHandlers.get(clazz);
		if(ret == null) {
			eventHandlers.put(clazz, ret = new NodeCollection());
		}
		
		return ret;
	}
	
	/**
	 * Copies (or clones) the event bus. 
	 * The cloned event bus will retain all copyable event handlers of the previous bus, as reference.
	 */
	public GuiEventBus copy() {
		GuiEventBus ret = new GuiEventBus();
		
		for(Entry< Class<? extends GuiEvent>, NodeCollection> ent : eventHandlers.entrySet()) {
			NodeCollection list = ret.getRawList(ent.getKey());
			for(GuiHandlerNode n : ent.getValue()) {
				if(n.copySensitive) {
					list.add(n);
				}
			}
		}
		return ret;
	}
	
	private class GuiHandlerNode implements Comparable<GuiHandlerNode> {
		final IGuiEventHandler handler;
		final int priority;
		final boolean copySensitive;
		
		public GuiHandlerNode(IGuiEventHandler _handler, int _priority, boolean _copySensitive) {
			handler = _handler;
			priority = _priority;
			copySensitive = _copySensitive;
		}
		
		@Override
		public boolean equals(Object another) {
			return another instanceof GuiHandlerNode && ((GuiHandlerNode) another).handler == handler;
		}
		
		@Override
		public int hashCode() {
			return handler.hashCode();
		}

		@Override
		public int compareTo(GuiHandlerNode handler) {
			return ((Integer) priority).compareTo(handler.priority);
		}
	}
	
}
