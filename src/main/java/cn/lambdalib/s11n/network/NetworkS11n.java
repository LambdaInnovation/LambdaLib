/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.s11n.network;

import cn.lambdalib.s11n.SerializeDynamic;
import cn.lambdalib.s11n.SerializeNullable;
import cn.lambdalib.util.mc.SideHelper;
import cn.lambdalib.s11n.SerializationHelper;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Supplier;

/**
 * This class handles recursive s11n on netty {@link ByteBuf}.
 * @author WeAthFolD
 */
public class NetworkS11n {

    public interface NetS11nAdaptor<T> {

        /**
         * Write the object to given ByteBuf.
         * @param buf The buf to write object to
         * @param obj The object, not null
         */
        void write(ByteBuf buf, T obj);

        /**
         * Read the object from given ByteBuf, MUST NOT BE NULL
         * @throws ContextException if the object can't be recovered at this time
         * @param buf The buf to read object from
         * @return Deserialized object
         */
        T read(ByteBuf buf) throws ContextException;

    }

    /**
     * Indicate that a type anticipates in network serialization. Equivalent to
     *  {@link NetworkS11n#register}.
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface NetworkS11nType {}

    /**
     * Thrown when the object can't be retrieved, because it can't be found in that runtime(context).
     */
    public static class ContextException extends RuntimeException {
        public ContextException(String msg) {
            super(msg);
        }
    }

    // ---
    private static final SerializationHelper serHelper = new SerializationHelper();

    private static final short IDX_NULL = -1, IDX_ARRAY = -2;

    private static List<Class<?>> serTypes = new ArrayList<>();
    private static Map<Class<?>, NetS11nAdaptor> adaptors = new HashMap<>();

    private static Map<Class, Supplier> suppliers = new HashMap<>();

    private static Map<Class, List<Field>> fieldCache = new HashMap<>();

    private NetworkS11n() {}

    public static <T> void addDirect(Class<T> type, NetS11nAdaptor<? super T> adaptor) {
        register(type);
        adaptors.put(type, adaptor);
        serHelper.regS11nType(type);
    }

    public static <T> void addDirectInstance(T instance) {
        register(instance.getClass());
        addDirect((Class<T>) instance.getClass(), new NetS11nAdaptor<T>() {
            @Override
            public void write(ByteBuf buf, T obj) {}
            @Override
            public T read(ByteBuf buf) throws ContextException {
                return instance;
            }
        });
    }

    public static <T> void addSupplier(Class<T> type, Supplier<? extends T> supplier) {
        suppliers.put(type, supplier);
    }

    /**
     * Registers an object for network s11n. You MUST do this on any object that will be serialized before
     *  any serialization is actually performed. The id table is used in both C and S to reduce data redundancy, so
     *  client programmers must keep registration in both sides consistent.
     */
    public static void register(Class<?> type) {
        if (!serTypes.contains(type)) {
            serTypes.add(type);
            serTypes.sort(Comparator.comparing(Class::getName));
            serHelper.regS11nType(type);
        }

        if (serTypes.size() > Short.MAX_VALUE) {
            throw new RuntimeException("Too many objects registered for network serialization...");
        }
    }

    private static void writeTypeIndex(ByteBuf buf, Class type) {
        if (type.isArray()) {
            buf.writeShort(IDX_ARRAY);
            writeTypeIndex(buf, type.getComponentType());
        } else {
            short idx = (short) typeIndex(type);
            if (idx == -1) {
                throw new RuntimeException("Type " + type + " not registered for net serialization");
            }
            buf.writeShort(idx);
        }
    }

    private static Class readTypeIndex(ByteBuf buf) {
        short idx = buf.readShort();
        if (idx == IDX_NULL) {
            return null;
        } else if (idx == IDX_ARRAY) {
            return getArrayClass(readTypeIndex(buf));
        } else {
            return serTypes.get(idx);
        }
    }

    private static <T> Class getArrayClass(Class<T> component) {
        return Array.newInstance(component, 0).getClass();
    }

