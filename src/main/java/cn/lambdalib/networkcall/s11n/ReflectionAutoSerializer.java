/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.networkcall.s11n;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

public class ReflectionAutoSerializer implements DataSerializer {
    private Class clazz;
    private List<Field> fieldList = new ArrayList();
    private List<DataSerializer> serList = new ArrayList();
    
    public ReflectionAutoSerializer(Class clazz) {
        this.clazz = clazz;
        if (!clazz.isAnnotationPresent(RegSerializable.class)) {
            throw new RuntimeException("Trying to creating auto serializer for class " +
                    clazz.getCanonicalName() + ".");
        }
        for (Field f : clazz.getDeclaredFields()) {
            if (f.isAnnotationPresent(RegSerializable.SerializeField.class) &&
                    !Modifier.isStatic(f.getModifiers())) {
                //Now only support data serialization for fields.
                DataSerializer ser = SerializationManager.INSTANCE.getDataSerializer(f.getType());
                if (ser == null) {
                    throw new RuntimeException("Can not create serializer for " +  f.toString() + ".");
                }
                f.setAccessible(true);
                fieldList.add(f);
                serList.add(ser);
            }
        }
    }

    @Override
    public Object readData(NBTBase nbt, Object obj) throws Exception {
        if (obj == null) {
            throw new Exception("Auto serializer can not create new instance for class " + 
                    this.clazz.getCanonicalName() + ".");
        }
        if (!obj.getClass().equals(this.clazz)) {
            throw new Exception("Auto serializer for " + this.clazz.getCanonicalName() + 
                    " is used on " + obj.getClass().getCanonicalName() + ".");
        }
        NBTTagCompound tag = (NBTTagCompound) nbt;
        for (int i = 0; i < fieldList.size(); ++i) {
            Field f = fieldList.get(i);
            DataSerializer ser = serList.get(i);
            Object fieldValue = ser.readData(tag.getTag(f.getName()), f.get(obj));
            f.set(obj, fieldValue);
        }
        return obj;
    }

    @Override
    public NBTBase writeData(Object obj) throws Exception {
        NBTTagCompound tag = new NBTTagCompound();
        if (!obj.getClass().equals(this.clazz)) {
            throw new Exception("Auto serializer for " + this.clazz.getCanonicalName() + 
                    " is used on " + obj.getClass().getCanonicalName() + ".");
        }
        for (int i = 0; i < fieldList.size(); ++i) {
            Field f = fieldList.get(i);
            DataSerializer ser = serList.get(i);
            NBTBase fieldNBT = ser.writeData(f.get(obj));
            tag.setTag(f.getName(), fieldNBT);
        }
        return tag;
    }

}
