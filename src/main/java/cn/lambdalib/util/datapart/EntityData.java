package cn.lambdalib.util.datapart;

import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.annoreg.mc.RegEventHandler;
import cn.lambdalib.annoreg.mc.RegEventHandler.Bus;
import cn.lambdalib.s11n.network.NetS11nAdapterRegistry.RegNetS11nAdapter;
import cn.lambdalib.s11n.network.NetworkS11n;
import cn.lambdalib.s11n.network.NetworkS11n.ContextException;
import cn.lambdalib.s11n.network.NetworkS11n.NetS11nAdaptor;
import cn.lambdalib.util.mc.SideHelper;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Registrant
public final class EntityData<Ent extends EntityLivingBase> implements IExtendedEntityProperties {

    private static final String ID = "LL_EntityData";

    private static final List<RegData> regList = new ArrayList<>();
    private static boolean init = false;

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
    }

    @SuppressWarnings("unchecked")
    public static <T extends EntityLivingBase> EntityData<T> get(T entity) {
        Objects.requireNonNull(entity);

        if (!init) {
            init = true;
            init();
        }

        EntityData<T> ret = (EntityData<T>) entity.getExtendedProperties(ID);
        if (ret == null) {
            ret = new EntityData<>();
            ret.entity = entity;
            entity.registerExtendedProperties(ID, ret);

            // Construct all non-lazy parts
            _allApplicable(entity).filter(data -> !data.lazy).forEach(ret::_constructPart);
        }

        return ret;
    }

    /**
     * @return The EntityData of given entity, or {@link null} if not created.
     */
    @SuppressWarnings("unchecked")
    public static <T extends EntityLivingBase> EntityData<T> getNonCreate(T entity) {
        return (EntityData<T>) entity.getExtendedProperties(ID);
    }

    private static void init() {
        regList.sort((lhs, rhs) -> lhs.type.getName().compareTo(rhs.type.getName()));
        Preconditions.checkState(regList.size() < Byte.MAX_VALUE);
        IntStream.range(0, regList.size()).forEach(i -> regList.get(i).networkID = (byte) i);
    }

    private Map<Class, DataPart> constructed = new HashMap<>();


    private Ent entity;

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
        if (constructed.containsKey(type)) {
            return constructed.get(type);
        } else {
            return null;
        }
    }

    public Ent getEntity() {
        return entity;
    }

    @Override
    public void saveNBTData(NBTTagCompound tag_) {
        NBTTagCompound tag = tag_.getCompoundTag("ForgeData");
        constructed.values().forEach(part -> {
            if (part.needNBTStorage) {
                NBTTagCompound partTag = new NBTTagCompound();
                part.toNBT(partTag);
                tag.setTag(_partNBTID(part), partTag);
            }
        });
    }

    @Override
    public void loadNBTData(NBTTagCompound tag) {
        // We don't do it this way. Loads manually from entity tag.
    }

    @SuppressWarnings("unchecked")
    @Override
    public void init(Entity entity, World world) {
        this.entity = (Ent) entity;
    }

    private String _partNBTID(DataPart part) {
        return part.getClass().getCanonicalName();
    }

    private void _constructPart(RegData data) {
        final Side runtimeSide = SideHelper.getRuntimeSide();
        Preconditions.checkState(data.sides.contains(runtimeSide));
        try {
            DataPart instance = data.type.newInstance();
            instance.entityData = (EntityData) this;
            constructed.put(data.type, instance);

            if (!SideHelper.isClient() && instance.needNBTStorage) {
                NBTTagCompound forgeTag = getEntity().getEntityData();
                String id = _partNBTID(instance);
                if (forgeTag.hasKey(id)) {
                    instance.fromNBT(forgeTag.getCompoundTag(id));
                }
            }
        } catch (IllegalAccessException |
                InstantiationException ex) {
            Throwables.propagate(ex);
        }
    }

    private static Stream<RegData> _allApplicable(Entity ent) {
        Class<? extends Entity> type = ent.getClass();
        return regList.stream().filter(data -> data.pred.test(type));
    }

    private static byte getNetworkID(Class<? extends DataPart> type) {
        return regList.stream()
                .filter(data -> data.type.equals(type))
                .findAny().get()
                .networkID;
    }

    private static Class<? extends DataPart> getTypeFromID(byte id) {
        return regList.get(id).type;
    }

    private void tick() {
        for (DataPart part : constructed.values()) {
            part.callTick();
        }
    }

    @RegEventHandler(Bus.Forge)
    public static class EventListener {
        @SubscribeEvent
        public void onLivingUpdate(LivingUpdateEvent evt) {
            EntityData<EntityLivingBase> data = EntityData.getNonCreate(evt.entityLiving);
            if (data != null) {
                data.tick();
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