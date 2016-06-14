/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.annoreg.core;

import cn.lambdalib.annoreg.base.RegistrationEmpty;
import cn.lambdalib.core.LLModContainer;
import cn.lambdalib.core.Profiler;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import cpw.mods.fml.common.discovery.ASMDataTable.ASMData;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class RegistrationManager {

    public static final RegistrationManager INSTANCE = new RegistrationManager();

    public final Profiler profiler = new Profiler();
    
    private Set<String> unloadedClass = new HashSet<>();
    
    private Set<String> unloadedRegType;
    private Map<Class<? extends Annotation>, RegistryType> regByClass = new HashMap<>();
    private Map<String, RegistryType> regByName = new HashMap<>();
    
    private Map<String, RegModInformation> modMap = new HashMap<>();
    private Set<RegModInformation> mods = new HashSet<>();
    
    public void annotationList(Set<String> data) {
        unloadedClass.addAll(data);
    }
    
    private void loadClasses() {
        profiler.begin("loadClasses");
        loadRegistryTypes();
        for (String name : unloadedClass) {
            prepareClass(name);
        }
        unloadedClass.clear();
        profiler.end("loadClasses");
    }

    private void prepareClass(String name) {
        try {
            Class<?> clazz = Class.forName(name);

            //Class annotations
            for (Annotation anno : clazz.getAnnotations()) {
                Class<? extends Annotation> annoclazz = anno.annotationType();
                if (regByClass.containsKey(annoclazz)) {
                    regByClass.get(annoclazz).visitClass(clazz);
                }
            }

            //Field annotations
            for (Field field : clazz.getDeclaredFields()) {
                for (Annotation anno : field.getAnnotations()) {
                    Class<? extends Annotation> annoclazz = anno.annotationType();
                    if (regByClass.containsKey(annoclazz)) {
                        field.setAccessible(true);

                        regByClass.get(annoclazz).visitField(field);
                    }
                }
            }

            //Method annotations
            for (Method method : clazz.getDeclaredMethods()) {
                for (Annotation anno : method.getAnnotations()) {
                    Class<? extends Annotation> annoclazz = anno.annotationType();
                    if(regByClass.containsKey(annoclazz)) {
                        method.setAccessible(true);

                        regByClass.get(annoclazz).visitMethod(method);
                    }
                }
            }
        } catch (Throwable e) {
            LLModContainer.log.fatal("Error on loading class {}.", name);
            Throwables.propagate(e);
        }
    }
    
    RegModInformation findMod(AnnotationData data) {
        for (RegModInformation mod : mods) {
            Class<?> clazz = data.type == AnnotationData.Type.CLASS ?
                    data.getTheClass() : 
                    (data.type == AnnotationData.Type.FIELD ? data.getTheField().getDeclaringClass() :
                        data.getTheMethod().getDeclaringClass());
            if (clazz.getCanonicalName().startsWith(mod.getPackage())) {
                data.mod = mod;
                return mod;
            }
        }
        return null;
    }
    
    public void addRegType(RegistryType type) {
        if (type.annoClass == null) {
            if (regByName.containsKey(type.name)) {
                LLModContainer.log.error("Unable to add the registry type {}.", type.name);
                Thread.dumpStack();
                return;
            }
            regByName.put(type.name, type);
        } else {
            if (regByClass.containsKey(type.annoClass) ||
                    regByName.containsKey(type.name)) {
                LLModContainer.log.error("Unable to add the registry type {}.", type.name);
                Thread.dumpStack();
                return;
            }
            regByClass.put(type.annoClass, type);
            regByName.put(type.name, type);
        }
    }
    
    private RegModInformation createModFromObj(String modClassName) {
        if (modMap.containsKey(modClassName)) {
            return modMap.get(modClassName);
        }
        RegModInformation ret = new RegModInformation(modClassName);
        modMap.put(modClassName, ret);
        return ret;
    }

    public void addAnnotationMod(Set<String> data) {
        for (String typeName : data) {
            mods.add(createModFromObj(typeName));
        }
    }
    
    private void registerAll(RegModInformation mod, String type) {
        //First load all classes that have not been loaded.
        loadClasses();

        final String id = String.format("task %s:%s", mod.getModID(), type);
        profiler.begin(id);

        RegistryType rt = Preconditions.checkNotNull(regByName.get(type), "RegistryType " + type + " not found.");

        rt.registerAll(mod);

        profiler.end(id);
    }
    
    public void registerAll(Object mod, String type) {
        registerAll(createModFromObj(mod.getClass().getName()), type);
    }

    public void addRegistryTypes(Set<String> data) {
        unloadedRegType = data;
    }

    private void loadRegistryTypes() {
        for (String typeName : unloadedRegType) {
            try {
                Class<?> clazz = Class.forName(typeName);
                RegistryType rt = (RegistryType) clazz.newInstance();
                addRegType(rt);
            } catch (Exception e) {
                throw new RuntimeException("Error loading registry type " + typeName, e);
            }
        }
        unloadedRegType.clear();
    }
    
    public void checkLoadState() {
        for (RegistryType rt : regByName.values()) {
            rt.checkLoadState();
        }
    }
    
    public Set<RegModInformation> getMods() {
        return mods;
    }
    
    public void addDependencyFor(String type, String dep) {
        regByName.get(type).addDependency(dep);
    }
    
    static {
        for (LoadStage ls : LoadStage.values()) {
            INSTANCE.addRegType(new RegistrationEmpty(ls.name));
        }
    }
}
