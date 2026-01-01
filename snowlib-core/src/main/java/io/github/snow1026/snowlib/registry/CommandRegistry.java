package io.github.snow1026.snowlib.registry;

import io.github.snow1026.snowlib.SnowLibrary;
import io.github.snow1026.snowlib.command.Sommand;
import io.github.snow1026.snowlib.command.SommandNode;
import io.github.snow1026.snowlib.util.reflect.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CommandRegistry {
    private static final String BRIGADIER_LITERAL_BUILDER = "com.mojang.brigadier.builder.LiteralArgumentBuilder";
    private static final String BRIGADIER_REQUIRED_BUILDER = "com.mojang.brigadier.builder.RequiredArgumentBuilder";
    private static final String BRIGADIER_COMMAND = "com.mojang.brigadier.Command";
    private static final String BRIGADIER_PREDICATE = "java.util.function.Predicate";
    private static final String BRIGADIER_SUGGESTION_PROVIDER = "com.mojang.brigadier.suggestion.SuggestionProvider";

    public static void register(Sommand sommand) {
        try {
            Object craftServer = Reflection.on(Bukkit.getServer()).get();
            Object minecraftServer = Reflection.on(craftServer).call("getServer").get();
            Object commandManager = Reflection.on(minecraftServer).call("getCommands").get();
            Object dispatcher = Reflection.on(commandManager).call("getDispatcher").get();

            SommandNode rootNode = sommand.root();
            Object rootBuilder = createLiteralBuilder(sommand.getName());

            if (rootNode.getPermission() != null) {
                setRequirement(rootBuilder, rootNode.getPermission());
            }

            for (SommandNode child : rootNode.getChildren().values()) {
                Object childBuilder = buildBrigadierNode(child);
                Reflection.on(rootBuilder).call("then", childBuilder);
            }

            setExecutor(rootBuilder, rootNode);

            Reflection.on(dispatcher).call("register", rootBuilder);

            Object rootCommandNode = Reflection.on(dispatcher).call("getRoot").call("getChild", sommand.getName()).get();
            for (String alias : rootNode.getAliases()) {
                Object aliasBuilder = createLiteralBuilder(alias);
                Reflection.on(aliasBuilder).call("redirect", rootCommandNode);
                Reflection.on(dispatcher).call("register", aliasBuilder);
            }

        } catch (Exception e) {
            SnowLibrary.snowlibrary().getLogger().severe("[SnowLib] Failed to register command: " + sommand.getName() + e.getMessage());
        }
    }

    private static Object buildBrigadierNode(SommandNode node) {
        Object builder;
        boolean isLiteral = Reflection.on(node).call("isLiteral").get();

        if (isLiteral) {
            builder = createLiteralBuilder(node.getName());
        } else {
            Class<?> type = Reflection.on(node).call("getType").get();
            Object argumentType = mapArgumentType(type);
            builder = createRequiredBuilder(node.getName(), argumentType);

            setSuggestions(builder, node);
        }

        if (node.getPermission() != null) {
            setRequirement(builder, node.getPermission());
        }

        setExecutor(builder, node);

        for (SommandNode child : node.getChildren().values()) {
            Object childBuilder = buildBrigadierNode(child);
            Reflection.on(builder).call("then", childBuilder);
        }

        return builder;
    }

    private static Object createLiteralBuilder(String name) {
        return Reflection.invokeStaticMethod(Reflection.getClass(BRIGADIER_LITERAL_BUILDER), "literal", name);
    }

    private static Object createRequiredBuilder(String name, Object argumentType) {
        return Reflection.invokeStaticMethod(Reflection.getClass(BRIGADIER_REQUIRED_BUILDER), "argument", name, argumentType);
    }

    private static Object mapArgumentType(Class<?> type) {
        try {
            if (type == Integer.class || type == int.class) {
                return Reflection.invokeStaticMethod(Reflection.getClass("com.mojang.brigadier.arguments.IntegerArgumentType"), "integer");
            }
            if (type == Double.class || type == double.class) {
                return Reflection.invokeStaticMethod(Reflection.getClass("com.mojang.brigadier.arguments.DoubleArgumentType"), "doubleArg");
            }
            if (type == Boolean.class || type == boolean.class) {
                return Reflection.invokeStaticMethod(Reflection.getClass("com.mojang.brigadier.arguments.BoolArgumentType"), "bool");
            }
            if (type == Player.class) {
                Class<?> entityArgumentClass = Reflection.getMinecraftClass("commands.arguments.EntityArgument");
                return Reflection.invokeStaticMethod(entityArgumentClass, "player");
            }
            return Reflection.invokeStaticMethod(Reflection.getClass("com.mojang.brigadier.arguments.StringArgumentType"), "word");
        } catch (Exception e) {
            throw new RuntimeException("Failed to map argument type for: " + type.getName(), e);
        }
    }

    private static void setExecutor(Object builder, SommandNode node) {
        Class<?> commandInterface = Reflection.getClass(BRIGADIER_COMMAND);

        Object commandProxy = Proxy.newProxyInstance(
                commandInterface.getClassLoader(),
                new Class[]{commandInterface},
                (proxy, method, args) -> {
                    String name = method.getName();

                    switch (name) {
                        case "equals" -> {
                            return proxy == args[0];
                        }
                        case "hashCode" -> {
                            return System.identityHashCode(proxy);
                        }
                        case "toString" -> {
                            return "SnowLibCommandProxy(" + node.getName() + ")";
                        }

                        case "run" -> {
                            Object context = args[0];
                            return executeSommand(context, node);
                        }
                    }

                    return 0;
                }
        );

        Reflection.on(builder).call("executes", commandProxy);
    }


    private static void setRequirement(Object builder, String permission) {
        Class<?> predicateInterface = Reflection.getClass(BRIGADIER_PREDICATE);

        Object predicateProxy = Proxy.newProxyInstance(
                predicateInterface.getClassLoader(),
                new Class[]{predicateInterface},
                (proxy, method, args) -> {
                    String name = method.getName();

                    switch (name) {
                        case "equals" -> {
                            return proxy == args[0];
                        }
                        case "hashCode" -> {
                            return System.identityHashCode(proxy);
                        }
                        case "toString" -> {
                            return "SnowLibPermissionPredicate(" + permission + ")";
                        }
                        case "test" -> {
                            Object commandSourceStack = args[0];
                            Object bukkitSender = Reflection.on(commandSourceStack).call("getBukkitSender").get();
                            return Reflection.on(bukkitSender).call("hasPermission", permission).get();
                        }
                    }

                    return false;
                }
        );

        Reflection.on(builder).call("requires", predicateProxy);
    }


    private static void setSuggestions(Object builder, SommandNode node) {
        Class<?> suggestionProviderInterface = Reflection.getClass(BRIGADIER_SUGGESTION_PROVIDER);

        Object suggestionProxy = Proxy.newProxyInstance(
                suggestionProviderInterface.getClassLoader(),
                new Class[]{suggestionProviderInterface},
                (proxy, method, args) -> {
                    String name = method.getName();

                    switch (name) {
                        case "equals" -> {
                            return proxy == args[0];
                        }
                        case "hashCode" -> {
                            return System.identityHashCode(proxy);
                        }
                        case "toString" -> {
                            return "SnowLibSuggestionProvider(" + node.getName() + ")";
                        }
                        case "getSuggestions" -> {
                            Object context = args[0];
                            Object suggestionsBuilder = args[1];
                            return getSuggestionsProxy(context, node, suggestionsBuilder);
                        }
                    }

                    return null;
                }
        );

        Reflection.on(builder).call("suggests", suggestionProxy);
    }


    private static int executeSommand(Object context, SommandNode node) {
        try {
            String input = Reflection.on(context).call("getInput").get();
            Object sourceStack = Reflection.on(context).call("getSource").get();
            Object sender = Reflection.on(sourceStack).call("getBukkitSender").get();

            if (input.startsWith("/")) {
                input = input.substring(1);
            }

            String[] parts = input.split(" ");
            String label = parts[0];
            String[] args = new String[0];

            if (parts.length > 1) {
                args = Arrays.copyOfRange(parts, 1, parts.length);
            }

            Reflection.on(node).call("execute", sender, label, args);
            return 1;
        } catch (Exception e) {
            String message = e.getMessage() != null ? e.getMessage() : "Unknown error";
            try {
                Object sourceStack = Reflection.on(context).call("getSource").get();
                Class<?> componentClass = Reflection.getMinecraftClass("network.chat.Component");
                Object component = Reflection.invokeStaticMethod(componentClass, "literal", message);

                Reflection.on(sourceStack).call("sendFailure", component);
            } catch (Exception ignored) {}
            return 0;
        }
    }

    private static CompletableFuture<?> getSuggestionsProxy(Object context, SommandNode node, Object suggestionsBuilder) {
        Object provider = Reflection.on(node).call("getSuggestionProvider").get();

        if (provider != null) {
            String remaining = Reflection.on(suggestionsBuilder).call("getRemaining").get();
            try {
                Object sourceStack = Reflection.on(context).call("getSource").get();
                Object sender = Reflection.on(sourceStack).call("getBukkitSender").get();

                List<String> suggestions = Reflection.on(provider).call("getSuggestions", sender, remaining).get();

                for (String s : suggestions) {
                    if (s.toLowerCase().startsWith(remaining.toLowerCase())) {
                        Reflection.on(suggestionsBuilder).call("suggest", s);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return Reflection.on(suggestionsBuilder).call("buildFuture").get();
    }
}
