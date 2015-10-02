package cn.liutils.vis.editor;

import java.util.List;

import cn.liutils.cgui.gui.Widget;
import net.minecraft.util.ResourceLocation;

public interface IEditorProp<T> {
	
	ResourceLocation getIcon();
	Widget createPropEditor(T object);
	List<IEditorProp> getChilds();
	
}
