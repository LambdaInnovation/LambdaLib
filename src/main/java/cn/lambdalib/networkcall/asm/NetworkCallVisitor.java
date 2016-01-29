/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.networkcall.asm;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.networkcall.NetworkCallManager;
import cn.lambdalib.networkcall.RegNetworkCall;
import cpw.mods.fml.relauncher.Side;

public class NetworkCallVisitor extends ClassVisitor {
    
    private boolean isReg;
    private List<ClassMethod> methods = new ArrayList();
    private String className;

    public NetworkCallVisitor(int api, String className) {
        super(api);
        this.className = className;
    }
    
    public class ClassMethod {
        public String name;
        public String desc;
        public Side side;
        
        public ClassMethod(String name, String desc, Side side) {
            this.name = name;
            this.desc = desc;
            this.side = side;
        }
    }

    public boolean needTransform() {
        return isReg && !methods.isEmpty();
    }
    
    public ClassVisitor getTransformer(ClassWriter cw) {
        return new NetworkCallTransformer(this.api, cw, className, methods);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new NetworkCallMethodVisitor(this.api, name, desc);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (desc.equals(Type.getDescriptor(Registrant.class))) {
            isReg = true;
        }
        return null;
    }
    
    /**
     * This class picks all network-call methods and save them in {@link NetworkCallVisitor#methods}.
     *
     */
    private class NetworkCallMethodVisitor extends MethodVisitor {
        private String name;
        private String desc;
        
        public NetworkCallMethodVisitor(int api, String name, String desc) {
            super(api);
            this.name = name;
            this.desc = desc;
        }
        
        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            final ClassMethod cm = new ClassMethod(this.name, this.desc, Side.CLIENT);
            if (desc.equals(Type.getDescriptor(RegNetworkCall.class))) {
                methods.add(cm);
            }
            return new AnnotationVisitor(api, super.visitAnnotation(desc, visible)) {
                @Override
                public void visitEnum(String name, String desc, String value) {
                    if (name.equals("side")) {
                        if (value.equals(Side.SERVER.toString())) {
                            cm.side = Side.SERVER;
                        } else {
                            cm.side = Side.CLIENT;
                        }
                    }
                }
            };
        }
        
    }
    
}
