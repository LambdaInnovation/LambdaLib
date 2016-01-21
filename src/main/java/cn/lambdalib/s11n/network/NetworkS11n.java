package cn.lambdalib.s11n.network;

import cn.lambdalib.annoreg.mc.SideHelper;
import cn.lambdalib.s11n.SerializationHelper;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;

/**
 * This class handles recursive s11n on netty {@link ByteBuf}. <br>
 * TODO add hash validation in debug mode to help error detection (optional) <br>
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
         * Read the object from given ByteBuf
         * @param buf The buf to read object from
         * @return Deserialized object
         */
        T read(ByteBuf buf);

    }

    /**
     * Indicate that a type anticipates in network serialization. Equivalent to
     *  {@link NetworkS11n#register}.
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface NetworkS11nType {}

    /**
     * Indicate that a field can be null while being serialized in parent object.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SerializeNullable {}

    /**
     * Indicate that a field can be null while being deserialized into parent object. Violating
     *  will serialize the object as null and stop the event from being triggered.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DeserializeNonNull {}

    /**
     * To optimize data usage, type info about the recursive object's fields are emitted. That will result in
     *  serialization with type defined in the class, rather than its runtime type. If you want normal dynamic type
     *  behaviour to be present, annotate this on the field.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SerializeDynamic {}

    public static class InterruptException extends RuntimeException {
        public InterruptException(String msg) {
            super(msg);
        }
    }

    // ---
    private static final SerializationHelper serHelper = new SerializationHelper();

    private static final short IDX_NULL = -1;

    private static List<Class<?>> serTypes = new ArrayList<>();
    private static Map<Class<?>, NetS11nAdaptor> adaptors = new HashMap<>();

    private NetworkS11n() {}

    public static <T> void addDirect(Class<T> type, NetS11nAdaptor<? super T> adaptor) {
        register(type);
        adaptors.put(type, adaptor);
        serHelper.regS11nType(type);
    }

    /**
     * Registers an object for network s11n. You MUST do this on any object that will be serialized before
     *  any serialization is actually performed. The id table is used in both C and S to reduce data redundancy, so
     *  client programmers must keep registration in both sides consistent.
     */
    public static void register(Class<?> type) {
        if (!serTypes.contains(type)) {
            serTypes.add(type);
            serTypes.sort((lhs, rhs) -> lhs.getName().compareTo(rhs.getName()));
            serHelper.regS11nType(type);
        }

        if (serTypes.size() > Short.MAX_VALUE) {
            throw new RuntimeException("Too many objects registered for network serialization...");
        }
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
            final Class type = obj.getClass();
            final short typeIndex = (short) typeIndex(type);

            if (typeIndex == -1) {
                throw new RuntimeException("Type " + type + " not registered for net serialization");
            }

            buf.writeShort(typeIndex);
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

        NetS11nAdaptor<? super T> adaptor = _adaptor(type);
        if (adaptor != null) { // Serialize direct types
            adaptor.write(buf, obj);
        } else if (type.isEnum()) { // Serialize enum
            buf.writeByte(((Enum) obj).ordinal());
        } else { // Serialize recursive types
            final List<Field> fields = _sortedFields(type);

            try {
                for(Field f : fields) {
                    Object sub = f.get(obj);
                    if (_isHintted(f)) {
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
    }

    /**
     * Deserializes a object from given buf.
     * @return The deserialized object. Could be null.
     * @throws InterruptException if deserialization is interrupted.
     * @throws RuntimeException if deserialization failed non-trivially.
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(ByteBuf buf) {
        final short typeIndex = buf.readShort();
        if (typeIndex == IDX_NULL) {
            return null;
        } else {
            if (typeIndex >= serTypes.size()) {
                throw new RuntimeException("Invalid type index.");
            }

            final Class<T> type = (Class<T>) serTypes.get(typeIndex);
            return deserializeWithHint(buf, type);
        }
    }

    /**
     * Deserialize an object with given type hint.
     * @return The deserialized object of type T.
     * @throws InterruptException if deserialization is interrupted.
     * @throws RuntimeException if deserialization failed non-trivially.
     */
    public static <T, U extends T> T deserializeWithHint(ByteBuf buf, Class<U> type) {
        NetS11nAdaptor<? super U> adaptor = _adaptor(type);
        if (adaptor != null) {
            // Note that if adaptor's real type doesn't extend T there will be a cast exception.
            // With type erasure we can't do much about it.
            return (T) adaptor.read(buf);
        } else if (type.isEnum()) { // Deserialize enum
            return type.getEnumConstants()[buf.readByte()];
        } else { // Recursive deserialization
            try {
                T instance = type.newInstance();
                for (Field f : _sortedFields(type)) {
                    // Both nullable and porlymorphic s11n needs type index.
                    Object sub;
                    if (_isHintted(f)) {
                        sub = deserialize(buf);
                    } else {
                        sub = deserializeWithHint(buf, f.getType());
                    }

                    // Required to deserialize non-null instance yet got null. Drop this deserialization progress.
                    if (sub == null && f.isAnnotationPresent(DeserializeNonNull.class)) {
                        throw new InterruptException("Unexpected null object");
                    }

                    try {
                        f.set(instance, sub);
                    } catch (IllegalArgumentException exc) {
                        throw new RuntimeException("Type mismatch in net s11n: expecting " +
                                f.getType() + ", found " + (sub != null ? sub.getClass() : "NULL"));
                    }
                }

                return instance;
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Error deserializing type " + type, e);
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
        List<Field> fields = serHelper.getExposedFields(type);
        // Sort to preserve s11n order
        Collections.sort(fields, (lhs, rhs) -> lhs.getName().compareTo(rhs.getName()));
        return fields;
    }

    private static boolean _isHintted(Field f) {
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
        return ret;
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
                return wrld == null ? null : wrld.getEntityByID(buf.readInt());
            }
        });
    }

    private static void _check(boolean pred, String errmsg) {
        if (!pred) {
            throw new RuntimeException(errmsg);
        }
    }

}
