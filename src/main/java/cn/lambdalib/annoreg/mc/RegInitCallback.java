package cn.lambdalib.annoreg.mc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Populated on any <code>public static</code> methods to let it invoked during INITIALIZATION.
 * The order of method calls is arbitary. 
 * @author WeAthFolD
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RegInitCallback {

}
