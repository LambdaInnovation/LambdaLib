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
package cn.lambdalib.vis.editor.common;

import cn.lambdalib.cgui.gui.Widget;
import cn.lambdalib.cgui.gui.component.DrawTexture;
import cn.lambdalib.cgui.gui.component.TextBox;
import cn.lambdalib.cgui.gui.component.TextBox.ChangeContentEvent;
import cn.lambdalib.cgui.gui.component.TextBox.ConfirmInputEvent;
import cn.lambdalib.cgui.gui.event.LostFocusEvent;
import cn.lambdalib.core.LambdaLib;

/**
 * @author WeAthFolD
 */
public abstract class EditBox extends Widget {
	
	protected DrawTexture drawer;
	protected TextBox text;
	
	public EditBox() {
		drawer = new DrawTexture();
		drawer.texture = null;
		drawer.color = VEVars.C_WINDOW_BODY2;
		addComponent(drawer);
		
		transform.setSize(35, 10);
		
		text = new TextBox();
		text.allowEdit = true;
		text.setSize(9);
		addComponent(text);
		
		listen(LostFocusEvent.class, (w, event) -> 
		{
			Widget parent = w.getWidgetParent();
			if(parent != null)
				parent.post(event);
		});
		
		listen(ChangeContentEvent.class, (w, e) -> 
		{
			drawer.color = VEVars.C_MODIFIED;
		});
		
		listen(ConfirmInputEvent.class, (w, e) -> 
		{
			try {
				setValue(text.content);
				drawer.color = VEVars.C_WINDOW_BODY2;
				updateRepr();
			} catch(NumberFormatException exc) {
				drawer.color = VEVars.C_ERRORED;
			} catch(Exception exc) {
				drawer.color = VEVars.C_ERRORED;
				LambdaLib.log.error("ModifierBase.confirmInput()", exc);
			}
		});
	}
	
	@Override
	public void onAdded() {
		updateRepr();
	}
	
	private void updateRepr() {
		try {
			text.setContent(repr());
		} catch(Exception e) {
			LambdaLib.log.error("ModifierBase.onAdded()", e);
			text.setContent("<error>");
		}
	}
	
	protected abstract String repr() throws Exception;
	
	protected abstract void setValue(String content) throws Exception;

}
