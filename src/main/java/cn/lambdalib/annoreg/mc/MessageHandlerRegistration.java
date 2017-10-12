/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.annoreg.mc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import cn.lambdalib.annoreg.base.RegistrationClassSimple;
import cn.lambdalib.annoreg.core.LoadStage;
import cn.lambdalib.annoreg.core.RegistryTypeDecl;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@RegistryTypeDecl
public class MessageHandlerRegistration extends RegistrationClassSimple<RegMessageHandler, IMessageHandler>
{
    
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
