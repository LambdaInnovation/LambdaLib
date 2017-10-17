/**
 * Copyright (c) Lambda Innovation, 2013-2016
 * This file is part of LambdaLib modding library.
 * https://github.com/LambdaInnovation/LambdaLib
 * Licensed under MIT, see project root for more information.
 */
package cn.lambdalib.util.convert;

import cn.lambdalib.cgui.gui.component.Component;
import cn.lambdalib.core.LLCommons;
import cn.lambdalib.s11n.SerializationHelper;
import cn.lambdalib.util.client.font.Fonts;
import cn.lambdalib.util.client.font.IFont;
import com.sun.istack.internal.NotNull;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

import net.minecraft.util.ResourceLocation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * DOM serialization used to read and write XML documents.
 * It also create component and assign it.
 * Rewrite by Paindar
 */
public class DOMSerialization
{

    @FunctionalInterface
    interface Backwarder<T>
    {
        T convert(Class<T> c, Node node);
    }

    @FunctionalInterface
    interface Forwarder<F1, T>
    {
        T convert(F1 from, Node node);
    }

    @FunctionalInterface
    interface FuncRef2Bool<F1, Boolean>
    {
        Boolean convert(F1 from);
    }

    private SerializationHelper serHelper = new SerializationHelper();
    private Map<FuncRef2Bool, Forwarder> forwarders = new HashMap<>();
    private Map<Class<?>, Backwarder> backwarders = new HashMap<>();


    /**
     * ? + Document -> Element
     */
    public <T> Element convertTo(T obj, String name, Document doc)
    {
        Element ret;
        for (Map.Entry<FuncRef2Bool, Forwarder> entry : forwarders.entrySet())
        {
            if ((boolean) entry.getKey().convert(obj))
            {
                ret = doc.createElement(name);
                entry.getValue().convert(obj, ret);
                return ret;
            }
        }
        return forwardDefault(obj, name, doc);
    }

    private <T> Element forwardDefault(T obj, String name, Document doc)
    {
        Element ret = doc.createElement(name);
        List<Field> fields = serHelper.getExposedFields(obj.getClass());
        for (Field f : fields)
        {
            Object o;
            try
            {
                o = f.get(obj);
            } catch (IllegalAccessException e)
            {
                e.printStackTrace();
                throw new RuntimeException("Fail to create instance:" + f.getName());
            }
            if (o != null)
            {
                ret.appendChild(convertTo(o, f.getName(), doc));
            } else
            {
                Element nullNode = doc.createElement(f.getName());
                nullNode.setAttribute("isNull", "true");
                ret.appendChild(nullNode);
            }
        }
        return ret;
    }


    /**
     * Convert from a Node value to any value you want, if it can be converted.
     *
     * @param klass
     * @param node
     * @param <T>
     * @return
     */
    public <T> T convertFrom(Class<T> klass, Node node)
    {
        Backwarder<T> bw = backwarders.get(klass);
        if (bw != null)
        {
            return bw.convert(klass, node);
        }
        return backwardDefault(klass, node);
    }

    /**
     * Called by {@function convertFrom}, where it cannot find any Backwarder to return a value.
     * It will return a default value, if possible.
     *
     * @param klass
     * @param node
     * @return
     */
    public <T, U extends T> T backwardDefault(Class<U> klass, Node node)
    {
        if (klass.isEnum())
        {
            String content = node.getTextContent();
            for (Object obj : klass.getEnumConstants())
            {
                if (obj.toString().equals(content))
                {
                    return (T) obj;
                }
            }
        }

        Object ret;
        try
        {
            ret = klass.newInstance();
        } catch (InstantiationException | IllegalAccessException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Fail to create an instance: " + klass);
        }
        List<Field> fields = serHelper.getExposedFields(klass);
        NodeList childs = node.getChildNodes();
        for (Field f : fields)
        {
            for (int i = 0; i < childs.getLength(); i++)
            {
                if (childs.item(i) instanceof Element && childs.item(i).getNodeName().equals(f.getName()))
                {
                    Element elem = ((Element) childs.item(i));
                    String attr = elem.getAttribute("isNull");
                    try
                    {
                        f.set(ret, (attr.isEmpty()) ? null : convertFrom(f.getType(), elem));
                    } catch (IllegalAccessException e)
                    {
                        e.printStackTrace();
                        throw new RuntimeException("Fail to set attr:" + attr + " to " + elem);
                    }
                }
            }

        }
        return (T) ret;
    }

    private <T> void bw(Function<String, T> parseMethod, Class<T> evidence)
    {
        backwarders.put(evidence.getClass(), (c, n) -> parseMethod.apply(n.getTextContent()));
    }

    private Node addText(Node node, String content) {
        return node.appendChild(node.getOwnerDocument().createTextNode(content));
    }

    private void addForward(FuncRef2Bool cond, Forwarder forwarder){
        forwarders.put(cond,forwarder);
    }
    private void addForwardType(Forwarder forwarder, Class...classes ){
        addForward(obj ->{
            for(Class klass:classes){
                if (klass.equals(obj))
                    return true;
            }
            return false;
        }, forwarder);
    }
    private void init()
    {

        // Primitive types
        addForwardType((obj, node) -> addText(node, obj.toString()),
                char.class, Character.class,int.class,Integer.class,float.class,Float.class,
                double.class,Double.class,boolean.class,Boolean.class,String.class,ResourceLocation.class);
        addForward(c->c.getClass().isEnum(), (obj, node) -> addText(node, obj.toString()));
        addForward(ifn->ifn instanceof IFont, (obj, node) -> addText(node, Fonts.getName((IFont) obj)));

        bw(s -> s.charAt(0), Character.class);
        bw(Integer::parseInt, int.class);
        bw(Integer::valueOf, Integer.class);
        bw(Float::parseFloat, float.class);
        bw(Float::valueOf, Float.class);
        bw(Double::parseDouble, double.class);
        bw(Double::valueOf, Double.class);
        bw(Boolean::parseBoolean, boolean.class);
        bw(Boolean::valueOf, Boolean.class);
        bw(String::valueOf, String.class);
        bw(ResourceLocation::new, ResourceLocation.class);
        bw(str -> {
            if (Fonts.exists(str))
            {
                return Fonts.get(str);
            } else
            {
                LLCommons.log.warn("Can't find font with name " + str + " while loading. Plase check your installed mods.");
                return Fonts.getDefault();
            }
        },IFont.class);
    }

    {
        init();
    }
}
