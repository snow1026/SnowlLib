package io.github.snow1026.snowlib.internal.command;

import io.github.snow1026.snowlib.api.command.SimpleContext;
import io.github.snow1026.snowlib.api.command.Sommand;
import io.github.snow1026.snowlib.api.command.SommandContext;
import io.github.snow1026.snowlib.api.command.argument.ArgumentParser;
import io.github.snow1026.snowlib.api.command.argument.ArgumentParsers;
import io.github.snow1026.snowlib.api.command.argument.SuggestionProvider;
import io.github.snow1026.snowlib.exceptions.CommandParseException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SnowSommand implements Sommand {
    private final String name;
    private final Class<?> type;
    private final ArgumentParser<?> parser;
    private final boolean required;

    private final Map<String, SnowSommand> children = new LinkedHashMap<>();
    private final List<Predicate<CommandSender>> requirements = new ArrayList<>();

    private final List<String> aliases = new ArrayList<>();
    private String description = "";
    private String usage = "";

    private Consumer<SommandContext> executor;
    private String permission;
    private SuggestionProvider suggestionProvider;


    public SnowSommand(String name) {
        this.name = name;
        this.type = String.class;
        this.parser = ArgumentParsers.get(type);
        this.required = true;
    }

    protected SnowSommand(String name, Class<?> type, boolean required) {
        this.name = name;
        this.type = type;
        this.parser = ArgumentParsers.get(type);
        this.required = required;
    }

    @Override
    public Sommand sub(@NotNull String name, @NotNull Consumer<Sommand> sommand) {
        SnowSommand child = new SnowSommand(name, String.class, true) {
            @Override
            protected @NotNull Object parseValue(CommandSender sender, String input) throws CommandParseException {
                if (input.equalsIgnoreCase(name)) return name;
                throw new CommandParseException("Expected '" + name + "', but got '" + input + "'");
            }
        };
        child.suggests((sender, current) -> name.toLowerCase(Locale.ROOT).startsWith(current.toLowerCase(Locale.ROOT)) ? List.of(name) : Collections.emptyList());

        sommand.accept(child);
        children.put(name.toLowerCase(Locale.ROOT), child);
        return this;
    }

    @Override
    public Sommand sub(@NotNull String name) {
        return sub(name, node -> {});
    }

    @Override
    public <T> Sommand sub(@NotNull String name, @NotNull Class<T> type, @NotNull Consumer<Sommand> sommand) {
        SnowSommand child = new SnowSommand(name, type, true);
        sommand.accept(child);
        children.put(name.toLowerCase(Locale.ROOT), child);
        return this;
    }

    @Override
    public <T> Sommand sub(@NotNull String name, @NotNull Class<T> type) {
        return sub(name, type, node -> {});
    }

    @Override
    public <T> Sommand subOptional(@NotNull String name, @NotNull Class<T> type, @NotNull Consumer<Sommand> sommand) {
        SnowSommand child = new SnowSommand(name, type, false);
        sommand.accept(child);
        children.put(name.toLowerCase(Locale.ROOT), child);
        return this;
    }

    @Override
    public <T> Sommand subOptional(@NotNull String name, @NotNull Class<T> type) {
        return subOptional(name, type, node -> {});
    }

    @Override
    public Sommand alias(@NotNull String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
        return this;
    }

    @Override
    public Sommand description(@NotNull String description) {
        this.description = description;
        return this;
    }

    @Override
    public Sommand usage(@NotNull String usage) {
        this.usage = usage;
        return this;
    }

    @Override
    public Sommand executes(@NotNull Consumer<SommandContext> executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public Sommand requires(@NotNull String permission) {
        this.permission = permission;
        return this;
    }

    @Override
    public Sommand requires(@NotNull Predicate<CommandSender> requirement, @NotNull String failMessage) {
        this.requirements.add(sender -> {
            if (!requirement.test(sender)) throw new CommandParseException(failMessage);
            return true;
        });
        return this;
    }

    @Override
    public Sommand playerOnly() {
        return requires(sender -> sender instanceof Player, "§c이 명령어는 플레이어만 실행할 수 있습니다.");
    }

    @Override
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
        SnowSommand matchedChild = null;

        for (SnowSommand child : children.values()) {
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

        for (SnowSommand child : children.values()) {
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
            for (SnowSommand child : children.values()) {
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

    @Override
    public Map<String, SnowSommand> getChildren() {
        return children;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public String getPermission() {
        return permission;
    }

    @Override
    public List<String> getAliases() {
        return aliases;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
