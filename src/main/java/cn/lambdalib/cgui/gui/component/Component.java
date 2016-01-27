/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.cgui.gui.component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.lambdalib.cgui.gui.Widget;
import cn.lambdalib.cgui.gui.annotations.CopyIgnore;
import cn.lambdalib.cgui.gui.event.GuiEvent;
import cn.lambdalib.cgui.gui.event.IGuiEventHandler;
import cn.lambdalib.util.deprecated.TypeHelper;
import cn.lambdalib.s11n.CopyHelper;
import cn.lambdalib.s11n.SerializeExcluded;
import cn.lambdalib.vis.editor.VisExcluded;

/**
 * <summary>
 * Component is attached to Widget. It can define a set of EventHandlers and store information by itself.
 * </summary>
 * <p>
 * Components supports prototype patteren natively. They can be copied to make duplicates, typically when its 
 *     container widget is being copied.
 * </p>
 * @author WeAthFolD
 */
public class Component {
    
    public final String name;
    
    public boolean enabled = true;

    /**
     * Whether this component can be edited in editor inspector.
     */
    @VisExcluded
    @SerializeExcluded
    public boolean canEdit = true;
    
    /**
     * The widget that this component is attached to. To ease impl and usage, this is exposed as
     *  public field, but DONT assign it, else it yields undefined behaviour.
     */
    @VisExcluded
    @SerializeExcluded
    public Widget widget;
    
    public Component(String _name) {
        name = _name;
        checkCopyFields();
    }
    
    public <T extends GuiEvent> void listen(Class<? extends T> type, IGuiEventHandler<T> handler) {
        listen(type, handler, 0);
    }
    
    public <T extends GuiEvent> void listen(Class<? extends T> type, IGuiEventHandler<T> handler, int prio) {
        if(widget != null)
            throw new RuntimeException("Can only add event handlers before componenet is added into widget");
        Node n = new Node();
        n.type = type;
        n.handler = new EHWrapper<>(handler);
        n.prio = prio;
        addedHandlers.add(n);
    }
    
    /**
     * Called when the component is added into a widget, and the widget field is correctly set.
     */
    public void onAdded() {
        for(Node n : addedHandlers) {
            widget.listen(n.type, n.handler, n.prio, false);
        }
    }
    
    public void onRemoved() {
        for(Node n : addedHandlers) {
            widget.unlisten(n.type, n.handler);
        }
    }
    
    public Component copy() {
        return CopyHelper.instance.copy(this);
    }
    
    public boolean canStore() {
        return true;
    }
    
    // Obsolete copy methods, preserved until old GUIs are all converted.
    @Deprecated
    public void fromPropertyMap(Map<String, String> map) {
        List<Field> fields = checkCopyFields();
        for(Field f : fields) {
            String val = map.get(f.getName());
            if(val != null) {
                TypeHelper.edit(f, this, val);
            }
        }
    }
    
    @Deprecated
    public Map<String, String> getPropertyMap() {
        Map<String, String> ret = new HashMap<>();
        for(Field f : checkCopyFields()) {
            String val = TypeHelper.repr(f, this);
            if(val != null) {
                ret.put(f.getName(), val);
            }
        }
        
        return ret;
    }

    @Deprecated
    public Collection<Field> getPropertyList() {
        return copiedFields.get(getClass());
    }

    @Deprecated
    private List<Field> checkCopyFields() {
        if(copiedFields.containsKey(getClass()))
            return copiedFields.get(getClass());
        List<Field> ret = new ArrayList<Field>();
        for(Field f : getClass().getFields()) {
            if(((f.getModifiers() & Modifier.FINAL) == 0)
            && !f.isAnnotationPresent(CopyIgnore.class) && TypeHelper.isTypeSupported(f.getType())) {
                ret.add(f);
            }
        }
        copiedFields.put(getClass(), ret);
        return ret;
    }

    @Deprecated
    private static Map<Class, List<Field>> copiedFields = new HashMap<>();

    // Obsolete end
    
    private List<Node> addedHandlers = new ArrayList<>();
    
    private final class EHWrapper<T extends GuiEvent> implements IGuiEventHandler<T> {
        
        final IGuiEventHandler<T> wrapped;

        public EHWrapper(IGuiEventHandler<T> _wrapped) {
            wrapped = _wrapped;
        }
        
        @Override
        public void handleEvent(Widget w, T event) {
            if(enabled)
                wrapped.handleEvent(w, event);
        }
        
    }
    
    private static class Node {
        Class<? extends GuiEvent> type;
        IGuiEventHandler handler;
        int prio;
    }
    
}

