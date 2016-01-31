package cn.lambdalib.s11n.nbt;

import cn.lambdalib.core.LambdaLib;
import cn.lambdalib.s11n.SerializationHelper;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import net.minecraft.nbt.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles NBT (Notch Bull**** Tag) serialization.
 * @author WeAthFolD
 */
public class NBTS11n {

    public interface BaseSerializer<NBT extends NBTBase, T> {
        NBT write(T value);
        T   read(NBT tag, Class<? extends T> type);
    }

    public interface CompoundSerializer<T> {
        void write(NBTTagCompound tag, T value);
        void read (NBTTagCompound tag, T value);
    }

    private static SerializationHelper helper = new SerializationHelper();

    private static Map<Class, BaseSerializer> baseSerializerMap = new HashMap<>();
    private static Map<Class, CompoundSerializer> compoundSerializerMap = new HashMap<>();

    /**
     * Writes the object to given {@link NBTTagCompound}.
     * If the object is recursive (no custom helper that handles obj has been added via
     *  {@link #addCompound(Class, CompoundSerializer)}, fields according to the rules of core API will be written.
     * Otherwise, the custom helper will be used to write the object.
     */
    @SuppressWarnings("unchecked")
    public static void write(NBTTagCompound tag, Object obj) {
        Preconditions.checkNotNull(obj);

        final Class<?> type = obj.getClass();
        CompoundSerializer serializer = _compound(type);
        if (serializer != null) {
            serializer.write(tag, obj);
        } else { // recursive
            List<Field> fields = helper.getExposedFields(type);
            for (Field f : fields) {
                try {
                    String fieldName = f.getName();
                    Object value = f.get(obj);
                    if (value != null) {
                        tag.setTag(fieldName, writeBase(value));
                    }
                } catch (IllegalAccessException | IllegalArgumentException ex) {
                    LambdaLib.log.error("Error writing field " + f + " in object " + obj);
                }
            }
        }
    }

