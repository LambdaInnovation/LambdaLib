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
package cn.lambdalib.cgui.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import cn.lambdalib.cgui.gui.component.Component;
import cn.lambdalib.cgui.gui.component.Transform;
import cn.lambdalib.cgui.gui.event.GuiEvent;
import cn.lambdalib.cgui.gui.event.GuiEventBus;
import cn.lambdalib.cgui.gui.event.IGuiEventHandler;
import org.apache.commons.lang3.StringUtils;


/**
 * @author WeathFolD
 */
public class Widget extends WidgetContainer {
	
	private GuiEventBus eventBus = new GuiEventBus();
	private List<Component> components = new ArrayList<>();
	
	public boolean disposed = false;
	public boolean dirty = true; //Indicate that this widget's pos data is dirty and requires update.

	LIGui gui;
	Widget parent;
	
	// Calculated absolute widget position and scale
	// Will only be updated if widget.dirty = true each frame
	public double x, y;
	public double scale;
	/**
	 * *INTERNAL*Used ONLY in editing gui.
	 */
	public boolean visible = true;
	
	/**
	 * Whether this widget can be copied when going down copy recursion process.
	 */
	public boolean needCopy = true;
	
	public Transform transform;
	
	//Transform is always present.
	{
		addComponent(transform = new Transform());
	}
	
	public Widget() {}

	// Ctors to aid syntax simplicity
	public Widget(double width, double height) {
		transform.setPos(width, height);
	}

	public Widget(double x, double y, double width, double height) {
		transform.setPos(x, y).setSize(width, height);
	}

	/**
	 * @return Whether the widget is visible (and called each draw frame).
	 */
	public boolean isVisible() {
		return transform.doesDraw && !dirty;
	}
		
