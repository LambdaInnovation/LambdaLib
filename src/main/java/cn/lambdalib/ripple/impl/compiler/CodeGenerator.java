/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.ripple.impl.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import cn.lambdalib.ripple.Calculation;
import cn.lambdalib.ripple.IFunction;
import cn.lambdalib.ripple.Path;
import cn.lambdalib.ripple.ScriptNamespace;
import cn.lambdalib.ripple.ScriptProgram;
import cn.lambdalib.ripple.ScriptStacktrace;
import cn.lambdalib.ripple.RippleException.RippleCompilerException;

/**
 * The code generator for a single function. Used by parser.
 * @author acaly, WeAthFold
 *
 */
public final class CodeGenerator {
    
    public IFunction testCompile() {
        this.addParameter("x");
        this.functionBodyBegin();
        this.pushIntegerConst(1);
        this.pushParameter("x");
        this.calcBinary(BinaryOperator.ADD);
        return this.functionBodyEnd();
    }
    
    private static class StackInfo {
    }
    
    private static class FunctionCallInfo {
        String path;
    }
    
    private static class SwitchBlockInfo {
        int localId;
        Label labelEnd;
        Label labelNext;
        boolean hasDefault;
    }
    
    private static class CachedFunctionInfo {
        int index;
        int nargs;
        String path;
    }
    
    private final ScriptProgram program;
    private final Parser parser;
    private final Path path;
    
    private final HashMap<String, Integer> paramMap = new HashMap();
    
    private Type classType;
    private MethodVisitor methodVisitor;
    private ClassWriter classWriter;
    
    private ArrayList<CachedFunctionInfo> cachedFunctions = new ArrayList();
    
    private Stack<StackInfo> tempVars = new Stack();
    
    private Stack<FunctionCallInfo> suspendedFunctionCall = new Stack();
    
    private Stack<SwitchBlockInfo> suspendedSwitchBlock = new Stack();
    
    private static Integer classNextId = 0;
    private static final Type objectType = Type.getType(Object.class);
    private static final String funcCacheFieldPrefix = "funcCache_";
    
    CodeGenerator(Parser parser, Path functionPath) {
        this.parser = parser;
        this.program = parser.program;
        this.path = functionPath;
    }

    void addParameter(String name) {
        if (methodVisitor != null) {
            throw new RippleCompilerException("Try to add parameter in function body", parser);
        }
        if (paramMap.size() >= 120) {
            throw new RippleCompilerException("Too many parameters", parser);
        }
        paramMap.put(name, paramMap.size());
    }
    
    void functionBodyBegin() {
        int classId;
        synchronized (classNextId) {
            classId = 0;// classNextId++;
        }
        visitCodeBegin(Integer.toString(classId));
    }
    
    IFunction functionBodyEnd() {
        this.popTemp();
        methodVisitor.visitInsn(Opcodes.ARETURN);
        this.checkEmpty();
        
        this.visitCodeEnd();
        try {
            Class<?> clazz = new FunctionClassLoader().defineClass(classType.getClassName(), classWriter.toByteArray());
            return (IFunction) clazz.getConstructor(ScriptProgram.class).newInstance(program);
        } catch (Throwable t) {
            throw new RippleCompilerException("Cannot generate function", this.parser, t);
        }
    }

