/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.annoreg.asm;

import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.core.LLModContainer;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;

public class InnerClassVisitor extends ClassVisitor {
    
    private List<String> innerClasses = new ArrayList<>();
    private boolean isReg = false;

    public InnerClassVisitor(int api) {
        super(api);
    }
    
    @Override
    public void visitInnerClass(String slashedName, String outerName, String innerName, int access) {
        if (isReg) {
            String name = slashedName.replace('/', '.');
            if (!LLModContainer.isClassRemoved(name)) {
                innerClasses.add(name);
            }
        }
    }
    
    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (desc.equals(Type.getDescriptor(Registrant.class))) {
            isReg = true;
        }
        return null;
    }
    
    public List<String> getInnerClassList() {
        return innerClasses;
    }

}
