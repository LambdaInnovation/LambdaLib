/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.annoreg.asm;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

import cn.lambdalib.annoreg.core.Registrant;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class InnerClassVisitor extends ClassVisitor {
    
    List<String> innerClasses = new ArrayList();
    boolean isReg = false;
    boolean clientOnly = false;

    public InnerClassVisitor(int api) {
        super(api);
    }
    
    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        innerClasses.add(name.replace('/', '.'));
    }
    
    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (desc.equals(Type.getDescriptor(SideOnly.class))) {
            //We need to know if it's client only.
            return new AnnotationVisitor(api) {
                @Override
                public void visitEnum(String name, String desc, String value) {
                    if (value == Side.CLIENT.toString()) {
                        InnerClassVisitor.this.clientOnly = true;
                    }
                }
            };
        }
        if (desc.equals(Type.getDescriptor(Registrant.class))) {
            isReg = true;
        }
        return null;
    }
    
    public List<String> getInnerClassList() {
        if (isReg && (FMLLaunchHandler.side() == Side.CLIENT || !clientOnly))
            return innerClasses;
        else {
            innerClasses.clear();
            return null;
        }
    }
}
