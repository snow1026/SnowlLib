package io.github.snow1026.snowlib.registry.internal;

import io.github.snow1026.snowlib.SnowKey;
import io.github.snow1026.snowlib.api.command.Sommand;
import io.github.snow1026.snowlib.internal.command.CommandRegister;
import io.github.snow1026.snowlib.registry.SnowRegistry;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 서버의 커스텀 명령어들을 등록하고 조회하는 레지스트리입니다.
 */
public final class CommandRegistry implements SnowRegistry<Sommand> {
    private final Map<SnowKey, Sommand> registeredCommands = new ConcurrentHashMap<>();

    private CommandRegistry() {}

    @Override
    public void register(SnowKey key, Sommand target) {
        if (target == null) return;
        CommandRegister.register(target);
        registeredCommands.put(key, target);
    }

    @Override
    public void unregister(SnowKey key) {
        if (key == null) return;
        CommandRegister.unregister(registeredCommands.get(key));
        registeredCommands.remove(key);
    }

    @Override
    public Sommand get(SnowKey key) {
        return registeredCommands.get(key);
    }

    @Override
    public Collection<Sommand> getAll() {
        return Collections.unmodifiableCollection(registeredCommands.values());
    }

    @Override
    public Map<SnowKey, Sommand> getEntries() {
        return Collections.unmodifiableMap(registeredCommands);
    }
}
