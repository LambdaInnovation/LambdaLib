package cn.lambdalib.cgui.loader.xml;

import cn.lambdalib.cgui.gui.WidgetContainer;
import cn.lambdalib.util.generic.RegistryUtils;
import cn.lambdalib.vis.editor.DOMConversion;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * CGUI Doc reader and writer.
 */
public class CGUIDocument {

	public static final DOMConversion converter = new DOMConversion();

	/**
	 * Reads a CGUI Document from given InputStream.
	 */
	public static WidgetContainer read(InputStream in) {
		// TODO
		return null;
	}

	/**
	 * Reads a CGUI Document from given ResourceLocation.
	 */
	public static WidgetContainer read(ResourceLocation location) {
		return read(RegistryUtils.getResourceStream(location));
	}

	public static void write(WidgetContainer container, OutputStream out) {
	}

	public static void write(WidgetContainer container, File dest) {}


}
