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
package cn.lambdalib.networkcall.asm;

import net.minecraft.entity.player.EntityPlayer;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import cn.lambdalib.networkcall.NetworkCallDelegate;
import cn.lambdalib.networkcall.NetworkCallManager;
import cn.lambdalib.networkcall.s11n.StorageOption;
import cpw.mods.fml.relauncher.Side;

public class DelegateGenerator {
    
    /**
     * Class loader used to generate delegate class.
     *
     */
    private static class DelegateClassLoader extends ClassLoader {
        public DelegateClassLoader() {
            super(NetworkCallDelegate.class.getClassLoader());
        }
        public Class<?> defineClass(String name, byte[] b) {
            return defineClass(name, b, 0, b.length);
        }
    }
    
    private static void pushIntegerConst(MethodVisitor mv, int val) {
        mv.visitLdcInsn(val);
        //mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Integer.class), 
        //        "intValue", Type.getMethodDescriptor(Type.INT_TYPE));
    }
    
    private static final DelegateClassLoader classLoader = new DelegateClassLoader();
    
    private static int delegateNextID = 0;
    
    public static MethodVisitor generateStaticMethod(ClassVisitor parentClass, MethodVisitor parent, 
            String className, String methodName, String desc, final Side side) {
        
        //This method is a little bit complicated.
        //We need to generate a delegate class implementing NetworkCallDelegate and a redirect
        //the code that originally generated here in parent, into the delegate class,
        //by returning a MethodVisitor under the ClassVisitor of the delegate class.
        //Besides, we should generate a call to NetworkCallManager into parent.
        
        //Above is the original method. Now it has a little bit change. To allow private call in
        //here, we need to generate the delegate method in this class instead of in a delegate class.
        //We make the delegate method public so that the delegate class can call it.
        
        //delegateName is a string used by both sides to identify a network-call delegate.
        final String delegateName = className + ":" + methodName + ":" + desc;
        final Type[] args = Type.getArgumentTypes(desc);
        final Type ret = Type.getReturnType(desc);
        
        //Check types
        for (Type t : args) {
            //TODO support these types
            if (!t.getDescriptor().startsWith("L") && !t.getDescriptor().startsWith("[")) {
                throw new RuntimeException("Unsupported argument type in network call. in method " + methodName + ", " + t.getDescriptor());
            }
        }
        if (!ret.equals(Type.VOID_TYPE)) {
            throw new RuntimeException("Unsupported return value type in network call. " + 
                    "Only void is supported.");
        }
        
        //Generate call to NetworkCallManager in parent.
        parent.visitCode();
        //First parameter
        parent.visitLdcInsn(delegateName);
        //Second parameter: object array
        pushIntegerConst(parent, args.length); //array size
        parent.visitTypeInsn(Opcodes.ANEWARRAY, Type.getInternalName(Object.class));
        for (int i = 0; i < args.length; ++i) {
            parent.visitInsn(Opcodes.DUP);
            pushIntegerConst(parent, i);
            parent.visitVarInsn(Opcodes.ALOAD, i);
            parent.visitInsn(Opcodes.AASTORE);
        }
        //Call cn.lambdalib.annoreg.mc.network.NetworkCallManager.onNetworkCall
        parent.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(NetworkCallManager.class), 
                "onNetworkCall", 
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class), Type.getType(Object[].class)));
        parent.visitInsn(Opcodes.RETURN);
        parent.visitMaxs(5, args.length);
        parent.visitEnd();
        
        //Create delegate object.
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        final String delegateId = Integer.toString(delegateNextID++);
        final Type delegateClassType = Type.getType("cn/lambdalib/networkcall/asm/NetworkCallDelegate_" + delegateId);
        cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, delegateClassType.getInternalName(), null, 
                Type.getInternalName(Object.class), new String[]{ Type.getInternalName(NetworkCallDelegate.class) });
        //package cn.lambdalib.annoreg.asm;
        //class NetworkCallDelegate_? implements NetworkCallDelegate {
        {
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, 
                    "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", "()V");
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        //public NetworkCallDelegate_?() {}
        
        final String delegateFunctionName = methodName + "_delegate_" + delegateId; 
        {
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, 
                    "invoke", 
                    Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object[].class)),
                    null, null);
            mv.visitCode();
            for (int i = 0; i < args.length; ++i) {
                mv.visitVarInsn(Opcodes.ALOAD, 1); //0 is this
                pushIntegerConst(mv, i);
                mv.visitInsn(Opcodes.AALOAD);
                mv.visitTypeInsn(Opcodes.CHECKCAST, args[i].getInternalName());
            }
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, 
                    //delegateClassType.getInternalName(), //changed to original class
                    className.replace('.', '/'),
                    delegateFunctionName, desc);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(args.length + 2, 2);
            mv.visitEnd();
        }
        //@Override public void invoke(Object[] args) {
        //    xxxx.xxxx_delegated_xxx((Type0) args[0], (Type1) args[1], ...);
        //}
        
        //The returned MethodVisitor will visit the original version of the method,
        //including its annotation, where we can get StorageOptions.
        return new MethodVisitor(Opcodes.ASM5, 
                parentClass.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, delegateFunctionName, desc, null, null)) {
            
            //Remember storage options for each argument
            StorageOption.Option[] options = new StorageOption.Option[args.length];
            int targetIndex = -1;
            StorageOption.Target.RangeOption range = StorageOption.Target.RangeOption.SINGLE;
            double sendRange = -1;
            
            {
                for (int i = 0; i < options.length; ++i) {
                    options[i] = StorageOption.Option.NULL; //set default value
                }
            }

            @Override
            public AnnotationVisitor visitParameterAnnotation(final int parameter, String desc, boolean visible) {
                Type type = Type.getType(desc);
                if (type.equals(Type.getType(StorageOption.Data.class))) {
                    options[parameter] = StorageOption.Option.DATA;
                } else if (type.equals(Type.getType(StorageOption.Instance.class))) {
                    //INSTANCE as defualt
                    options[parameter] = StorageOption.Option.INSTANCE;
                    
                    //Change to NULLABLE_INSTANCE if nullable set to true
                    return new AnnotationVisitor(this.api, super.visitParameterAnnotation(parameter, desc, visible)) {
                        @Override
                        public void visit(String name, Object value) {
                            if (name.equals("nullable")) {
                                if ((Boolean) value == true) {
                                    options[parameter] = StorageOption.Option.NULLABLE_INSTANCE;
                                }
                            }
                            super.visit(name, value);
                        }
                    };
                } else if (type.equals(Type.getType(StorageOption.Update.class))) {
                    options[parameter] = StorageOption.Option.UPDATE;
                } else if (type.equals(Type.getType(StorageOption.Null.class))) {
                    options[parameter] = StorageOption.Option.NULL;
                } else if (type.equals(Type.getType(StorageOption.Target.class))) {
                    if (!args[parameter].equals(Type.getType(EntityPlayer.class))) {
                        throw new RuntimeException("Target annotation can only be used on EntityPlayer.");
                    }
                    if(targetIndex != -1) {
                    	throw new RuntimeException("You can not specify multiple targets.");
                    }
                    options[parameter] = StorageOption.Option.INSTANCE;
                    targetIndex = parameter;
                    return new AnnotationVisitor(this.api, super.visitParameterAnnotation(parameter, desc, visible)) {
                    	boolean optionsVisited;
                    	
                        @Override
                        public void visitEnum(String name, String desc, String value) {
                            super.visitEnum(name, desc, value);
                            if(name.equals("range")) // range() default SINGLE;
                            	range = StorageOption.Target.RangeOption.valueOf(value);
                            else if(name.equals("option")) {// option() default INSTANCE;
                            	options[parameter] = StorageOption.Option.valueOf(value);
                            	optionsVisited = true;
                            }
                        }
                        
                        @Override
                        public void visitEnd() {
                        	if(!optionsVisited)
                        		options[parameter] = StorageOption.Option.INSTANCE;
                        }
                    };
                } else if(type.equals(Type.getType(StorageOption.RangedTarget.class))) {
                	if(targetIndex != -1) {
                    	throw new RuntimeException("You can not specify multiple targets.");
                    }
                	range = null;
                	targetIndex = parameter;
                	return new AnnotationVisitor(this.api, super.visitParameterAnnotation(parameter, desc, visible)) {
                		boolean optionsVisited;
                		
                		@Override
                	    public void visit(String name, Object value) {
                			super.visit(name, value);
                			sendRange = (double) value;
                	    }
                		
                		@Override
                        public void visitEnum(String name, String desc, String value) {
                			super.visitEnum(name, desc, value);
                			// option() default INSTANCE;
                			options[parameter] = StorageOption.Option.valueOf(value);
                			optionsVisited = true;
                		}
                		
                		@Override
                		public void visitEnd() {
                			if(!optionsVisited)
                				options[parameter] = StorageOption.Option.INSTANCE;
                		}
                	};
                }
                return super.visitParameterAnnotation(parameter, desc, visible);
            }
            
            @Override
            public void visitEnd() {
                super.visitEnd();
                //This is the last method in the delegate class.
                //Finish the class and do the registration.
                cw.visitEnd();
                try {
                    Class<?> clazz = classLoader.defineClass(delegateClassType.getClassName(), cw.toByteArray());
                    NetworkCallDelegate delegateObj = (NetworkCallDelegate) clazz.newInstance(); 
                    if (side == Side.CLIENT) {
                        NetworkCallManager.registerClientDelegateClass(delegateName, delegateObj, options, targetIndex, range, sendRange);
                    } else {
                        NetworkCallManager.registerServerDelegateClass(delegateName, delegateObj, options);
                    }
                } catch (Throwable e) {
                    throw new RuntimeException("Can not create delegate for network call.", e);
                }
            }
        };
        //public static void delegated(Type0 arg0, Type1, arg1, ...) {
        //    //Code generated by caller.
        //}
        //}
    }
    
    public static MethodVisitor generateNonStaticMethod(ClassVisitor parentClass, MethodVisitor parent, 
            String className, String methodName, String desc, final Side side) {
        
        //convert desc to a non-static method form
        String nonstaticDesc;
        {
            Type staticType = Type.getMethodType(desc);
            Type retType = staticType.getReturnType();
            Type[] argsType = staticType.getArgumentTypes();
            Type[] argsTypeWithThis = new Type[argsType.length + 1];
            argsTypeWithThis[0] = Type.getType('L' + className.replace('.', '/') + ';');
            System.arraycopy(argsType, 0, argsTypeWithThis, 1, argsType.length);
            nonstaticDesc = Type.getMethodDescriptor(retType, argsTypeWithThis);
        }

        //delegateName is a string used by both sides to identify a network-call delegate.
        final String delegateName = className + ":" + methodName + ":" + desc;
        final Type[] args = Type.getArgumentTypes(nonstaticDesc);
        final Type ret = Type.getReturnType(nonstaticDesc);
        
        //Check types
        for (Type t : args) {
            //TODO support these types
            if (!t.getDescriptor().startsWith("L") && !t.getDescriptor().startsWith("[")) {
                throw new RuntimeException("Unsupported argument type in network call. ");
            }
        }
        if (!ret.equals(Type.VOID_TYPE)) {
            throw new RuntimeException("Unsupported return value type in network call. " + 
                    "Only void is supported.");
        }
        
        //Generate call to NetworkCallManager in parent.
        parent.visitCode();
        //First parameter
        parent.visitLdcInsn(delegateName);
        //Second parameter: object array
        pushIntegerConst(parent, args.length); //this (0) has been included
        parent.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
        for (int i = 0; i < args.length; ++i) {
            parent.visitInsn(Opcodes.DUP);
            pushIntegerConst(parent, i);
            parent.visitVarInsn(Opcodes.ALOAD, i);
            parent.visitInsn(Opcodes.AASTORE);
        }
        //Call cn.lambdalib.annoreg.mc.network.NetworkCallManager.onNetworkCall
        parent.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(NetworkCallManager.class), 
                "onNetworkCall", "(Ljava/lang/String;[Ljava/lang/Object;)V");
        parent.visitInsn(Opcodes.RETURN);
        parent.visitMaxs(5, args.length);
        parent.visitEnd();

        //Create delegate object.
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        final String delegateId = Integer.toString(delegateNextID++);
        final Type delegateClassType = Type.getType("cn/lambdalib/networkcall/asm/NetworkCallDelegate_" + delegateId);
        cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, delegateClassType.getInternalName(), null, 
                Type.getInternalName(Object.class), new String[]{ Type.getInternalName(NetworkCallDelegate.class) });
        //package cn.lambdalib.annoreg.asm;
        //class NetworkCallDelegate_? implements NetworkCallDelegate {
        {
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, 
                    "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", "()V");
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        //public NetworkCallDelegate_?() {}
        
        final String delegateMethodName = methodName + "_delegate_" + delegateId;
        {
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, 
                    "invoke", 
                    Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object[].class)),
                    null, null);
            mv.visitCode();
            
            //check if this is null
            mv.visitVarInsn(Opcodes.ALOAD, 1); //0 is this
            pushIntegerConst(mv, 0);
            mv.visitInsn(Opcodes.AALOAD);
            Label lblEnd = new Label();
            mv.visitJumpInsn(Opcodes.IFNULL, lblEnd);
            
            for (int i = 0; i < args.length; ++i) {
                mv.visitVarInsn(Opcodes.ALOAD, 1); //0 is this
                pushIntegerConst(mv, i);
                mv.visitInsn(Opcodes.AALOAD);
                mv.visitTypeInsn(Opcodes.CHECKCAST, args[i].getInternalName());
            }
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, 
                    //delegateClassType.getInternalName(), 
                    className.replace('.', '/'),
                    delegateMethodName, nonstaticDesc);
            
            mv.visitLabel(lblEnd);
            
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(args.length + 2, 2);
            mv.visitEnd();
        }
        //@Override public void invoke(Object[] args) {
        //    delegated((Type0) args[0], (Type1) args[1], ...);
        //}
        
        //The returned MethodVisitor will visit the original version of the method,
        //including its annotation, where we can get StorageOptions.
        return new MethodVisitor(Opcodes.ASM5, 
                parentClass.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, delegateMethodName, nonstaticDesc, null, null)) {
            
            //Remember storage options for each argument
            StorageOption.Option[] options = new StorageOption.Option[args.length];
            int targetIndex = -1;
            double sendRange = -1;
            StorageOption.Target.RangeOption range = StorageOption.Target.RangeOption.SINGLE;
            
            {
                for (int i = 0; i < options.length; ++i) {
                    options[i] = StorageOption.Option.NULL; //set default value
                }
                options[0] = StorageOption.Option.INSTANCE;
            }
            
            @Override
            public AnnotationVisitor visitParameterAnnotation(int parameter_in_func, String desc, boolean visible) {
                final int parameter = parameter_in_func + 1; //skip this
                Type type = Type.getType(desc);
                if (type.equals(Type.getType(StorageOption.Data.class))) {
                    options[parameter] = StorageOption.Option.DATA;
                } else if (type.equals(Type.getType(StorageOption.Instance.class))) {
                    //INSTANCE as defualt
                    options[parameter] = StorageOption.Option.INSTANCE;
                    
                    //Change to NULLABLE_INSTANCE if nullable set to true
                    return new AnnotationVisitor(this.api, super.visitParameterAnnotation(parameter, desc, visible)) {
                        @Override
                        public void visit(String name, Object value) {
                            if (name.equals("nullable")) {
                                if ((Boolean) value == true) {
                                    options[parameter] = StorageOption.Option.NULLABLE_INSTANCE;
                                }
                            }
                            super.visit(name, value);
                        }
                    };
                } else if (type.equals(Type.getType(StorageOption.Update.class))) {
                    options[parameter] = StorageOption.Option.UPDATE;
                } else if (type.equals(Type.getType(StorageOption.Null.class))) {
                    options[parameter] = StorageOption.Option.NULL;
                } else if (type.equals(Type.getType(StorageOption.Target.class))) {
                    if (!args[parameter].equals(Type.getType(EntityPlayer.class))) {
                        throw new RuntimeException("Target annotation can only be used on EntityPlayer.");
                    }
                    if(targetIndex != -1) {
                    	throw new RuntimeException("You can not specify multiple targets.");
                    }
                    targetIndex = parameter;
                    return new AnnotationVisitor(this.api, super.visitParameterAnnotation(parameter, desc, visible)) {
                    	boolean optionsVisited;
                    	
                        @Override
                        public void visitEnum(String name, String desc, String value) {
                            super.visitEnum(name, desc, value);
                            if(name.equals("range")) // range() default SINGLE;
                            	range = StorageOption.Target.RangeOption.valueOf(value);
                            else if(name.equals("option")) {// option() default INSTANCE;
                            	options[parameter] = StorageOption.Option.valueOf(value);
                            	optionsVisited = true;
                            }
                        }
                        
                        @Override
                        public void visitEnd() {
                        	if(!optionsVisited)
                        		options[parameter] = StorageOption.Option.INSTANCE;
                        }
                    };
                } else if(type.equals(Type.getType(StorageOption.RangedTarget.class))) {
                	if(targetIndex != -1) {
                    	throw new RuntimeException("You can not specify multiple targets.");
                    }
                	targetIndex = parameter;
                	range = null;
                	return new AnnotationVisitor(this.api, super.visitParameterAnnotation(parameter, desc, visible)) {
                		boolean optionsVisited;
                		
                		@Override
                	    public void visit(String name, Object value) {
                			super.visit(name, value);
                			sendRange = (double) value;
                	    }
                		
                		@Override
                        public void visitEnum(String name, String desc, String value) {
                			super.visitEnum(name, desc, value);
                			// option() default INSTANCE;
                			options[parameter] = StorageOption.Option.valueOf(value);
                			optionsVisited = true;
                		}
                		
                		@Override
                		public void visitEnd() {
                			if(!optionsVisited)
                				options[parameter] = StorageOption.Option.INSTANCE;
                		}
                	};
                }
                return super.visitParameterAnnotation(parameter, desc, visible);
            }
            
            //TODO this option (from annotation)
            
            @Override
            public void visitEnd() {
                super.visitEnd();
                //This is the last method in the delegate class.
                //Finish the class and do the registration.
                cw.visitEnd();
                try {
                    Class<?> clazz = classLoader.defineClass(delegateClassType.getClassName(), cw.toByteArray());
                    NetworkCallDelegate delegateObj = (NetworkCallDelegate) clazz.newInstance(); 
                    if (side == Side.CLIENT) {
                        NetworkCallManager.registerClientDelegateClass(delegateName, delegateObj, options, targetIndex, range, sendRange);
                    } else {
                        NetworkCallManager.registerServerDelegateClass(delegateName, delegateObj, options);
                    }
                } catch (Throwable e) {
                    throw new RuntimeException("Can not create delegate for network call.", e);
                }
            }
        };
        //public static void delegated(Type0 arg0, Type1, arg1, ...) {
        //    //Code generated by caller.
        //}
        //}
    }
}
