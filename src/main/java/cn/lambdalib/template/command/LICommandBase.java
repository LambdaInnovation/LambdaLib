/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.template.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.TextComponentTranslation;

/**
 * @author WeAthFolD
 *
 */
public abstract class LICommandBase extends CommandBase {
    
    public LICommandBase() {
    }
    
    public static void sendChat(ICommandSender ics, String st, Object ...pars) {
        ics.sendMessage(new TextComponentTranslation(st, pars));
    }
    
    public static void sendError(ICommandSender ics, String st) {
        ics.sendMessage(new TextComponentTranslation(st));
    }
    
    public static void sendWithTranslation(ICommandSender ics, String unlStr, Object... args) {
        ics.sendMessage(new TextComponentTranslation(unlStr, args));
    }

}
