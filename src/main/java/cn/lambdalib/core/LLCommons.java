package cn.lambdalib.core;

import org.apache.logging.log4j.Logger;

public class LLCommons {

    public static final Logger log = LambdaLib.log;

    public static void debug(Object obj) {
        if (LambdaLib.DEBUG) {
            log.info(obj);
        }
    }

}
