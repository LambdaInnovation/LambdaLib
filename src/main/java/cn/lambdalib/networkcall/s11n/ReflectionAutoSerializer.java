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
