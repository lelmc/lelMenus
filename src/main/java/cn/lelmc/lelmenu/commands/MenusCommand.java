package cn.lelmc.lelmenu.commands;

import cn.lelmc.lelmenu.Lelmenus;
import cn.lelmc.lelmenu.menus.ChestMenu;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;

import java.util.HashMap;
import java.util.Map;

public class MenusCommand {
    Lelmenus lelmenus = Lelmenus.instance;
    Map<String, String> currentCommandMapping = new HashMap<>();

    public MenusCommand(RegisterCommandEvent<Command.Parameterized> event) {
        // 加载所有菜单配置
        lelmenus.configManager.loadMenus();
        // 获取需要注册的命令
        Map<String, String> commandsToRegister = lelmenus.menuLoader.getRegisteredCommands();

        // 注册固定命令
        registerFixedCommands(event);

        // 注册动态菜单命令
        registerMenuCommands(event, commandsToRegister);

        // 保存当前命令映射用于后续重载
        this.currentCommandMapping = new HashMap<>(commandsToRegister);
    }

    private void registerFixedCommands(RegisterCommandEvent<Command.Parameterized> event) {
        // 菜单参数
        final Parameter.Value<String> menuParam = Parameter.string()
                .key("menu")
                .optional()
                .build();

        // /menu 命令
        event.register(lelmenus.container, Command.builder()
                .addParameter(menuParam)
                .permission("lelmenu.use")
                .executor(ctx -> {
                    if (ctx.cause().audience() instanceof ServerPlayer player) {
                        final String menuName = ctx.one(menuParam).orElse("main");
                        final ChestMenu menu = lelmenus.menuLoader.loadMenu(menuName, player);
                        if (menu != null) {
                            menu.open(player);
                        } else {
                            player.sendMessage(Component.text("菜单不存在: " + menuName, NamedTextColor.RED));
                        }
                    } else {
                        ctx.sendMessage(Identity.nil(), Component.text("只能玩家使用此命令", NamedTextColor.RED));
                    }
                    return CommandResult.success();
                })
                .build(), "menu", "lelmenu");

        event.register(lelmenus.container, Command.builder()
                .permission("lelmenu.reload")
                .executor(this::handleMenuReload)
                .build(), "menureload");

        NBTCommand nbtCommand = new NBTCommand(lelmenus.container);
        event.register(lelmenus.container, nbtCommand.createCommand(), "nbtinfo");
    }

    private void registerMenuCommands(RegisterCommandEvent<Command.Parameterized> event, Map<String, String> commandMappings) {
        for (Map.Entry<String, String> entry : commandMappings.entrySet()) {
            String commandName = entry.getKey();
            String menuName = entry.getValue();

            event.register(lelmenus.container, Command.builder()
                    .permission("lelmenu.use")
                    .executor(ctx -> {
                        if (ctx.cause().audience() instanceof ServerPlayer player) {
                            final ChestMenu menu = lelmenus.menuLoader.loadMenu(menuName, player);
                            if (menu != null) {
                                menu.open(player);
                            } else {
                                player.sendMessage(Component.text("菜单不存在: " + menuName, NamedTextColor.RED));
                            }
                        } else {
                            ctx.sendMessage(Identity.nil(), Component.text("只能玩家使用此命令", NamedTextColor.RED));
                        }
                        return CommandResult.success();
                    })
                    .build(), commandName);
        }
    }

    private CommandResult handleMenuReload(CommandContext ctx) {
        // 重新加载配置
        lelmenus.configManager.reload();

        // 获取新的命令映射
        Map<String, String> newCommandMapping = lelmenus.menuLoader.getRegisteredCommands();

        // 发送重载成功消息
        ctx.sendMessage(Identity.nil(), Component.text("配置已重载!", NamedTextColor.GREEN));

        // 更新内部映射
        this.currentCommandMapping = new HashMap<>(newCommandMapping);
        return CommandResult.success();
    }
}