    /**
     * Reads the object from given {@link NBTTagCompound}
     * If the object is recursive (no custom helper that handles obj has been added via
     *  {@link #addCompound(Class, CompoundSerializer)}, fields according to the rules of core API will be read.
     * Otherwise, the custom helper will be used to read the object.
     */
    @SuppressWarnings("unchecked")
    public static void read(NBTTagCompound tag, Object obj) {
        Preconditions.checkNotNull(obj);

        final Class<?> type = obj.getClass();
        CompoundSerializer serializer = _compound(type);
        if (serializer != null) {
            serializer.read(tag, obj);
        } else { // recursive
            List<Field> fields = helper.getExposedFields(type);
            for (Field f : fields) {
                try {
                    String fieldName = f.getName();
                    NBTBase base = tag.getTag(fieldName);
                    if (base != null) {
                        f.set(obj, readBase(base, f.getType()));
                    }
                } catch (IllegalAccessException ex) {
                    LambdaLib.log.error("Error reading field " + f + " in object " + obj);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static NBTBase writeBase(Object obj) {
        Preconditions.checkNotNull(obj);

        final Class<?> type = obj.getClass();

        if (type.isEnum()) {
            return enumSer.write((Enum) obj);
        }

        BaseSerializer base = _base(type);

        if (base != null) {
            return base.write(obj);
        } else {
            NBTTagCompound tag = new NBTTagCompound();
            write(tag, obj);
            return tag;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T readBase(NBTBase base, Class<T> type) {
        Preconditions.checkNotNull(base);

        if (type.isEnum()) {
            return (T) enumSer.read((NBTTagByte) base, (Class) type);
        }

        BaseSerializer serializer = _base(type);
        if (serializer != null) {
            return (T) serializer.read(base, type);
        } else if (base instanceof NBTTagCompound) {
            try {
                NBTTagCompound tag = (NBTTagCompound) base;
                Constructor<T> ctor = type.getConstructor();
                ctor.setAccessible(true);
                T instance = ctor.newInstance();

                read(tag, instance);

                return instance;
            } catch (NoSuchMethodException|
                    InstantiationException|
                    IllegalAccessException|
                    InvocationTargetException ex) {
                throw Throwables.propagate(ex);
            }
        } else throw new RuntimeException("Doesn't support tag type " + base);
    }

    public static <T> void addBase(Class<T> type, BaseSerializer<?, T> serializer) {
        baseSerializerMap.put(type, serializer);
    }

    public static <T> void addCompound(Class<T> type, CompoundSerializer<? super T> serializer) {
        compoundSerializerMap.put(type, serializer);
    }

    /**
     * Make given type to be exposed in recursive s11n. Subtypes not included.
     */
    public static void regS11nType(Class<?> type) {
        helper.regS11nType(type);
    }

    @SuppressWarnings("unchecked")
    private static <T> BaseSerializer<?, T> _base(Class<T> type) {
        return baseSerializerMap.get(type);
    }

    @SuppressWarnings("unchecked")
    private static <T> CompoundSerializer<? super T> _compound(Class<T> type) {
        Class<? super T> cur = type;
        while (cur != null) {
            if (compoundSerializerMap.containsKey(type)) {
                return compoundSerializerMap.get(type);
            }
            cur = cur.getSuperclass();
        }
        return null;
    }

    static {
        {
            BaseSerializer<NBTTagByte, Byte> ser = new BaseSerializer<NBTTagByte, Byte>() {
                @Override
                public NBTTagByte write(Byte value) {
                    return new NBTTagByte(value);
                }
                @Override
                public Byte read(NBTTagByte tag, Class<? extends Byte> type) {
                    return tag.func_150290_f();
                }
            };
            addBase(byte.class, ser);
            addBase(Byte.class, ser);
        }
        {
            BaseSerializer<NBTTagShort, Short> ser = new BaseSerializer<NBTTagShort, Short>() {
                @Override
                public NBTTagShort write(Short value) {
                    return new NBTTagShort(value);
                }
                @Override
                public Short read(NBTTagShort tag, Class<? extends Short> type) {
                    return tag.func_150289_e();
                }
            };
            addBase(short.class, ser);
            addBase(Short.class, ser);
        }
        {
            BaseSerializer<NBTTagInt, Integer> ser = new BaseSerializer<NBTTagInt, Integer>() {
                @Override
                public NBTTagInt write(Integer value) {
                    return new NBTTagInt(value);
                }
                @Override
                public Integer read(NBTTagInt tag, Class<? extends Integer> type) {
                    return tag.func_150287_d();
                }
            };
            addBase(int.class, ser);
            addBase(Integer.class, ser);
        }
        {
            BaseSerializer<NBTTagFloat, Float> ser = new BaseSerializer<NBTTagFloat, Float>() {
                @Override
                public NBTTagFloat write(Float value) {
                    return new NBTTagFloat(value);
                }
                @Override
                public Float read(NBTTagFloat tag, Class<? extends Float> type) {
                    return tag.func_150288_h();
                }
            };
            addBase(float.class, ser);
            addBase(Float.class, ser);
        }
        {
            BaseSerializer<NBTTagDouble, Double> ser = new BaseSerializer<NBTTagDouble, Double>() {
                @Override
                public NBTTagDouble write(Double value) {
                    return new NBTTagDouble(value);
                }
                @Override
                public Double read(NBTTagDouble tag, Class<? extends Double> type) {
                    return tag.func_150286_g();
                }
            };
            addBase(double.class, ser);
            addBase(Double.class, ser);
        }
        {
            BaseSerializer<NBTTagLong, Long> ser = new BaseSerializer<NBTTagLong, Long>() {
                @Override
                public NBTTagLong write(Long value) {
                    return new NBTTagLong(value);
                }
                @Override
                public Long read(NBTTagLong tag, Class<? extends Long> type) {
                    return tag.func_150291_c();
                }
            };
            addBase(long.class, ser);
            addBase(Long.class, ser);
        }
        {
            BaseSerializer<NBTTagByte, Boolean> ser = new BaseSerializer<NBTTagByte, Boolean>() {
                @Override
                public NBTTagByte write(Boolean value) {
                    return new NBTTagByte((byte) (value ? 1 : 0));
                }
                @Override
                public Boolean read(NBTTagByte tag, Class<? extends Boolean> type) {
                    return tag.func_150290_f() != 0;
                }
            };
            addBase(boolean.class, ser);
            addBase(Boolean.class, ser);
        }
        {
            BaseSerializer<NBTTagByteArray, byte[]> ser = new BaseSerializer<NBTTagByteArray, byte[]>() {
                @Override
                public NBTTagByteArray write(byte[] value) {
                    return new NBTTagByteArray(value);
                }
                @Override
                public byte[] read(NBTTagByteArray tag, Class<? extends byte[]> type) {
                    return tag.func_150292_c();
                }
            };
            addBase(byte[].class, ser);
        }
        {
            BaseSerializer<NBTTagIntArray, int[]> ser = new BaseSerializer<NBTTagIntArray, int[]>() {
                @Override
                public NBTTagIntArray write(int[] value) {
                    return new NBTTagIntArray(value);
                }
                @Override
                public int[] read(NBTTagIntArray tag, Class<? extends int[]> type) {
                    return tag.func_150302_c();
                }
            };
            addBase(int[].class, ser);
        }
        {
            BaseSerializer<NBTTagString, String> ser = new BaseSerializer<NBTTagString, String>() {
                @Override
                public NBTTagString write(String value) {
                    return new NBTTagString(value);
                }
                @Override
                public String read(NBTTagString tag, Class<? extends String> type) {
                    return tag.func_150285_a_();
                }
            };
            addBase(String.class, ser);
        }
    }

    private static final BaseSerializer<NBTTagByte, Enum> enumSer = new BaseSerializer<NBTTagByte, Enum>() {
        @Override
        public NBTTagByte write(Enum value) {
            Preconditions.checkArgument(value.ordinal() < Byte.MAX_VALUE);
            return new NBTTagByte((byte) value.ordinal());
        }
        @Override
        public Enum read(NBTTagByte tag, Class<? extends Enum> type) {
            return type.getEnumConstants()[tag.func_150290_f()];
        }
    };

}
