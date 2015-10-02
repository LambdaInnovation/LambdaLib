package cn.liutils.vis.editor;

import java.util.List;

import com.google.gson.JsonElement;

/**
 * Each Vis editor works with an IEditorContext. the context provides the necessary
 * information to the editor, including:
 * <br>
 * <ol>
 * 	<li>What property it needs to edit</li>
 *  <li>What happens when we draw preview area</li>
 *  <li>How many preview modes do we have</li>
 * </ol>
 * And such.
 * @author WeAthFolD
 */
public interface IEditorContext {
	
	void initProps(PropFolder folder);
	void initPreviewModes(List<String> list);
	void drawPreviewArea(String mode);
	
	/**
	 * @return Convert the current editing object to its equivalent json representation, 
	 *  which can be saved into a new file, merged into existing json, and (likely)
	 *  later loaded from elsewhere.
	 */
	JsonElement toJson();
	
}
