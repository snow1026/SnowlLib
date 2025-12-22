package io.github.snow1026.snowlib.command;

import io.github.snow1026.snowlib.command.argument.ArgumentParser;
import io.github.snow1026.snowlib.command.argument.ArgumentParsers;
import io.github.snow1026.snowlib.command.argument.SuggestionProvider;
import io.github.snow1026.snowlib.exceptions.CommandParseException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 명령어 트리의 노드를 나타내며, 유창한 방식으로 명령어 구문을 구성할 수 있도록 합니다.
 * <p>
 * 각 노드는 리터럴 서브 명령어, 타입이 지정된 인자, 또는 명령어의 루트(Root)를 나타낼 수 있습니다.
 * 이 클래스는 자식 노드, 실행 로직, 권한, 그리고 기타 제약 조건에 대한 정보를 보유합니다.
 */
public class SommandNode {

    private final String name;
    private final Class<?> type;
    private final ArgumentParser<?> parser;
    private final boolean required; // 이 인자가 필수인지 여부

    private final Map<String, SommandNode> children = new LinkedHashMap<>();
    private final List<Predicate<CommandSender>> requirements = new ArrayList<>();

    private final PluginCommand bukkitCommand;

    private Consumer<SommandContext> executor;
    private String permission;
    private SuggestionProvider suggestionProvider;

    /**
     * 루트 명령어 노드를 위한 내부 생성자.
     * @param name 노드의 이름.
     * @param type 이 노드가 나타내는 데이터 타입.
     * @param bukkitCommand 연관된 PluginCommand (루트 노드에만 해당).
     * @param required 이 노드의 인자가 필수인지 여부.
     */
    protected SommandNode(String name, Class<?> type, @Nullable PluginCommand bukkitCommand, boolean required) {
        this.name = name;
        this.type = type;
        this.parser = ArgumentParsers.get(type);
        this.bukkitCommand = bukkitCommand;
        this.required = required;
    }

    /**
     * 자식 노드를 위한 내부 생성자.
     */
    private SommandNode(String name, Class<?> type, boolean required) {
        this(name, type, null, required);
    }

    /**
     * 이 노드에 리터럴(literal) 서브 명령어를 추가합니다. 리터럴은 대소문자를 구분하지 않는 고정 문자열입니다.
     *
     * @param name    리터럴 문자열 값.
     * @param builder 새로 생성된 리터럴 노드를 구성하기 위한 Consumer.
     * @return 체이닝을 위한 이 노드 인스턴스.
     */
    public SommandNode sub(@NotNull String name, @NotNull Consumer<SommandNode> builder) {
        SommandNode child = new SommandNode(name, String.class, true) {
            @Override
            protected @NotNull Object parseValue(CommandSender sender, String input) throws CommandParseException {
                if (input.equalsIgnoreCase(name)) {
                    return name;
                }
                throw new CommandParseException("Expected '" + name + "', but got '" + input + "'");
            }
        };
        child.suggests((sender, current) -> name.toLowerCase(Locale.ROOT).startsWith(current.toLowerCase(Locale.ROOT)) ? List.of(name) : Collections.emptyList());

        builder.accept(child);
        children.put(name.toLowerCase(Locale.ROOT), child);
        return this;
    }

    /**
     * 더 이상 자식이 없는 리터럴 서브 명령어를 추가합니다.
     *
     * @param name 리터럴 문자열 값.
     * @return 체이닝을 위한 이 노드 인스턴스.
     */
    public SommandNode sub(@NotNull String name) {
        return sub(name, node -> {});
    }

    /**
     * 이 명령어 노드에 **필수** 인자를 추가합니다.
     *
     * @param name    인자의 이름. {@link SommandContext}에서 값을 가져올 때 사용됩니다.
     * @param type    인자 타입의 클래스 (예: {@code String.class}, {@code Player.class}).
     * @param builder 새로 생성된 인자 노드를 구성하기 위한 Consumer.
     * @param <T>     인자의 타입 파라미터.
     * @return 체이닝을 위한 이 노드 인스턴스.
     */
    public <T> SommandNode sub(@NotNull String name, @NotNull Class<T> type, @NotNull Consumer<SommandNode> builder) {
        SommandNode child = new SommandNode(name, type, true); // 필수(required = true)
        builder.accept(child);
        children.put(name.toLowerCase(Locale.ROOT), child);
        return this;
    }

    /**
     * 더 이상 자식이 없는 **필수** 인자를 추가합니다.
     *
     * @param name 인자의 이름.
     * @param type 인자 타입의 클래스.
     * @param <T>  인자의 타입 파라미터.
     * @return 체이닝을 위한 이 노드 인스턴스.
     */
    public <T> SommandNode sub(@NotNull String name, @NotNull Class<T> type) {
        return sub(name, type, node -> {});
    }

