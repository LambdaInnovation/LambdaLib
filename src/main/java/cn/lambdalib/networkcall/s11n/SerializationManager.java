/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.networkcall.s11n;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.apache.commons.lang3.ArrayUtils;

import cn.lambdalib.util.mc.SideHelper;
import cn.lambdalib.core.LLModContainer;
import cn.lambdalib.networkcall.Future;
import cn.lambdalib.networkcall.NetworkTerminal;

public class SerializationManager {
    
    public static final SerializationManager INSTANCE = new SerializationManager();
    
    static {
        INSTANCE.initInternalSerializers();
    }
    
    private Map<Class, DataSerializer> dataSerializers = new HashMap();
    private Map<Class, InstanceSerializer> instanceSerializers = new HashMap();
    private Map<String, InstanceSerializer> instanceSerializersFromId = new HashMap();
    
    private static final String NULL_TAG = "_n", COLLECTION_TAG = "_c";

    // should only return NBTTagCompound
    public NBTBase serialize(Object obj, StorageOption.Option option) {
        if(obj == null) { //Allow to pass over null instances.
            NBTTagCompound tag = new NBTTagCompound();
            tag.setBoolean(NULL_TAG, true);
            return tag;
        }
        Class clazz = obj.getClass();
        DataSerializer d = getDataSerializer(clazz);
        InstanceSerializer i = getInstanceSerializer(clazz);
        NBTTagCompound ret = new NBTTagCompound();
        ret.setString("class", clazz.getName());
        ret.setInteger("option", option.ordinal());
        switch (option) {
        case NULL:
            return ret;
        case DATA:
            try {
                if (d == null) {
                    if (obj instanceof Collection) {
                        Collection coll = (Collection) obj;
                        NBTTagList list = new NBTTagList();
                        for (Object element : coll) {
                            list.appendTag(serialize(element, StorageOption.Option.DATA));
                        }
                        ret.setBoolean(COLLECTION_TAG, true);
                        ret.setTag("collection", list);
                        return ret;
                    } else {
                        throw new RuntimeException("Serializer not found.");
                    }
                }
                
                ret.setTag("data", d.writeData(obj));
                return ret;
            } catch (Exception e) {
                LLModContainer.log.error("Failed in data serialization. Class: {}.", clazz.getCanonicalName());
                e.printStackTrace();
                return null;
            }
        case INSTANCE:
        case NULLABLE_INSTANCE:
            try {
                if (i == null) {
                    if (obj instanceof Collection) {
                        Collection coll = (Collection) obj;
                        NBTTagList list = new NBTTagList();
                        for (Object element : coll) {
                            list.appendTag(serialize(element, StorageOption.Option.INSTANCE));
                        }
                        ret.setBoolean(COLLECTION_TAG, true);
                        ret.setTag("collection", list);
                        return ret;
                    } else {
                        throw new RuntimeException("Serializer not found.");
                    }
                }
                
                ret.setTag("instance", i.writeInstance(obj));
                //we need to remember the class of i (also the id of i), not of obj.
                ret.setString("class", i.getClass().getName());
                return ret;
            } catch (Exception e) {
                LLModContainer.log.error("Failed in instance serialization. Class: {}, Object: {}", clazz.getCanonicalName(), obj.toString());
                e.printStackTrace();
                return null;
            }
        case UPDATE:
            try {
                if (i == null || d == null) {
                    throw new RuntimeException("Serializer not found.");
                }
                ret.setTag("instance", i.writeInstance(obj));
                ret.setTag("data", d.writeData(obj));
                return ret;
            } catch (Exception e) {
                LLModContainer.log.error("Failed in update serialization. Class: {}.", clazz.getCanonicalName());
                e.printStackTrace();
                return null;
            }
        default:
            LLModContainer.log.error("Failed in serialization. Class: {}. Unknown option.",
                    clazz.getCanonicalName());
            Thread.dumpStack();
            return null;
        }
    }
    
