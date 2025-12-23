package io.github.snow1026.snowlib.debug;

import java.util.*;
import java.util.logging.Logger;

public final class SnowLogger {
    private static final Logger LOGGER = Logger.getLogger("SnowLib");

    public static void info(String msg) { LOGGER.info(msg); }
    public static void warn(String msg) { LOGGER.warning(msg); }
    public static void error(String msg, Throwable t) {
        LOGGER.severe(msg);
        if (t != null) throw new RuntimeException(t);
    }
}

