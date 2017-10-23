package cn.lambdalib.util.datapart;

import cn.lambdalib.annoreg.core.LoadStage;
import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.annoreg.mc.RegCallback;
import cn.lambdalib.core.LLCommons;
import cn.lambdalib.core.LambdaLib;
import cn.lambdalib.s11n.network.NetS11nAdapterRegistry.RegNetS11nAdapter;
import cn.lambdalib.s11n.network.NetworkS11n;
import cn.lambdalib.s11n.network.NetworkS11n.ContextException;
import cn.lambdalib.s11n.network.NetworkS11n.NetS11nAdaptor;
import cn.lambdalib.util.mc.SideHelper;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import net.minecraft.nbt.NBTBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Registrant
public final class EntityData<Ent extends EntityLivingBase> implements IDataPart {

    private static final String ID = "LL_EntityData";

    private static final List<RegData> regList = new ArrayList<>();
    private static final List<RegData> bothSideList = new ArrayList<>();
    private static boolean init = false;


    /**
     * Register all the DataPart into List. Use @RegDataPart or register your DataPart in @event FMLInitializationEvent manually.
     * @param type
     * @param sides
     * @param pred
     * @param lazy
     */
    @SuppressWarnings("unchecked")
    public static <T extends EntityLivingBase> void
    register(Class<? extends DataPart<T>> type,
             EnumSet<Side> sides,
             Predicate<Class<? extends T>> pred,
             boolean lazy) {
        RegData add = new RegData();
        add.type = type;
        add.sides = EnumSet.copyOf(sides);
        add.pred = (Predicate) pred;
        add.lazy = lazy;

        regList.add(add);
        if (sides.contains(Side.CLIENT) && sides.contains(Side.SERVER)) {
            bothSideList.add(add);
        }
    }

    private static  Capability<IDataPart> getCapability(){
        return CapDataPartHandler.DATA_PART_CAPABILITY;
    }

    /**
     * Get entity's EntityData. By get it you can get other data by getPart().
     * @param entity can't be null.
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends EntityLivingBase> EntityData<T> get(T entity) {
        Objects.requireNonNull(entity);

        if (!init) {
            init = true;
            init();
        }

        IDataPart ret =  entity.getCapability(getCapability(),null);
        if (ret == null || !(ret instanceof EntityData)) {
            throw new RuntimeException("Failed to get EntityData of "+entity+" ret="+ret);
        }
        if(((EntityData)ret).getEntity()==null){
            ((EntityData)ret).initEntity(entity);
        }

        return (EntityData)ret;
    }

    private static void init() {
        bothSideList.sort(Comparator.comparing(lhs -> lhs.type.getName()));
        Preconditions.checkState(bothSideList.size() < Byte.MAX_VALUE);
        IntStream.range(0, bothSideList.size()).forEach(i -> bothSideList.get(i).networkID = (byte) i);

        LLCommons.debug("EntityData initialized. Network participants: " +
                bothSideList.stream().map(x -> x.type).collect(Collectors.toList()));
    }

    private Map<Class, DataPart> constructed = new HashMap<>();

    private Ent entity;

    public void initEntity(Ent entity){
        this.entity=entity;
    }

    /**
     * @return The datapart of exact type, never null
     * @throws NullPointerException if no such DataPart was registered before
     */
    @SuppressWarnings("unchecked")
    public <T extends DataPart<?>>
    T getPart(Class<T> type) {
        if (constructed.containsKey(type)) {
            return (T) constructed.get(type);
        } else {
            RegData regData = _allApplicable(getEntity())
                    .filter(data -> data.type.equals(type))
                    .findFirst()
                    .get();
            _constructPart(regData);
            return (T) constructed.get(type);
        }
    }

    /**
     * @return The datapart of exact type, or null if not present
     */
    @SuppressWarnings("unchecked")
    public <T extends EntityLivingBase>
    DataPart<T> getPartNonCreate(Class<? extends DataPart<T>> type) {
        return constructed.getOrDefault(type, null);
    }

    public Ent getEntity() {
        return entity;
    }



    //NBT path: tag_->TAG("ForgeData"->TAG(ID))
    @Override
    public void writeNBT(NBTTagCompound tag_) {
        NBTTagCompound tag=new NBTTagCompound();

        constructed.values().forEach(part -> {
            if (part.needNBTStorage) {
                NBTTagCompound partTag = new NBTTagCompound();
                part.toNBT(partTag);
                tag.setTag(_partNBTID(part), partTag);
            }
        });
        if(tag.getCompoundTag("ForgeData").hasKey(ID))
            LambdaLib.log.warn("Find existed log:"+ID+" when storage NBTTag in EntityData:155.");
        tag.getCompoundTag("ForgeData").setTag(ID,tag);
    }

