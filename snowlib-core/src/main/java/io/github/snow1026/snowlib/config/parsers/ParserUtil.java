package io.github.snow1026.snowlib.config.parsers;

import io.github.snow1026.snowlib.exception.ConfigParserNotFoundException;
import io.github.snow1026.snowlib.exception.ConfigResultParseException;

import java.lang.reflect.Type;

public class ParserUtil {

    private ParserUtil() {}

    public static Object parseGeneric(Object raw, Type type) {
        if (type instanceof Class<?> cls) {
            ConfigParser<?> parser = ParserRegistry.get(cls);
            if (parser == null)
                throw new ConfigParserNotFoundException(cls);
            return parser.parse(raw);
        }

        for (GenericConfigParser<?> gp : ParserRegistry.getGenericParsers()) {
            if (gp.supports(type)) {
                return gp.parse(raw, type);
            }
        }

        throw new ConfigResultParseException("No generic parser for type: " + type, null);
    }
}
