/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.networkcall.asm;

import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cn.lambdalib.networkcall.NetworkCallManager;
import cn.lambdalib.networkcall.asm.NetworkCallVisitor.ClassMethod;
import cpw.mods.fml.relauncher.Side;

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