    /**
     * 이 명령어 노드에 **선택적** 인자를 추가합니다.
     * <p>
     * 이 인자가 제공되지 않으면, {@link SommandContext#getArgument(String, Object)}로 {@code null}을 가져와야 합니다.
     *
     * @param name    인자의 이름.
     * @param type    인자 타입의 클래스.
     * @param builder 새로 생성된 인자 노드를 구성하기 위한 Consumer.
     * @param <T>     인자의 타입 파라미터.
     * @return 체이닝을 위한 이 노드 인스턴스.
     */
    public <T> SommandNode subOptional(@NotNull String name, @NotNull Class<T> type, @NotNull Consumer<SommandNode> builder) {
        SommandNode child = new SommandNode(name, type, false); // 선택적(required = false)
        builder.accept(child);
        // 선택적 인자는 필수 인자보다 나중에 정의되어야 합니다.
        children.put(name.toLowerCase(Locale.ROOT), child);
        return this;
    }

    /**
     * 더 이상 자식이 없는 **선택적** 인자를 추가합니다.
     *
     * @param name 인자의 이름.
     * @param type 인자 타입의 클래스.
     * @param <T>  인자의 타입 파라미터.
     * @return 체이닝을 위한 이 노드 인스턴스.
     */
    public <T> SommandNode subOptional(@NotNull String name, @NotNull Class<T> type) {
        return subOptional(name, type, node -> {});
    }

    /**
     * 이 명령어의 별칭을 설정합니다. 루트 노드에서만 유효합니다.
     * @param aliases 명령어 별칭 목록.
     * @return 체이닝을 위한 이 노드 인스턴스.
     */
    public SommandNode alias(@NotNull String... aliases) {
        if (bukkitCommand != null) {
            bukkitCommand.setAliases(Arrays.asList(aliases));
        }
        return this;
    }

    /**
     * 이 명령어의 설명을 설정합니다. 루트 노드에서만 유효합니다.
     * @param description 명령어 설명.
     * @return 체이닝을 위한 이 노드 인스턴스.
     */
    public SommandNode description(@NotNull String description) {
        if (bukkitCommand != null) {
            bukkitCommand.setDescription(description);
        }
        return this;
    }

    /**
     * 이 명령어의 사용법 메시지를 설정합니다. 루트 노드에서만 유효합니다.
     * @param usage 사용법 메시지 (예: "/<command> [args]").
     * @return 체이닝을 위한 이 노드 인스턴스.
     */
    public SommandNode usage(@NotNull String usage) {
        if (bukkitCommand != null) {
            bukkitCommand.setUsage(usage);
        }
        return this;
    }

    /**
     * 이 노드가 성공적으로 도달하고 실행될 때 수행할 작업을 정의합니다.
     *
     * @param executor 실행 컨텍스트({@link SommandContext})를 받는 Consumer.
     * @return 체이닝을 위한 이 노드 인스턴스.
     */
    public SommandNode executes(@NotNull Consumer<SommandContext> executor) {
        this.executor = executor;
        return this;
    }

    /**
     * 이 노드와 모든 자식 노드를 실행하는 데 필요한 권한을 설정합니다.
     * 이 노드가 루트 노드인 경우 기본 Bukkit 명령어에도 권한을 설정합니다.
     *
     * @param permission 권한 문자열.
     * @return 체이닝을 위한 이 노드 인스턴스.
     */
    public SommandNode requires(@NotNull String permission) {
        this.permission = permission;
        if (bukkitCommand != null) {
            bukkitCommand.setPermission(permission);
        }
        return this;
    }

    /**
     * 이 노드를 실행하기 위한 추가적인 일반 요구 사항을 추가합니다.
     *
     * @param requirement 발신자가 진행하려면 true를 반환해야 하는 Predicate.
     * @param failMessage 요구 사항이 충족되지 않았을 때 보낼 메시지.
     * @return 체이닝을 위한 이 노드 인스턴스.
     */
    public SommandNode requires(@NotNull Predicate<CommandSender> requirement, @NotNull String failMessage) {
        this.requirements.add(sender -> {
            if (!requirement.test(sender)) {
                throw new CommandParseException(failMessage);
            }
            return true;
        });
        return this;
    }

    /**
     * 명령어 발신자가 반드시 {@link Player}여야 한다는 요구 사항을 추가하는 편의 메서드.
     *
     * @return 체이닝을 위한 이 노드 인스턴스.
     */
    public SommandNode playerOnly() {
        return requires(sender -> sender instanceof Player, "§c이 명령어는 플레이어만 실행할 수 있습니다.");
    }

    /**
     * 이 인자 노드에 대한 커스텀 탭 자동 완성 제안 Provider를 설정합니다.
     *
     * @param provider 제안 Provider.
     */
    public void suggests(@NotNull SuggestionProvider provider) {
        this.suggestionProvider = provider;
    }

