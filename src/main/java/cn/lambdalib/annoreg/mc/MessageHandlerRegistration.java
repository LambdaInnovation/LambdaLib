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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import cn.lambdalib.annoreg.base.RegistrationClassSimple;
import cn.lambdalib.annoreg.core.LoadStage;
import cn.lambdalib.annoreg.core.RegModInformation;
import cn.lambdalib.annoreg.core.RegistryTypeDecl;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

@RegistryTypeDecl
public class MessageHandlerRegistration extends RegistrationClassSimple<RegMessageHandler, IMessageHandler> {
    
    public MessageHandlerRegistration() {
        super(RegMessageHandler.class, "MessageHandler");
        this.setLoadStage(LoadStage.INIT);
        
        //Set this annotation to prepare for calling getModField.
        helper.setModFieldAnnotation(RegMessageHandler.WrapperInstance.class);
    }
    
    private <REQ extends IMessage> void register(Class<?> handler, Class<REQ> msg, Side side) {
        Class<? extends IMessageHandler<REQ, IMessage>> messageHandler = (Class<? extends IMessageHandler<REQ, IMessage>>) handler;
        SimpleNetworkWrapper wrapper = (SimpleNetworkWrapper) helper.getModField();
        int id = helper.getNextIDForMod();
        //System.out.println(msg + " -> " + id);
        wrapper.registerMessage(messageHandler, msg, id, side);
    }
    
    @Override
    protected void register(Class theClass, RegMessageHandler anno) throws Exception {
        Class<? extends IMessage> msg = (Class<? extends IMessage>) anno.msg();
        for (RegMessageHandler.Side side : anno.side()) {
            switch (side) {
            case CLIENT:
                register(theClass, msg, Side.CLIENT);
                break;
            case SERVER:
                register(theClass, msg, Side.SERVER);
                break;
            }
        }
    }

}
