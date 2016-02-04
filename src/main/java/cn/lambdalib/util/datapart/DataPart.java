package cn.lambdalib.util.datapart;

import cn.lambdalib.networkcall.TargetPointHelper;
import cn.lambdalib.s11n.network.NetworkMessage;
import cn.lambdalib.s11n.network.NetworkMessage.Listener;
import cn.lambdalib.util.mc.SideHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import static cn.lambdalib.core.LLCommons.*;

/**
 * A tickable data-storage entity attached to EntityLivingBase.
 * see {@link EntityData} for access methods.
 * @author WeAthFolD
 */
public abstract class DataPart<T extends EntityLivingBase> {

    EntityData<T> entityData;
    private boolean syncInit = false;

    boolean needNBTStorage = false;
    boolean needTick = false;
    boolean clientNeedSync = false;
    double serverSyncRange = 10.0;

    // Behaviour

    /**
     * Make this DataPart's tick() method be called every tick. Can be called during runtime or construction.
     */
    protected final void setTick(boolean state) {
        needTick = true;
    }

    /**
     * Make this DataPart to be saved or loaded via NBT when constructed in SERVER.
     */
    protected final void setNBTStorage() {
        needNBTStorage = true;
    }

    /**
     * Make this DataPart to automatically retrieve sync from server when constructed in client.
     */
    protected final void setClientNeedSync() {
        clientNeedSync = true;
    }

    /**
     * @param range The range for other clients to receive sync (if called sync() in server). Can
     *  be called during runtime or construction. Defaults to 10.
     */
    protected final void setServerSyncRange(double range) {
        serverSyncRange = range;
    }

    //

    /**
     * Sync this DataPart's data. If in client, data will be synced to server. Otherwise, data will be synced to any
     *  clients within the range specified by {@link #setServerSyncRange(double)}.
     * @throws IllegalStateException if this DataPart is registered to be client-only or server-only
     */
    public final void sync() {
        if (isClient()) {
            __syncClient();
        } else {
            NetworkMessage.sendToAllAround(TargetPointHelper.convert(getEntity(), serverSyncRange), this, "itn_sync", __genSyncTag());
        }
    }

    @SideOnly(Side.CLIENT)
    private void __syncClient() {
        T ent = getEntity();
        if (!(ent instanceof EntityPlayer)) {
            log.warn("Trying to call sync() in client for non-EntityPlayers in" + this +
                    ". This usually doesn't make sense.");
        } else if (!(ent.equals(Minecraft.getMinecraft().thePlayer))) {
            log.warn("Trying to sync non-local player data to server D  ataPart in " + this +
                    ". This usually doesn't make sense.");
        }

        NetworkMessage.sendToServer(this, "itn_sync", __genSyncTag());
    }

    private NBTTagCompound __genSyncTag() {
        NBTTagCompound tag = new NBTTagCompound();
        toNBTSync(tag);
        return tag;
    }

    /**
     * Invoked every tick if {@link #setTick(boolean)} has been invoked with argument true.
     */
    public void tick() {}

    /**
     * Stores this DataPart. Called during {@link #sync()} if {@link #toNBTSync(NBTTagCompound)} is not overriden,
     *  or when the DataPart is being stored at SERVER.
     */
    public void toNBT(NBTTagCompound tag) {}

    /**
     * Loads the DataPart. Called during {@link #sync()} if {@link #fromNBTSync(NBTTagCompound)} is not overridden,
     *  or when the DataPart is being loaded at SERVER.
     */
    public void fromNBT(NBTTagCompound tag) {}

    /**
     * Loads the DataPart when synchronizing. ({@link #sync()})
     */
    public void fromNBTSync(NBTTagCompound tag) {
        fromNBT(tag);
    }

    /**
     * Stores this DataPart when synchorinizing. ({@link #sync()})
     */
    public void toNBTSync(NBTTagCompound tag) {
        toNBT(tag);
    }

    //

    // Utils
    /**
     * @return Whether we are in client.
     */
    protected boolean isClient() {
        return getEntity().worldObj.isRemote;
    }

    protected Side getSide() {
        return isClient() ? Side.CLIENT : Side.SERVER;
    }

    /**
     * @return The entity that this DataPart is attached to.
     */
    public T getEntity() {
        return entityData.getEntity();
    }

    /**
     * @return The {@link EntityData} that handles this entity.
     */
    public EntityData<T> getData() {
        return entityData;
    }

    /**
     * Assert that side is same to parameter and crashes the game if not.
     */
    protected void checkSide(Side side) {
        if (isClient() != side.isClient()) {
            throw new IllegalStateException("Invalid side, expected " + side);
        }
    }

    // Internal

    void callTick() {
        if (isClient() && clientNeedSync && !syncInit) {
            syncInit = true;
            NetworkMessage.sendToServer(this, "itn_query_init", SideHelper.getThePlayer());
        }
        if (needTick) {
            tick();
        }
    }

    @Listener(channel="itn_query_init", side={Side.SERVER})
    private void onQuerySync(EntityPlayerMP client) {
        NetworkMessage.sendTo(client, this, "itn_sync_init", __genSyncTag());
    }

    @Listener(channel="itn_sync_init", side={Side.CLIENT})
    private void onInitSync(NBTTagCompound tag) {
        fromNBTSync(tag);
    }

    @Listener(channel="itn_sync", side={Side.CLIENT, Side.SERVER})
    private void onSync(NBTTagCompound tag) {
        fromNBTSync(tag);
    }

}
