package cn.lambdalib.ripple;

import java.util.HashMap;

import cn.lambdalib.ripple.RippleException.RippleRuntimeException;

/**
 * A wrapper for Java function injected into a Ripple program.
 * 
 * @author acaly
 *
 */
public abstract class NativeFunction implements IFunction {

	public class NativeFunctionFrame {

		private Object[] args;

		NativeFunctionFrame(Object[] args) {
			this.args = args;
		}

		public Object getArgument(String name) {
			Integer id = parameterMap.get(name);
			if (id == null) {
				throw new RippleRuntimeException("Parameter not found");
			}
			return args[id];
		}

		public Object getArgument(int id) {
			if (id < 0 || id >= args.length) {
				throw new RippleRuntimeException("Invalid parameter index");
			}
			return args[id];
		}

		public int getIntArgument(String name) {
			return Calculation.castInt(getArgument(name));
		}

		public double getDoubleArgument(String name) {
			return Calculation.castDouble(getArgument(name));
		}

		public boolean getBooleanArgument(String name) {
			return Calculation.castBoolean(getArgument(name));
		}

		public int getIntArgument(int id) {
			return Calculation.castInt(getArgument(id));
		}

		public double getDoubleArgument(int id) {
			return Calculation.castDouble(getArgument(id));
		}

		public boolean getBooleanArgument(int id) {
			return Calculation.castBoolean(getArgument(id));
		}

		// get value or get function can also be added here if really needed
	}

	private ScriptProgram env;
	private Path functionPath;
	private final HashMap<String, Integer> parameterMap;

	protected NativeFunction(String[] parameters) {
		this.parameterMap = new HashMap();
		for (int i = 0; i < parameters.length; ++i) {
			parameterMap.put(parameters[i], i);
		}
	}

	@Override
	public Object call(Object[] args) {
		if (args.length != parameterMap.size()) {
			throw new RippleRuntimeException("Invalid argument count for function " + functionPath.path + ". "
					+ parameterMap.size() + " expected, " + args.length + " received.");
		}
		for (Object arg : args) {
			if (!Calculation.checkType(arg)) {
				throw new RippleRuntimeException("Invalid argument type for function " + functionPath.path + ".");
			}
		}

		NativeFunctionFrame frame = new NativeFunctionFrame(args);
		Object ret = call(frame);

		if (!Calculation.checkType(ret)) {
			throw new RippleRuntimeException("Invalid return value");
		}
		return ret;
	}

	void bind(ScriptProgram env, Path path) {
		if (this.env != null) {
			throw new RippleRuntimeException("Try to rebind a native function");
		}
		this.env = env;
		this.functionPath = path;
	}

	int getParamterCount() {
		return parameterMap.size();
	}

	protected abstract Object call(NativeFunctionFrame frame);
}
