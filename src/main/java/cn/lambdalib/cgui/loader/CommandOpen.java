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
package cn.lambdalib.cgui.loader;

import java.io.File;
import java.io.FileInputStream;

import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

import org.apache.commons.io.IOUtils;

import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.annoreg.mc.RegCommand;
import cn.lambdalib.cgui.client.CGUILang;
import cn.lambdalib.cgui.gui.LIGui;
import cn.lambdalib.cgui.loader.ui.GuiEdit;
import cn.lambdalib.cgui.loader.xml.CGUIDocLoader;
import cn.liutils.template.command.LICommandBase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author WeAthFolD
 */
@Registrant
@RegCommand
@SideOnly(Side.CLIENT)
public class CommandOpen extends LICommandBase {

	@Override
	public String getCommandName() {
		return "cgui";
	}

	@Override
	public String getCommandUsage(ICommandSender p_71518_1_) {
		return CGUILang.commUsage();
	}

	@Override
	public void processCommand(ICommandSender ics, String[] args) {
		if(args.length >= 1) {
			String path = "cgui/" + args[0];
			File file = new File(path);
			String xml = "";
			try {
				xml = IOUtils.toString(new FileInputStream(file));
			} catch (Exception e) {
				this.sendChat(ics, CGUILang.commFileNotFound());
				return;
			}
			LIGui gui = CGUIDocLoader.load(xml);
			Minecraft.getMinecraft().displayGuiScreen(new GuiEdit(path, gui));
		} else {
			Minecraft.getMinecraft().displayGuiScreen(new GuiEdit());
		}
	}

}
