package io.github.snow1026.snowlib.api.event;

/**
 * 등록된 이벤트 리스너에 대한 제어 권한을 가진 객체입니다.
 * 이를 통해 외부에서 명시적으로 리스너 등록을 해제하거나 상태를 확인할 수 있습니다.
 */
public interface Subscription {

    /**
     * 이벤트 리스너 등록을 해제합니다.
     * 더 이상 이벤트 핸들러가 호출되지 않습니다.
     */
    void unregister();

    /**
     * 현재 리스너가 활성화되어 있는지 확인합니다.
     *
     * @return 등록되어 있고 활성 상태라면 true
     */
    boolean isActive();

    /**
     * 이 리스너가 총 몇 번 실행되었는지 반환합니다.
     *
     * @return 실행 횟수
     */
    int getCallCount();
}
