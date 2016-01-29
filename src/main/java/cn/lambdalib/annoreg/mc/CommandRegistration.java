/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
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
