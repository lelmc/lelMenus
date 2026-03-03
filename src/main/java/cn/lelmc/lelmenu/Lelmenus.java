package cn.lelmc.lelmenu;

import cn.lelmc.lelmenu.commands.MenusCommand;
import cn.lelmc.lelmenu.menus.ChestMenu;
import cn.lelmc.lelmenu.menus.ConfigManager;
import cn.lelmc.lelmenu.menus.MenuLoader;
import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;


@Plugin("lelmenus")
public class Lelmenus {
    public final PluginContainer container;
    public final Logger logger;
    public ConfigManager configManager;
    public MenuLoader menuLoader;
    public static Lelmenus instance;

    @Inject
    Lelmenus(final PluginContainer container, final Logger logger) {
        this.container = container;
        this.logger = logger;
        instance = this;
    }

    @Listener
    public void onConstruct(ConstructPluginEvent event) {
        // 初始化
        ChestMenu.setPlugin(container);
        configManager = new ConfigManager(container);
        menuLoader = new MenuLoader(configManager);
    }

    @Listener
    public void onRegisterCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        new MenusCommand(event);
    }


    @Listener
    public void onServerStart(final StartedEngineEvent<?> event) {
        // 创建默认配置（如果不存在）
        configManager.createDefaultConfigsIfNotExists();
        // 加载所有配置
        configManager.loadMenus();
        PlaceholderProvider.registerExpansion();
    }
}
