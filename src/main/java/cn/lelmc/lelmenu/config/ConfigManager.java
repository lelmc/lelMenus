package cn.lelmc.lelmenu.config;

import cn.lelmc.lelmenu.Lelmenus;
import org.apache.logging.log4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ConfigManager {

    public final Map<String, MenuConfig> menus = new HashMap<>();
    Logger logger = Lelmenus.instance.logger;
    Path configDir = Lelmenus.instance.configDir;
    private ObjectMapper<MenuConfig> mapper;

    public ConfigManager() {
        try {
            this.mapper = ObjectMapper.factory().get(MenuConfig.class);
        } catch (ConfigurateException e) {
            logger.error("Unable to create object mapper", e);
        }
    }

    private static MenuConfig.MenuItemConfig getMenuItemConfig() {
        MenuConfig.MenuItemConfig spawnItem = new MenuConfig.MenuItemConfig();
        spawnItem.setMaterial("beacon");
        spawnItem.setCount(10);
        spawnItem.setDisplayName("&ctest");
        spawnItem.setLore(Arrays.asList(
                "&9Add lore here",
                "&7Can be multiple lines"
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

    public void createDefaultConfigsIfNotExists() {
        Path configFile = configDir.resolve("main.conf");
        if (!Files.exists(configFile)) {
            createDefaultConfigs();
            logger.info("Create default menu file: main.conf");
        }
    }

    // 加载所有菜单配置
    public void loadMenus() {
        // 加载所有 .conf 文件
        try (Stream<Path> pathStream = Files.list(configDir)) {
            pathStream
                    .filter(path -> path.toString().endsWith(".conf"))
                    .forEach(this::loadMenu);
        } catch (IOException e) {
            logger.error("Unable to read menu directory", e);
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
            for (MenuConfig.MenuItemConfig item : menuConfig.getItems().values()) {
                item.parseNbtData();
            }

            menus.put(menuName, menuConfig);
        } catch (ConfigurateException e) {
            logger.error("Unable to load menu file: {}", configFile, e);
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
            logger.error("Unable to save the menu file: {}", menuName, e);
        }
    }

    // 创建默认配置
    public void createDefaultConfigs() {
        // 创建主菜单配置
        MenuConfig mainMenu = new MenuConfig();
        mainMenu.setMenuTitle("&c&lHello&7-> <player_name> &dThis is the default title");
        mainMenu.setOpenCommand("cd");
        mainMenu.setRows(6);

        Map<String, MenuConfig.MenuItemConfig> items = new HashMap<>();

        MenuConfig.MenuItemConfig spawnItem = getMenuItemConfig();
        items.put("10", spawnItem);

        MenuConfig.MenuItemConfig backgroundItem = new MenuConfig.MenuItemConfig();
        backgroundItem.setMaterial("white_stained_glass_pane");
        backgroundItem.setDisplayName("&7Welcome");
        backgroundItem.setCount(1);
        backgroundItem.setSlots(Arrays.asList(
                1, 2, 3, 4, 5, 6, 7, 9, 11, 12, 13, 14, 15, 16, 18, 19, 20, 21, 22, 23, 24, 25
        ));
        backgroundItem.setPriority(0);
        items.put("all", backgroundItem);


        mainMenu.setItems(items);
        saveMenu("main", mainMenu);
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