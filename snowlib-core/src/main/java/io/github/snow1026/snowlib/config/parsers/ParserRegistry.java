package io.github.snow1026.snowlib.config.parsers;

import io.github.snow1026.snowlib.config.parsers.collection.GenericListParser;
import io.github.snow1026.snowlib.config.parsers.collection.GenericMapParser;
import io.github.snow1026.snowlib.config.parsers.collection.GenericSetParser;
import io.github.snow1026.snowlib.config.parsers.object.ObjectParser;
import io.github.snow1026.snowlib.config.parsers.primitive.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ParserRegistry {
    private static final Map<Class<?>, ConfigParser<?>> PARSERS = new ConcurrentHashMap<>();
    private static final List<GenericConfigParser<?>> GENERIC = new ArrayList<>();
    private static final Map<Class<?>, ConfigParser<?>> ENUM_PARSER_CACHE = new ConcurrentHashMap<>();

    static {
        register(new StringParser());
        register(new IntegerParser());
        register(new DoubleParser());
        register(new BooleanParser());
        register(new LongParser());
        register(new FloatParser());
        register(new UUIDParser());
        register(new MaterialParser());

        registerGeneric(new GenericListParser());
        registerGeneric(new GenericSetParser());
        registerGeneric(new GenericMapParser());
    }

    public static <T> void register(ConfigParser<T> parser) {
        PARSERS.put(parser.getType(), parser);
    }

    public static void registerGeneric(GenericConfigParser<?> parser) {
        GENERIC.add(parser);
    }

    public static List<GenericConfigParser<?>> getGenericParsers() {
        return GENERIC;
    }

    public static void registerObject(Class<?> type) {
        registerGeneric(new ObjectParser<>(type));
    }

    @SuppressWarnings("unchecked")
    public static <T> ConfigParser<T> get(Class<T> type) {
        if (type.isEnum()) {
            return (ConfigParser<T>) ENUM_PARSER_CACHE.computeIfAbsent(type, ParserRegistry::createEnumParser);
        }
        return (ConfigParser<T>) PARSERS.get(type);
    }

    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> EnumParser<E> createEnumParser(Class<?> enumClass) {
        return new EnumParser<>((Class<E>) enumClass);
    }
}