package io.github.snow1026.snowlib.api.event;

import org.bukkit.event.Event;

/**
 * 이벤트 클래스 타입을 고유하게 식별하기 위한 레코드입니다.
 * 맵(Map)의 키로 사용하거나 특정 이벤트를 구분할 때 활용됩니다.
 */
public record EventKey(Class<? extends Event> type) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventKey(Class<? extends Event> type1))) return false;
        return type.equals(type1);
    }
}
