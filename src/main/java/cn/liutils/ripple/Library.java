package cn.liutils.ripple;

import cn.liutils.util.generic.MathUtils;
import cn.liutils.util.generic.RandUtils;


/**
 * Implementations of standard library.
 * @author acaly
 *
 */
public final class Library {
    
    private Library() {}

    static void openLibrary(ScriptProgram program) {
        ScriptNamespace root = program.root;
        
        root.setNativeFunction("print", new NativeFunction(new String[] {"arg0"}) {
            @Override
            protected Object call(NativeFunctionFrame frame) {
                Object arg0 = frame.getArgument(0);
                System.out.println("Ripple print: " + arg0.toString());
                return arg0;
            }
        });
        
        root.setNativeFunction("sqrt", new NativeFunction(new String[] { "x" }) {

			@Override
			protected Object call(NativeFunctionFrame frame) {
				double x = frame.getDoubleArgument(0);
				return Math.sqrt(x);
			}
        	
        });
        
        root.setNativeFunction("exp", new NativeFunction(new String[] { "x" }) {

			@Override
			protected Object call(NativeFunctionFrame frame) {
				double x = frame.getDoubleArgument(0);
				return Math.exp(x);
			}
        	
        });
        
        root.setNativeFunction("lerp", new NativeFunction(new String[] { "a", "b", "lambda" }) {

			@Override
			protected Object call(NativeFunctionFrame frame) {
				double a = frame.getDoubleArgument(0), b = frame.getDoubleArgument(1), l = frame.getDoubleArgument(2);
				return MathUtils.lerp(a, b, l);
			}
        	
        });
        
        root.setNativeFunction("pow", new NativeFunction(new String[] { "x", "e" }) {

			@Override
			protected Object call(NativeFunctionFrame frame) {
				double x = frame.getDoubleArgument(0), e = frame.getDoubleArgument(1);
				return Math.pow(x, e);
			}
        	
        });
        
        root.setNativeFunction("sin", new NativeFunction(new String[] { "x" }) {

			@Override
			protected Object call(NativeFunctionFrame frame) {
				double x = frame.getDoubleArgument(0);
				return Math.sin(x);
			}
        	
        });
        
        root.setNativeFunction("cos", new NativeFunction(new String[] { "x" }) {

			@Override
			protected Object call(NativeFunctionFrame frame) {
				double x = frame.getDoubleArgument(0);
				return Math.cos(x);
			}
        	
        });
        
        root.setNativeFunction("max", new NativeFunction(new String[] { "a", "b" }) {

			@Override
			protected Object call(NativeFunctionFrame frame) {
				double a = frame.getDoubleArgument(0), b = frame.getDoubleArgument(1);
				return Math.max(a, b);
			}
        	
        });
        
        root.setNativeFunction("min", new NativeFunction(new String[] { "a", "b" }) {

			@Override
			protected Object call(NativeFunctionFrame frame) {
				double a = frame.getDoubleArgument(0), b = frame.getDoubleArgument(1);
				return Math.min(a, b);
			}
        	
        });
        
        
        root.setNativeFunction("range_double", new NativeFunction(new String[] { "a", "b" }) {

			@Override
			protected Object call(NativeFunctionFrame frame) {
				double a = frame.getDoubleArgument(0), b = frame.getDoubleArgument(1);
				return RandUtils.ranged(a, b);
			}
        	
        });
        
        root.setNativeFunction("range_int", new NativeFunction(new String[] { "a", "b" }) {

			@Override
			protected Object call(NativeFunctionFrame frame) {
				int a = frame.getIntArgument(0), b = frame.getIntArgument(1);
				return RandUtils.rangei(a, b);
			}
        	
        });
        
        root.setNativeFunction("floor", new NativeFunction(new String[] { "a" }) {

			@Override
			protected Object call(NativeFunctionFrame frame) {
				double a = frame.getDoubleArgument(0);
				return (int) a;
			}
        	
        });
        
        root.setConst("PI", Math.PI);
    }
}
