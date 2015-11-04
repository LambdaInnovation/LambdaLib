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
package cn.lambdalib.cgui.loader.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.minecraft.util.ResourceLocation;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cn.lambdalib.cgui.gui.LIGui;
import cn.lambdalib.cgui.gui.Widget;
import cn.lambdalib.cgui.gui.WidgetContainer;
import cn.lambdalib.cgui.gui.component.Component;
import cn.lambdalib.cgui.gui.component.Transform;
import cn.lambdalib.cgui.loader.EventLoader;
import cn.lambdalib.core.LambdaLib;
import cn.lambdalib.util.generic.RegistryUtils;

/**
 * @author WeAthFolD
 */
public class CGUIDocLoader {
	
	private static CGUIDocLoader instance = new CGUIDocLoader();
	
	DocumentBuilderFactory dbf;
	
	DocumentBuilder db;
	
	public CGUIDocLoader() {
		dbf = DocumentBuilderFactory.newInstance();
		dbf.setIgnoringElementContentWhitespace(true);
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public LIGui loadXml(String xml) throws Exception {
		return loadXml(new ReaderInputStream(new StringReader(xml)));
	}
	
	public LIGui loadXml(InputStream xml) throws Exception {
		LIGui retval = new LIGui();
		
		Document doc = db.parse(xml);
		Element root = doc.getDocumentElement();
		NodeList nl = root.getChildNodes();
		for(int i = 0; i < nl.getLength(); ++i) {
			Node node = (Node) nl.item(i);
			if(node.getNodeName().equals("Widget")) {
				parseWidget(nl.item(i), retval);
			}
		}
		
		return retval;
	}
	
	private void parseWidget(Node widgetNode, WidgetContainer target) {
		Widget val = new Widget();
		val.getComponentList().clear();
		
		NodeList nl = widgetNode.getChildNodes();
		String name = ((Element) widgetNode).getAttribute("name");
		for(int i = 0; i < nl.getLength(); ++i) {
			Node node = nl.item(i);
			switch(node.getNodeName()) {
			case "Components":
				parseComponents(node, val);
				break;
			case "Widget":
				parseWidget(node, val);
				break;
			}
		}
		if(name == null) {
			throw new RuntimeException("No name specified for the widget");
		}
//		System.out.println("Add " + name + " to " + (target instanceof LIGui ? "LIGui" : ((Widget)target).getName()));
		target.addWidget(name, val);
	}
	
	private void parseComponent(Node n, Widget target) {
		NodeList nl = n.getChildNodes();
		Map<String, String> propertyMap = new HashMap();
		String clazz = ((Element)n).getAttribute("class");
		for(int i = 0; i < nl.getLength(); ++i) {
			Node n2 = nl.item(i);
			propertyMap.put(n2.getNodeName(), n2.getTextContent());
		}
		if(clazz == null) {
			throw new RuntimeException("didn't find one of the component class when parsing CGUI document.");
		}
		
		Component c = null;
		try {
			c = (Component) Class.forName(clazz).newInstance();
		} catch(Exception e) {
			LambdaLib.log.error("An error occured trying to instantiate class " + clazz, e);
		}
		
		c.fromPropertyMap(propertyMap);
		target.addComponent(c);
		if(c.name.equals("Transform")) {
			target.transform = (Transform) c;
		}
	}
	
	private void parseComponents(Node n, Widget target) {
		Element e = (Element) n;
		NodeList nl = e.getElementsByTagName("Component");
		for(int i = 0; i < nl.getLength(); ++i) {
			parseComponent(nl.item(i), target);
		}
	}
	
	public static LIGui load(String xml) {
		try {
			return instance.loadXml(xml);
		} catch (Exception e) {
			LambdaLib.log.error("An error occured when loading CGUI document.", e);
		}
		return null;
	}
	
	public static LIGui load(ResourceLocation xml) {
		String str = null;
		try {
			str = IOUtils.toString(RegistryUtils.getResourceStream(xml));
		} catch (IOException e) {
			LambdaLib.log.error("An error occured when loading CGUI document.", e);
		}
		return load(str);
	}
	
	public static LIGui load(String xml, Object callbackProvider) {
		LIGui ret = load(xml);
		if(ret == null) return null;
		EventLoader.load(ret, callbackProvider);
		return ret;
	}
	
}
