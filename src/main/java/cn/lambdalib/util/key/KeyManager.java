/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.key;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import cn.lambdalib.util.client.ClientUtils;

/**
 * The instance of this class handles a set of KeyHandlers, and restore their key bindings
 * from a configuration. (If any)
 * @author WeAthFolD
 */
public class KeyManager {
    
    /**
     * The most commonly used KeyManager. Use this if you don't want to use any config on keys.
     */
    public static final KeyManager dynamic = new KeyManager();
    
    public static final int 
        MOUSE_LEFT = -100, MOUSE_MIDDLE = -98, MOUSE_RIGHT = -99,
        MWHEELDOWN = -50, MWHEELUP = -49;
    
    private boolean activated = true;
    
    public KeyManager() {
        activate();
        FMLCommonHandler.instance().bus().register(this);
    }
    
    public void deactivate() {
        activated = false;
    }
    
    public void activate() {
        activated = true;
    }
    
    Map<String, KeyBinding> nameMap = new HashMap();
    
    public void addKeyHandler(String name, int defKeyID, KeyHandler handler) {
        addKeyHandler(name, "", defKeyID, false, handler);
    }
    
    public void addKeyHandler(String name, String keyDesc, int defKeyID, KeyHandler handler) {
        addKeyHandler(name, keyDesc, defKeyID, false, handler);
    }
    
    private KeyBinding getKeyBinding(KeyHandler handler) {
        for(KeyBinding kb : nameMap.values()) {
            if(kb.handler == handler)
                return kb;
        }
        return null;
    }
    
    public int getKeyID(KeyHandler handler) {
        KeyBinding kb = getKeyBinding(handler);
        return kb == null ? -1 : kb.keyID;
    }
    
    /**
     * Add a key handler.
     * @param name
     * @param keyDesc Description of the key in the configuration file
     * @param defKeyID Default key ID in config file
     * @param global If global=true, this key will have callback even if opening GUI.
     * @param handler 
     */
    public void addKeyHandler(String name, String keyDesc, int defKeyID, boolean global, KeyHandler handler) {
        if(nameMap.containsKey(name))
            throw new RuntimeException("Duplicate key: " + name + " of object " + handler);
        
        Configuration conf = getConfig();
        int keyID = defKeyID;
        if(conf != null) {
            keyID = conf.getInt(name, "keys", defKeyID, -1000, 1000, keyDesc);
        }
        KeyBinding kb = new KeyBinding(handler, keyID, global);
        nameMap.put(name, kb);
    }
    
    public void resetBindingKey(String name, int newKey) {
        KeyBinding kb = nameMap.get(name);
        if(kb != null) {
            Configuration cfg = getConfig();
            if(cfg != null) {
                Property p = cfg.get("keys", name, kb.keyID);
                p.set(newKey);
            }
            
            kb.keyID = newKey;
            if(kb.keyDown)
                kb.handler.onKeyAbort();
            
            kb.keyDown = false;
        }
    }
    
    /**
     * Removes a key handler from map, if exists.
     */
    public void removeKeyHandler(String name) {
        KeyBinding kb = nameMap.get(name);
        if(kb != null)
            kb.dead = true;
    }
    
    private void tick() {
        Iterator< Entry<String, KeyBinding> > iter = nameMap.entrySet().iterator();
        boolean shouldAbort = !ClientUtils.isPlayerInGame();
        
        while(iter.hasNext()) {
            Entry<String, KeyBinding> entry = iter.next();
            KeyBinding kb = entry.getValue();
            if(kb.dead) {
                iter.remove();
            } else {
                boolean down = getKeyDown(kb.keyID);

                if (kb.keyDown && shouldAbort) {
                    kb.keyDown = false;
                    kb.keyAborted = true;
                    kb.handler.onKeyAbort();
                } else if (!kb.keyDown && down && !shouldAbort && !kb.keyAborted) {
                    kb.keyDown = true;
                    kb.handler.onKeyDown();
                } else if (kb.keyDown && !down && !shouldAbort) {
                    kb.keyDown = false;
                    kb.handler.onKeyUp();
                } else if (kb.keyDown && down && !shouldAbort) {
                    kb.handler.onKeyTick();
                }

                if (!down) {
                    kb.keyAborted = false;
                }
                
                kb.keyDown = down;
            }
        }
    }
    
    public static boolean getKeyDown(int keyID) {
        if(keyID > 0) {
            return Keyboard.isKeyDown(keyID);
        }
        
        return Mouse.isButtonDown(keyID + 100);
    }
    
    private class KeyBinding {
        KeyHandler handler;
        boolean isGlobal;
        
        int keyID;
        
        boolean keyDown;
        boolean keyAborted;
        
        boolean dead;
        
        public KeyBinding(KeyHandler h, int k, boolean g) {
            handler = h;
            keyID = k;
            isGlobal = g;
        }
    }
    
    @SubscribeEvent
    public void onEvent(TickEvent.ClientTickEvent event) {
        if(event.phase == TickEvent.Phase.START && activated) {
            tick();
        }
    }
    
    protected Configuration getConfig() {
        return null;
    }

    public static String getKeyName(int keyid) {
        if(keyid >= 0) {
            String ret = Keyboard.getKeyName(keyid);
            return ret == null ? "undefined" : ret;
        } else {
            String ret = Mouse.getButtonName(keyid + 100);
            return ret == null ? "undefined" : ret;
        }
    }
    
}