    //use null in obj if the instance is unknown.
    public Object deserialize(Object obj, NBTBase nbt, StorageOption.Option option) {
        NBTTagCompound tag = (NBTTagCompound) nbt;
        Class<?> clazz = null;
        
        if(tag.getBoolean(NULL_TAG)) {
            return null;
        }
        if (tag.getBoolean(COLLECTION_TAG)) {
            //create the container
            try {
                clazz = Class.forName(tag.getString("class"));
                Collection coll = (Collection) clazz.newInstance();
                NBTTagList nbtcoll = (NBTTagList) tag.getTag("collection");
                for (int i = 0; i < nbtcoll.tagCount(); ++i) {
                    Object o = deserialize(null, nbtcoll.getCompoundTagAt(i), option);
                    if(o != null) coll.add(o);
                }
                return coll;
            } catch (Exception e) {
                LLModContainer.log.error("Failed in deserialization. Class: {}.", tag.getString("class"));
                e.printStackTrace();
                return null;
            }
        }
        
        if (option == StorageOption.Option.AUTO) {
            option = StorageOption.Option.values()[tag.getInteger("option")];
        }
        if (tag.getInteger("option") != option.ordinal()) {
            LLModContainer.log.error("Failed in deserialization. Class: {}.", tag.getString("class"));
            Thread.dumpStack();
        }
        NBTBase data = tag.getTag("data");
        NBTBase ins = tag.getTag("instance");
        switch (option) {
        case NULL:
            return null;
        case DATA:
        {
            try {
                clazz = Class.forName(tag.getString("class"));
            } catch (ClassNotFoundException e) {
                LLModContainer.log.error("Failed in deserialization. Class: {}.", tag.getString("class"));
                e.printStackTrace();
                return null;
            }
            DataSerializer ser = getDataSerializer(clazz);
            try {
                return ser.readData(data, obj);
            } catch (Exception e) {
                LLModContainer.log.error("Failed in data deserialization. Class: {}.",
                        tag.getString("class"));
                e.printStackTrace();
                return null;
            }
        }
        case INSTANCE:
        {
            InstanceSerializer ser = instanceSerializersFromId.get(tag.getString("class"));
            try {
                if (ser == null)
                    throw new RuntimeException("Can not find instance serializer with id " + tag.getString("class"));
                Object ret = ser.readInstance(ins);
                if (ret == null) {
                    throw new NullInstanceException();
                }
                return ret;
            } catch (NullInstanceException e) {
                throw e;
            } catch (Exception e) {
                LLModContainer.log.error("Failed in instance deserialization. Class: {}.",
                        tag.getString("class"));
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        case NULLABLE_INSTANCE:
        {
            InstanceSerializer ser = instanceSerializersFromId.get(tag.getString("class"));
            try {
                if (ser == null)
                    throw new RuntimeException("Can not find instance serializer with id " + tag.getString("class"));
                return ser.readInstance(ins);
            } catch (Exception e) {
                LLModContainer.log.error("Failed in instance deserialization. Class: {}.",
                        tag.getString("class"));
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        case UPDATE:
        {
            InstanceSerializer i = instanceSerializersFromId.get(tag.getString("class"));
            try {
                Object objIns = i.readInstance(ins);
                if (objIns == null) {
                    throw new Exception("Instance is null.");
                }
                DataSerializer d = getDataSerializer(objIns.getClass());
                d.readData(data, objIns);
                return objIns;
            } catch (Exception e) {
                LLModContainer.log.error("Failed in update deserialization. Class: {}.",
                        tag.getString("class"));
                e.printStackTrace();
                return null;
            }
        }
        default:
            LLModContainer.log.error("Failed in deserialization. Class: {}. Unknown option.",
                    tag.getString("class"));
            Thread.dumpStack();
            return null;
        }
    }
    
    public <T> InstanceSerializer<T> getInstanceSerializer(Class<T> clazz) {
        //We need to search for super classes
        Class c = clazz;
        while (c != null) {
            InstanceSerializer<T> ret = instanceSerializers.get(c);
            if (ret != null) {
                return ret;
            }
            c = c.getSuperclass();
        }
        return null;
    }
    
    public <T> DataSerializer<T> getDataSerializer(Class<T> clazz) {
        DataSerializer<T> ser = dataSerializers.get(clazz);
        if (ser == null && clazz.isAnnotationPresent(RegSerializable.class)) {
            ser = createAutoSerializerFor(clazz);
        }
        return ser;
    }
    
    public boolean hasDataSerializer(Class clazz) {
        return dataSerializers.containsKey(clazz);
    }
    
    private Set<Class> autoSerializerCreating = new HashSet();
    
    DataSerializer createAutoSerializerFor(Class<?> clazz) {
        if (autoSerializerCreating.contains(clazz)) {
            throw new RuntimeException("Circular dependencies in auto serializer.");
        }
        autoSerializerCreating.add(clazz);
        DataSerializer ret = new ReflectionAutoSerializer(clazz);
        autoSerializerCreating.remove(clazz);
        return ret;
    }
    
    void setDataSerializerFor(Class<?> clazz, DataSerializer serializer) {
        dataSerializers.put(clazz, serializer);
    }
    
    void setInstanceSerializerFor(Class<?> clazz, InstanceSerializer serializer) {
        instanceSerializers.put(clazz, serializer);
        instanceSerializersFromId.put(serializer.getClass().getName(), serializer);
    }
    
    private void initInternalSerializers() {
        //First part: java internal class.
        {
            InstanceSerializer ser = new InstanceSerializer<Enum>() {
                @Override
                public Enum readInstance(NBTBase nbt) throws Exception {
                    NBTTagCompound tag = (NBTTagCompound) nbt;
                    try {
                        Class enumClass = Class.forName(tag.getString("class"));
                        Object[] objs = (Object[]) enumClass.getMethod("values").invoke(null);
                        
                        return (Enum) objs[tag.getInteger("ordinal")];
                    } catch(Exception e) {
                        LLModContainer.log.error("Failed in enum deserialization. Class: {}.",
                                tag.getString("class"));
                        e.printStackTrace();
                        return null;
                    }
                }

                @Override
                public NBTBase writeInstance(Enum obj) throws Exception {
                    NBTTagCompound ret = new NBTTagCompound();
                    ret.setString("class", obj.getClass().getName());
                    ret.setByte("ordinal", (byte) ((Enum)obj).ordinal());
                    return ret;
                }
            };
            setInstanceSerializerFor(Enum.class, ser);
        }
        {
            DataSerializer ser = new DataSerializer<Byte>() {
                @Override
                public Byte readData(NBTBase nbt, Byte obj) throws Exception {
                    return ((NBTTagByte) nbt).func_150290_f();
                }

                @Override
                public NBTBase writeData(Byte obj) throws Exception {
                    return new NBTTagByte(obj);
                }
            };
            setDataSerializerFor(Byte.TYPE, ser);
            setDataSerializerFor(Byte.class, ser);
        }
        {
            DataSerializer ser = new DataSerializer<Byte[]>() {
                @Override
                public Byte[] readData(NBTBase nbt, Byte[] obj) throws Exception {
                    return ArrayUtils.toObject(((NBTTagByteArray) nbt).func_150292_c());
                }

                @Override
                public NBTBase writeData(Byte[] obj) throws Exception {
                    return new NBTTagByteArray(ArrayUtils.toPrimitive(obj));
                }
            };
            setDataSerializerFor(Byte[].class, ser);
        }
        {
            DataSerializer ser = new DataSerializer<byte[]>() {
                @Override
                public byte[] readData(NBTBase nbt, byte[] obj) throws Exception {
                    return ((NBTTagByteArray) nbt).func_150292_c();
                }

                @Override
                public NBTBase writeData(byte[] obj) throws Exception {
                    return new NBTTagByteArray(obj);
                }
            };
            setDataSerializerFor(byte[].class, ser);
        }
        {
            DataSerializer ser = new DataSerializer<Double>() {
                @Override
                public Double readData(NBTBase nbt, Double obj) throws Exception {
                    return ((NBTTagDouble) nbt).func_150286_g();
                }

                @Override
                public NBTBase writeData(Double obj) throws Exception {
                    return new NBTTagDouble(obj);
                }
            };
            setDataSerializerFor(Double.TYPE, ser);
            setDataSerializerFor(Double.class, ser);
        }
        {
            DataSerializer ser = new DataSerializer<Float>() {
                @Override
                public Float readData(NBTBase nbt, Float obj) throws Exception {
                    return ((NBTTagFloat) nbt).func_150288_h();
                }

                @Override
                public NBTBase writeData(Float obj) throws Exception {
                    return new NBTTagFloat(obj);
                }
            };
            setDataSerializerFor(Float.TYPE, ser);
            setDataSerializerFor(Float.class, ser);
        }
        {
            DataSerializer ser = new DataSerializer<Integer>() {
                @Override
                public Integer readData(NBTBase nbt, Integer obj) throws Exception {
                    return ((NBTTagInt) nbt).func_150287_d();
                }

                @Override
                public NBTBase writeData(Integer obj) throws Exception {
                    return new NBTTagInt(obj);
                }
            };
            setDataSerializerFor(Integer.TYPE, ser);
            setDataSerializerFor(Integer.class, ser);
        }
        {
            DataSerializer ser = new DataSerializer<Integer[]>() {
                @Override
                public Integer[] readData(NBTBase nbt, Integer[] obj) throws Exception {
                    return ArrayUtils.toObject(((NBTTagIntArray) nbt).func_150302_c());
                }

                @Override
                public NBTBase writeData(Integer[] obj) throws Exception {
                    return new NBTTagIntArray(ArrayUtils.toPrimitive(obj));
                }
            };
            setDataSerializerFor(Integer[].class, ser);
        }
        {
            DataSerializer ser = new DataSerializer<int[]>() {
                @Override
                public int[] readData(NBTBase nbt, int[] obj) throws Exception {
                    return ((NBTTagIntArray) nbt).func_150302_c();
                }

                @Override
                public NBTBase writeData(int[] obj) throws Exception {
                    return new NBTTagIntArray(obj);
                }
            };
            setDataSerializerFor(int[].class, ser);
        }
        {
            DataSerializer ser = new DataSerializer<Long>() {
                @Override
                public Long readData(NBTBase nbt, Long obj) throws Exception {
                    return ((NBTTagLong) nbt).func_150291_c();
                }

                @Override
                public NBTBase writeData(Long obj) throws Exception {
                    return new NBTTagLong(obj);
                }
            };
            setDataSerializerFor(Long.TYPE, ser);
            setDataSerializerFor(Long.class, ser);
        }
        {
            DataSerializer ser = new DataSerializer<Short>() {
                @Override
                public Short readData(NBTBase nbt, Short obj) throws Exception {
                    return ((NBTTagShort) nbt).func_150289_e();
                }

                @Override
                public NBTBase writeData(Short obj) throws Exception {
                    return new NBTTagShort(obj);
                }
            };
            setDataSerializerFor(Short.TYPE, ser);
            setDataSerializerFor(Short.class, ser);
        }
        {
            DataSerializer ser = new DataSerializer<String>() {
                @Override
                public String readData(NBTBase nbt, String obj) throws Exception {
                    return ((NBTTagString) nbt).func_150285_a_();
                }

                @Override
                public NBTBase writeData(String obj) throws Exception {
                    return new NBTTagString(obj);
                }
            };
            setDataSerializerFor(String.class, ser);
        }
        {
            //TODO: Maybe there is a more data-friendly method?
            DataSerializer ser = new DataSerializer<Boolean>() {
                @Override
                public Boolean readData(NBTBase nbt, Boolean obj) throws Exception {
                    return ((NBTTagCompound)nbt).getBoolean("v");
                }

                @Override
                public NBTBase writeData(Boolean obj) throws Exception {
                    NBTTagCompound tag = new NBTTagCompound();
                    tag.setBoolean("v", obj);
                    return tag;
                }
            };
            setDataSerializerFor(Boolean.class, ser);
            setDataSerializerFor(Boolean.TYPE, ser);
        }
        
        //Second part: Minecraft objects.
        {
            DataSerializer ser = new DataSerializer<NBTTagCompound>() {
                @Override
                public NBTTagCompound readData(NBTBase nbt, NBTTagCompound obj) throws Exception {
                    return (NBTTagCompound) nbt;
                }

                @Override
                public NBTBase writeData(NBTTagCompound obj) throws Exception {
                    return obj;
                }
            };
            setDataSerializerFor(NBTTagCompound.class, ser);
        }
        {
            InstanceSerializer ser = new InstanceSerializer<Entity>() {
                @Override
                public Entity readInstance(NBTBase nbt) throws Exception {
                    int[] ids = ((NBTTagIntArray) nbt).func_150302_c();
                    World world = SideHelper.getWorld(ids[0]);
                    if (world != null) {
                        return world.getEntityByID(ids[1]);
                    }
                    return null;
                }

                @Override
                public NBTBase writeInstance(Entity obj) throws Exception {
                    return new NBTTagIntArray(new int[] { obj.dimension, obj.getEntityId() });
                }
            };
            setInstanceSerializerFor(Entity.class, ser);
        }
        {
            InstanceSerializer ser = new InstanceSerializer<TileEntity>() {
                @Override
                public TileEntity readInstance(NBTBase nbt) throws Exception {
                    int[] ids = ((NBTTagIntArray) nbt).func_150302_c();
                    World world = SideHelper.getWorld(ids[0]);
                    if (world != null) {
                        return world.getTileEntity(ids[1], ids[2], ids[3]);
                    }
                    return null;
                }

                @Override
                public NBTBase writeInstance(TileEntity obj) throws Exception {
                    return new NBTTagIntArray(new int[] { obj.getWorldObj().provider.dimensionId,
                            obj.xCoord, obj.yCoord, obj.zCoord });
                }
            };
            setInstanceSerializerFor(TileEntity.class, ser);
        }
        {
            //TODO this implementation can not be used to serialize player's inventory container.
            InstanceSerializer ser = new InstanceSerializer<Container>() {
                @Override
                public Container readInstance(NBTBase nbt) throws Exception {
                    int[] ids = ((NBTTagIntArray) nbt).func_150302_c();
                    World world = SideHelper.getWorld(ids[0]);
                    if (world != null) {
                        Entity entity = world.getEntityByID(ids[1]);
                        if (entity instanceof EntityPlayer) {
                            return SideHelper.getPlayerContainer((EntityPlayer) entity, ids[2]);
                        }
                    }
                    return SideHelper.getPlayerContainer(null, ids[2]);
                }

                @Override
                public NBTBase writeInstance(Container obj) throws Exception {
                    EntityPlayer player = SideHelper.getThePlayer();
                    if (player != null) {
                        //This is on client. The server needs player to get the Container.
                        return new NBTTagIntArray(new int[] { player.worldObj.provider.dimensionId,
                                player.getEntityId(), obj.windowId});
                    } else {
                        //This is on server. The client doesn't need player (just use thePlayer), use MAX_VALUE here.
                        return new NBTTagIntArray(new int[] { Integer.MAX_VALUE, 0, obj.windowId});
                    }
                }
            };
            setInstanceSerializerFor(Container.class, ser);
        }
        {
            InstanceSerializer ser = new InstanceSerializer<World>() {

                @Override
                public World readInstance(NBTBase nbt) throws Exception {
                    return SideHelper.getWorld(((NBTTagInt) nbt).func_150287_d());
                }

                @Override
                public NBTBase writeInstance(World obj) throws Exception {
                    return new NBTTagInt(obj.provider.dimensionId);
                }
                
            };
            setInstanceSerializerFor(World.class, ser);
        }
        {
            DataSerializer ser = new DataSerializer<ItemStack>() {
                @Override
                public ItemStack readData(NBTBase nbt, ItemStack obj) throws Exception {
                    if (obj == null) {
                        return ItemStack.loadItemStackFromNBT((NBTTagCompound) nbt);
                    } else {
                        obj.readFromNBT((NBTTagCompound) nbt);
                        return obj;
                    }
                }

                @Override
                public NBTBase writeData(ItemStack obj) throws Exception {
                    NBTTagCompound nbt = new NBTTagCompound();
                    obj.writeToNBT(nbt);
                    return nbt;
                }
            };
            setDataSerializerFor(ItemStack.class, ser);
        }
        {
            DataSerializer ser = new DataSerializer<Vec3>() {
                @Override
                public Vec3 readData(NBTBase nbt, Vec3 obj) throws Exception {
                    NBTTagCompound tag = (NBTTagCompound) nbt;
                    return Vec3.createVectorHelper(tag.getFloat("x"), tag.getFloat("y"), tag.getFloat("z"));
                }

                @Override
                public NBTBase writeData(Vec3 obj) throws Exception {
                    NBTTagCompound nbt = new NBTTagCompound();
                    nbt.setFloat("x", (float) obj.xCoord);
                    nbt.setFloat("y", (float) obj.yCoord);
                    nbt.setFloat("z", (float) obj.zCoord);
                    return nbt;
                }
            };
            setDataSerializerFor(Vec3.class, ser);
        }
        //network part
        {
            DataSerializer ser = new DataSerializer<NetworkTerminal>() {
                @Override
                public NetworkTerminal readData(NBTBase nbt, NetworkTerminal obj) throws Exception {
                    return NetworkTerminal.fromNBT(nbt);
                }

                @Override
                public NBTBase writeData(NetworkTerminal obj) throws Exception {
                    return obj.toNBT();
                }
            };
            setDataSerializerFor(NetworkTerminal.class, ser);
        }
        {
            Future.FutureSerializer ser = new Future.FutureSerializer();
            setDataSerializerFor(Future.class, ser);
            setInstanceSerializerFor(Future.class, ser);
        }
        //misc
        {
            DataSerializer ser = new DataSerializer<BitSet>() {

                @Override
                public BitSet readData(NBTBase nbt, BitSet obj)
                        throws Exception {
                    NBTTagCompound tag = (NBTTagCompound) nbt;
                    BitSet ret = BitSet.valueOf(tag.getByteArray("l"));
                    return ret;
                }

                @Override
                public NBTBase writeData(BitSet obj) throws Exception {
                    NBTTagCompound tag = new NBTTagCompound();
                    byte[] barray = obj.toByteArray();
                    tag.setByteArray("l", barray);
                    return tag;
                }
                
            };
            
            setDataSerializerFor(BitSet.class, ser);
        }
    }
}
