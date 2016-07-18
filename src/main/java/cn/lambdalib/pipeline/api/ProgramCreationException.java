package cn.lambdalib.pipeline.api;

/**
 * An exception representing error when compiling shader.
 */
public class ProgramCreationException extends RuntimeException {

    public enum ErrorType { LINK, COMPILE }

    public final ErrorType errorType;
    public final String log;

    public ProgramCreationException(ErrorType type, String log) {
        super (type + ": " + log);

        this.errorType = type;
        this.log = log;
    }

}
