/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.mc;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.annoreg.mc.RegEventHandler;
import cn.lambdalib.annoreg.mc.RegEventHandler.Bus;
import cn.lambdalib.annoreg.mc.RegInitCallback;
import cn.lambdalib.core.LambdaLib;
import cn.lambdalib.util.generic.RegistryUtils;
import com.google.common.base.Throwables;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.IntHashMap;

/**
 * Overrides (disables) key in vanilla minecraft.
 * Use {@link #override(String, int...)} to add an override group, and use
 * {@link #endOverride(String)} to end one.
 * Complete override (all keys) is also supported.
 * @author WeAthFolD
 */
@SideOnly(Side.CLIENT)
@Registrant
public class ControlOverrider {
    
    private static IntHashMap kbMap;
    private static Field pressedField;
    private static Field kbMapField;
    
    private static Map<Integer, Override> activeOverrides = new HashMap<>();
    private static Map<String, OverrideGroup> overrideGroups = new HashMap<>();
    
    private static boolean completeOverriding;

    @RegInitCallback
    private static void init() {
        try {
            kbMapField = RegistryUtils.getObfField(KeyBinding.class, "hash", "field_74514_b");
            kbMap = getOriginalKbMap();
            
            pressedField = RegistryUtils.getObfField(KeyBinding.class, "pressed", "field_74513_e");
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(kbMapField, kbMapField.getModifiers() & (~Modifier.FINAL));
        } catch(Exception e) {
            Throwables.propagate(e);
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
            throw Throwables.propagate(e);
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

    /**
     * Activates an override group. The previous group with given name is ended.
     */
    public static void override(String name, int... keys) {
        overrideGroups.put(name, new OverrideGroup(keys));
        rebuild();
    }

    /**
     * Ends an override group.
     */
    public static void endOverride(String name) {
        Optional.of(overrideGroups.get(name)).ifPresent(OverrideGroup::end);
    }

    private static void rebuild() {
        clearInternal();

        Set<Integer> keys = new HashSet<>();

        Collection<OverrideGroup> groups = overrideGroups.values();
        Iterator<OverrideGroup> iter = groups.iterator();
        while (iter.hasNext()) {
            OverrideGroup group = iter.next();
            if (group.ended) {
                iter.remove();
            } else {
                for (int i : group.keys) {
                    keys.add(i);
                }
            }
        }

        for (int keyid : keys) {
            KeyBinding kb = (KeyBinding) kbMap.removeObject(keyid);
            if(kb != null) {
                try {
                    pressedField.set(kb, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //kb.setKeyCode(-1);
                activeOverrides.put(keyid, new Override(kb));
                log("Override new [" + keyid + "]");
            } else {
                log("Override ignored [" + keyid + "]");
            }
        }
    }

    private static void clearInternal() {
        activeOverrides.entrySet().forEach(entry -> {
            Override ovr = entry.getValue();
            int keyid = entry.getKey();

            ovr.kb.setKeyCode(keyid);
            kbMap.addKey(keyid, ovr.kb);
            log("Override remove [" + keyid + "]");
        });

        activeOverrides.clear();
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
                Throwables.propagate(e);
            }
            kbMap.removeObject(ao.getKey());
        }
    }
    
    private static GuiScreen lastTickGui;
    
    private static void log(String s) {
        if(LambdaLib.DEBUG)
            LambdaLib.log.info(s);
    }

    private static RuntimeException error(String s) {
        return new RuntimeException("ControlOverrider error: " + s);
    }
    
    private static class Override {
        final KeyBinding kb;
        
        public Override(KeyBinding _kb) {
            kb = _kb;
        }
    }

    /**
     * A group of key overrides with lifetime.
     */
    private static final class OverrideGroup {
        private final int[] keys;
        private boolean ended = false;

        public OverrideGroup(int... _keys) {
            keys = _keys;
        }

        public void end() {
            ended = true;
            ControlOverrider.rebuild();
        }
    }

    @Registrant
    @SideOnly(Side.CLIENT)
    public enum Events {
        @RegEventHandler(Bus.Forge)
        instance_;

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

        @SubscribeEvent
        public void onDisconnect(ClientDisconnectionFromServerEvent evt) {
            if (SideHelper.isClient()) {
                clearInternal();
                endCompleteOverride();
                overrideGroups.clear();
            }
        }
    }
}