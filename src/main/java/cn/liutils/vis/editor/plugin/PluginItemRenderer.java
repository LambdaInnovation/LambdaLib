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
package cn.liutils.vis.editor.plugin;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cn.annoreg.core.Registrant;
import cn.liutils.cgui.gui.LIGuiScreen;
import cn.liutils.util.mc.PlayerUtils;
import cn.liutils.vis.editor.IVisPluginCommand;
import cn.liutils.vis.editor.common.EditorHelper;
import cn.liutils.vis.editor.common.widget.Toolbar;
import cn.liutils.vis.editor.common.widget.WindowHierarchy;
import cn.liutils.vis.editor.registry.RegVisPluginCommand;
import cn.liutils.vis.gson.GsonAdapters;
import cn.liutils.vis.model.CompTransform;
import cn.liutils.vis.model.renderer.ItemModelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.MinecraftForgeClient;

/**
 * @author WeAthFolD
 */
@Registrant
@RegVisPluginCommand("item_renderer")
public class PluginItemRenderer implements IVisPluginCommand {
	
	private static final Gson gson =
		new GsonBuilder()
			.registerTypeAdapter(Vec3.class, GsonAdapters.vec3Adapter)
			.registerTypeAdapter(CompTransform.class, GsonAdapters.compTransformAdapter)
			.registerTypeAdapter(ResourceLocation.class, GsonAdapters.resourceLocationAdapter)
			.create();

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
			
			Toolbar toolbar = new Toolbar();
			toolbar.addButton("json", "Json->Clipboard", () -> { 
				try {
					saveClipboardContent(ItemModelRenderer.baseAdapter.toJson(r));
				} catch (Exception e) {
					throw new RuntimeException("Saving to json", e);
				} 
				PlayerUtils.sendChat(Minecraft.getMinecraft().thePlayer, "Copied edit data to as JSON to clipboard");
			});
			gui.addWidget(toolbar);
		}
		
		private void saveClipboardContent(String content) {
			Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			StringSelection ss = new StringSelection(content);
			cb.setContents(ss, ss);
		}
		
	}

}
