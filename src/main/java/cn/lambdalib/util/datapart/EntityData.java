/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.datapart;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import cn.lambdalib.s11n.network.NetS11nAdapterRegistry.RegNetS11nAdapter;
import cn.lambdalib.s11n.network.NetworkMessage;
import cn.lambdalib.s11n.network.NetworkMessage.Listener;
import cn.lambdalib.s11n.network.NetworkS11n;
import cn.lambdalib.s11n.network.NetworkS11n.InterruptException;
import cn.lambdalib.s11n.network.NetworkS11n.NetS11nAdaptor;
import cn.lambdalib.s11n.network.NetworkS11n.NetworkS11nType;
import cn.lambdalib.util.client.ClientUtils;
import cn.lambdalib.util.mc.WorldUtils;

import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.annoreg.mc.RegEventHandler;
import cn.lambdalib.util.mc.SideHelper;
import cn.lambdalib.core.LambdaLib;
import cn.lambdalib.networkcall.s11n.InstanceSerializer;
import cn.lambdalib.networkcall.s11n.RegSerializable;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * The environment provider and handler of DataPart. <br>
 *
 * It is recommended for DataPart users to not access this class explicitly, but create
 * some static get method in each DataPart class. <br>
 *
 * Note that EntityData is lazy. It is not created until anyone asked for it, and so is DataPart.
 *
 * @see DataPart
 * @author WeAthFolD
 */
@Registrant
@NetworkS11nType
@RegSerializable(instance = EntityData.Serializer.class) // For legacy compat
public abstract class EntityData<Ent extends Entity> implements IExtendedEntityProperties {

    // Static registry
    private static String IDENTIFIER = "liu_playerData";

    /**
     * Cache alive data in a map and update it each tick. Better optimized loop xD
     */
    private static ThreadLocal<Map<Entity, EntityData>> alive = new ThreadLocal<Map<Entity, EntityData>>() {
        @Override
        protected Map<Entity, EntityData> initialValue() {
            return new WeakHashMap<>();
        }
    };

    private static Map<String, Registered> staticParts = new HashMap<>();
    private static Map<Class<? extends DataPart<? extends Entity>>, String> nameRev = new HashMap<>();

    /**
     * Register a DataPart with given <strong>Type predicate</strong>. A type predicate dertermines
     * whether the DataPart will be created on a kind of entity on construction.
     */
    public static void register(String name,
                                Class<? extends DataPart<? extends Entity>> partType,
                                Predicate<Class<? extends Entity>> pred) {
        Registered added = new Registered();
        added.type = partType;
        added.filterPred = pred;

        staticParts.put(name, added);
        nameRev.put(partType, name);
    }

    /**
     * Register a DataPart that is applicable for only instances of given entity type.
     */
    public static <T extends Entity>
    void register(String name, Class<? extends DataPart<T>> partType, Class<? extends T> entityType) {
        register(name, partType, entityType::isAssignableFrom);
    }

    /**
     * Register a DataPart that is applicable for only {@link EntityPlayer}.
     */
    public static void register(String name, Class<? extends DataPart<EntityPlayer>> partType) {
        register(name, partType, EntityPlayer.class);
    }

    /**
     * @return The EntityData associated with the entity. Always non-null. Will create one if none attached.
     */
    public static <Ent extends Entity> EntityData<Ent> get(Ent player) {
        EntityData<Ent> data = (EntityData<Ent>) player.getExtendedProperties(IDENTIFIER);

        if(data == null) {
            if(player.worldObj.isRemote) {
                data = new EntityData.Client<>(player);
            } else {
                data = new EntityData.Server<>(player);
                data.loadNBTDataCustom(player.getEntityData());
            }
            player.registerExtendedProperties(IDENTIFIER, data);

            Map<Entity, EntityData> map = alive.get();
            map.put(player, data);
        }

        data.entity = player;
        return data;
    }

    /**
     * @return The EntityData associated with the entity. Null if currently none attached.
     */
    public static <EntT extends Entity> EntityData<EntT> getNonCreate(EntT player) {
        return (EntityData) player.getExtendedProperties(IDENTIFIER);
    }

    private static class Registered {
        Class<? extends DataPart> type;
        Predicate<Class<? extends Entity>> filterPred;
    }

    // API
    public String getName(DataPart part) {
        return nameRev.get(part.getClass());
    }

    public <T extends DataPart<?>> T getPart(String name) {
        // debug(staticParts.get(name).type);
        return (T) constructed.get(staticParts.get(name).type);
    }