    //STACK +1
    void pushIntegerConst(int value) {
        this.newTemp();
        methodVisitor.visitLdcInsn(value);
        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, 
                Type.getInternalName(Calculation.class), 
                "castObject",
                Type.getMethodDescriptor(objectType, Type.INT_TYPE));
    }

    //STACK +1
    void pushDoubleConst(double value) {
        this.newTemp();
        methodVisitor.visitLdcInsn(value);
        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, 
                Type.getInternalName(Calculation.class), 
                "castObject",
                Type.getMethodDescriptor(objectType, Type.DOUBLE_TYPE));
    }

    //STACK +1
    void pushBooleanConst(boolean value) {
        this.newTemp();
        if (value) {
            methodVisitor.visitInsn(Opcodes.ICONST_1);
        } else {
            methodVisitor.visitInsn(Opcodes.ICONST_0);
        }
        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, 
                Type.getInternalName(Calculation.class), 
                "castObject",
                Type.getMethodDescriptor(objectType, Type.BOOLEAN_TYPE));
    }

    //STACK +1
    void pushParameter(String name) {
        this.newTemp();
        Integer id = paramMap.get(name);
        if (id == null) {
            throw new RippleCompilerException("Parameter not found: " + name, this.parser);
        }
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
        methodVisitor.visitIntInsn(Opcodes.BIPUSH, id);
        methodVisitor.visitInsn(Opcodes.AALOAD);
    }

    //STACK -2+1
    void calcBinary(BinaryOperator op) {
        this.mergeTemp();
        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, 
                Type.getInternalName(Calculation.class), 
                op.methodName,
                Type.getMethodDescriptor(objectType, objectType, objectType));
    }

    //STACK -1+1
    void calcUnary(UnaryOperator op) {
        if (op.caseOp == null) {
            this.transformTemp();
            methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC,
                    Type.getInternalName(Calculation.class),
                    op.methodName,
                    Type.getMethodDescriptor(objectType, objectType));
        } else {
            this.pushSwitchValue();
            //swap
            methodVisitor.visitInsn(Opcodes.DUP_X1);
            methodVisitor.visitInsn(Opcodes.POP);
            
            this.calcBinary(op.caseOp);
        }
    }
    
    //STACK 0
    void beforeCallFunction(Path path) {
        suspendedFunctionCall.push(new FunctionCallInfo()).path = path.path;
    }
    
    //STACK -nargs+1
    void afterCallFunction(int nargs) {
        if (nargs > 250) {
            throw new RippleCompilerException("Too many arguments", this.parser);
        }
        
        String path = suspendedFunctionCall.pop().path;
        this.mergeTemp(nargs);

        methodVisitor.visitIntInsn(Opcodes.BIPUSH, nargs);
        methodVisitor.visitTypeInsn(Opcodes.ANEWARRAY, objectType.getInternalName());
        
        //wrap arguments
        for (int i = nargs - 1; i >= 0; --i) {
            methodVisitor.visitInsn(Opcodes.DUP_X1);
            methodVisitor.visitInsn(Opcodes.DUP_X1);
            methodVisitor.visitInsn(Opcodes.POP);
            methodVisitor.visitIntInsn(Opcodes.BIPUSH, i);
            methodVisitor.visitInsn(Opcodes.DUP_X1);
            methodVisitor.visitInsn(Opcodes.POP);
            methodVisitor.visitInsn(Opcodes.AASTORE);
        }
        
        //get method id
        int id = cacheFunction(path, nargs);
        
        //push IFunction
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor.visitFieldInsn(Opcodes.GETFIELD, 
                classType.getInternalName(),
                funcCacheFieldPrefix + id, 
                Type.getDescriptor(IFunction.class));
        
        //exchange IFunction and arguments
        methodVisitor.visitInsn(Opcodes.DUP_X1);
        methodVisitor.visitInsn(Opcodes.POP);

        //push frame
        methodVisitor.visitLdcInsn(path);
        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC,
                Type.getInternalName(ScriptStacktrace.class),
                "pushFrame",
                Type.getMethodDescriptor(Type.INT_TYPE, Type.getType(String.class)));
        methodVisitor.visitInsn(Opcodes.POP);
        
        //call
        methodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, 
                Type.getInternalName(IFunction.class), 
                "call", 
                Type.getMethodDescriptor(objectType, Type.getType(Object[].class)));
        
        //pop frame
        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, 
                Type.getInternalName(ScriptStacktrace.class),
                "popFrame",
                Type.getMethodDescriptor(Type.VOID_TYPE));
    }

    //STACK -1
    void pushSwitchBlock() {
        this.popTemp();
        
        SwitchBlockInfo s = new SwitchBlockInfo();
        s.localId = suspendedSwitchBlock.size() + 2; //local starts from 2 (this, args)
        s.labelEnd = new Label();
        suspendedSwitchBlock.push(s);
        
        methodVisitor.visitVarInsn(Opcodes.ASTORE, s.localId);
    }
    
    //STACK +1
    void popSwitchBlock() {
        SwitchBlockInfo s = suspendedSwitchBlock.pop();
        if (!s.hasDefault) {
            //throw exception
            methodVisitor.visitLdcInsn("Invalid switch input value");
            methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC,
                    Type.getInternalName(Calculation.class),
                    "throwRuntimeException", 
                    Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class)));
            //We have to push a value onto the stack, or jvm will throw an exception in control flow analysis
            methodVisitor.visitInsn(Opcodes.ACONST_NULL);
        }

        this.newTemp();
        
        methodVisitor.visitLabel(s.labelEnd);
    }
    
    private void pushSwitchValue() {
        SwitchBlockInfo s = suspendedSwitchBlock.peek();
        this.newTemp();
        methodVisitor.visitVarInsn(Opcodes.ALOAD, s.localId);
    }
    
    //STACK -1
    void switchCase(boolean hasWhen) {
        SwitchBlockInfo s = suspendedSwitchBlock.peek();
        if (s.hasDefault) {
            throw new RippleCompilerException("Default must be the last case", this.parser);
        }
        
        this.popTemp();
        
        if (!hasWhen) {
            //first compare
            this.pushSwitchValue();
            this.calcBinary(BinaryOperator.EQUAL);
        }
        
        s.labelNext = new Label();
        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC,
                Type.getInternalName(Calculation.class),
                "castBoolean", 
                Type.getMethodDescriptor(Type.BOOLEAN_TYPE, objectType));
        methodVisitor.visitJumpInsn(Opcodes.IFEQ, s.labelNext);
    }
    
    //STACK 0
    void switchCaseDefault() {
        SwitchBlockInfo s = suspendedSwitchBlock.peek();
        if (s.hasDefault) {
            throw new RippleCompilerException("Default must be the last case", this.parser);
        }
        s.hasDefault = true;
    }
    
    //STACK -1
    void switchCaseEnd() {
        SwitchBlockInfo s = suspendedSwitchBlock.peek();
        
        this.popTemp();
        
        if (!s.hasDefault) {
            //not the default case
            methodVisitor.visitJumpInsn(Opcodes.GOTO, s.labelEnd);
            methodVisitor.visitLabel(s.labelNext);
        }
    }
    
    //Internal helpers
    private int cacheFunction(String path, int nargs) {
        for (CachedFunctionInfo f : cachedFunctions) {
            if (f.path.equals(path) && f.nargs == nargs) {
                return f.index;
            }
        }
        
        CachedFunctionInfo f = new CachedFunctionInfo();
        f.index = cachedFunctions.size();
        f.path = path;
        f.nargs = nargs;
        cachedFunctions.add(f);
        return f.index;
    }
    
    private void newTemp() {
        tempVars.push(new StackInfo());
    }
    
    private void popTemp() {
        if (tempVars.empty()) {
            throw new RippleCompilerException("Stack error", this.parser);
        }
        tempVars.pop();
    }
    
    private void mergeTemp() {
        if (tempVars.empty()) {
            throw new RippleCompilerException("Stack error", this.parser);
        }
        tempVars.pop();
    }
    
    private void mergeTemp(int merged) {
        merged = merged - 1;
        
        for (int i = 0; i < merged; ++i) {
            if (tempVars.empty()) {
                throw new RippleCompilerException("Stack error", this.parser);
            }
            tempVars.pop();
        }
    }
    
    private void transformTemp() {
        if (tempVars.empty()) {
            throw new RippleCompilerException("Stack error", this.parser);
        }
    }
    
    private void swapTopTemp() {
        if (tempVars.size() < 2) {
            throw new RippleCompilerException("Stack error", this.parser);
        }
    }
    
    private boolean checkEmpty() {
        return tempVars.empty() && suspendedSwitchBlock.empty() && suspendedFunctionCall.empty();
    }
    
    private void visitCodeBegin(String id) {
        classWriter = new ClassWriter(0);
        classType = Type.getType("cn/liutils/ripple/Function_" + id);
        //class ? implements IFunction
        classWriter.visit(Opcodes.V1_5, 
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                classType.getInternalName(), null, 
                objectType.getInternalName(), 
                new String[]{ Type.getInternalName(IFunction.class) });
        //public ?(ScriptProgram program) {
        {
            MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC, 
                    "<init>",
                    Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(ScriptProgram.class)), 
                    null, null);
            //super();
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, objectType.getInternalName(), "<init>", "()V");
            //this.program = program;
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitFieldInsn(Opcodes.PUTFIELD, classType.getInternalName(),
                    "program", Type.getDescriptor(ScriptProgram.class));
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(100, 100);
            mv.visitEnd();
        }
        //}
        
        //private ScriptProgram program;
        classWriter.visitField(Opcodes.ACC_PRIVATE, 
                "program",
                Type.getDescriptor(ScriptProgram.class),
                null, null);
        
        //public Object call(Object[] params) {
        methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "call", 
                Type.getMethodDescriptor(objectType, Type.getType(Object[].class)), 
                null, null);
        methodVisitor.visitCode();
        //    this.setupCache();
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, classType.getInternalName(), 
                "setupCache", Type.getMethodDescriptor(Type.VOID_TYPE));
    }
    
    private void visitCodeEnd() {
        //}
        methodVisitor.visitMaxs(100, 100);
        methodVisitor.visitEnd();
        
        //private IFunction funcCache_?;
        {
            int count = cachedFunctions.size();
            for (int i = 0; i < count; ++i) {
                classWriter.visitField(
                        Opcodes.ACC_PRIVATE, 
                        funcCacheFieldPrefix + Integer.toString(i),
                        Type.getDescriptor(IFunction.class), 
                        null, null);
            }
        }
        
        //private void setupCache() {
        {
            MethodVisitor mv = classWriter.visitMethod(
                    Opcodes.ACC_PRIVATE,
                    "setupCache",
                    Type.getMethodDescriptor(Type.VOID_TYPE),
                    null, null);
            mv.visitCode();
            for (CachedFunctionInfo func : cachedFunctions) {
                //funcCache_? = Calculation.getFunctionOverload(this.program, "?", ?);
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitInsn(Opcodes.DUP);
                mv.visitFieldInsn(Opcodes.GETFIELD, classType.getInternalName(), 
                        "program", Type.getDescriptor(ScriptProgram.class)); //arg1
                mv.visitLdcInsn(func.path);//arg2
                mv.visitLdcInsn(path.path);//arg3
                mv.visitLdcInsn(func.nargs);//arg4
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, 
                        Type.getInternalName(Calculation.class), 
                        "getFunctionOverload", 
                        Type.getMethodDescriptor(Type.getType(IFunction.class), 
                                Type.getType(ScriptProgram.class),
                                Type.getType(String.class),
                                Type.getType(String.class),
                                Type.INT_TYPE));
                mv.visitFieldInsn(Opcodes.PUTFIELD, classType.getInternalName(),
                        funcCacheFieldPrefix + Integer.toString(func.index), 
                        Type.getDescriptor(IFunction.class));
            }
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(100, 100);
            mv.visitEnd();
        }
        //}
        
        classWriter.visitEnd();
    }
}
