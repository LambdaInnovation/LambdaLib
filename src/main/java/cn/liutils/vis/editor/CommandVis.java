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
package cn.liutils.vis.editor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import cn.annoreg.core.Registrant;
import cn.annoreg.mc.RegCommand;
import cn.liutils.template.command.LICommandBase;
import net.minecraft.command.ICommandSender;

/**
 * @author WeAthFolD
 */
@Registrant
@RegCommand
public class CommandVis extends LICommandBase {
	
	private static final String
		USAGE = "/vis <pluginName> [parameters...]: Open the vis editor with the given plugin.";
	
	private static Map<String, IVisPluginCommand> cmds = new HashMap();
	
	public static void register(String id, IVisPluginCommand command) {
		if(cmds.containsKey(id))
			throw new RuntimeException("Plugin with name " + id + " already exists!");
		cmds.put(id, command);
	}

	@Override
	public String getCommandName() {
		return "vis";
	}

	@Override
	public String getCommandUsage(ICommandSender ics) {
		return USAGE;
	}
	
	private void sendAvailable(ICommandSender ics) {
		sendChat(ics, "Current available commands:");
		for(String str : cmds.keySet()) {
			sendChat(ics, str);
		}
	}

	@Override
	public void processCommand(ICommandSender ics, String[] pars) {
		if(pars.length == 0) {
			sendChat(ics, USAGE);
			sendAvailable(ics);
			return;
		}
		
		if(cmds.containsKey(pars[0])) {
			String[] args = Arrays.copyOfRange(pars, 1, pars.length);
			cmds.get(pars[0]).onCommand(ics, args);
		} else {
			sendChat(ics, "There is no plugin with name '" + pars[0] + "'.");
			sendAvailable(ics);
		}
	}

}