	/**
	 * Return a reasonable copy of this widget. Retains all the properties and functions, 
	 * along with its all sub widgets recursively.
	 */
	public Widget copy() {
		Widget n = null;
		try {
			n = getClass().newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		copyInfoTo(n);
		return n;
	}
	
	protected void copyInfoTo(Widget n) {
		n.components.clear();
		
		n.transform = (Transform) transform.copy();
		n.addComponent(n.transform);
		
		n.eventBus = eventBus.copy();
		
		for(Component c : components) {
			if(c.getClass() != Transform.class)
				n.addComponent(c.copy());
		}
		
		//Also copy the widget's sub widgets recursively.
		for(Widget asub : getDrawList()) {
			if(asub.needCopy) n.addWidget(asub.getName(), asub.copy());
		}
	}
	
	/**
	 * Called when added into a GUI. Use this to do initialization.
	 */
	protected void onAdded() {}
	
	public boolean initialized() {
		return gui != null;
	}
	
	public boolean isWidgetParent() {
		return parent != null;
	}
	
	public Widget getWidgetParent() {
		return parent;
	}
	
	public LIGui getGui() {
		return gui;
	}
	
	/**
	 * Dispose this gui. Will get removed next frame.
	 */
	public void dispose() {
		disposed = true;
	}
	
	//Component handling
	/**
	 * Java generic type erasure makes this unsafe, so use at your own risk.
	 * @return the component with the name specified, or null if no such component.
	 */
	public <T extends Component> T getComponent(String name) {
		for(Component c : components) {
			if(c.name.equals(name))
				return (T) c;
		}
		return null;
	}

	/**
	 * Find the first component that is of the given type.
	 * @throws NoSuchElementException if there is no value present
	 */
	public <T extends Component> T getComponent(Class<T> type) {
		return (T) components.stream()
				.filter(c -> !type.isAssignableFrom(c.getClass()))
				.findFirst()
				.get();
	}
	
	public Widget addComponents(Component ...c) {
		for(Component x : c) {
			addComponent(x);
		}
		return this;
	}
	
	public Widget addComponent(Component c) {
		if(c.widget != null)
			throw new RuntimeException("Can't add one component into multiple widgets!");

		for(Component cc : components) {
			if(cc.name.equals(c.name)) {
				throw new RuntimeException("Duplicate component!");
			}
		}
		
		c.widget = this;
		components.add(c);
		c.onAdded();
		return this;
	}
	
	public void removeComponent(Component c) {
		removeComponent(c.name);
	}
	
	public void removeComponent(String name) {
		Iterator<Component> iter = components.iterator();
		while(iter.hasNext()) {
			Component c = iter.next();
			if(c.name.equals(name)) {
				c.onRemoved();
				c.widget = null;
				iter.remove();
				break;
			}
		}
	}
	
	/**
	 * Return the raw component list.
	 */
	public List<Component> getComponentList() {
		return (components);
	}
	
	//Event dispatch
	
	public <T extends GuiEvent> void listen(Class<? extends T> clazz, IGuiEventHandler<T> handler) {
		listen(clazz, handler, 0);
	}
	
	public <T extends GuiEvent> void listen(Class<? extends T> clazz, IGuiEventHandler<T> handler, int priority) {
		eventBus.listen(clazz, handler, priority);
	}
	
	public <T extends GuiEvent> void listen(Class<? extends T> clazz, IGuiEventHandler<T> handler, int priority, boolean copyable) {
		eventBus.listen(clazz, handler, priority, copyable);
	}
	
	public <T extends GuiEvent> void unlisten(Class<? extends T> clazz, IGuiEventHandler<T> handler) {
		eventBus.unlisten(clazz, handler);
	}
	
	/**
	 * Post a event to this widget's event bus.
	 * @param event
	 */
	public void post(GuiEvent event) {
		post(event, false);
	}
	
	/**
	 * Post a event to this widget's event bus (and all it's childs hierarchically, if tochild=true)
	 * @param event
	 * @param tochild If we should post event to all childs hierarchically
	 */
	public void post(GuiEvent event, boolean tochild) {
		eventBus.postEvent(this, event);
		if(tochild) {
			widgets
				.values()
				.stream()
				.filter(w -> !w.disposed)
				.forEach(w -> w.post(event, true));
		}
	}
	
	//Utils
	public String getName() {
		WidgetContainer parent = getAbstractParent();
		return parent == null ? "null" : parent.getWidgetName(this);
	}
	
	public boolean isPointWithin(double tx, double ty) {
		double w = transform.width, h = transform.height;
		double x1 = x + w * scale, y1 = y + h * scale;
		return (x <= tx && tx <x1) && (y <= ty && ty < y1);
	}
	
	public boolean isFocused() {
		return gui != null && this == gui.getFocus();
	}

	@Override
	protected void onWidgetAdded(String name, Widget w) {
		w.parent = this;
		w.gui = gui;
	}
	
	public int getHierarchyLevel() {
		int ret = 0;
		Widget cur = this;
		while(cur.isWidgetParent()) {
			cur = cur.getWidgetParent();
			++ret;
		}
		return ret;
	}
	
	public WidgetContainer getAbstractParent() {
		return isWidgetParent() ? parent : gui;
	}
	
	public boolean rename(String newName) {
		WidgetContainer parent = getAbstractParent();
		if(parent.hasWidget(newName))
			return false;
		getAbstractParent().renameWidget(getName(), newName);
		return true;
	}
	
	public boolean isChildOf(Widget another) {
		Widget cur = this.getWidgetParent();
		while(cur != null) {
			if(cur == another)
				return true;
			cur = cur.getWidgetParent();
		}
		return false;
	}
	
	public void gainFocus() {
		getGui().gainFocus(this);
	}
	
	@Override
	public String toString() {
		return this.getName() + "@" + this.getClass().getSimpleName();
	}

	interface IWidgetFormatter {
		String format(Widget w);
	}

	/**
	 * Get the widget's hierarchical structure for debugging.
	 */
	public String getHierarchyStructure() {
		return getHierarchyStructure(w -> w.toString());
	}

	public String getHierarchyStructure_pos() {
		return getHierarchyStructure(w -> String.format("%s: (%f,%f)[%f,%f]x%f", w.getName(),
				w.transform.x, w.transform.y,
				w.transform.width, w.transform.height,
				w.transform.scale));
	}

	public String getHierarchyStructure(IWidgetFormatter f) {
		return getHierarchyStructure_int(f, 0);
	}

	private String _rep(int times) {
		StringBuilder sb = new StringBuilder(times*2);
		while (times-- > 0) sb.append("  ");
		return sb.toString();
	}

	private String getHierarchyStructure_int(IWidgetFormatter f, int indent) {
		String istr = _rep(indent);
		StringBuilder ret = new StringBuilder();
		ret.append(istr).append(f.format(this));
		if(getDrawList().size() != 0) {
			ret.append(istr).append("{\n");
			getDrawList().forEach(w -> ret.append(w.getHierarchyStructure_int(f, indent + 1)).append(','));
			ret.append(istr).append("\n}");
		}
		return ret.toString();
	}

}