    /**
     * Serializes a object.
     * @param buf The buffer to serialize the object into
     * @param obj The object to be serialized, whose type MUST be previously registered using {@link #register(Class)}
     * @param nullable if paramterer is allowed to be <code>null</code>
     * @throws RuntimeException if serialization failed
     */
    @SuppressWarnings("unchecked")
    public static void serialize(ByteBuf buf, Object obj, boolean nullable) {
        if (obj == null) {
            if (nullable) {
                buf.writeShort(IDX_NULL);
            } else {
                throw new NullPointerException("Trying to serialize a null object where it's not accepted");
            }
        } else {
            Class type = obj.getClass();
            writeTypeIndex(buf, type);
            serializeWithHint(buf, obj, type);
        }
    }

    /**
     * serialize an object without writing the object type index into the buffer. Used to reduce data redundancy.
     * Use {@link #deserializeWithHint} with the same signature to recover the object.
     *
     * @param buf The buffer to serialize the object into
     * @param obj The object to be serialized, must not be null
     * @param type the top-level type used during serialization. When deserialization from the buf, same class should
     *  be used.
     */
    @SuppressWarnings("unchecked")
    public static <T> void serializeWithHint(ByteBuf buf, T obj, Class<? super T> type) {
        _check(obj != null, "Hintted serialization doesn't take null");

        NetS11nAdaptor<? super T> adaptor = (NetS11nAdaptor) _adaptor(type);
        if (adaptor != null) { // Serialize direct types
            adaptor.write(buf, obj);
        } else if (type.isEnum()) { // Serialize enum
            buf.writeByte(((Enum) obj).ordinal());
        } else if (type.isArray()) { // Serialize array
            int length = Array.getLength(obj);
            Preconditions.checkArgument(length < Short.MAX_VALUE, "Array too large");

            buf.writeShort(length);
            for (int i = 0; i < length; ++i) {
                serialize(buf, Array.get(obj, i), true);
            }
        } else { // Serialize recursive types
            serializeRecursively(buf, obj, type);
        }
    }

    public static <T> void serializeRecursively(ByteBuf buf, T obj, Class<? super T> type) {
        final List<Field> fields = _sortedFields(type);
        try {
            for(Field f : fields) {
                Object sub = f.get(obj);
                if (_needsTypeIndex(f)) {
                    boolean subNullable = f.isAnnotationPresent(SerializeNullable.class);
                    serialize(buf, sub, subNullable);
                } else {
                    serializeWithHint(buf, sub, (Class) f.getType());
                }
            }
        } catch (IllegalArgumentException|IllegalAccessException e) {
            throw new RuntimeException("Error serializing object " + obj, e);
        }
    }

    /**
     * Deserializes a object from given buf.
     * @return The deserialized object. Could be null.
     * @throws ContextException if deserialization is interrupted.
     * @throws RuntimeException if deserialization failed non-trivially.
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(ByteBuf buf) {
        Class type = readTypeIndex(buf);

        if (type == null) {
            return null;
        } else {
            Class<T> type2 = (Class<T>) type;
            return deserializeWithHint(buf, type2);
        }
    }

    /**
     * Deserialize an object with given type hint.
     * @return The deserialized object of type T.
     * @throws ContextException if deserialization is interrupted. That is, the object can't be restored out of
     *  contextual difference (e.g. Some entity exists on server, but not created in one client).
     * @throws RuntimeException if deserialization failed non-trivially.
     */
    public static <T, U extends T> T deserializeWithHint(ByteBuf buf, Class<U> type) {
        NetS11nAdaptor<? super U> adaptor = (NetS11nAdaptor) _adaptor(type);
        // System.out.println("adaptor " + type + " is " + adaptor);
        if (adaptor != null) {
            // Note that if adaptor's real type doesn't extend T there will be a cast exception.
            // With type erasure we can't do much about it.
            return (T) adaptor.read(buf);
        } else if (type.isArray()) { // Deserialize array
            int size = buf.readShort();
            Class componentType = type.getComponentType();

            Object ret = Array.newInstance(componentType, size);
            for (int i = 0; i < size; ++i) {
                Array.set(ret, i, deserialize(buf));
            }

            return (T) ret;
        } else if (type.isEnum()) { // Deserialize enum
            return type.getEnumConstants()[buf.readByte()];
        } else { // Recursive deserialization
            return deserializeRecursively(buf, type);
        }
    }