    public <T extends DataPart<?>> T getPart(Class<T> clazz) {
        return (T) constructed.get(clazz);
    }

    // Internal
    // Implementation note
    // The key in alive map is inconsistent. If player dies or something else happens, the key might not represent
    // the instance of actual entity. Use EntityData#player which is well maintained.
    // FIXME Currently the EntityPlayer reference is dangling, to preserve the data for respawn. Should clean it up.
    
    protected Map<Class<?> , DataPart<? extends Ent>> constructed = new HashMap<>();
    private DataPart[] ticked;

    public Ent entity;

    EntityData(Ent entity) {
        this.entity = entity;
        constructData();
    }
    
    private void constructData() {
        staticParts.entrySet()
                .stream()
                .filter(p -> p.getValue().filterPred.test(entity.getClass()))
                .forEach(entry -> {
                    Registered reg = entry.getValue();
                    construct((Class<? extends DataPart<Ent>>) reg.type);
                });

        reconstructTickList();
    }

    private DataPart<Ent> construct(Class<? extends DataPart<Ent>> type) {
        try {
            DataPart<Ent> dp = type.newInstance();
            dp.data = this;
            constructed.put(type, dp);
            return dp;
        } catch(InstantiationException | IllegalAccessException e) {
            LambdaLib.log.error("Error constructing DataPart", e);
            throw new RuntimeException(e);
        }
    }
    
    protected void tick() {
        for(DataPart p : ticked) {
            p.tick();
        }
    }

    private void reconstructTickList() {
        ticked = constructed.values().stream().filter(x -> x.tick).toArray(DataPart[]::new);
    }

    @Override
    public void init(Entity entity, World world) {
        this.entity = (Ent) entity;
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
        loadNBTDataCustom(tag.getCompoundTag("ForgeData"));
    }
    
    @Override
    public void saveNBTData(NBTTagCompound tag2) {
        NBTTagCompound tag = tag2.getCompoundTag("ForgeData");
        saveNBTDataCustom(tag);
        tag2.setTag("ForgeData", tag);
    }
    
    private static class Client<Ent extends Entity> extends EntityData<Ent> {
        
        public Client(Ent player) {
            super(player);
        }

        @SideOnly(Side.CLIENT)
        @Override
        protected void tick() {
            for(DataPart<? extends Ent> p : constructed.values()) {
                if(p.dirty) {
                    if(p.tickUntilQuery-- == 0) {
                        p.tickUntilQuery = 20;
                        NetworkMessage.sendToServer(this, NET_QUERY, Minecraft.getMinecraft().thePlayer, getName(p));
                    }
                }
            }
            
            super.tick();
        }
        
        @Override
        public void saveNBTDataCustom(NBTTagCompound tag) {}
        
    }
    
    static class Server<Ent extends Entity> extends EntityData<Ent> {

