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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;

public class StorageOption {

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Null {}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Data {}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Instance {
		boolean nullable() default false;
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Update {}

	/**
	 * Used only in server-to-client network call.
	 * Used on argument with the type of EntityPlayer.
	 * If Target is given, the message is only sent to this player.
	 * With this annotation, the StorageOption of INSTANCE is used.
	 * @author acaly
	 *
	 */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Target {
        
        public enum RangeOption {
            SINGLE,
            EXCEPT,
        }
        
        RangeOption range() default RangeOption.SINGLE;
        /**
         * The storage potion of this parameter. INSTANCE by default.
         */
        Option option() default Option.INSTANCE;
    }
    
    /**
     * Used only in server-to-client network call.
	 * Used on argument with the type that can be convert to a {@link cpw.mods.fml.common.network.NetworkRegistry.TargetPoint}
	 *  using {@link cn.lambdalib.networkcall.TargetPointHelper}.
	 * The message is then send around the given target point with given range parameter.
     * @author WeAthFolD
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RangedTarget {
    	//The hardcoded range option. The TargetPoint converter can choose to ignore this parameter.
    	double range() default -1; 
    	
    	/**
         * The storage potion of this parameter. INSTANCE by default.
         */
        Option option() default Option.INSTANCE;
    }
	
	public enum Option {
	    NULL,
		DATA,
		INSTANCE,
		UPDATE,
		
		/**
		 * Used only in deserialization.
		 * Will use the option contained in the data.
		 */
		AUTO,
		
		/**
		 * Allow the instance to be null.
		 * You can use this option directly, or use <code>@Instance(nullable=true)</code>
		 */
		NULLABLE_INSTANCE,
	}
}
