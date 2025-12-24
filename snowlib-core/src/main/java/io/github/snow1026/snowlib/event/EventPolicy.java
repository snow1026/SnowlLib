// io.github.snow1026.snowlib.events.EventPolicy
package io.github.snow1026.snowlib.event;

public interface EventPolicy {
    boolean catchException();

    static EventPolicy defaultPolicy() {
        return () -> true;
    }
}
