package cn.lambdalib.ripple;

import java.io.IOException;

import cn.lambdalib.ripple.impl.compiler.Parser;

/**
 * Super class for all exceptions that may be thrown in Ripple.
 * 
 * @author acaly
 *
 */
public class RippleException extends RuntimeException {

    public RippleException(String message) {
        super(message);
    }

    public RippleException(Throwable cause) {
        super(cause);
    }

    public RippleException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class RippleCompilerException extends RippleException {

        /* Parser instance here */

        public RippleCompilerException(String message, Parser parser) {
            super(errstr(message, parser));
        }

        public RippleCompilerException(String message, Parser parser, Throwable cause) {
            super(errstr(message, parser), cause);
        }

        public RippleCompilerException(Throwable cause, Parser parser) {
            super(errstr(parser), cause);
        }

        private static String errstr(String msg, Parser parser) {
            return errstr(parser) + ": " + msg;
        }

        private static String errstr(Parser parser) {
            return "at " + parser.getScriptName() + ", line " + parser.getLineNumber();
        }
    }

    public static class RippleRuntimeException extends RippleException {

        public final ScriptStacktrace stacktrace;

        public RippleRuntimeException(Throwable cause) {
            super(cause);
            this.stacktrace = ScriptStacktrace.getStacktrace();
        }

        public RippleRuntimeException(String message) {
            super(message);
            this.stacktrace = ScriptStacktrace.getStacktrace();
        }

        public RippleRuntimeException(String message, Throwable cause) {
            super(message, cause);
            this.stacktrace = ScriptStacktrace.getStacktrace();
        }

    }
}
