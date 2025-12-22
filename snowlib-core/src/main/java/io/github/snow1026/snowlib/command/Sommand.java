package io.github.snow1026.snowlib.command;

import io.github.snow1026.snowlib.exceptions.CommandParseException;
import io.github.snow1026.snowlib.utils.reflect.Reflection;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * SnowLib 커맨드의 중앙 레지스트리(Registry)이자 실행기(Executor).
 * <p>
 * 이 클래스는 Bukkit의 {@link CommandMap}에 명령어를 자동으로 등록하고,
 * 복잡한 명령어 구조를 유창한 빌더 스타일 API로 정의할 수 있도록 지원합니다.
 */
public class Sommand implements TabExecutor {

    private static final Sommand INSTANCE = new Sommand();

    private static final Map<String, SommandNode> rootCommands = new HashMap<>();

    private static JavaPlugin plugin;
    private static CommandMap commandMap;

    /**
     * Sommand 라이브러리를 초기화합니다. 플러그인의 onEnable에서 호출해야 합니다.
     * @param instance 메인 플러그인 인스턴스
     */
    public static void init(JavaPlugin instance) {
        plugin = instance;
        commandMap = instance.getServer().getCommandMap();
    }

    private Sommand() {}

    /**
     * 새로운 최상위(top-level) 명령어를 등록하고 구성에 사용할 루트 노드를 반환합니다.
     * <p>
     * 이 노드를 사용하여 명령어의 별칭, 설명, 사용법 메시지, 서브 명령어 및 실행 로직을 정의할 수 있습니다.
     * @param name 명령어의 이름 (대소문자 구분 없음).
     * @return 유창한 명령어 구성을 위한 루트 {@link SommandNode}.
     */
    public static SommandNode register(@NotNull String name) {
        if (plugin == null) {
            throw new IllegalStateException("Sommand has not been initialized. Call Sommand.init(plugin) first.");
        }
        Objects.requireNonNull(name, "Command name cannot be null");
        String lowerName = name.toLowerCase(Locale.ROOT);

        PluginCommand cmd = getOrCreateCommand(name);
        SommandNode root = new SommandNode(lowerName, String.class, cmd, false); // 루트는 항상 필수가 아님

        rootCommands.put(lowerName, root);

        cmd.setExecutor(INSTANCE);
        cmd.setTabCompleter(INSTANCE);

        return root;
    }

    private static PluginCommand getOrCreateCommand(String name) {
        Command existing = commandMap.getCommand(name);

        if (existing instanceof PluginCommand && ((PluginCommand) existing).getPlugin().equals(plugin)) {
            return (PluginCommand) existing;
        }

        PluginCommand newCmd = (PluginCommand) Reflection.getConstructor(PluginCommand.class).invoke(name, plugin);

        commandMap.register(plugin.getName(), newCmd);

        newCmd.setExecutor(INSTANCE);
        newCmd.setTabCompleter(INSTANCE);
        return newCmd;
    }


    /**
     * Bukkit의 {@link CommandExecutor#onCommand} 구현.
     * (이 메서드는 INSTANCE를 통해 Bukkit에 의해 호출됩니다.)
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        SommandNode root = rootCommands.get(command.getName().toLowerCase(Locale.ROOT));
        if (root == null) return false;

        try {
            root.execute(sender, label, args);
        } catch (CommandParseException e) {
            sender.sendMessage("§c" + e.getMessage());
        } catch (Exception e) {
            sender.sendMessage("§c명령어 실행 중 내부 오류가 발생했습니다.");
            throw new RuntimeException(e);
        }
        return true;
    }

    /**
     * Bukkit의 {@link TabCompleter#onTabComplete} 구현.
     * (이 메서드는 INSTANCE를 통해 Bukkit에 의해 호출됩니다.)
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        SommandNode root = rootCommands.get(command.getName().toLowerCase(Locale.ROOT));
        if (root == null) return Collections.emptyList();
        try {
            return root.suggest(sender, args);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
