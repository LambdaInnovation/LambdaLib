package cn.lambdalib.ripple;

import cn.lambdalib.ripple.RippleException.RippleRuntimeException;

/**
 * A wrapped function object compiled from script.
 * 
 * @author acaly
 *
 */
public final class ScriptFunction {

	private Path path;
	private IFunction[] internalFunc;

	ScriptFunction(ScriptProgram program, Path path) {
		this.path = path;
		this.internalFunc = new IFunction[0];
	}

	void merge(IFunction newFunc, int sizeArg) {
		// At this point of time, the thread may also hold a lock on the program
		// object.
		// As the program is always locked before a function, no deadlocks.
		synchronized (this) {
			if (internalFunc.length <= sizeArg) {
				IFunction[] newFuncArray = new IFunction[sizeArg + 1];
				for (int i = 0; i < internalFunc.length; ++i) {
					newFuncArray[i] = internalFunc[i];
				}
				newFuncArray[sizeArg] = newFunc;
				internalFunc = newFuncArray;
			} else if (internalFunc[sizeArg] == null) {
				internalFunc[sizeArg] = newFunc;
			} else {
				throw new RippleRuntimeException("Function overloading fails. Argument number " + sizeArg);
			}
		}
	}

	private IFunction getOverload(int sizeArg) {
		synchronized (this) {
			if (sizeArg >= internalFunc.length || internalFunc[sizeArg] == null) {
				throw new RippleRuntimeException("Function overload not found. Argument number " + sizeArg);
			}
			return internalFunc[sizeArg];
		}
	}

	public Object callObject(Object... args) {
		IFunction f = getOverload(args.length);
		int frameCount = ScriptStacktrace.pushFrame(path.path);
		try {
			Object ret = f.call(args);
			ScriptStacktrace.popFrame();
			return ret;
		} catch (RippleRuntimeException e) {
			ScriptStacktrace.adjustFrame(frameCount);
			throw e;
		} catch (Throwable t) {
			ScriptStacktrace.adjustFrame(frameCount);
			throw new RippleRuntimeException(t);
		}
	}

	public int callInteger(Object... args) {
		return Calculation.castInt(callObject(args));
	}

	public float callFloat(Object... args) {
		return (float) callDouble(args);
	}

	public double callDouble(Object... args) {
		return Calculation.castDouble(callObject(args));
	}

	public boolean callBoolean(Object... args) {
		return Calculation.castBoolean(callObject(args));
	}

	/**
	 * This function is for internal use only.
	 * 
	 * @param nargs
	 * @return
	 */
	IFunction cacheOverload(int nargs) {
		return getOverload(nargs);
	}
}
