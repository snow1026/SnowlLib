package io.github.snow1026.snowlib.api.command.argument;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link ArgumentParser} 인스턴스를 관리하는 레지스트리(Registry).
 * <p>
 * 이 클래스는 일반적인 Bukkit 및 Java 타입에 대한 기본 파서를 제공하며,
 * 커스텀 파서의 등록을 허용합니다.
 */
public final class ArgumentParsers {

    private static final Map<Class<?>, ArgumentParser<?>> parsers = new HashMap<>();

    static {
        register(String.class, new ArgumentParser<>() {
            @Override
            public @NotNull String parse(@NotNull org.bukkit.command.CommandSender sender, @NotNull String input) {
                return input;
            }
            @NotNull
            @Override
            public String getErrorMessage() {
                return "문자열";
            }
        });
        register(Integer.class, new ArgumentParser<>() {
            @Nullable
            @Override
            public Integer parse(@NotNull org.bukkit.command.CommandSender sender, @NotNull String input) {
                try { return Integer.parseInt(input); } catch (NumberFormatException e) { return null; }
            }
            @NotNull
            @Override
            public String getErrorMessage() {
                return "정수";
            }
        });
        register(Double.class, new ArgumentParser<>() {
            @Nullable
            @Override
            public Double parse(@NotNull org.bukkit.command.CommandSender sender, @NotNull String input) {
                try { return Double.parseDouble(input); } catch (NumberFormatException e) { return null; }
            }
            @NotNull
            @Override
            public String getErrorMessage() {
                return "실수";
            }
        });
        register(Boolean.class, new ArgumentParser<>() {
            @Nullable
            @Override
            public Boolean parse(@NotNull org.bukkit.command.CommandSender sender, @NotNull String input) {
                if (input.equalsIgnoreCase("true")) return true;
                if (input.equalsIgnoreCase("false")) return false;
                return null;
            }
            @NotNull
            @Override
            public List<String> suggest(@NotNull org.bukkit.command.CommandSender sender, @NotNull String input) {
                return Stream.of("true", "false").filter(s -> s.startsWith(input.toLowerCase())).collect(Collectors.toList());
            }
            @NotNull
            @Override
            public String getErrorMessage() {
                return "true 또는 false";
            }
        });

        register(Player.class, new ArgumentParser<>() {
            @Override
            public Player parse(@NotNull org.bukkit.command.CommandSender sender, @NotNull String input) {
                return Bukkit.getPlayerExact(input);
            }

            @Override
            public @NotNull List<String> suggest(@NotNull org.bukkit.command.CommandSender sender, @NotNull String input) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                        .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
                        .collect(Collectors.toList());
            }
            @NotNull
            @Override
            public String getErrorMessage() {
                return "온라인 플레이어 이름";
            }
        });

        register(OfflinePlayer.class, new ArgumentParser<>() {
            @Override
            public @NotNull OfflinePlayer parse(@NotNull org.bukkit.command.CommandSender sender, @NotNull String input) {
                return Bukkit.getOfflinePlayer(input);
            }
            @NotNull
            @Override
            public String getErrorMessage() {
                return "플레이어 이름";
            }
        });

        register(World.class, new ArgumentParser<>() {
            @Override
            public World parse(@NotNull org.bukkit.command.CommandSender sender, @NotNull String input) {
                return Bukkit.getWorld(input);
            }

            @Override
            public @NotNull List<String> suggest(@NotNull org.bukkit.command.CommandSender sender, @NotNull String input) {
                return Bukkit.getWorlds().stream().map(World::getName)
                        .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
                        .collect(Collectors.toList());
            }
            @NotNull
            @Override
            public String getErrorMessage() {
                return "월드 이름";
            }
        });

        register(Material.class, new ArgumentParser<>() {
            @Override
            public Material parse(@NotNull org.bukkit.command.CommandSender sender, @NotNull String input) {
                Material mat = Material.matchMaterial(input);
                if (mat == null && input.equalsIgnoreCase("air")) return Material.AIR;
                return mat;
            }

            @Override
            public @NotNull List<String> suggest(@NotNull org.bukkit.command.CommandSender sender, @NotNull String input) {
                String lowerInput = input.toLowerCase(Locale.ROOT);
                return Arrays.stream(Material.values())
                        .map(Enum::name)
                        .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(lowerInput))
                        .limit(80) // 너무 많은 결과 방지
                        .collect(Collectors.toList());
            }

            @Override
            public @NotNull String getErrorMessage() {
                return "아이템 이름";
            }
        });
    }

    private ArgumentParsers() {}

    /**
     * 특정 타입에 대한 새로운 인자 파서를 등록합니다.
     *
     * @param type   파싱할 타입의 클래스.
     * @param parser 파서 구현체.
     * @param <T>    타입 파라미터.
     */
    public static <T> void register(@NotNull Class<T> type, @NotNull ArgumentParser<T> parser) {
        parsers.put(type, parser);
    }

    /**
     * Enum 타입을 위한 파서를 동적으로 생성하여 반환합니다.
     * <p>
     * 사용법: <code>node.sub("mode", ArgumentParsers.forEnum(GameMode.class))</code>
     *
     * @param enumType 파싱할 Enum 클래스
     * @param <E> Enum 타입
     * @return 해당 Enum을 위한 ArgumentParser
     */
    public static <E extends Enum<E>> ArgumentParser<E> forEnum(@NotNull Class<E> enumType) {
        return new ArgumentParser<>() {
            @Override
            public E parse(@NotNull org.bukkit.command.CommandSender sender, @NotNull String input) {
                try {
                    return Enum.valueOf(enumType, input.toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }

            @Override
            public @NotNull List<String> suggest(@NotNull org.bukkit.command.CommandSender sender, @NotNull String input) {
                return Arrays.stream(enumType.getEnumConstants())
                        .map(Enum::name)
                        .filter(name -> name.startsWith(input.toUpperCase(Locale.ROOT)))
                        .collect(Collectors.toList());
            }

            @Override
            public @NotNull String getErrorMessage() {
                return enumType.getSimpleName() + " 값";
            }
        };
    }

    /**
     * 주어진 타입에 대해 등록된 파서를 검색합니다.
     * <p>
     * 만약 등록된 파서가 없고 타입이 Enum이라면, 자동으로 Enum 파서를 생성하여 반환합니다.
     *
     * @param type 타입의 클래스.
     * @param <T>  타입 파라미터.
     * @return 등록된 {@link ArgumentParser}, 또는 존재하지 않으면 {@code null}.
     */
    @Nullable
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> ArgumentParser<T> get(@NotNull Class<T> type) {
        if (parsers.containsKey(type)) {
            return (ArgumentParser<T>) parsers.get(type);
        }
        if (type.isEnum()) {
            return (ArgumentParser<T>) forEnum((Class) type);
        }
        return null;
    }
}
