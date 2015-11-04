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
package cn.liutils.util.helper;

import java.util.Map.Entry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.annoreg.mc.RegEventHandler;
import cn.lambdalib.annoreg.mc.SideHelper;
import cn.lambdalib.core.LambdaLib;
import cn.lambdalib.networkcall.RegNetworkCall;
import cn.lambdalib.networkcall.s11n.InstanceSerializer;
import cn.lambdalib.networkcall.s11n.RegSerializable;
import cn.lambdalib.networkcall.s11n.StorageOption;
import cn.lambdalib.networkcall.s11n.StorageOption.Data;
import cn.lambdalib.networkcall.s11n.StorageOption.Instance;
import cn.lambdalib.networkcall.s11n.StorageOption.RangedTarget;
import cn.liutils.util.client.ClientUtils;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * The environment provider and handler of DataPart. <br/>
 * It is recommended not to access this class explicitly, but create
 * some static get method in each DataPart class.
 * @author WeAthFolD
 */
@Registrant
@RegSerializable(instance = PlayerData.Serializer.class)
public abstract class PlayerData implements IExtendedEntityProperties {
	
	private static String IDENTIFIER = "liu_playerData";
	
	static BiMap<String, Class<? extends DataPart> > staticParts = HashBiMap.create();
	
	BiMap<Class<?> , DataPart> constructed = HashBiMap.create();
	
	public static void register(String name, Class<? extends DataPart> clazz) {
		staticParts.put(name, clazz);
	}
	
	/**
	 * Do NOT modify this field!
	 */
	public EntityPlayer player;
	
	PlayerData(EntityPlayer player) {
		this.player = player;
		
		try {
			constructData();
		} catch(Exception e) {
			LambdaLib.log.error("Error constructing DataPart");
			e.printStackTrace();
		}
	}
	
	private void constructData() throws InstantiationException, IllegalAccessException {
		for(Entry< String, Class<? extends DataPart> > s : staticParts.entrySet()) {
			String name = s.getKey();
			Class<? extends DataPart> clazz = s.getValue();
			
			DataPart dp = clazz.newInstance();
			dp.data = this;
			constructed.put(clazz, dp);
		}
	}
	
	protected void tick() {
		for(DataPart p : constructed.values()) {
			p.tick();
		}
	}

	@Override
	public void init(Entity entity, World world) {
		player = (EntityPlayer) entity;
	}
	
	public String getName(DataPart part) {
		return staticParts.inverse().get(part.getClass());
	}
	
	public <T extends DataPart> T getPart(String name) {
		return (T) constructed.get(staticParts.get(name));
	}
	
	public <T extends DataPart> T getPart(Class<T> clazz) {
		return (T) constructed.get(clazz);
	}
	
	public static PlayerData get(EntityPlayer player) {
		PlayerData data = (PlayerData) player.getExtendedProperties(IDENTIFIER);
		if(data == null) {
			if(player.worldObj.isRemote) {
				data = new PlayerData.Client(player);
			} else {
				data = new PlayerData.Server(player);
				data.loadNBTDataCustom(player.getEntityData());
			}
			player.registerExtendedProperties(IDENTIFIER, data);
			
		}
		
		return data;
	}
	
	public static PlayerData getNonCreate(EntityPlayer player) {
		return (PlayerData) player.getExtendedProperties(IDENTIFIER);
	}
	
	void loadNBTDataCustom(NBTTagCompound tag) {
		for(DataPart p : constructed.values()) {
			String name = getName(p);
			NBTTagCompound t = (NBTTagCompound) tag.getTag(name);
			if(t != null) {
				p.fromNBT(t);
			}
			p.dirty = false;
		}
	}
	
	abstract void saveNBTDataCustom(NBTTagCompound tag);
	
	@Override
	public void loadNBTData(NBTTagCompound tag) {
		// HACKHACK
		loadNBTDataCustom(tag.getCompoundTag("ForgeData"));
	}
	
	@Override
	public void saveNBTData(NBTTagCompound tag2) {
		NBTTagCompound tag = tag2.getCompoundTag("ForgeData");
		saveNBTDataCustom(tag);
		tag2.setTag("ForgeData", tag);
	}
	
	public static class Client extends PlayerData {
		
		public Client(EntityPlayer player) {
			super(player);
		}

		@Override
		protected void tick() {
			for(DataPart p : constructed.values()) {
				if(p.dirty) {
					if(p.tickUntilQuery-- == 0) {
						p.tickUntilQuery = 20;
						query(getName(p));
					}					
				}
			}
			
			super.tick();
		}
		
		@Override
		public void saveNBTDataCustom(NBTTagCompound tag) {}
		
	}
	
	public static class Server extends PlayerData {

		public Server(EntityPlayer player) {
			super(player);
		}
		
		@Override
		public void loadNBTData(NBTTagCompound tag) {
			super.loadNBTData(tag);
		}
		
		@Override
		public void saveNBTDataCustom(NBTTagCompound tag) {
			for(DataPart p : constructed.values()) {
				if(p.isSynced()) {
					NBTTagCompound ret = p.toNBT();
					if(ret != null)
						tag.setTag(getName(p), ret);
				} else {
					LambdaLib.log.warn("Ignored saving of " + p.getName());
				}
			}
		}
		
	}
	
	@RegNetworkCall(side = Side.SERVER, thisStorage = StorageOption.Option.INSTANCE)
	protected void query(@Data String pname) {
		DataPart part = getPart(pname);
		if(part != null) // FIX for client-only DataParts.
			synced(player, player, pname, getPart(pname).toNBT());
	}
	
	@RegNetworkCall(side = Side.CLIENT, thisStorage = StorageOption.Option.INSTANCE)
	protected void synced(
			@RangedTarget(range = 10) EntityPlayer _player, 
			@Instance EntityPlayer player, @Data String pname, @Data NBTTagCompound tag) {
		DataPart part = getPart(pname);
		part.fromNBT(tag);
		part.dirty = false;
	}
	
	@RegEventHandler
	public static class Events {
		
		@SubscribeEvent
		public void onPlayerTick(PlayerTickEvent event) {
			if(event.phase == Phase.END)
				return;
			PlayerData data = PlayerData.get(event.player);
			if(data != null) {
				data.player = event.player;
				data.tick();
			}
		}
		
		@SubscribeEvent
		public void onPlayerClone(PlayerEvent.Clone event) {
			EntityPlayer player = event.entityPlayer;
			PlayerData data = PlayerData.getNonCreate(event.original);
			if(data != null) {
				data.player = player;
				player.registerExtendedProperties(IDENTIFIER, data);
			}
		}
		
	}
	
	public static class Serializer implements InstanceSerializer<PlayerData> {

		@Override
		public PlayerData readInstance(NBTBase nbt) throws Exception {
			int[] ids = ((NBTTagIntArray) nbt).func_150302_c();
			World world = SideHelper.getWorld(ids[0]);
			if (world != null) {
				Entity ent = world.getEntityByID(ids[1]);
				if(ent instanceof EntityPlayer) {
					return PlayerData.get((EntityPlayer) ent);
				}
			}
			return null;
		}

		@Override
		public NBTBase writeInstance(PlayerData obj) throws Exception {
			EntityPlayer ent = obj.player;
			return new NBTTagIntArray(new int[] { ent.dimension, ent.getEntityId() });
		}
		
	}

}
