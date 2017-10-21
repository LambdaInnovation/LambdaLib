///**
//* Copyright (c) Lambda Innovation, 2013-2016
//* This file is part of LambdaLib modding library.
//* https://github.com/LambdaInnovation/LambdaLib
//* Licensed under MIT, see project root for more information.
//*/
//package cn.lambdalib.cgui.loader.xml;
//
//import java.io.File;
//import java.util.Map;
//
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.transform.Transformer;
//import javax.xml.transform.TransformerFactory;
//import javax.xml.transform.dom.DOMSource;
//import javax.xml.transform.stream.StreamResult;
//
//import cn.lambdalib.cgui.gui.CGui;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//
//import cn.lambdalib.cgui.gui.Widget;
//import cn.lambdalib.cgui.gui.WidgetContainer;
//import cn.lambdalib.cgui.gui.component.Component;
//
///**
// * @author WeAthFolD
// */
//@Deprecated
//public class CGUIDocWriter {
//
//    public static CGUIDocWriter instance = new CGUIDocWriter();
//
//    DocumentBuilderFactory dbf;
//
//    DocumentBuilder db;
//
//    WidgetContainer container = new WidgetContainer();
//
//    public CGUIDocWriter() {
//        dbf = DocumentBuilderFactory.newInstance();
//        try {
//            db = dbf.newDocumentBuilder();
//        } catch (ParserConfigurationException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void clearContent() {
//        container.clear();
//    }
//
//    public void feed(CGui gui) {
//        for(Map.Entry<String, Widget> entry : gui.getEntries()) {
//            container.addWidget(entry.getKey(), entry.getValue());
//        }
//    }
//
//    public boolean saveToXml(File dest) {
//        try {
//            Document doc = db.newDocument();
//
//            Element root = doc.createElement("Widgets");
//            doc.appendChild(root);
//
//            for(Map.Entry<String, Widget> entry : container.getEntries()) {
//                root.appendChild(createWidgetNode(doc, entry.getKey(), entry.getValue()));
//            }
//
//            TransformerFactory transformerFactory = TransformerFactory.newInstance();
//            Transformer transformer = transformerFactory.newTransformer();
//            DOMSource source = new DOMSource(doc);
//            StreamResult result =  new StreamResult(dest);
//            transformer.transform(source, result);
//
//            return true;
//        } catch(Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    private Element createWidgetNode(Document doc, String name, Widget w) {
//        Element root = doc.createElement("Widget");
//
//        root.setAttribute("name", name);
//
//        //Write components
//        Element element = doc.createElement("Components");
//        for(Component c : w.getComponentList()) {
//            if(!c.canStore()) continue;
//
//            Element e = doc.createElement("Component");
//            e.setAttribute("class", c.getClass().getName());
//
//            //Add properties
//            Map<String, String> props = c.getPropertyMap();
//            for(Map.Entry<String, String> ent : props.entrySet()) {
//                Element a = doc.createElement(ent.getKey());
//                a.appendChild(doc.createTextNode(ent.getValue()));
//                e.appendChild(a);
//            }
//
//            element.appendChild(e);
//        }
//        root.appendChild(element);
//
//        //Write sub widgets
//        for(Widget child : w.getDrawList()) {
//            root.appendChild(createWidgetNode(doc, child.getName(), child));
//        }
//
//        return root;
//    }
//
//    public static boolean save(CGui gui, File file) {
//        instance.clearContent();
//        instance.feed(gui);
//        return instance.saveToXml(file);
//    }
//
//}
