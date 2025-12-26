package io.github.snow1026.snowlib.debug;

import java.util.*;
import java.util.logging.Logger;

/**
 * SnowLib 프레임워크 전용 로깅 유틸리티입니다.
 * 표준 Java Logger를 사용하여 정보, 경고 및 에러 메시지를 관리합니다.
 */
public final class SnowLogger {
    private static final Logger LOGGER = Logger.getLogger("SnowLib");

    /**
     * 일반적인 정보성 메시지를 기록합니다.
     * @param msg 기록할 메시지
     */
    public static void info(String msg) { LOGGER.info(msg); }

    /**
     * 주의가 필요한 경고 메시지를 기록합니다.
     * @param msg 기록할 경고 내용
     */
    public static void warn(String msg) { LOGGER.warning(msg); }

    /**
     * 치명적인 에러 메시지를 기록하고 실행 예외를 발생시킵니다.
     * * @param msg 기록할 에러 메시지
     * @param t   발생한 원인 예외 (Nullable)
     * @throws RuntimeException 제공된 원인 예외를 포함한 런타임 예외를 즉시 던집니다.
     */
    public static void error(String msg, Throwable t) {
        LOGGER.severe(msg);
        if (t != null) throw new RuntimeException(t);
    }
}