    @Override
    public void readNBT(NBTTagCompound tag_) {
        NBTTagCompound tag=tag_.getCompoundTag("ForgeData").getCompoundTag(ID);

        constructed.values().forEach(part -> {
            if (part.needNBTStorage) {
                NBTTagCompound partTag = tag.getCompoundTag(_partNBTID(part));
                part.fromNBT(partTag);
            }
        });
    }


    private String _partNBTID(DataPart part) {
        return part.getClass().getCanonicalName();
    }

    private void _constructPart(RegData data) {
        final Side runtimeSide = SideHelper.getRuntimeSide();
        Preconditions.checkState(data.sides.contains(runtimeSide));
        try {
            DataPart instance = data.type.newInstance();
            instance.entityData = this;
            constructed.put(data.type, instance);

            instance.wake();

            if (!SideHelper.isClient() && instance.needNBTStorage) {
                NBTTagCompound tag = getEntity().getEntityData().getCompoundTag(ID);
                String id = _partNBTID(instance);
                if (tag.hasKey(id)) {
                    instance.fromNBT(tag.getCompoundTag(id));
                }
            }
        } catch (IllegalAccessException |
                InstantiationException ex) {
            Throwables.propagate(ex);
        }
    }

    private static Stream<RegData> _allApplicable(Entity ent) {
        Class<? extends Entity> type = ent.getClass();
        final Side runtimeSide = SideHelper.getRuntimeSide();
        return regList.stream().filter(data -> data.sides.contains(runtimeSide) && data.pred.test(type));
    }

    private static byte getNetworkID(Class<? extends DataPart> type) {
        for (RegData data : bothSideList) {
            if (data.type == type) {
                return data.networkID;
            }
        }
        throw new IllegalStateException(type + " isn't registered as both side");
    }

    private static Class<? extends DataPart> getTypeFromID(byte id) {
        return bothSideList.get(id).type;
    }

    private void tick() {
        for (DataPart part : constructed.values()) {
            part.callTick();
        }
    }

    @Registrant
    public enum EventListener {
        instance;

        @RegCallback(stage= LoadStage.PRE_INIT)
        public static void preInit(FMLPreInitializationEvent event){
            MinecraftForge.EVENT_BUS.register(instance);
        }

        @SubscribeEvent
        public void onLivingUpdate(LivingUpdateEvent evt) {
            EntityData<EntityLivingBase> data = EntityData.get(evt.getEntityLiving());
            if (data != null) {
                data.tick();
            }
        }

        @SubscribeEvent
        public void onLivingDeath(LivingDeathEvent evt) {
            if (evt.getEntityLiving() instanceof EntityPlayer) {
                EntityData<EntityPlayer> playerData = EntityData.get((EntityPlayer) evt.getEntityLiving());
                playerData.constructed.values().removeIf(dp -> dp.clearOnDeath);
            }
        }

        @SubscribeEvent
        public void onPlayerClone(PlayerEvent.Clone evt) {
            EntityData<EntityPlayer> data = EntityData.get(evt.getOriginal());
            if (data != null) {
                data.entity = evt.getEntityPlayer();
                NBTBase nbt = CapDataPartHandler.storage.writeNBT(getCapability(), evt.getOriginal().getCapability(getCapability(), null), null);
                CapDataPartHandler.storage.readNBT(getCapability(), evt.getEntityPlayer().getCapability(getCapability(), null), null, nbt);
            }
        }
    }

    @RegNetS11nAdapter(EntityData.class)
    public static NetS11nAdaptor<EntityData> adaptor = new NetS11nAdaptor<EntityData>() {
        @Override
        public void write(ByteBuf buf, EntityData obj) {
            NetworkS11n.serializeWithHint(buf, obj.getEntity(), EntityLivingBase.class);
        }
        @Override
        public EntityData read(ByteBuf buf) throws ContextException {
            EntityLivingBase living = NetworkS11n.deserializeWithHint(buf, EntityLivingBase.class);
            if (living != null) {
                return EntityData.get(living);
            } else {
                throw new ContextException("Entity not found");
            }
        }
    };

    @RegNetS11nAdapter(DataPart.class)
    public static NetS11nAdaptor<DataPart> partAdaptor = new NetS11nAdaptor<DataPart>() {
        @Override
        public void write(ByteBuf buf, DataPart obj) {
            NetworkS11n.serializeWithHint(buf, obj.getData(), EntityData.class);
            buf.writeByte(getNetworkID(obj.getClass()));
        }
        @Override
        public DataPart read(ByteBuf buf) throws ContextException {
            return NetworkS11n.deserializeWithHint(buf, EntityData.class).getPart(getTypeFromID(buf.readByte()));
        }
    };

}

class RegData {

    Class<? extends DataPart<?>> type;
    EnumSet<Side> sides;
    Predicate<Class<? extends Entity>> pred;
    boolean lazy;

    byte networkID; // Only useful if created in both sides

}