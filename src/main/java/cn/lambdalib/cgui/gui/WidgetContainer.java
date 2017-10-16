/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.cgui.gui;

import java.util.*;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;


/**
 * A class that has capability to store widgets. Used by CGui and Widget.
 * Every widget is associated with a name. You can use that name to lookup a widget.
 * @author WeAthFolD
 */
public class WidgetContainer implements Iterable<Widget> {
    
    HashBiMap<String, Widget> widgets = HashBiMap.create();
    LinkedList<Widget> widgetList = new LinkedList<>(); //List sorted in non-descending widget zOrder.
    
    private static final String UNNAMED_PRE = "Unnamed ";
    
    /**
     * Deep copy.
     */
    public void addAll(WidgetContainer container) {
        for (Widget w : container.getDrawList()) {
            addWidget(w.getName(), w.copy());
        }
    }
    
    /**
     * Walk the widget list and check their states. This should be called explicitly from tick check events.
     */
    protected void update() {
        Iterator<Widget> iter = widgetList.iterator();
        while(iter.hasNext()) {
            Widget w = iter.next();
            if(w.disposed) {
                iter.remove();
                widgets.inverse().remove(w);
            }
        }
    }
    
    /**
     * @throws NullPointerException if the widget wasn't found.
     */
    public void renameWidget(String name, String newName) {
        Widget w = widgets.remove(name);
        if(w == null)
            throw new NullPointerException();
        widgets.put(newName, w);
    }
    
    public Set<Map.Entry<String, Widget>> getEntries() {
        return widgets.entrySet();
    }
    
    public boolean addWidget(Widget add) {
        return addWidget(getNextName(), add);
    }
    
    public boolean addWidget(String name, Widget add) {
        return addWidget(name, add, false);
    }
    
    public boolean addWidget(Widget add, boolean begin) {
        return addWidget(getNextName(), add, begin);
    }
    
    /**
     * Add a widget into the container.
     * @param name
     * @param add
     * @param begin If true the widget will be add at the begin of the draw list. (Draw first), otherwise the last.
     * @return if the operation is successful. (False for id duplication)
     */
    public boolean addWidget(String name, Widget add, boolean begin) {
        if(!checkInit(name, add))
            return false;
        
        if(begin)
            widgetList.addFirst(add);
        else
            widgetList.add(add);
        
        checkAdded(name, add);
        return true;
    }

    public boolean addWidgetAfter(Widget add, Widget pivot) {
        return addWidgetAfter(getNextName(), add, pivot);
    }
    
    public boolean addWidgetAfter(String name, Widget add, Widget pivot) {
        int index = widgetList.indexOf(pivot);
        if(index == -1)
            return false;
        if(!checkInit(name, add))
            return false;
        
        widgetList.add(index + 1, add);
        checkAdded(name, add);
        return true;
    }

    public boolean addWidgetBefore(Widget add, Widget pivot) {
        return addWidgetBefore(getNextName(), add, pivot);
    }
    
    public boolean addWidgetBefore(String name, Widget add, Widget pivot) {
        int index = widgetList.indexOf(pivot);
        if(index == -1)
            index = 0;
        if(!checkInit(name, add))
            return false;
        
        widgetList.add(index, add);
        checkAdded(name, add);
        return true;
    }
    
    private boolean checkInit(String name, Widget add) {
        //Check duplicate
        if(widgets.containsKey(name)) {
            Widget w = widgets.get(name);
            if(!w.disposed) {
                return false;
            }
            widgets.remove(name); // Remove the previously disposed widget
        }

        if(widgets.containsValue(add)) {
            widgets.inverse().remove(add);
        }
        
        add.disposed = false; // Reset the dispose flag
        add.dirty = true; // Force update
        widgets.put(name, add);
        return true;
    }
    
    private void checkAdded(String name, Widget add) {
        onWidgetAdded(name, add);
        add.abstractParent = this;
        add.onAdded();
    }
    
