package io.github.snow1026.snowlib.api.component.text;

import io.github.snow1026.snowlib.internal.text.PlaceholderUtil;
import io.github.snow1026.snowlib.internal.text.TextParser;
import io.github.snow1026.snowlib.internal.text.TextSendUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 유연한 메시지 생성 및 전송을 위한 빌더 클래스입니다.
 * <p>
 * MiniMessage 문법을 지원하며, 다양한 객체(Component, Item, Player 등)를
 * 태그(Tag)로 쉽게 바인딩할 수 있습니다.
 * </p>
 */
public class TextComponent implements Cloneable {
    private String text;
    private final List<TagResolver> resolvers = new ArrayList<>();
    private boolean useMiniMessage = false;

    private TextComponent(String text) {
        this.text = text;
    }

    /**
     * 원본 텍스트로 컴포넌트를 생성합니다.
     *
     * @param text 처리할 텍스트
     * @return TextComponent 인스턴스
     */
    @Contract("_ -> new")
    public static TextComponent of(@NotNull String text) {
        return new TextComponent(text);
    }

    /**
     * 번역 키를 사용하여 컴포넌트를 생성합니다.
     * 자동으로 MiniMessage 모드가 활성화됩니다.
     *
     * @param key 번역 키
     * @return TextComponent 인스턴스
     */
    public static TextComponent translatable(@NotNull String key) {
        String translated = TextConfig.get().getTranslationProvider().translate(key);
        return new TextComponent(translated).mm();
    }

    /**
     * 원본 텍스트(Raw String)를 반환합니다.
     * ItemTemplate 등에서 사용됩니다.
     */
    public String raw() {
        return this.text;
    }

    /**
     * {key} 형태의 플레이스홀더를 값으로 문자열 치환합니다.
     * <p>
     * 이 작업은 <b>즉시</b> 수행되며 원본 텍스트(String)를 변경합니다.
     * </p>
     *
     * @param key   키 (중괄호 제외, 예: "name" -> "{name}")
     * @param value 치환될 값
     * @return this
     */
    public TextComponent placeholder(@NotNull String key, @NotNull Object value) {
        return placeholders(Collections.singletonMap(key, value));
    }

    /**
     * Map을 사용하여 여러 {key} 플레이스홀더를 한 번에 치환합니다.
     *
     * @param map 키-값 맵
     * @return this
     * @see PlaceholderUtil
     */
    public TextComponent placeholders(@NotNull Map<String, Object> map) {
        // PlaceholderUtil을 사용하여 text 필드 자체를 업데이트
        this.text = PlaceholderUtil.resolve(this.text, map);
        return this;
    }

    /**
     * 단일 태그(변수)를 추가합니다.
     * <p>
     * 지원되는 타입:
     * <ul>
     * <li>String, Number: 텍스트로 변환</li>
     * <li>Component: 스타일 유지</li>
     * <li>TextComponent: 빌드 후 병합</li>
     * <li>ItemStack: 호버 이벤트가 포함된 아이템 이름으로 변환</li>
     * </ul>
     * </p>
     *
     * @param key   태그 키 (예: "player")
     * @param value 태그 값
     */
    public void tag(@NotNull String key, @Nullable Object value) {
        this.resolvers.add(TextParser.createSmartResolver(key, value));
    }

    /**
     * Map을 사용하여 여러 태그를 한 번에 추가합니다.
     *
     * @param map 태그 키-값 맵
     * @return 체이닝을 위한 this
     */
    public TextComponent tags(@NotNull Map<String, ?> map) {
        map.forEach(this::tag);
        return this;
    }

    /**
     * 플레이어 정보를 자동으로 태그에 바인딩합니다.
     * <p>
     * 생성되는 태그:
     * {@code <player>}, {@code <player_name>}, {@code <player_displayname>},
     * {@code <player_uuid>}, {@code <player_ping>}, {@code <player_world>}
     * </p>
     *
     * @param sender 바인딩할 커맨드 센더 (Player가 아닐 경우 이름만 바인딩됨)
     * @return 체이닝을 위한 this
     */
    public TextComponent bind(@NotNull CommandSender sender) {
        tag("sender_name", sender.getName());
        if (sender instanceof Player p) {
            tag("player", p.getName());
            tag("player_name", p.getName());
            tag("player_displayname", p.displayName());
            tag("player_uuid", p.getUniqueId());
            tag("player_ping", p.getPing());
            tag("player_world", p.getWorld().getName());
        }
        return this;
    }

