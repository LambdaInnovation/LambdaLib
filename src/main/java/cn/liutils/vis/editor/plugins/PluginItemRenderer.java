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
package cn.liutils.vis.editor.plugins;

import cn.annoreg.core.Registrant;
import cn.liutils.cgui.gui.LIGuiScreen;
import cn.liutils.util.mc.PlayerUtils;
import cn.liutils.vis.editor.IVisPluginCommand;
import cn.liutils.vis.editor.common.widget.WindowHierarchy;
import cn.liutils.vis.editor.registry.RegVisPluginCommand;
import cn.liutils.vis.editor.util.EditorHelper;
import cn.liutils.vis.model.renderer.ItemModelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.MinecraftForgeClient;

/**
 * @author WeAthFolD
 */
@Registrant
@RegVisPluginCommand("item_renderer")
public class PluginItemRenderer implements IVisPluginCommand {

	@Override
	public void onCommand(ICommandSender ics, String[] args) {
		ItemStack cur = Minecraft.getMinecraft().thePlayer.getCurrentEquippedItem(); 
		if(cur == null) {
			PlayerUtils.sendChat(ics, "No equipped item");
		} else {
			Item item = cur.getItem();
			IItemRenderer renderer = MinecraftForgeClient.getItemRenderer(cur, ItemRenderType.EQUIPPED_FIRST_PERSON);
			if(renderer != null && renderer instanceof ItemModelRenderer) {
				Minecraft.getMinecraft().displayGuiScreen(new Gui((ItemModelRenderer) renderer));
			} else {
				PlayerUtils.sendChat(ics, "No ItemModelRenderer associated with given item was found.");
			}
		}
		
	}
	
	private static class Gui extends LIGuiScreen {
		
		Gui(ItemModelRenderer r) {
			WindowHierarchy window = new WindowHierarchy();
			EditorHelper.initHierarchy(window, r);
			gui.addWidget(window);
		}
		
	}

}