    public static <T, U extends T> T deserializeRecursively(ByteBuf buf, Class<U> type) {
        try {
            T instance = instantiate(type);
            deserializeRecursivelyInto(buf, instance, type);
            return instance;
        } catch (RuntimeException e) {
            throw new RuntimeException("Error deserializing type " + type, e);
        }
    }

    public static <T, U extends T> void deserializeRecursivelyInto(ByteBuf buf, T instance, Class<U> type) {
        for (Field f : _sortedFields(type)) {
            // Both nullable and porlymorphic s11n needs type index.
            Object sub;
            if (_needsTypeIndex(f)) {
                sub = deserialize(buf);
            } else {
                sub = deserializeWithHint(buf, f.getType());
            }

            try {
                f.set(instance, sub);
            } catch (IllegalArgumentException|IllegalAccessException exc) {
                throw new RuntimeException("Type mismatch in net s11n: expecting " +
                        f.getType() + ", found " + (sub != null ? sub.getClass() : "NULL"));
            }
        }
    }

    private static int typeIndex(Class<?> type) {
        Class<?> cur = type;
        while (cur != null) {
            int idx = serTypes.indexOf(cur);
            if (idx != -1) {
                return idx;
            }
            cur = cur.getSuperclass();
        }

        return -1;
    }

    private static List<Field> _sortedFields(Class<?> type) {
        List<Field> ret = fieldCache.get(type);
        if (ret == null) {
            ret = Lists.newArrayList(serHelper.getExposedFields(type));
            // Sort to preserve s11n order
            Collections.sort(ret, (lhs, rhs) -> lhs.getName().compareTo(rhs.getName()));
            fieldCache.put(type, ret);
        }

        return ret;
    }

    private static boolean _needsTypeIndex(Field f) {
        return  f.isAnnotationPresent(SerializeDynamic.class) ||
                f.isAnnotationPresent(SerializeNullable.class);
    }

    private static <T> NetS11nAdaptor<? super T> _adaptor(Class<T> topClass) {
        Class<? super T> cur = topClass;
        NetS11nAdaptor<? super T> ret = null;
        while (cur != null) {
            ret = adaptors.get(cur);
            if (ret != null) {
                return ret;
            }
            cur = cur.getSuperclass();
        }

        for (Class<?> itf : topClass.getInterfaces()) {
            ret = adaptors.get(itf);
            if (ret != null) {
                return ret;
            }
        }

        return ret;
    }

