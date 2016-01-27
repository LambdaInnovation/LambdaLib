/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.mc;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.annoreg.mc.RegEventHandler;
import cn.lambdalib.annoreg.mc.RegEventHandler.Bus;
import cn.lambdalib.annoreg.mc.RegInitCallback;
import cn.lambdalib.core.LambdaLib;
import cn.lambdalib.util.generic.RegistryUtils;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.IntHashMap;

/**
 * This class overrides(i.e.Disables) vanilla minecraft's control on a certain key. 
 * It is currently intented to be used DURING gameplay and will unlock all overrides when any GUI is present.
 * There are two ways to use ControlOverrider: activate/deactivate a key manually, or add key event filters into it.
 * @author WeAthFolD
 */
@SideOnly(Side.CLIENT)
@Registrant
@RegEventHandler(Bus.Forge)
public class ControlOverrider {
    
    private static IntHashMap kbMap;
    private static Field pressedField;
    private static Field kbMapField;
    
    private static Map<Integer, Override> activeOverrides = new HashMap<>();
    
    private static boolean completeOverriding;

    @RegInitCallback
    public static void init() {
        try {
            kbMapField = RegistryUtils.getObfField(KeyBinding.class, "hash", "field_74514_b");
            kbMap = getOriginalKbMap();
            
            pressedField = RegistryUtils.getObfField(KeyBinding.class, "pressed", "field_74513_e");
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(kbMapField, kbMapField.getModifiers() & (~Modifier.FINAL));
        } catch(Exception e) {
            throw error("init", e);
        }
    }
    
    private static IntHashMap createCopy(IntHashMap from, IntHashMap to) {
        if(to == null)
            to = new IntHashMap();
        // Awkward, but who knows if this is faster than reflection?
        for(int i = -100; i <= 250; ++i) {
            if(from.containsItem(i))
                to.addKey(i, from.lookup(i));
        }
        return to;
    }
    
    private static IntHashMap getOriginalKbMap() {
        try {
            return (IntHashMap) kbMapField.get(null);
        } catch (Exception e) {
            throw error("getOriginalKbMap", e);
        }
    }
    
    // SUPERHACKTECH Starts
    /**
     * A complete override stops ALL minecraft keys to function. Currently you can open up complete override globally only once.
     */
    public static void startCompleteOverride() {
        if(!completeOverriding) {
            completeOverriding = true;
            kbMap = createCopy(kbMap, null);
            getOriginalKbMap().clearMap();
        }
    }
    
    public static void endCompleteOverride() {
        if(completeOverriding) {
            completeOverriding = false;
            createCopy(kbMap, getOriginalKbMap());
            kbMap = getOriginalKbMap();
        } else {
            throw error("Try to stop complete override while not overriding at all");
        }
    }
    // SUPERHACKTECH Ends
    
    public static void override(int keyID) {
        if(activeOverrides.containsKey(keyID)) {
            activeOverrides.get(keyID).count++;
            if(activeOverrides.get(keyID).count > 100)
                LambdaLib.log.warn("Over 100 override locks for " + 
                        keyID + ". Might be a programming error?");
            log("Override increment " + "[" + keyID + "]" + activeOverrides.get(keyID).count);
            return;
        }
        
        KeyBinding kb = (KeyBinding) kbMap.removeObject(keyID);
        if(kb != null) {
            try {
                pressedField.set(kb, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //kb.setKeyCode(-1);
            activeOverrides.put(keyID, new Override(kb));
            log("Override new [" + keyID + "]");
        } else {
            log("Override ignored [" + keyID + "]");
        }
    }
    
    public static void removeOverride(int keyID) {
        Override ovr = activeOverrides.get(keyID);
        if(ovr == null)
            return;
        
        if(ovr.count > 1) {
            ovr.count--;
            
            log("Override decrement [" + keyID + "]" + ovr.count);
        } else {
            activeOverrides.remove(keyID);
            
            ovr.kb.setKeyCode(keyID);
            kbMap.addKey(keyID, ovr.kb);
            
            log("Override remove [" + keyID + "]");
        }
    }
    
    private static void releaseLocks() {
        for(Map.Entry<Integer, Override> ao: activeOverrides.entrySet()) {
            kbMap.addKey(ao.getKey(), ao.getValue().kb);
        }
    }
    
    private static void restoreLocks() {
        for(Map.Entry<Integer, Override> ao: activeOverrides.entrySet()) {
            try {
                pressedField.set(ao.getValue().kb, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            kbMap.removeObject(ao.getKey());
        }
    }
    
    GuiScreen lastTickGui;
    @SubscribeEvent
    public void onClientTick(ClientTickEvent cte) {
        GuiScreen cgs = Minecraft.getMinecraft().currentScreen;
        if(lastTickGui == null && cgs != null) {
            releaseLocks();
        }
        if(lastTickGui != null && cgs == null) {
            restoreLocks();
        }
        lastTickGui = cgs;
    }
    
    private static void log(String s) {
        if(LambdaLib.DEBUG)
            LambdaLib.log.info(s);
    }

    private static RuntimeException error(String s) {
        return new RuntimeException("ControlOverrider error: " + s);
    }
    
    private static RuntimeException error(String s, Exception e) {
        return new RuntimeException("ControlOverrider error: " + s, e);
    }
    
    private static class Override {
        final KeyBinding kb;
        int count;
        
        public Override(KeyBinding _kb) {
            kb = _kb;
            count = 1;
        }
    }
}
