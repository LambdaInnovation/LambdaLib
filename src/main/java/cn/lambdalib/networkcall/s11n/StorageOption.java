/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
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
