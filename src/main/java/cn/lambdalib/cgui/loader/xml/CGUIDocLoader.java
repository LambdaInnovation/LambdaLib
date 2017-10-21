/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.cgui.loader.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import cn.lambdalib.cgui.gui.CGui;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cn.lambdalib.cgui.gui.Widget;
import cn.lambdalib.cgui.gui.WidgetContainer;
import cn.lambdalib.cgui.gui.component.Component;
import cn.lambdalib.cgui.gui.component.Transform;
import cn.lambdalib.core.LambdaLib;
import cn.lambdalib.util.generic.RegistryUtils;

/**
 * @author WeAthFolD
 */
//@Deprecated
//public class CGUIDocLoader {
//
//    private static CGUIDocLoader instance = new CGUIDocLoader();
//
//    DocumentBuilderFactory dbf;
//
//    DocumentBuilder db;
//
//    public CGUIDocLoader() {
//        dbf = DocumentBuilderFactory.newInstance();
//        dbf.setIgnoringElementContentWhitespace(true);
//        try {
//            db = dbf.newDocumentBuilder();
//        } catch (ParserConfigurationException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public CGui loadXml(String xml) throws Exception {
//        return loadXml(new ReaderInputStream(new StringReader(xml)));
//    }
//
//    public CGui loadXml(InputStream xml) throws Exception {
//        CGui retval = new CGui();
//
//        Document doc = db.parse(xml);
//        Element root = doc.getDocumentElement();
//        NodeList nl = root.getChildNodes();
//        for(int i = 0; i < nl.getLength(); ++i) {
//            Node node = (Node) nl.item(i);
//            if(node.getNodeName().equals("Widget")) {
//                parseWidget(nl.item(i), retval);
//            }
//        }
//
//        return retval;
//    }
//
//    private void parseWidget(Node widgetNode, WidgetContainer target) {
//        Widget val = new Widget();
//        val.getComponentList().clear();
//
//        NodeList nl = widgetNode.getChildNodes();
//        String name = ((Element) widgetNode).getAttribute("name");
//        for(int i = 0; i < nl.getLength(); ++i) {
//            Node node = nl.item(i);
//            switch(node.getNodeName()) {
//            case "Components":
//                parseComponents(node, val);
//                break;
//            case "Widget":
//                parseWidget(node, val);
//                break;
//            }
//        }
//        if(name == null) {
//            throw new RuntimeException("No name specified for the widget");
//        }
////        System.out.println("Add " + name + " to " + (target instanceof CGui ? "CGui" : ((Widget)target).getName()));
//        target.addWidget(name, val);
//    }
//
//    private void parseComponent(Node n, Widget target) {
//        NodeList nl = n.getChildNodes();
//        Map<String, String> propertyMap = new HashMap();
//        String clazz = ((Element)n).getAttribute("class");
//        for(int i = 0; i < nl.getLength(); ++i) {
//            Node n2 = nl.item(i);
//            propertyMap.put(n2.getNodeName(), n2.getTextContent());
//        }
//        if(clazz == null) {
//            throw new RuntimeException("didn't find one of the component class when parsing CGUI document.");
//        }
//
//        Component c = null;
//        try {
//            c = (Component) Class.forName(clazz).newInstance();
//        } catch(Exception e) {
//            LambdaLib.log.error("An error occured trying to instantiate class " + clazz, e);
//        }
//
//        c.fromPropertyMap(propertyMap);
//        target.addComponent(c);
//        if(c.name.equals("Transform")) {
//            target.transform = (Transform) c;
//        }
//    }
//
//    private void parseComponents(Node n, Widget target) {
//        Element e = (Element) n;
//        NodeList nl = e.getElementsByTagName("Component");
//        for(int i = 0; i < nl.getLength(); ++i) {
//            parseComponent(nl.item(i), target);
//        }
//    }
//
//    public static CGui load(String xml) {
//        try {
//            return instance.loadXml(xml);
//        } catch (Exception e) {
//            LambdaLib.log.error("An error occured when loading CGUI document.", e);
//        }
//        return null;
//    }
//
//    public static CGui load(ResourceLocation xml) {
//        String str = null;
//        try {
//            str = IOUtils.toString(RegistryUtils.getResourceStream(xml));
//        } catch (IOException e) {
//            LambdaLib.log.error("An error occured when loading CGUI document.", e);
//        }
//        return load(str);
//    }
//
//}
