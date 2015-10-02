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
package cn.annoreg.mc.network;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;

/**
 * Helper to convert objects into TargetPoint and do further network sending.
 * @author WeAthFolD
 */
public class TargetPointHelper {
	
	public static final double DEFAULT_RANGE = 16;
	
	public interface TargetPointConverter<T> {
		TargetPoint convert(T object, double range);
	}
	
	private static Map<Class<?>, TargetPointConverter> converters = new HashMap();
	
	public static void addConverter(Class<?> klass, TargetPointConverter conv) {
		if(converters.containsKey(klass)) {
			throw new RuntimeException("Cannot add multiple TargetPointConverter for class " 
					+ klass);
		}
		converters.put(klass, conv);
	}
	
	public static TargetPointConverter findConverter(Object obj) {
		Class klass = obj.getClass();
		TargetPointConverter conv;
		while(klass != null) {
			if((conv = converters.get(klass)) != null)
				return conv;
			klass = klass.getSuperclass();
		}
		return null;
	}
	
	public static TargetPoint convert(Object obj, double range) {
		TargetPointConverter conv = findConverter(obj);
		if(conv == null) {
			throw new UnsupportedOperationException("Didn't find TargetPoint converter for " + obj);
		}
		return conv.convert(obj, range);
	}
	
	static {
		addConverter(TargetPoint.class, new TargetPointConverter<TargetPoint>() {

			@Override
			public TargetPoint convert(TargetPoint object, double range) {
				return object;
			}
			
		});
		
		addConverter(Entity.class, new TargetPointConverter<Entity>() {

			@Override
			public TargetPoint convert(Entity object, double range) {
				if(range == -1)
					range = DEFAULT_RANGE;
				return new TargetPoint(object.dimension, object.posX, object.posY, object.posZ, range);
			}
			
		});
		
		addConverter(TileEntity.class, new TargetPointConverter<TileEntity>() {

			@Override
			public TargetPoint convert(TileEntity object, double range) {
				if(range == -1)
					range = DEFAULT_RANGE;
				return new TargetPoint(object.getWorldObj().provider.dimensionId,
						object.xCoord + 0.5, object.yCoord + 0.5, object.zCoord + 0.5, range);
			}
			
		});
	}
	
}
