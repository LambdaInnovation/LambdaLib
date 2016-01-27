/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.core.asm;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import cn.lambdalib.annoreg.asm.InnerClassVisitor;
import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.annoreg.core.RegistrationManager;
import cn.lambdalib.networkcall.asm.NetworkCallVisitor;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.launchwrapper.IClassTransformer;

/**
 * This class currently does nothing.
 * @author acaly
 *
 */
public class RegistryTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String arg0, String arg1, byte[] data) {
        if (data == null) return data;
        try {
            if (arg0.startsWith("cn.lambdalib.annoreg.") || arg0.startsWith("cn.lambdalib.networkcall.")) {
                return data;
            }
            ClassReader cr = new ClassReader(data);
            
            //Get inner class list for each class
            {
                InnerClassVisitor cv = new InnerClassVisitor(Opcodes.ASM5);
                cr.accept(cv, 0);
                List<String> inner = cv.getInnerClassList();
                if (inner != null) {
                    RegistrationManager.INSTANCE.addInnerClassList(arg0, inner);
                }
            }
            //Transform network-calls for each class
            {
                NetworkCallVisitor cv = new NetworkCallVisitor(Opcodes.ASM5, arg0);
                cr.accept(cv, 0);
                if (cv.needTransform()) {
                    ClassWriter cw = new ClassWriter(Opcodes.ASM5);
                    cr.accept(cv.getTransformer(cw), 0);
                    data = cw.toByteArray();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException(t);
        }
        return data;
    }

}
