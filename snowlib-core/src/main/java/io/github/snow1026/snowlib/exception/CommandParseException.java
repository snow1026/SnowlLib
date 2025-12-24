package io.github.snow1026.snowlib.exception;

/**
 * 명령어의 파싱 또는 실행 중에 오류가 발생했을 때 발생하는 예외.
 * <p>
 * 이는 유효하지 않은 인자, 불충분한 권한, 또는 충족되지 않은 요구 사항 때문일 수 있습니다.
 */
public class CommandParseException extends RuntimeException {

    public CommandParseException(String message) {
        super(message);
    }

    public CommandParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
