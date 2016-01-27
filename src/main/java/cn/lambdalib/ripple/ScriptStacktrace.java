/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.ripple;

import java.util.ArrayList;
import java.util.Stack;

import com.google.common.collect.ImmutableList;

/**
 * Stack trace of the current thread.
 * 
 * @author acaly
 *
 */
public final class ScriptStacktrace {
    private static final ThreadLocal<Stack<String>> threadStacktrace = new ThreadLocal<Stack<String>>() {
        @Override
        protected Stack<String> initialValue() {
            return new Stack<String>();
        }
    };

    public final ImmutableList<String> stacktrace;

    private ScriptStacktrace() {
        this.stacktrace = ImmutableList.copyOf(threadStacktrace.get());
    }

    public static ScriptStacktrace getStacktrace() {
        return new ScriptStacktrace();
    }

    // internal use only
    public static int pushFrame(String path) {
        Stack<String> s = threadStacktrace.get();
        int ret = s.size();
        s.push(path);
        return ret;
    }

    // internal use only
    public static void popFrame() {
        threadStacktrace.get().pop();
    }

    static void adjustFrame(int count) {
        threadStacktrace.get().setSize(count);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Script stacktrace: \n");
        for (int i = stacktrace.size() - 1; i >= 0; --i) {
            String s = stacktrace.get(i);
            sb.append("    in function ");
            sb.append(s);
            sb.append('\n');
        }
        return sb.toString();
    }
}
