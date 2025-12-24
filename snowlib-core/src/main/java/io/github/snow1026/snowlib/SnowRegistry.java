package io.github.snow1026.snowlib;

public abstract class SnowRegistry<T extends Snow> {

    public abstract T getByKey(SnowKey key);

    public abstract void register(T target);
    public abstract void unRegister(T target);
    public abstract boolean isRegistered(T target);
}
