package cn.lelmc.lelmenu.menus;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.plugin.PluginContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ConfigManager {

    private final Path configDir;
    private final PluginContainer plugin;
    public final Map<String, MenuConfig> menus = new HashMap<>();
    private ObjectMapper<MenuConfig> mapper;

    public ConfigManager(PluginContainer plugin) {
        this.plugin = plugin;
        this.configDir = Paths.get("config", plugin.metadata().id());

        try {
            // 创建ObjectMapper实例
            this.mapper = ObjectMapper.factory().get(MenuConfig.class);
        } catch (ConfigurateException e) {
            plugin.logger().error("无法创建对象映射器", e);
        }
    }

    public void createDefaultConfigsIfNotExists() {
        Path configFile = configDir.resolve("main.conf");
        if (!Files.exists(configFile)) {
            createDefaultConfigs();
            plugin.logger().info("创建默认配置文件: main.conf");
        }
    }

    // 加载所有菜单配置
    public void loadMenus() {
        try {
            // 创建配置目录
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            // 加载所有 .conf 文件
            try (Stream<Path> pathStream = Files.list(configDir)) {
                pathStream
                        .filter(path -> path.toString().endsWith(".conf"))
                        .forEach(this::loadMenu);
            } catch (IOException e) {
                plugin.logger().error("无法读取配置目录", e);
            }

        } catch (IOException e) {
            plugin.logger().error("无法读取配置目录", e);
        }
    }

    // 加载单个菜单配置
    private void loadMenu(Path configFile) {
        try {
            HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                    .path(configFile)
                    .build();

            CommentedConfigurationNode node = loader.load();

            String menuName = configFile.getFileName().toString().replace(".conf", "");

            // 使用对象映射器加载配置
            MenuConfig menuConfig = mapper.load(node);

            menus.put(menuName, menuConfig);
        } catch (ConfigurateException e) {
            plugin.logger().error("无法加载配置文件: {}", configFile, e);
        }
    }

    // 保存菜单配置
    public void saveMenu(String menuName, MenuConfig menuConfig) {
        try {
            Path configFile = configDir.resolve(menuName + ".conf");

            HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                    .path(configFile)
                    .build();

            CommentedConfigurationNode node = loader.createNode();

            // 使用对象映射器保存配置
            mapper.save(menuConfig, node);

            loader.save(node);

            menus.put(menuName, menuConfig);

        } catch (ConfigurateException e) {
            plugin.logger().error("无法保存配置文件: {}", menuName, e);
        }
    }

    // 创建默认配置
    public void createDefaultConfigs() {
        // 创建主菜单配置
        MenuConfig mainMenu = new MenuConfig();
        mainMenu.setMenuTitle("&c&l菜单&7-> &d箱子菜单");
        mainMenu.setOpenCommand("cd");
        mainMenu.setRows(6);

        Map<String, MenuConfig.MenuItemConfig> items = new HashMap<>();

        MenuConfig.MenuItemConfig spawnItem = getMenuItemConfig();
        items.put("10", spawnItem);

        MenuConfig.MenuItemConfig backgroundItem = new MenuConfig.MenuItemConfig();
        backgroundItem.setMaterial("white_stained_glass_pane");
        backgroundItem.setDisplayName("&7欢迎使用");
        backgroundItem.setCount(1);
        backgroundItem.setSlots(Arrays.asList(
                1, 2, 3, 4, 5, 6, 7, 9, 11, 12, 13, 14, 15, 16, 18, 19, 20, 21, 22, 23, 24, 25
        ));
        backgroundItem.setPriority(0);
        items.put("all", backgroundItem);


        mainMenu.setItems(items);
        saveMenu("main", mainMenu);
    }

    private static MenuConfig.MenuItemConfig getMenuItemConfig() {
        MenuConfig.MenuItemConfig spawnItem = new MenuConfig.MenuItemConfig();
        spawnItem.setMaterial("beacon");
        spawnItem.setCount(10);
        spawnItem.setDisplayName("&ctest");
        spawnItem.setLore(Arrays.asList(
                "&9这里添加描述信息",
                "&7可以多行"
        ));
        spawnItem.setSlot(10);
        spawnItem.setHideEnchantments(true);
        spawnItem.setEnchantments(List.of("minecraft:binding_curse;1"));
        spawnItem.setLeftClickCommands(Arrays.asList(
                "[close]",
                "[player] warp zc"
        ));
        return spawnItem;
    }

    // 获取菜单配置
    public MenuConfig getMenu(String menuName) {
        return menus.get(menuName);
    }

    // 重新加载所有配置
    public void reload() {
        menus.clear();
        loadMenus();
    }
}