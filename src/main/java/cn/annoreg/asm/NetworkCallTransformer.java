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

import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cpw.mods.fml.relauncher.Side;
import cn.annoreg.asm.NetworkCallVisitor.ClassMethod;
import cn.annoreg.mc.network.NetworkCallManager;

public class NetworkCallTransformer extends ClassVisitor {
    private List<ClassMethod> methods;
    private String className;
    
    public NetworkCallTransformer(int api, ClassVisitor cv, String className, List<ClassMethod> methods) {
        super(api, cv);
        this.className = className;
        this.methods = methods;
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        for (ClassMethod m : methods) {
            if (m.name == name && m.desc == desc) {
                switch (access & Opcodes.ACC_STATIC) {
                case Opcodes.ACC_STATIC:
                    return DelegateGenerator.generateStaticMethod(
                            this,
                            super.visitMethod(access, name, desc, signature, exceptions),
                            className, name, desc, m.side);
                default:
                    return DelegateGenerator.generateNonStaticMethod(
                            this,
                            super.visitMethod(access, name, desc, signature, exceptions),
                            className, name, desc, m.side);
                }
            }
        }
        //Not found
        return super.visitMethod(access, name, desc, signature, exceptions);
    }
}