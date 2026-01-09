package io.github.snow1026.snowlib.registry;

import java.util.Optional;

/**
 * 시스템에 존재하는 다양한 {@link SnowRegistry}들을 조회하고 관리하는 중앙 접근 지점입니다.
 */
public interface RegistryAccess {

    /**
     * 주어진 키에 해당하는 레지스트리를 조회합니다.
     * <p>
     * 예시: {@code registryAccess.lookup(RegistryKey.PACKET)}
     *
     * @param key 조회할 레지스트리의 키 (타입 정보 포함)
     * @param <T> 레지스트리에 저장된 객체의 타입
     * @return 해당 타입의 레지스트리
     * @throws IllegalArgumentException 해당 키에 매핑된 레지스트리가 없을 경우
     */
    <T extends Registrable> SnowRegistry<T> lookup(RegistryKey key);

    /**
     * 주어진 키에 해당하는 레지스트리를 Optional로 반환합니다.
     *
     * @param key 조회할 레지스트리의 키
     * @param <T> 타입
     * @return Optional 레지스트리
     */
    <T extends Registrable> Optional<SnowRegistry<T>> find(RegistryKey key);
}
