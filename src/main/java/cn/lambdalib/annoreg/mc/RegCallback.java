package cn.lambdalib.annoreg.mc;

import cn.lambdalib.annoreg.core.LoadStage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marked a method with this annotation, the method would be called at the stage where
 * you want.
 * To use this annotation, your class should have annotation @Registrant.
 * Created by Paindar on 2017.10.22.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RegCallback {

    LoadStage stage();
}
