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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.lambdalib.annoreg.core.AnnotationData.Type;
import cn.lambdalib.core.LLModContainer;

public abstract class RegistryType {
	
	private Map<RegModInformation, List<AnnotationData>> data = new HashMap();
	private List<AnnotationData> unknownData = new LinkedList();
	
	public final Class<? extends Annotation> annoClass;
	public final String name;
	
	private Set<RegModInformation> loadedMods = new HashSet();
	private Set<String> dependencies = new HashSet();
	
	public RegistryHelper helper = new RegistryHelper(this);
	
	public RegistryType(Class<? extends Annotation> annoClass, String name) {
		this.annoClass = annoClass;
		this.name = name;
	}
	
	public RegistryType(String name) {
		this(null, name);
	}
	
	public void addDependency(String dep) {
		dependencies.add(dep);
	}
	
	protected void setLoadStage(LoadStage stage) {
		RegistrationManager.INSTANCE.addDependencyFor(stage.name, this.name);
	}
	
	private void newData(AnnotationData anno) {
		RegModInformation mod = RegistrationManager.INSTANCE.findMod(anno);
		if (mod != null) {
			if (!data.containsKey(mod)) data.put(mod, new LinkedList());
			data.get(mod).add(anno);
		} else {
			unknownData.add(anno);
		}
	}
	
	public void visitClass(Class<?> clazz) {
		Annotation anno = clazz.getAnnotation(annoClass);
		if (anno != null) {
			newData(new AnnotationData(anno, clazz));
		}
	}
	
	public void visitField(Field field) {
		Annotation anno = field.getAnnotation(annoClass);
		if (anno != null) {
			newData(new AnnotationData(anno, field));
		}
	}
	
	public void visitMethod(Method method) {
		Annotation anno = method.getAnnotation(annoClass);
		if(anno != null) {
			newData(new AnnotationData(anno, method));
		}
	}
	
	public void registerAll(RegModInformation mod) {
		LLModContainer.log.info("Reg " + this.name);
		
		//Dependencies.
		for (String dep : dependencies) {
			RegistrationManager.INSTANCE.registerAll(mod.getModInstance(), dep);
		}
		
		loadedMods.add(mod);
		
		//First find if there's unknownData.
		Iterator<AnnotationData> itor = unknownData.iterator();
		while (itor.hasNext()) {
			AnnotationData ad = itor.next();
			RegModInformation rm = RegistrationManager.INSTANCE.findMod(ad);
			if (rm != null) {
				if (!data.containsKey(rm)) data.put(rm, new LinkedList());
				data.get(rm).add(ad);
				itor.remove();
			}
		}
		
		if (!data.containsKey(mod))	return;
		
		//Sort
		List<AnnotationData> regList = data.get(mod);
		Collections.sort(regList, new Comparator<AnnotationData>() {
			@Override
			public int compare(AnnotationData arg0, AnnotationData arg1) {
				if (arg0.type != arg1.type) {
					return arg0.type.compareTo(arg1.type);
				} else if (arg0.type == Type.CLASS) {
					return arg0.getTheClass().getCanonicalName().compareTo(arg1.getTheClass().getCanonicalName());
				} else {
					return arg0.getTheField().toString().compareTo(arg1.getTheField().toString());
				}
			}
		});

		//Do registration
		String entryPrefix = mod.getPrefix() + this.name + "_";
		this.currentMod = mod;
		
		itor = regList.iterator();
		while (itor.hasNext()) {
			AnnotationData ad = itor.next();
			switch (ad.type) {
			case CLASS:
				Class<?> theClass = ad.getTheClass();
				if (theClass.isAnnotationPresent(RegWithName.class)) {
					this.currentSuggestedName = theClass.getAnnotation(RegWithName.class).value();
				} else {
					this.currentSuggestedName = entryPrefix + ad.getTheClass().getSimpleName();
				}
				try {
					if (registerClass(ad))
						itor.remove();
				} catch (Exception e) {
					LLModContainer.log.error("Error when registering {}.", ad.toString());
					e.printStackTrace();
					itor.remove();
				}
				break;
			case FIELD:
				Field theField = ad.getTheField();
				if (theField.isAnnotationPresent(RegWithName.class)) {
					this.currentSuggestedName = theField.getAnnotation(RegWithName.class).value();
				} else {
					this.currentSuggestedName = entryPrefix + ad.getTheField().getName();
				}
				try {
					if (registerField(ad))
						itor.remove();
				} catch (Exception e) {
					LLModContainer.log.error("Error when registering {}.", ad.toString());
					e.printStackTrace();
					itor.remove();
				}
				break;
			case METHOD:
				Method theMethod = ad.getTheMethod();
				// METHOD doesn't need SuggestedName feature.
				try {
					if (registerMethod(ad))
						itor.remove();
				} catch (Exception e) {
					LLModContainer.log.error("Error when registering {}.", ad.toString());
					LLModContainer.log.error(e);
					itor.remove();
				}
				break;
			default:
				LLModContainer.log.error("Unknown registry data type.");
				break;
			}
		}
		
		this.currentSuggestedName = null;
		this.currentMod = null;
	}
	
	// CALLBACKS
	
	/**
	 * Return true to remove the data from list. 
	 * (For Command, reg is done each time the server is started, so can not always remove.)
	 * @param data
	 * @return
	 */
	public boolean registerClass(AnnotationData data) throws Exception {
		return true;
	}
	
	public boolean registerField(AnnotationData data) throws Exception {
		return true;
	}
	
	public boolean registerMethod(AnnotationData data) throws Exception {
		return true;
	}
	
	// CALLBACKS END
	
	public void checkLoadState() {
		for (RegModInformation mod : RegistrationManager.INSTANCE.getMods()) {
			if (!loadedMods.contains(mod)) {
				if (data.containsKey(mod) && !data.get(mod).isEmpty()) {
					LLModContainer.log.error("{} in mod {} is not registered.", this.name, mod.getModID());
					throw new RuntimeException();
				}
			}
		}
	}
	
	private RegModInformation currentMod;
	
	public RegModInformation getCurrentMod() {
		return currentMod;
	}
	
	private String currentSuggestedName;
	
	/**
	 * @return A readable suggested name for this item of registration. NULL for method registration process.
	 */
	protected String getSuggestedName() {
		return currentSuggestedName;
	}
}