    private static <T> T instantiate(Class<T> type) {
        if (suppliers.containsKey(type)) {
            return (T) suppliers.get(type).get();
        }
        try {
            Constructor<T> ctor = type.getConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (Exception ex) {
            throw Throwables.propagate(ex);
        }
    }

    // default s11n types
    static {
        // QUESTION boxing/unboxing might not be performance wise, optimize?

        { // Byte
            NetS11nAdaptor<Byte> adp = new NetS11nAdaptor<Byte>() {
                public void write(ByteBuf buf, Byte value) {
                    buf.writeByte(value);
                }
                public Byte read(ByteBuf buf) {
                    return buf.readByte();
                }
            };
            addDirect(byte.class, adp);
            addDirect(Byte.class, adp);
        }
        { // Short
            NetS11nAdaptor<Short> adp = new NetS11nAdaptor<Short>() {
                public void write(ByteBuf buf, Short value) {
                    buf.writeShort(value);
                }
                public Short read(ByteBuf buf) {
                    return buf.readShort();
                }
            };
            addDirect(short.class, adp);
            addDirect(Short.class, adp);
        }
        { // Int
            NetS11nAdaptor<Integer> adp = new NetS11nAdaptor<Integer>() {
                public void write(ByteBuf buf, Integer value) {
                    buf.writeInt(value);
                }
                public Integer read(ByteBuf buf) {
                    return buf.readInt();
                }
            };
            addDirect(int.class, adp);
            addDirect(Integer.class, adp);
        }
        { // Float
            NetS11nAdaptor<Float> adp = new NetS11nAdaptor<Float>() {
                @Override
                public void write(ByteBuf buf, Float obj) {
                    buf.writeFloat(obj);
                }
                @Override
                public Float read(ByteBuf buf) {
                    return buf.readFloat();
                }
            };
            addDirect(float.class, adp);
            addDirect(Float.class, adp);
        }
        { // Double
            NetS11nAdaptor<Double> adp = new NetS11nAdaptor<Double>() {
                @Override
                public void write(ByteBuf buf, Double obj) {
                    buf.writeDouble(obj);
                }
                @Override
                public Double read(ByteBuf buf) {
                    return buf.readDouble();
                }
            };
            addDirect(double.class, adp);
            addDirect(Double.class, adp);
        }
        { // Boolean
            NetS11nAdaptor<Boolean> adp = new NetS11nAdaptor<Boolean>() {
                @Override
                public void write(ByteBuf buf, Boolean obj) {
                    buf.writeBoolean(obj);
                }
                @Override
                public Boolean read(ByteBuf buf) throws ContextException {
                    return buf.readBoolean();
                }
            };
            addDirect(boolean.class, adp);
            addDirect(Boolean.class, adp);
        }

        addDirect(String.class, new NetS11nAdaptor<String>() {
            @Override
            public void write(ByteBuf buf, String obj) {
                ByteBufUtils.writeUTF8String(buf, obj);
            }
            @Override
            public String read(ByteBuf buf) {
                return ByteBufUtils.readUTF8String(buf);
            }
        });

        addDirect(ByteBuf.class, new NetS11nAdaptor<ByteBuf>() {
            @Override
            public void write(ByteBuf buf, ByteBuf obj) {
                obj.readBytes(buf, obj.readableBytes());
            }
            @Override
            public ByteBuf read(ByteBuf buf) throws ContextException {
                ByteBuf buf_ = buf.duplicate();
                ByteBuf buf2 = Unpooled.buffer();
                buf_.readBytes(buf2, buf_.readableBytes());
                return buf2;
            }
        });

        // Meta: Class
        addDirect(Class.class, new NetS11nAdaptor<Class>() {
            @Override
            public void write(ByteBuf buf, Class obj) {
                int idx = serTypes.indexOf(obj);
                if (idx == -1) throw new IllegalArgumentException(obj + " is not a network s11n type");

                buf.writeInt(idx);
            }

            @Override
            public Class read(ByteBuf buf) throws ContextException {
                return serTypes.get(buf.readInt());
            }
        });

        // Basic collection types
        addDirect(List.class, new NetS11nAdaptor<List>() {
            @Override
            public void write(ByteBuf buf, List obj) {
                _check(obj.size() <= Byte.MAX_VALUE, "Too many objects to serialize");

                buf.writeByte(obj.size());
                for (Object elem : obj) {
                    serialize(buf, elem, true);
                }
            }
            @Override
            public List read(ByteBuf buf) {
                ArrayList<Object> ret = new ArrayList<>();
                int size = buf.readByte();
                for (int i = 0; i < size; ++i) {
                    ret.add(deserialize(buf));
                }
                return ret;
            }
        });
        addDirect(Map.class, new NetS11nAdaptor<Map>() {
            @Override
            public void write(ByteBuf buf, Map obj) {
                _check(obj.size() <= Byte.MAX_VALUE, "Too many objects to serialize");

                buf.writeByte(obj.size());
                final Set<Entry> set = obj.entrySet();
                for (Entry e : set) {
                    serialize(buf, e.getKey(), false);
                    serialize(buf, e.getValue(), true);
                }
            }
            @Override
            public Map read(ByteBuf buf) {
                HashMap<Object, Object> ret = new HashMap<>();
                int size = buf.readByte();
                for (int i = 0; i < size; ++i) {
                   ret.put(deserialize(buf), deserialize(buf));
                }
                return ret;
            }
        });
        addDirect(Set.class, new NetS11nAdaptor<Set>() {
            @Override
            public void write(ByteBuf buf, Set obj) {
                Preconditions.checkArgument(obj.size() < Short.MAX_VALUE, "Too many objects to serialize");

                buf.writeShort(obj.size());

                for (Object o : obj) {
                    serialize(buf, o, false);
                }
            }
            @Override
            public Set read(ByteBuf buf) throws ContextException {
                int size = buf.readShort();
                Set<Object> ret = new HashSet<>();

                while (size-- > 0) {
                    ret.add(deserialize(buf));
                }

                return ret;
            }
        });
        addDirect(BitSet.class, new NetS11nAdaptor<BitSet>() {
            @Override
            public void write(ByteBuf buf, BitSet obj) {
                byte[] bytes = obj.toByteArray();
                _check(bytes.length <= Byte.MAX_VALUE, "Too many bytes to write");
                buf.writeByte(bytes.length);
                buf.writeBytes(obj.toByteArray());
            }
            @Override
            public BitSet read(ByteBuf buf) {
                int readBytes = buf.readByte();
                byte[] bytes = buf.readBytes(readBytes).array();
                return BitSet.valueOf(bytes);
            }
        });
        //

        // MC types
        addDirect(NBTTagCompound.class, new NetS11nAdaptor<NBTTagCompound>() {
            @Override
            public void write(ByteBuf buf, NBTTagCompound obj) {
                ByteBufUtils.writeTag(buf, obj);
            }
            @Override
            public NBTTagCompound read(ByteBuf buf) {
                return ByteBufUtils.readTag(buf);
            }
        });
        addDirect(Entity.class, new NetS11nAdaptor<Entity>() {
            @Override
            public void write(ByteBuf buf, Entity obj) {
                buf.writeByte(obj.dimension);
                buf.writeInt(obj.getEntityId());
            }
            @Override
            public Entity read(ByteBuf buf) {
                World wrld = SideHelper.getWorld(buf.readByte());
                if (wrld == null) {
                    throw new ContextException("Invalid world");
                } else {
                    Entity ret = wrld.getEntityByID(buf.readInt());
                    if (ret == null) {
                        throw new ContextException("No entity with such ID");
                    } else {
                        return ret;
                    }
                }
            }
        });
        addDirect(World.class, new NetS11nAdaptor<World>() {
            @Override
            public void write(ByteBuf buf, World obj) {
                buf.writeByte(obj.provider.getDimension());
            }
            @Override
            public World read(ByteBuf buf) throws ContextException {
                World wrld = SideHelper.getWorld(buf.readByte());
                if (wrld == null) {
                    throw new ContextException("invalid world");
                } else {
                    return wrld;
                }
            }
        });
        addDirect(TileEntity.class, new NetS11nAdaptor<TileEntity>() {
            @Override
            public void write(ByteBuf buf, TileEntity obj) {
                serializeWithHint(buf, obj.getWorld(), World.class);

                buf.writeInt(obj.getPos().getX());
                buf.writeInt(obj.getPos().getY());
                buf.writeInt(obj.getPos().getZ());
            }
            @Override
            public TileEntity read(ByteBuf buf) throws ContextException {
                World world = deserializeWithHint(buf, World.class);

                TileEntity tileEntity = world.getTileEntity(new BlockPos(buf.readInt(), buf.readInt(), buf.readInt()));

                if (tileEntity == null) {
                    throw new ContextException("No such TileEntity is present");
                } else {
                    return tileEntity;
                }
            }
        });
        addDirect(BlockPos.class, new NetS11nAdaptor<BlockPos>() {
            @Override
            public void write(ByteBuf buf, BlockPos obj) {
                buf.writeInt(obj.getX());
                buf.writeInt(obj.getY());
                buf.writeInt(obj.getZ());
            }
            @Override
            public BlockPos read(ByteBuf buf) throws ContextException {
                return new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
            }
        });
        addDirect(ItemStack.class, new NetS11nAdaptor<ItemStack>() {
            @Override
            public void write(ByteBuf buf, ItemStack obj) {
                NBTTagCompound tag = new NBTTagCompound();
                obj.writeToNBT(tag);
                serializeWithHint(buf, tag, NBTTagCompound.class);
            }
            @Override
            public ItemStack read(ByteBuf buf) throws ContextException {
                NBTTagCompound tag = deserializeWithHint(buf, NBTTagCompound.class);
                return new ItemStack(tag);
            }
        });
    }

    private static void _check(boolean pred, String errmsg) {
        if (!pred) {
            throw new RuntimeException(errmsg);
        }
    }

}
