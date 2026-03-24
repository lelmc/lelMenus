package cn.lelmc.lelmenu;

import cn.lelmc.lelmenu.commands.MenusCommand;
import cn.lelmc.lelmenu.config.ConfigManager;
import cn.lelmc.lelmenu.menus.MenuLoader;
import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.nio.file.Path;


@Plugin("lelmenu")
public class Lelmenus {
    public static Lelmenus instance;
    public final Logger logger;
    public final PluginContainer container;
    public Path configDir;
    public ConfigManager configManager;

    @Inject
    Lelmenus(final PluginContainer container, @ConfigDir(sharedRoot = false) Path configDir, final Logger logger) {
        instance = this;
        this.logger = logger;
        this.container = container;
        this.configDir = configDir;
    }

    @Listener
    public void onConstruct(ConstructPluginEvent event) {
        // 初始化
        configManager = new ConfigManager();
        MenuLoader.setConfigManager(configManager);
    }

    @Listener
    public void onRegisterCommands(RegisterCommandEvent<Command.Parameterized> event) {
        new MenusCommand(event);
    }

    @Listener
    public void onServerStart(StartedEngineEvent<?> event) {
        // 加载所有配置
        configManager.loadMenus();
        PlaceholderProvider.registerExpansion();
    }

}
