package io.github.snow1026.snowlib.registry;

import io.github.snow1026.snowlib.SnowKey;
import java.util.Collection;
import java.util.Optional;
import java.util.Map;

/**
 * 특정 타입의 객체들을 관리하는 레지스트리 인터페이스입니다.
 *
 * @param <T> 등록될 객체의 타입 (Registrable 상속)
 */
public interface SnowRegistry<T extends Registrable> {

    /**
     * 객체를 레지스트리에 등록합니다.
     *
     * @param key 등록할 객체의 고유 키
     * @param target 등록할 객체
     */
    void register(SnowKey key, T target);

    /**
     * 객체의 등록을 해제합니다.
     *
     * @param key 해제할 객체의 키
     */
    void unregister(SnowKey key);

    /**
     * 키를 통해 등록된 객체를 조회합니다.
     *
     * @param key 조회할 키
     * @return 등록된 객체 (존재하지 않을 경우 null, API 안전성을 위해 Optional 사용 권장)
     */
    T get(SnowKey key);

    /**
     * 키를 통해 등록된 객체를 Optional로 반환합니다.
     *
     * @param key 조회할 키
     * @return Optional로 감싸진 객체
     */
    default Optional<T> find(SnowKey key) {
        return Optional.ofNullable(get(key));
    }

    /**
     * 현재 레지스트리에 등록된 모든 객체의 변경 불가능한(Unmodifiable) 컬렉션을 반환합니다.
     *
     * @return 등록된 모든 객체
     */
    Collection<T> getAll();

    /**
     * 현재 레지스트리에 등록된 모든 키와 객체의 맵을 반환합니다.
     *
     * @return 키-객체 맵
     */
    Map<SnowKey, T> getEntries();
}