        public Server(Ent player) {
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

    private static final String NET_QUERY = "query", NET_SYNC = "sync", NET_NEXIST = "nexist";

    @Listener(channel=NET_QUERY, side=Side.SERVER)
    private void nQuery(EntityPlayerMP sender, String pname) {
        DataPart part = getPart(pname);
        if (part != null) {
            NetworkMessage.sendTo(sender, this, NET_SYNC, pname, part.toNBTSync());
        } else {
            NetworkMessage.sendTo(sender, this, NET_NEXIST, pname);
        }
    }

    @Listener(channel=NET_SYNC, side=Side.CLIENT)
    private void nSync(String pname, NBTTagCompound tag) {
        DataPart part = getPart(pname);
        part.fromNBT(tag);
        part.dirty = false;
    }

    @Listener(channel=NET_NEXIST, side=Side.CLIENT)
    private void nNexist(String pname) {
        getPart(pname).dirty = false;
    }
    
    @RegEventHandler
    public static class Events {

        static final int CHECK_RATE = 20;
        int cntClient = CHECK_RATE, cntServer = CHECK_RATE;

        @SubscribeEvent
        public void onServerTick(ServerTickEvent event) {
            if(event.phase == Phase.START) {
                if(--cntServer == 0) {
                    cntServer = CHECK_RATE;
                    checkAlive();
                }
                update();
            }
        }

        @SideOnly(Side.CLIENT)
        @SubscribeEvent
        public void onClientTick(ClientTickEvent event) {
            if(event.phase == Phase.START && ClientUtils.isPlayerInGame()) {
                if(--cntClient == 0) {
                    cntClient = CHECK_RATE;
                    checkAlive();
                }
                update();
            }
        }

        private void checkAlive() {
            Iterator<Entry<Entity, EntityData>> itr = alive.get().entrySet().iterator();
            while(itr.hasNext()) {
                Entry<Entity, EntityData> val = itr.next();
                if(shouldRemove(val.getValue().entity)) {
                    itr.remove();
                }
            }
        }

        private void update() {
            // Ticking
            Iterator<Entry<Entity, EntityData>> itr = alive.get().entrySet().iterator();
            while(itr.hasNext()) {
                Entry<Entity, EntityData> val = itr.next();
                EntityData data = val.getValue();
                Entity entity = data.entity;

                if(entity.isEntityAlive()) {
                    data.tick();
                }
            }
        }

        private boolean shouldRemove(Entity e) {
            return !WorldUtils.isWorldValid(e.worldObj) || (!e.isEntityAlive() && !(e instanceof EntityPlayer));
        }

        // NOTE: gets fired only in client.
        @SubscribeEvent
        public void onPlayerClone(PlayerEvent.Clone event) {
            EntityPlayer player = event.entityPlayer;
            EntityData<EntityPlayer> data = EntityData.getNonCreate(event.original);
            if(data != null) {
                data.entity = player;
                player.registerExtendedProperties(IDENTIFIER, data);
                Map<Entity, EntityData> map = alive.get();

                // "Reput" the key-value pair to keep the key reference correct.
                map.remove(event.original);
                map.put(player, data);
            }
        }

        @SubscribeEvent
        public void onLivingDeath(LivingDeathEvent event) {
            if(!(event.entity instanceof EntityPlayer))
                return;

            EntityData<EntityPlayer> data = EntityData.getNonCreate((EntityPlayer) event.entityLiving);
            if(data != null) {
                // Reconstruct non-consistent DataParts
                data.constructed.values().stream()
                        .filter(d -> !d.keepOnDeath)
                        .collect(Collectors.toList()) // Pre collect to eliminate cross-iterating possibility
                        .forEach(d -> {
                            DataPart part = data.construct((Class<? extends DataPart<EntityPlayer>>) d.getClass());
                            part.sync();
                            part.dirty = false;
                        });
                data.reconstructTickList();
            }
        }
        
    }

    public static class Serializer implements InstanceSerializer<EntityData> {

        @Override
        public EntityData readInstance(NBTBase nbt) throws Exception {
            int[] ids = ((NBTTagIntArray) nbt).func_150302_c();
            World world = SideHelper.getWorld(ids[0]);
            if (world != null) {
                Entity ent = world.getEntityByID(ids[1]);
                if(ent instanceof EntityPlayer) {
                    return EntityData.get(ent);
                }
            }
            return null;
        }

        @Override
        public NBTBase writeInstance(EntityData obj) throws Exception {
            Entity ent = obj.entity;
            return new NBTTagIntArray(new int[] { ent.dimension, ent.getEntityId() });
        }

    }

    @RegNetS11nAdapter(EntityData.class)
    public static final NetS11nAdaptor<EntityData> adapter = new NetS11nAdaptor<EntityData>() {
        @Override
        public void write(ByteBuf buf, EntityData obj) {
            NetworkS11n.serializeWithHint(buf, obj.entity, Entity.class);
        }

        @Override
        public EntityData read(ByteBuf buf) {
            Entity ret = NetworkS11n.deserializeWithHint(buf, Entity.class);
            if (ret == null) throw new InterruptException("Null entity while getting EntityData");
            else             return EntityData.get(ret);
        }
    };

    @RegNetS11nAdapter(DataPart.class)
    public static final NetS11nAdaptor<DataPart> dataPartAdapter = new NetS11nAdaptor<DataPart>() {
        @Override
        public void write(ByteBuf buf, DataPart obj) {
            NetworkS11n.serializeWithHint(buf, obj.data, EntityData.class);
            ByteBufUtils.writeUTF8String(buf, obj.getName());
        }

        @Override
        public DataPart read(ByteBuf buf) {
            EntityData data = NetworkS11n.deserializeWithHint(buf, EntityData.class);
            if (data == null) {
                throw new InterruptException("No corresponding EntityData present");
            }
            return data.getPart(ByteBufUtils.readUTF8String(buf));
        }
    };

    private static void debug(Object msg) {
        LambdaLib.log.info("[EntityData] " + msg);
    }

}
