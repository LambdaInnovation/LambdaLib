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
package cn.lambdalib.util.datapart;

import cn.lambdalib.networkcall.s11n.StorageOption.RangedTarget;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.networkcall.RegNetworkCall;
import cn.lambdalib.networkcall.s11n.InstanceSerializer;
import cn.lambdalib.networkcall.s11n.RegSerializable;
import cn.lambdalib.networkcall.s11n.SerializationManager;
import cn.lambdalib.networkcall.s11n.StorageOption.Data;
import cpw.mods.fml.relauncher.Side;

/**
 * DataPart represents a single tickable data-storage object attached on an Entity.
 *  It is driven by the {@link EntityData} of this entity. <br>
 *  
 * The DataPart is attached statically via {@link EntityData#register}
 *  method or @RegDataPart  annotation (This should be done at init stages), and is automatically constructed
 *  on <em>first time querying</em>. At server, the {@link DataPart#fromNBT(NBTTagCompound)}
 *  method will be called right away, and the NBT will be sync to client ASAP. <br>
 *
 *  Also when the world is being saved, the {@link DataPart#toNBT()} will be called to save stuffs
 *   in server. <br>
 *
 * A simple sync helper is provided. You can use sync() in both CLIENT and SERVER side to make a new NBT
 *  synchronization. However, for complex syncs you might want to consider using NetworkCall. <br>
 *  
 * DataPart supports ticking by nature. Use setTick() to enable it. <br>
 *
 * For DataPart of EntityPlayer, all DataParts are kept except for those who called {@link DataPart#clearOnDeath()}
 * 	in their ctor when player is respawned.
 *
 * @author WeAthFolD
 */
@Registrant
@RegSerializable(instance = DataPart.Serializer.class)
public abstract class DataPart<Ent extends Entity> {

	// API

	/**
	 * The default constructor must be kept for subclasses, for using reflections to create instance.
	 */
	public DataPart() {}

	/**
	 * Invoked every tick if setTick() is called
	 */
	public void tick() {}

	/**
	 * Set this DataPart to need ticking. Called in ctor.
	 */
	public final void setTick() {
		tick = true;
	}

	/**
	 * Set this DataPart to be reset when entity is dead. Effective for EntityPlayer only. Called in ctor.
	 * (Other entities don't revive)
	 */
	public final void clearOnDeath() {
		keepOnDeath = false;
	}

	/**
	 * Restore data of this DataPart from the NBT. Will be called externally only for world saving
	 */
	public abstract void fromNBT(NBTTagCompound tag);

	/**
	 * Convert data of this DataPart to a NBT. Will be called externally only for world saving
	 */
	public abstract NBTTagCompound toNBT();

	/**
	 * Same as fromNBT, but only get called when synchronizing across network.
	 */
	public void fromNBTSync(NBTTagCompound tag) { fromNBT(tag); }

	/**
	 * Same as toNBT, but only get called when synchronizing across network.
	 */
	public NBTTagCompound toNBTSync() { return toNBT(); }

	public Ent getEntity() {
		return data.entity;
	}

	public boolean isRemote() {
		return getEntity().worldObj.isRemote;
	}

	public <T extends DataPart> T getPart(String name) {
		return data.getPart(name);
	}

	public <T extends DataPart> T getPart(Class<T> type) {
		return data.getPart(type);
	}

	public String getName() {
		return data.getName(this);
	}

	// Internal

	/**
	 * Internal sync flag, used to determine whether this part is init in client.
	 */
	boolean dirty = true;
	
	int tickUntilQuery = 0;
	
	/**
	 * The player instance when this data is available. Do NOT modify this field!
	 */
	EntityData<Ent> data;

	boolean tick = false, keepOnDeath = true;
	
	/**
	 * Return true if this data has received the initial sync.
	 * ALWAYS true in server.
	 */
	protected boolean isSynced() {
		return !dirty;
	}
	
	protected void sync() {
		if(isRemote()) {
			syncFromClient(toNBTSync());
		} else {
			syncFromServer(getEntity(), toNBTSync());
		}
	}
	
	@RegNetworkCall(side = Side.SERVER)
	private void syncFromClient(@Data NBTTagCompound tag) {
		fromNBTSync(tag);
	}
	
	@RegNetworkCall(side = Side.CLIENT)
	private void syncFromServer(@RangedTarget(range = 10) Entity player, @Data NBTTagCompound tag) {
		fromNBTSync(tag);
	}
	
	protected void checkSide(boolean isRemote) {
		if(isRemote ^ isRemote()) {
			throw new IllegalStateException("Wrong side: " + isRemote());
		}
	}
	
	public static class Serializer implements InstanceSerializer<DataPart> {
		
		InstanceSerializer entitySer = SerializationManager.INSTANCE.getInstanceSerializer(Entity.class);

		@Override
		public DataPart readInstance(NBTBase nbt) throws Exception {
			NBTTagCompound tag = (NBTTagCompound) nbt;
			NBTBase entityTag = tag.getTag("e");
			if(entityTag != null) {
				Entity e = (Entity) entitySer.readInstance(entityTag);
				EntityData data = EntityData.get(e);
				if(data != null) {
					return data.getPart(tag.getString("n"));
				}
			}
			
			return null;
		}

		@Override
		public NBTBase writeInstance(DataPart obj) throws Exception {
			NBTTagCompound ret = new NBTTagCompound();
			
			NBTBase entityTag = entitySer.writeInstance(obj.getEntity());
			
			ret.setTag("e", entityTag);
			ret.setString("n", obj.getName());
			
			return ret;
		}
		
	}
	
}
