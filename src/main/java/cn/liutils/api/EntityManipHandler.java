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
package cn.liutils.api;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.entity.Entity;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;

/**
 * The class that handles entity manipulator storage and running.
 * @author WeathFolD
 */
public class EntityManipHandler {

	/**
	 * A interface that has the ability to change an entity's state outside its own running code.
	 * @author WeathFolD
	 */
	public static abstract class EntityManip<T extends Entity> {
		
		public final T entity;
		public boolean alive;
		
		public EntityManip(T ent) {
			entity = ent;
			alive = true;
		}
		
		/**
		 * Get the universal identifier of this instance.
		 */
		public abstract String getID();
		/**
		 * called each tick to update entity state
		 */
		public abstract void onTick();
		/**
		 * called when the manip is to be removed
		 */
		public abstract void onEnd();
		
		protected boolean isRemote() { //Fast alias
			return entity.worldObj.isRemote;
		}
		
		public final void setDead() {
			alive = false;
		}
		
	}
	
	private static Map<Entity, Map<String, EntityManip>> 
		table_client = new WeakHashMap<Entity, Map<String, EntityManip>>(),
		table_server = new WeakHashMap<Entity, Map<String, EntityManip>>();
	
	public static boolean addEntityManip(EntityManip em) {
		return addEntityManip(em, false);
	}
	
	/**
	 * Add a entity manipulator instance.
	 * @param em
	 * @param force Still add if one instance with same ID exists?
	 * @return if there is conflict(another instance with same ID exists)
	 */
	public static boolean addEntityManip(EntityManip em, boolean force) {
		Map<String, EntityManip> map = entityMap(em.entity);
		EntityManip now = map.get(em.getID());
		if(now != null && !force) return false;
		map.put(em.getID(), em);
		em.onTick();
		return now == null;
	}
	
	public static boolean hasManip(Entity e, String str) {
		return entityMap(e).containsKey(str);
	}
	
	private static Map<String, EntityManip> entityMap(Entity e) {
		Map<Entity, Map<String, EntityManip>> table = e.worldObj.isRemote ? table_client : table_server;
		Map<String, EntityManip> res = table.get(e);
		//Lazy loading
		if(res == null) {
			res = new HashMap<String, EntityManip>();
			table.put(e, res);
		}
		return res;
	}
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
		onTick(table_client);
	}
	
	@SubscribeEvent
	public void onServerTick(ServerTickEvent event) {
		//System.out.println("ost");
		onTick(table_server);
	}
	
	private void onTick(Map<Entity, Map<String, EntityManip>> table) {
		for(Map.Entry<Entity, Map<String, EntityManip>> te : table.entrySet()) {
			Entity ent = te.getKey();
			if(ent.isDead) continue; //Wait for it being garbage collected
			
			Map<String, EntityManip> mlist = te.getValue();
			//Iterate through the entity manip table
			Iterator<Map.Entry<String, EntityManip>> iter = mlist.entrySet().iterator();
			while(iter.hasNext()) {
				Map.Entry<String, EntityManip> eman = iter.next();
				if(eman.getValue().alive) {
					eman.getValue().onTick();
				} else {
					eman.getValue().onEnd();
					iter.remove();
				}
			}
		}
	}
	
	public static void init() {
		FMLCommonHandler.instance().bus().register(new EntityManipHandler());
	}

}