    /**
     * 이 노드부터 명령 실행을 시작합니다.
     *
     * @param sender 명령 발신자.
     * @param label 사용된 레이블.
     * @param args 원본 인자 배열.
     */
    protected void execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        executeRecursive(sender, label, args, 0, new SimpleContext(sender, label, args));
    }

    private void executeRecursive(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int index, @NotNull SimpleContext context) {
        checkRequirements(sender);

        if (index >= args.length) {
            if (this.executor != null) {
                this.executor.accept(context);
                return;
            }

            boolean hasRequiredChild = children.values().stream().anyMatch(n -> n.required);
            if (hasRequiredChild) {
                throw new CommandParseException("§c불완전한 명령어입니다. 사용법을 확인하세요: " + (bukkitCommand != null ? bukkitCommand.getUsage() : "/<command> ..."));
            }

            if (!children.isEmpty()) {
                throw new CommandParseException("§c유효한 서브 명령어를 지정해야 합니다.");
            }

            throw new CommandParseException("§c명령어 실행기가 정의되지 않았습니다.");
        }

        String currentArg = args[index];
        SommandNode matchedChild = null;

        for (SommandNode child : children.values()) {
            if (!child.hasPermission(sender)) continue;

            try {
                Object parsedValue = child.parseValue(sender, currentArg);
                context.addArgument(child.name, parsedValue);
                matchedChild = child;
                break;
            } catch (CommandParseException e) {
                throw new RuntimeException(e);
            }
        }

        if (matchedChild != null) {
            matchedChild.executeRecursive(sender, label, args, index + 1, context);
            return;
        }

        for (SommandNode child : children.values()) {
            if (!child.required) {
                context.addArgument(child.name, null);
            }
        }

        if (this.executor != null) {
            this.executor.accept(context);
            return;
        }

        throw new CommandParseException("§c유효하지 않은 인자: '" + currentArg + "'");
    }

    /**
     * 이 노드부터 탭 자동 완성 제안을 시작합니다.
     *
     * @param sender 명령 발신자.
     * @param args 현재까지 입력된 인자 배열.
     * @return 필터링된 제안 목록.
     */
    protected List<String> suggest(@NotNull CommandSender sender, @NotNull String[] args) {
        return suggestRecursive(sender, args, 0);
    }

    private List<String> suggestRecursive(@NotNull CommandSender sender, @NotNull String[] args, int index) {
        if (!hasPermission(sender)) return Collections.emptyList();

        if (args.length == 0 || index >= args.length) return Collections.emptyList();

        String currentInput = args[index];
        List<String> suggestions = new ArrayList<>();

        if (index == args.length - 1) {
            suggestions = children.values().stream().filter(child -> child.hasPermission(sender)).flatMap(child -> child.getSuggestions(sender, currentInput).stream()).toList();

        } else {
            String nextArg = args[index];
            for (SommandNode child : children.values()) {
                if (!child.hasPermission(sender)) continue;

                try {
                    child.parseValue(sender, nextArg);
                    return child.suggestRecursive(sender, args, index + 1);
                } catch (CommandParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return suggestions.stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(currentInput.toLowerCase(Locale.ROOT))).collect(Collectors.toList());
    }

    /**
     * 권한 및 사용자 정의 요구 사항을 확인합니다.
     *
     * @param sender 명령 발신자.
     * @throws CommandParseException 요구 사항이 충족되지 않은 경우.
     */
    private void checkRequirements(@NotNull CommandSender sender) {
        if (!hasPermission(sender)) {
            String permissionMessage = "§c이 명령어를 사용할 권한이 없습니다.";
            throw new CommandParseException(permissionMessage);
        }
        for (Predicate<CommandSender> requirement : requirements) {
            requirement.test(sender);
        }
    }

    /**
     * 발신자가 이 노드의 권한을 가지고 있는지 확인합니다.
     *
     * @param sender 명령 발신자.
     * @return 권한이 필요하지 않거나 가지고 있으면 true, 아니면 false.
     */
    private boolean hasPermission(@NotNull CommandSender sender) {
        return permission == null || sender.hasPermission(permission);
    }

    /**
     * 문자열 입력을 이 노드의 타입으로 파싱합니다.
     *
     * @param sender 명령 발신자.
     * @param input 파싱할 문자열.
     * @return 파싱된 객체.
     * @throws CommandParseException 파싱에 실패한 경우.
     */
    @NotNull
    protected Object parseValue(CommandSender sender, String input) throws CommandParseException {
        if (parser == null) {
            throw new CommandParseException("No parser found for type " + type.getSimpleName());
        }
        Object value = parser.parse(sender, input);
        if (value == null) {
            String expected = parser.getErrorMessage();
            throw new CommandParseException("유효하지 않은 입력: '" + input + "' (예상: " + expected + ")");
        }
        return value;
    }

    /**
     * 이 노드에 대한 탭 자동 완성 제안 목록을 가져옵니다.
     *
     * @param sender 명령 발신자.
     * @param current 현재 입력된 문자열.
     * @return 제안 목록.
     */
    @NotNull
    private List<String> getSuggestions(CommandSender sender, String current) {
        if (suggestionProvider != null) {
            return suggestionProvider.getSuggestions(sender, current);
        }
        if (parser != null) {
            return parser.suggest(sender, current);
        }
        return Collections.emptyList();
    }
}