    /**
     * 현재 시간 관련 정보를 바인딩합니다.
     * <p>
     * 생성되는 태그: {@code <time>} (HH:mm:ss), {@code <date>} (yyyy-MM-dd)
     * </p>
     *
     * @return 체이닝을 위한 this
     */
    public TextComponent bindTime() {
        LocalDateTime now = LocalDateTime.now();
        tag("time", now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        tag("date", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        return this;
    }

    /**
     * MiniMessage 파서를 활성화합니다.
     * {@code <red>}, {@code <bold>} 등의 태그가 작동하게 됩니다.
     *
     * @return 체이닝을 위한 this
     */
    public TextComponent mm() {
        this.useMiniMessage = true;
        return this;
    }

    /**
     * 설정된 옵션들을 바탕으로 최종 {@link Component}를 생성합니다.
     * 전역 설정({@link TextConfig})의 태그들도 이 단계에서 병합됩니다.
     *
     * @return Adventure Component
     */
    public Component build() {
        // 로컬 리졸버와 전역 리졸버 병합
        List<TagResolver> finalResolvers = new ArrayList<>(TextConfig.get().getGlobalResolvers());
        finalResolvers.addAll(this.resolvers);

        if (useMiniMessage) {
            return TextParser.parseMiniMessage(text, finalResolvers);
        }
        return TextParser.parseSimple(text, finalResolvers);
    }

    // --- 전송 유틸리티 ---

    /**
     * 대상에게 채팅 메시지를 전송합니다.
     * @param sender 수신자
     */
    public void send(@NotNull CommandSender sender) {
        TextSendUtil.send(sender, build());
    }

    /**
     * 대상의 액션바(Action Bar)에 메시지를 전송합니다.
     * @param sender 수신자
     */
    public void sendActionBar(@NotNull CommandSender sender) {
        TextSendUtil.sendActionBar(sender, build());
    }

    /**
     * 대상에게 타이틀(Title)을 전송합니다. 서브 타이틀은 비워둡니다.
     *
     * @param sender  수신자
     * @param fadeIn  나타나는 시간 (tick)
     * @param stay    유지되는 시간 (tick)
     * @param fadeOut 사라지는 시간 (tick)
     */
    public void sendTitle(@NotNull CommandSender sender, int fadeIn, int stay, int fadeOut) {
        sendTitle(sender, TextComponent.of(""), fadeIn, stay, fadeOut);
    }

    /**
     * 대상에게 타이틀과 서브 타이틀을 함께 전송합니다.
     *
     * @param sender   수신자
     * @param subtitle 서브 타이틀 컴포넌트
     * @param fadeIn   나타나는 시간 (tick)
     * @param stay     유지되는 시간 (tick)
     * @param fadeOut  사라지는 시간 (tick)
     */
    public void sendTitle(@NotNull CommandSender sender, @NotNull TextComponent subtitle, int fadeIn, int stay, int fadeOut) {
        TextSendUtil.sendTitle(sender, build(), subtitle.build(), fadeIn, stay, fadeOut);
    }

    /**
     * 서버 전체에 메시지를 방송(Broadcast)합니다.
     */
    public void broadcast() {
        Bukkit.broadcast(build());
    }

    // --- 직렬화 및 변환 ---

    public String toLegacy() { return TextParser.toLegacy(build()); }
    public String toPlain() { return TextParser.toPlain(build()); }
    public String toMiniMessage() { return TextParser.toMiniMessage(build()); }

    @Override
    public String toString() {
        return "TextComponent{text='" + text + "', mm=" + useMiniMessage + ", resolvers=" + resolvers.size() + "}";
    }

    /**
     * 이 컴포넌트의 깊은 복사본을 생성합니다.
     * 템플릿 시스템에서 원본을 훼손하지 않고 변수를 적용할 때 유용합니다.
     */
    @Override
    public TextComponent clone() {
        try {
            return (TextComponent) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