    public void clear() {
        widgets.clear();
        widgetList.clear();
    }
    
    public Widget getWidget(int i) {
        return widgetList.get(i);
    }
    
    public int locate(Widget w) {
        return widgetList.indexOf(w);
    }
    
    /**
     * Callback when a widget was loaded. Allows sub class to do
     * some specific data setup.
     */
    protected void onWidgetAdded(String name, Widget w) {}
    
    /**
     * This method supports recursive searching.
     * For example, you can use "a/b" to get the subWidget named 'b' of a in this
     * widget container.
     * @param name Widget name
     * @return The widget with this name.
     */
    public Widget getWidget(String name) {
        int ind = name.indexOf('/');
        if(ind == -1) {
            return widgets.get(name);
        } else if(ind != name.length() - 1){
            String cp = name.substring(0, ind);
            String ep = name.substring(ind + 1);
            Widget w = widgets.get(cp);
            return w == null ? null : w.getWidget(ep);
        } else {
            return null;
        }
    }
    
    /**
     * Check if a widget with given name exists.
     * @param name Widget name
     * @return If the widget exists
     */
    public boolean hasWidget(String name) {
        Widget w = getWidget(name);
        return w != null && !w.disposed;
    }
    
    /**
     * Remove a widget from container.
     */
    public void removeWidget(String name) {
        Widget w = widgets.get(name);
        if(w != null) {
            removeWidget(w);
        }
    }
    
    public void removeWidget(Widget w) {
        w.dispose();
        //w.gui = null;
        w.parent = null;
    }
    
    public void forceRemoveWidget(Widget w) {
        if(w.getAbstractParent() != this)
            return;
        widgets.remove(w.getName());
        widgetList.remove(w);
        w.gui = null;
        w.parent = null;
    }
    
    /**
     * Get the id of the widget, provided that the widget is in this container.
     * @return Name of the widget, or null if it's not in this container.
     */
    public String getWidgetName(Widget w) {
        return widgets.inverse().get(w);
    }
    
    /**
     * Assign a new name for the widget.
     */
    public void changeWidgetName(Widget w, String newName) {
        widgets.inverse().put(w, newName);
    }

    /**
     * @return An immutable list of all widgets in this WidgetContainer in draw order.
     */
    public List<Widget> getDrawList() {
        return ImmutableList.copyOf(widgetList);
    }

    public int widgetCount() {
        return widgetList.size();
    }

    /**
     * Reorder the given widget before the pivot. The target and pivot must be all within this container,
     *     or the result is undefined.
     * @param target The target to reorder
     * @param pivot if null, put at the beginning of the draw list, otherwise after the given pivot.
     */
    public void reorder(Widget target, Widget pivot) {
        widgetList.remove(target);
        ListIterator<Widget> litr = widgetList.listIterator();
        if (pivot == null) {
            litr.add(target);
        } else {
            while (litr.hasNext()) {
                Widget w = litr.next();
                if (w == pivot) {
                    litr.add(target);
                    break;
                }
            }
        }
    }

    /**
     * Reorder the given widget to before the element at the given index, in view of the draw list before reordering.
     * @throws NoSuchElementException if newIndex > size
     */
    public void reorder(Widget target, int newIndex) {
        int prevIndex = widgetList.indexOf(target);
        widgetList.remove(prevIndex);
        if (newIndex > prevIndex) {
            widgetList.add(newIndex - 1, target);
        } else {
            widgetList.add(newIndex, target);
        }
    }
    
    public Iterator<Widget> iterator() {
        return getDrawList().iterator();
    }
    
    /**
     * Get a next free, auto-generated name for the widget.
     */
    public String getNextName() {
        String res;
        int nameCount = 0;
        do {
            res = UNNAMED_PRE + (nameCount++);
        } while(hasWidget(res));
        return res;
    }
    
}
