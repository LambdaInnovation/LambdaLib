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
package cn.lambdalib.annoreg.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.lambdalib.annoreg.base.RegistrationEmpty;
import cn.lambdalib.core.LLModContainer;
import cn.lambdalib.core.LambdaLib;
import cpw.mods.fml.common.discovery.ASMDataTable.ASMData;

public class RegistrationManager {
	
	public static final RegistrationManager INSTANCE = new RegistrationManager();
	
	private Set<String> unloadedClass = new HashSet();
	private Set<Class<?>> loadedClass = new HashSet();
	
	private Set<ASMData> unloadedRegType;
	private Map<Class<? extends Annotation>, RegistryType> regByClass = new HashMap();
	private Map<String, RegistryType> regByName = new HashMap();
	
	private Map<String, RegModInformation> modMap = new HashMap();
	private Set<RegModInformation> mods = new HashSet();
	
	private Map<String, List<String>> innerClassList = new HashMap();
	
	public void annotationList(Set<ASMData> data) {
		for (ASMData asm : data) {
			unloadedClass.add(asm.getClassName());
		}
	}
	
	private void loadClasses() {
	    loadRegistryTypes();
		for (String name : unloadedClass) {
			try {
				prepareClass(Class.forName(name));
			} catch (ClassNotFoundException e) {
                LLModContainer.log.debug("Can not load class {}, maybe a SideOnly class.", name);
            } catch (Throwable e) {
				LLModContainer.log.fatal("Error on loading class {}. Please check the implementation.", name);
                LLModContainer.log.fatal(e);
			}
		}
		unloadedClass.clear();
	}
	
	private void prepareClass(Class<?> clazz) {
		//First check loadedClass to avoid infinite recursion (when extending the enclosing class).
		if (loadedClass.contains(clazz)) {
			return;
		}
		loadedClass.add(clazz);
		
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
					regByClass.get(annoclazz).visitField(field);
				}
			}
		}
		
		//Method annotations
		for (Method method : clazz.getDeclaredMethods()) {
			for (Annotation anno : method.getAnnotations()) {
				Class<? extends Annotation> annoclazz = anno.annotationType();
				if(regByClass.containsKey(annoclazz)) {
					regByClass.get(annoclazz).visitMethod(method);
				}
			}
		}
		
		
		//Inner classes
		if (innerClassList.containsKey(clazz.getName())) {
		    for (String inner : innerClassList.get(clazz.getName())) {
		        try {
                    prepareClass(Class.forName(inner));
	            } catch (Exception e) {
	                LLModContainer.log.warn("Can not load class {}, maybe a SideOnly class.", inner);
                } catch (Throwable e) {
                    LLModContainer.log.fatal("Error on loading class {}. Please check the implementation.", inner);
                    LLModContainer.log.fatal(e);
                }
		    }
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

	public void addAnnotationMod(Set<ASMData> data) {
		for (ASMData asm : data) {
		    mods.add(createModFromObj(asm.getClassName()));
		}
	}
	
	private void registerAll(RegModInformation mod, String type) {
		//First load all classes that have not been loaded.
		loadClasses();
		RegistryType rt = regByName.get(type);
		if (rt == null) {
			LLModContainer.log.error("RegistryType {} not found.", type);
			//TODO side only type go here.
			return;
		}
		rt.registerAll(mod);
	}
	
	public void registerAll(Object mod, String type) {
		registerAll(createModFromObj(mod.getClass().getName()), type);
	}

    public void addRegistryTypes(Set<ASMData> data) {
        unloadedRegType = data;
    }

    private void loadRegistryTypes() {
        for (ASMData asm : unloadedRegType) {
            try {
                Class<?> clazz = Class.forName(asm.getClassName());
                RegistryType rt = (RegistryType) clazz.newInstance();
                addRegType(rt);
            } catch (Exception e) {
            	if(LambdaLib.DEBUG) {
            		LLModContainer.log.warn("No registry type {}. Might be a SideOnly regtype.", asm.getClassName()); //TODO side only type will go here
            	}
            }
        }
        unloadedRegType.clear();
    }
	
	public void addSideOnlyRegAnnotation(Set<ASMData> data) {
		for (ASMData asm : data) {
			try {
				Class<?> clazz = Class.forName(asm.getClassName());
			} catch (Exception e) {
				LLModContainer.log.error("Error on adding registry annotation {}.", asm.getClassName());
			}
		}
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
	
	public void addInnerClassList(String outer, List<String> inner) {
	    if (innerClassList.containsKey(outer)) {
	        innerClassList.get(outer).addAll(inner);
	    } else {
	        innerClassList.put(outer, inner);
	    }
	}
	
	static {
		for (LoadStage ls : LoadStage.values()) {
			INSTANCE.addRegType(new RegistrationEmpty(ls.name));
		}
	}
}
