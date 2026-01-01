package io.github.snow1026.snowlib.command;

import io.github.snow1026.snowlib.command.argument.ArgumentParser;
import io.github.snow1026.snowlib.command.argument.ArgumentParsers;
import io.github.snow1026.snowlib.command.argument.SuggestionProvider;
import io.github.snow1026.snowlib.exception.CommandParseException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 명령어 트리의 노드를 나타내며, 유창한 방식으로 명령어 구문을 구성할 수 있도록 합니다.
 * <p>
 * 각 노드는 리터럴 서브 명령어, 타입이 지정된 인자, 또는 명령어의 루트(Root)를 나타낼 수 있습니다.
 */
public class SommandNode {

    private final String name;
    private final Class<?> type;
    private final ArgumentParser<?> parser;
    private final boolean required;

    private final Map<String, SommandNode> children = new LinkedHashMap<>();
    private final List<Predicate<CommandSender>> requirements = new ArrayList<>();

    private final List<String> aliases = new ArrayList<>();
    private String description = "";
    private String usage = "";

    private Consumer<SommandContext> executor;
    private String permission;
    private SuggestionProvider suggestionProvider;

    /**
     * 명령어 노드 생성을 위한 생성자입니다.
     * * @param name 노드의 이름.
     * @param type 데이터 타입.
     * @param required 필수 여부.
     */
    protected SommandNode(String name, Class<?> type, boolean required) {
        this.name = name;
        this.type = type;
        this.parser = ArgumentParsers.get(type);
        this.required = required;
    }

    /**
     * 이 노드에 리터럴 서브 명령어를 추가합니다.
     *
     * @param name    리터럴 값.
     * @param builder 노드 구성 빌더.
     * @return 현재 노드 인스턴스.
     */
    public SommandNode sub(@NotNull String name, @NotNull Consumer<SommandNode> builder) {
        SommandNode child = new SommandNode(name, String.class, true) {
            @Override
            protected @NotNull Object parseValue(CommandSender sender, String input) throws CommandParseException {
                if (input.equalsIgnoreCase(name)) return name;
                throw new CommandParseException("Expected '" + name + "', but got '" + input + "'");
            }
        };
        child.suggests((sender, current) -> name.toLowerCase(Locale.ROOT).startsWith(current.toLowerCase(Locale.ROOT)) ? List.of(name) : Collections.emptyList());

        builder.accept(child);
        children.put(name.toLowerCase(Locale.ROOT), child);
        return this;
    }

    public SommandNode sub(@NotNull String name) {
        return sub(name, node -> {});
    }

    /**
     * 이 노드에 필수 인자를 추가합니다.
     */
    public <T> SommandNode sub(@NotNull String name, @NotNull Class<T> type, @NotNull Consumer<SommandNode> builder) {
        SommandNode child = new SommandNode(name, type, true);
        builder.accept(child);
        children.put(name.toLowerCase(Locale.ROOT), child);
        return this;
    }

    public <T> SommandNode sub(@NotNull String name, @NotNull Class<T> type) {
        return sub(name, type, node -> {});
    }

    /**
     * 이 노드에 선택적 인자를 추가합니다.
     */
    public <T> SommandNode subOptional(@NotNull String name, @NotNull Class<T> type, @NotNull Consumer<SommandNode> builder) {
        SommandNode child = new SommandNode(name, type, false);
        builder.accept(child);
        children.put(name.toLowerCase(Locale.ROOT), child);
        return this;
    }

    public <T> SommandNode subOptional(@NotNull String name, @NotNull Class<T> type) {
        return subOptional(name, type, node -> {});
    }

    /**
     * 명령어 별칭을 설정합니다. (루트 노드에서 주로 사용)
     */
    public SommandNode alias(@NotNull String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
        return this;
    }

    /**
     * 명령어 설명을 설정합니다.
     */
    public SommandNode description(@NotNull String description) {
        this.description = description;
        return this;
    }

    /**
     * 사용법 메시지를 설정합니다.
     */
    public SommandNode usage(@NotNull String usage) {
        this.usage = usage;
        return this;
    }

    /**
     * 실행 로직을 정의합니다.
     */
    public SommandNode executes(@NotNull Consumer<SommandContext> executor) {
        this.executor = executor;
        return this;
    }

