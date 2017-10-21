package cn.lambdalib.core;

import org.apache.logging.log4j.Logger;

/**
 * To print debug message.
 * It works only if LLib's prefix started with '@'..
 */
public class LLCommons {

    public static final Logger log = LambdaLib.log;

    public static void debug(Object obj) {
        if (LambdaLib.DEBUG) {
            log.info(obj);
        }
    }

}
