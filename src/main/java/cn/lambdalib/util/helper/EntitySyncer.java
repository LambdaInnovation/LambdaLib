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
package cn.lambdalib.util.helper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.lambdalib.util.generic.RegistryUtils;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.DataWatcher.WatchableObject;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;

/**
 * A helper to help syncing fields within entity, which gets rid of the ANNOYING 
 * registering proccess. Supports all the type that is supported by DataWatcher. <br>
 * You should delegate the init() method within entityInit(), and update() method within onUpdate().
 * <br> The direction is always server -> client.
 * <br> The registered fields should be symmetric in two sides so that we can track the ID correctly.
 * 
 * <br> Currently EntitySyncer supports the following types:
 * <code>
 * <br>  * int, Integer
 * <br>  * float, Float
 * <br>  * short, Short
 * <br>  * byte, Byte
 * <br>  * String
 * <br>  * Entity
 * <br>  * ChunkCoordinates
 * <br>  * ItemStack
 * </code>
 * <br> More commonly used types will be added soon.
 * @author WeAthFolD
 */
public class EntitySyncer {
    
    public enum SyncType {
        /**
         * This field is only synchronized on startup.
         */
        ONCE,
        
        /**
         * This field is synchronized every tick when entity is alive.
         */
        RUNTIME
    }
    
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Synchronized {
        
        SyncType value() default SyncType.RUNTIME;
        
        boolean allowNull() default false;
        
    }
    
    boolean firstUpdate;
    
    final Entity entity;
    final DataWatcher dataWatcher;
    final HashMap<Integer, WatchableObject> watchedObjects;
    
    final List<SyncInstance> watched;
    
    static final Map<Class<?>, Type> typeMap = new HashMap();
    static final Map<Class<?>, Integer> idMap = new HashMap();
    
    static final Method mGetWatchedObject = RegistryUtils.getMethod(DataWatcher.class, "getWatchedObject", "func_75691_i", int.class);
    
    
    static final Fetcher 
        defaultFetcher = (EntitySyncer d, int id) -> (getWatchableObject(d, id).getObject()),
        entityFetcher = (EntitySyncer d, int id) -> { 
            Integer i = (Integer) defaultFetcher.supply(d, id);
            if(i == null) return null;
            
            Entity e = 
                d.entity.worldObj.getEntityByID(i); 
            return e;
        };
    
    static void put(Creator c, Fetcher f, Object d, Class... classes) {
        for(Class cc : classes)
            typeMap.put(cc, new Type(c, f, d));
    }

    static void put(Creator c, Object d, Class ...classes) {
        put(c, defaultFetcher, d, classes);
    }
    
    static final Creator
        byteCreator = (Object b) -> (byte) b,
        shortCreator = (Object b) -> (short) b,
        intCreator = (Object b) -> (int) b,
        floatCreator = (Object b) -> (float) b,
        stringCreator = (Object b) -> b.toString(),
        itemStackCreator = (Object s) -> ((ItemStack)s).copy(),
        ccCreator = (Object s) -> ((ChunkCoordinates)s),
        entityCreator = (Object s) -> ((Entity)s).getEntityId();
        
        
    static {
        put(byteCreator, Byte.valueOf((byte) 0), Byte.class, byte.class);
        put(shortCreator, Short.valueOf((short)0), Short.class, short.class);
        put(intCreator, Integer.valueOf(0), Integer.class, int.class);
        put(floatCreator, Float.valueOf(0), Float.class, float.class);
        put(stringCreator, (String)null, String.class);
        put(itemStackCreator, (ItemStack)null, ItemStack.class);
        put(ccCreator, (ChunkCoordinates)null, ChunkCoordinates.class);
        put(entityCreator, entityFetcher, Integer.valueOf(-1), Entity.class);
        
        idMap.put(Byte.class, 0);
        idMap.put(byte.class, 0);
        idMap.put(Short.class, 1);
        idMap.put(short.class, 1);
        idMap.put(Integer.class, 2);
        idMap.put(int.class, 2);
        idMap.put(Float.class, 3);
        idMap.put(float.class, 3);
        idMap.put(String.class, 4);
        idMap.put(ItemStack.class, 5);
        idMap.put(ChunkCoordinates.class, 6);
        
        idMap.put(Entity.class, 2);
    }
    

    
    public EntitySyncer(Entity ent) {
        entity = ent;
        dataWatcher = RegistryUtils.getFieldInstance(Entity.class, ent, "dataWatcher", "field_70180_af");
        watchedObjects = RegistryUtils.getFieldInstance(DataWatcher.class, dataWatcher, 
                "watchedObjects", "field_75695_b");
        watched = new ArrayList();
    }
    
