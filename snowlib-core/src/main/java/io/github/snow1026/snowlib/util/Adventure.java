package io.github.snow1026.snowlib.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.ArrayList;
import java.util.List;

public final class Adventure {

    private Adventure() {
        throw new UnsupportedOperationException();
    }

    /**
     * 문자열을 MiniMessage 컴포넌트로 변환
     *
     * @param input MiniMessage 문자열
     * @return 변환된 Component
     */
    public static Component mm(String input) {
        return MiniMessage.miniMessage().deserialize(input);
    }

    /**
     * 문자열 리스트를 MiniMessage 컴포넌트 리스트로 변환
     *
     * @param input 문자열 리스트
     * @return 변환된 Component 리스트
     */
    public static List<Component> mm(List<String> input) {
        List<Component> output = new ArrayList<>();
        input.forEach(string -> output.add(MiniMessage.miniMessage().deserialize(string)));
        return output;
    }

    /**
     * 문자열을 String 문자열로 변환
     *
     * @param input Component
     * @return 변환된 String
     */
    public static String plain(Component input) {
        return PlainTextComponentSerializer.plainText().serialize(input);
    }

    /**
     * 문자열을 String 문자열 리스트로 변환
     *
     * @param input Component 리스트
     * @return 변환된 String 리스트
     */
    public static List<String> plain(List<Component> input) {
        List<String> output = new ArrayList<>();
        input.forEach(component -> output.add(PlainTextComponentSerializer.plainText().serialize(component)));
        return output;
    }
}
