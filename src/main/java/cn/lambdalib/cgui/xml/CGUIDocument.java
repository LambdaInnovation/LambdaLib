/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.cgui.xml;

import cn.lambdalib.cgui.gui.Widget;
import cn.lambdalib.cgui.gui.WidgetContainer;
import cn.lambdalib.cgui.gui.component.Component;
import cn.lambdalib.cgui.gui.component.Transform;
import cn.lambdalib.core.LambdaLib;
import cn.lambdalib.util.generic.RegistryUtils;
import cn.lambdalib.util.convert.DOMSerialization;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CGUI Doc reader and writer.
 */
public enum CGUIDocument {
    instance;
    // API
    /**
     * Reads a CGUI Document from given InputStream.
     */
    public static WidgetContainer read(InputStream in) throws IOException,SAXException {
        return instance.readInternal(instance.db.parse(in));
    }

    /**
     * Reads a CGUI Document from given ResourceLocation.
     */
    public static WidgetContainer read(ResourceLocation location) throws IOException,SAXException {
        return read(RegistryUtils.getResourceStream(location));
    }

    /**
     * Reads a CGUI Document from given File.
     */
    public static WidgetContainer read(File location) throws IOException, SAXException {
        FileInputStream ifs = null;
        try {
            ifs = new FileInputStream(location);
            return read(ifs);
        } finally {
            if (ifs != null)
                ifs.close();
        }
    }

    /**
     * Reads a CGUI document, and crashes the game if not present.
     * Preferable when using in mod.
     */
    public static WidgetContainer panicRead(ResourceLocation location) {
        try {
            return read(location);
        } catch(Exception e) {
            throw new RuntimeException("Can't read CGUI document", e);
        }
    }

    /**
     * Writes the given CGUI document to the output stream. The stream is to be closed by the user.
     */
    public static void write(WidgetContainer container, OutputStream out) throws IOException {
        Document doc = instance.db.newDocument();
        instance.writeInternal(container, doc);
        instance.writeDoc(out, doc);
    }

    /**
     * Writes the given CGUI document to the given file.
     */
    public static void write(WidgetContainer container, File dest) throws IOException {
        FileOutputStream ofs = null;
        try {
            ofs = new FileOutputStream(dest);
            write(container, ofs);
        } finally {
            if (ofs != null) {
                ofs.close();
            }
        }
    }

    // IMPL
    private static final String TAG_WIDGET = "Widget", TAG_COMPONENT = "Component";

    public final DOMSerialization converter = new DOMSerialization();
    private final DocumentBuilder db;
    private final Logger log = LambdaLib.log;

    CGUIDocument() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setIgnoringElementContentWhitespace(true);
        try {
            db = dbf.newDocumentBuilder();
        } catch (Exception e) {
            log.error("Can't create DocumentBuilder", e);
            throw new RuntimeException(e);
        }
    }

    private List<Node> toStdList(NodeList l) {
        List<Node> ret = new ArrayList<>();
        for (int i = 0; i < l.getLength(); ++i) {
            ret.add (l.item(i));
        }
        return ret;
    }

    private WidgetContainer readInternal(Document doc) throws IOException {
        WidgetContainer ret = new WidgetContainer();
        Node root = doc.getFirstChild();
        if (root == null || !root.getNodeName().equals("Root"))
            throw new IOException("Root widget invalid");
        toStdList(root.getChildNodes())
                .stream()
                .filter(n -> n.getNodeName().equalsIgnoreCase(TAG_WIDGET))
                .forEach(n -> readWidget(ret, (Element) n));
        return ret;
    }

    /**
     * Reads a widget from given node and add it into the container.
     */
    private void readWidget(WidgetContainer container, Element node) {
        Widget w = new Widget();
        String name = node.getAttribute("name");
        toStdList(node.getChildNodes()).stream()
                .forEach(n ->
                {
                    switch (n.getNodeName()) {
                    case TAG_WIDGET:
                        readWidget(w, (Element) n);
                        break;
                    case TAG_COMPONENT:
                        Optional<Component> comp = readComponent((Element) n);
                        comp.ifPresent(c -> {
                            if (c.name.equals("Transform")) { // Currently Transform needs special treatment
                                w.removeComponent("Transform");
                                w.transform = (Transform) c;
                            }
                            w.addComponent(c);
                        });
                        break;
                    }
                });

        if (!container.addWidget(name, w)) {
            log.warn("Name clash while reading widget: %s, it is ignored.", name);
        }
    }

    private Optional<Component> readComponent(Element node) {
        try {
            Class<? extends Component> klass = (Class<? extends Component>) Class.forName(node.getAttribute("class"));
            return Optional.of(converter.convertFrom(klass, node));
        } catch (Exception e) {
            log.error("Failed reading component", e);
            return Optional.empty();
        }
    }

    private void writeInternal(WidgetContainer container, Document doc) {
        Element root = doc.createElement("Root");
        // Use drawList to preserve order
        container.getDrawList().forEach(widget -> {
            Element elem = doc.createElement(TAG_WIDGET);
            writeWidget(widget.getName(), widget, elem);
            root.appendChild(elem);
        });
        doc.appendChild(root);
    }

    private void writeWidget(String name, Widget w, Element dst) {
        Document doc = dst.getOwnerDocument();
        dst.setAttribute("name", name);
        w.getComponentList().forEach(c -> dst.appendChild(writeComponent(c, doc)));
        w.getDrawList().forEach(child -> {
            Element elem = doc.createElement(TAG_WIDGET);
            writeWidget(child.getName(), child, elem);
            dst.appendChild(elem);
        });
    }

    private Node writeComponent(Component component, Document doc) {
        Element ret = (Element) converter.convertTo(component, TAG_COMPONENT, doc);
        ret.setAttribute("class", component.getClass().getCanonicalName());
        return ret;
    }

    private void writeDoc(OutputStream dst, Document doc) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(dst);
            transformer.transform(source, result);
        } catch (Exception e) {
            log.error("Can't write CGUI document", e);
        }
    }


}
