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
package cn.annoreg.asm;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import cpw.mods.fml.relauncher.Side;
import cn.annoreg.core.Registrant;
import cn.annoreg.core.RegistrationManager;
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
    		if (arg0.startsWith("cn.annoreg.")) {
    			return data;
    		}
    		ClassReader cr = new ClassReader(data);
    		
    		//Get inner class list for each class
    		{
    	        InnerClassVisitor cv = new InnerClassVisitor(Opcodes.ASM4);
    	        cr.accept(cv, 0);
    	        List<String> inner = cv.getInnerClassList();
    	        if (inner != null) {
    	            RegistrationManager.INSTANCE.addInnerClassList(arg0, inner);
    	        }
    		}
    		//Transform network-calls for each class
    		{
    		    NetworkCallVisitor cv = new NetworkCallVisitor(Opcodes.ASM4, arg0);
    		    cr.accept(cv, 0);
    	        if (cv.needTransform()) {
    	            ClassWriter cw = new ClassWriter(Opcodes.ASM4);
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