    private static WatchableObject getWatchableObject(EntitySyncer w, int id) {
        try {
            return (WatchableObject) mGetWatchedObject.invoke(w.dataWatcher, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    interface Test {
        void call(Object o);
    }
    
    /**
     * Delegated when the entity enters entityInit().
     */
    public void init() {
        for(Field f : entity.getClass().getDeclaredFields()) {
            if(f.isAnnotationPresent(Synchronized.class)) {
                int id = nextID();
                Synchronized anno = f.getAnnotation(Synchronized.class);
                
                f.setAccessible(true);
                watched.add(new SyncInstance(id, f, anno));
            }
        }
    }
    
    /**
     * Delegated during entity onUpdate() tick.
     */
    public void update() {
        if(!firstUpdate) {
            firstUpdate = true;
            for(SyncInstance si : watched) {
                si.init();
            }
        } else {
            for(SyncInstance si : watched) {
                si.tick();
            }
        }
    }
    
    private int nextID() { 
        for(int i = 0;; ++i) {
            if(!watchedObjects.containsKey(i))
                return i;
        }
    }
    
    private class SyncInstance {
        
        protected final int id;
        protected final Field field;
        
        protected final Creator c;
        protected final Fetcher f;
        
        Synchronized anno;
        
        public SyncInstance(int id, Field f, Synchronized _anno) {
            this.id = id;
            field = f;
            anno = _anno;
            
            Type t = null;
            Class clazz = f.getType();
            while(t == null && clazz != null) {
                t = typeMap.get(clazz);
                if(t == null)
                    clazz = clazz.getSuperclass();
            }
            
            if(t == null)
                throw new UnsupportedOperationException("Unsupported sync type " + f.getType());
            
            c = t.creator;
            this.f = t.fetcher;
            
            int tid = idMap.get(clazz);
            
            dataWatcher.addObjectByDataType(id, tid);
            
            Object val = convert();
            if(val == null)
                val = t.defaultValue;
            
            dataWatcher.updateObject(id, val);
        }
        
        void init() {
            updateAll(true);
        }
        
        protected Object convert() {
            try {
                return c.supply(field.get(entity));
            } catch (Exception e) {
                return null;
            }
        }
        
        void tick() {
            updateAll(anno.value() == SyncType.RUNTIME);
        }
        
        private void updateAll(boolean doServer) {
            //System.out.println("Synchronizing " + field.getName());
            try {
                if(entity.worldObj.isRemote) {
                    Object obj = f.supply(EntitySyncer.this, id);
                    
                    if(obj != null || anno.allowNull()) {
                        field.set(entity, obj);
                    }
                } else {
                    if(doServer) {
                        Object o = convert();
                        if(o != null) {
                            dataWatcher.updateObject(id, o);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Sync failed");
            }
        }
        
    }

    private interface Creator<T, U> {
        T supply(U value);
    }
    
    private interface Fetcher {
        Object supply(EntitySyncer d, int id);
    }
    
    private static class Type {
        Creator creator;
        Fetcher fetcher;
        Object defaultValue;
        
        public Type(Creator _c, Fetcher _f, Object _d) {
            creator = _c;
            fetcher = _f;
            defaultValue = _d;
        }
    }
}
