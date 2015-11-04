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
package cn.lambdalib.annoreg.mc;

import java.lang.annotation.Annotation;

import cn.lambdalib.annoreg.base.RegistrationClassRepeat;
import cn.lambdalib.annoreg.core.LoadStage;
import cn.lambdalib.annoreg.core.RegModInformation;
import cn.lambdalib.annoreg.core.RegistryTypeDecl;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.server.MinecraftServer;

@RegistryTypeDecl
public class CommandRegistration extends RegistrationClassRepeat<RegCommand, ICommand> {

	public CommandRegistration() {
		super(RegCommand.class, "Command");
		this.setLoadStage(LoadStage.START_SERVER);
	}

	@Override
	protected void register( Class<? extends ICommand> theClass, RegCommand anno) throws Exception {
		CommandHandler ch = (CommandHandler) MinecraftServer.getServer().getCommandManager();
		ch.registerCommand(theClass.newInstance());
	}
}
