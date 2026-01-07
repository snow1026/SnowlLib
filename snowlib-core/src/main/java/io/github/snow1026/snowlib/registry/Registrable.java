package io.github.snow1026.snowlib.registry;

/**
 * SnowLib의 레지스트리 시스템에 등록될 수 있는 모든 객체의 마커 인터페이스입니다.
 * <p>
 * 이 인터페이스를 구현하는 객체는 {@link RegistryKey}를 통해 관리될 수 있습니다.
 */
public interface Registrable {
    // 필요한 경우 식별자(ID)를 반환하는 메서드를 추가할 수 있습니다.
    // SnowKey getKey();
}
