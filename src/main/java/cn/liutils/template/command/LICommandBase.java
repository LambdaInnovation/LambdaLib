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
package cn.liutils.template.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentTranslation;

/**
 * @author WeAthFolD
 *
 */
public abstract class LICommandBase extends CommandBase {
	
	public LICommandBase() {
	}
	
	public static void sendChat(ICommandSender ics, String st, Object ...pars) {
		ics.addChatMessage(new ChatComponentTranslation(st, pars));
	}
	
	public static void sendError(ICommandSender ics, String st) {
		ics.addChatMessage(new ChatComponentTranslation(st));
	}
	
	public static void sendWithTranslation(ICommandSender ics, String unlStr, Object... args) {
		ics.addChatMessage(new ChatComponentTranslation(unlStr, args));
	}

}