    /**
     * 필요한 권한을 설정합니다.
     */
    public SommandNode requires(@NotNull String permission) {
        this.permission = permission;
        return this;
    }

    /**
     * 일반적인 요구 사항을 추가합니다.
     */
    public SommandNode requires(@NotNull Predicate<CommandSender> requirement, @NotNull String failMessage) {
        this.requirements.add(sender -> {
            if (!requirement.test(sender)) throw new CommandParseException(failMessage);
            return true;
        });
        return this;
    }

    public SommandNode playerOnly() {
        return requires(sender -> sender instanceof Player, "§c이 명령어는 플레이어만 실행할 수 있습니다.");
    }

    public void suggests(@NotNull SuggestionProvider provider) {
        this.suggestionProvider = provider;
    }

    protected Class<?> getType() {
        return type;
    }

    protected boolean isLiteral() {
        return type == String.class;
    }

    protected SuggestionProvider getSuggestionProvider() {
        return suggestionProvider;
    }

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
            if (children.values().stream().anyMatch(n -> n.required)) {
                throw new CommandParseException("§c불완전한 명령어입니다. 사용법: " + usage);
            }
            if (!children.isEmpty()) throw new CommandParseException("§c유효한 서브 명령어를 지정해야 합니다.");
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
            } catch (CommandParseException ignored) {}
        }

        if (matchedChild != null) {
            matchedChild.executeRecursive(sender, label, args, index + 1, context);
            return;
        }

        for (SommandNode child : children.values()) {
            if (!child.required) context.addArgument(child.name, null);
        }

        if (this.executor != null) {
            this.executor.accept(context);
            return;
        }

        throw new CommandParseException("§c유효하지 않은 인자: '" + currentArg + "'");
    }

    protected List<String> suggest(@NotNull CommandSender sender, @NotNull String[] args) {
        return suggestRecursive(sender, args, 0);
    }

    private List<String> suggestRecursive(@NotNull CommandSender sender, @NotNull String[] args, int index) {
        if (!hasPermission(sender)) return Collections.emptyList();
        if (args.length == 0 || index >= args.length) return Collections.emptyList();

        String currentInput = args[index];
        if (index == args.length - 1) {
            return children.values().stream()
                    .filter(child -> child.hasPermission(sender))
                    .flatMap(child -> child.getSuggestions(sender, currentInput).stream())
                    .filter(s -> s.toLowerCase(Locale.ROOT).startsWith(currentInput.toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        } else {
            for (SommandNode child : children.values()) {
                if (!child.hasPermission(sender)) continue;
                try {
                    child.parseValue(sender, args[index]);
                    return child.suggestRecursive(sender, args, index + 1);
                } catch (CommandParseException ignored) {}
            }
        }
        return Collections.emptyList();
    }

    private void checkRequirements(@NotNull CommandSender sender) {
        if (!hasPermission(sender)) throw new CommandParseException("§c이 명령어를 사용할 권한이 없습니다.");
        for (Predicate<CommandSender> requirement : requirements) requirement.test(sender);
    }

    private boolean hasPermission(@NotNull CommandSender sender) {
        return permission == null || sender.hasPermission(permission);
    }

    @NotNull
    protected Object parseValue(CommandSender sender, String input) throws CommandParseException {
        if (parser == null) throw new CommandParseException("No parser found for type " + type.getSimpleName());
        Object value = parser.parse(sender, input);
        if (value == null) throw new CommandParseException("유효하지 않은 입력: '" + input + "' (예상: " + parser.getErrorMessage() + ")");
        return value;
    }

    @NotNull
    private List<String> getSuggestions(CommandSender sender, String current) {
        if (suggestionProvider != null) return suggestionProvider.getSuggestions(sender, current);
        if (parser != null) return parser.suggest(sender, current);
        return Collections.emptyList();
    }

    // NMS 등록 시 접근을 위한 Getter들
    public Map<String, SommandNode> getChildren() { return children; }
    public String getName() { return name; }
    public String getPermission() { return permission; }
    public List<String> getAliases() { return aliases; }

    public String getDescription() {
        return description;
    }
}
