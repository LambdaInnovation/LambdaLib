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
package cn.lambdalib.util.key;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import cn.lambdalib.util.client.ClientUtils;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

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
		boolean inGame = ClientUtils.isPlayerInGame();
		if(!inGame)
			return;
		
		while(iter.hasNext()) {
			Entry<String, KeyBinding> entry = iter.next();
			KeyBinding kb = entry.getValue();
			if(kb.dead) {
				iter.remove();
			} else {
				boolean down = getKeyDown(kb.keyID);
				
				if(down) {
					if(!kb.isGlobal && !inGame) {
						kb.keyAborted = true;
						if(kb.keyDown) {
							kb.handler.onKeyAbort();
						}
					} else if(!kb.keyAborted) {
						if(!kb.keyDown) {
							kb.handler.onKeyDown();
						} else {
							kb.handler.onKeyTick();
						}
					}
				} else {
					if(kb.keyDown) {
						if(inGame) 
							kb.handler.onKeyUp();
						else 
							kb.handler.onKeyAbort();
					}
					kb.keyAborted = false;
				}
				
				kb.keyDown = down;
			}
		}
	}
	
	private boolean getKeyDown(int keyID) {
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
	public void onEvent(ClientTickEvent event) {
		if(event.phase == Phase.START && activated) {
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
