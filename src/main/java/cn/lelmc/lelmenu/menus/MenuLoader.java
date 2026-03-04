package cn.lelmc.lelmenu.menus;

import cn.lelmc.lelmenu.Lelmenus;
import cn.lelmc.lelmenu.utils.ColorUtils;
import cn.lelmc.lelmenu.utils.Placeholder;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.menu.ClickTypes;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.*;

public class MenuLoader {

    public static ConfigManager configManager;

    public static void setConfigManager(ConfigManager configManager) {
        MenuLoader.configManager = configManager;
    }

    public static ChestMenu loadMenu(String menuName, ServerPlayer player) {
        MenuConfig config = configManager.getMenu(menuName);
        if (config == null) {
            return null;
        }
        ChestMenu menu = new ChestMenu(config.getMenuTitle(), config.getRows());
        menu.setMenuName(menuName);
        menu.setUpdateInterval(config.getUpdateInterval());

        // 按优先级排序物品配置
        List<MenuConfig.MenuItemConfig> sortedItems = config.getItems().values().stream()
                .sorted(Comparator.comparingInt(MenuConfig.MenuItemConfig::getPriority).reversed())
                .toList();

        // 创建一个映射来跟踪每个槽位已设置的物品（按优先级）
        Map<Integer, MenuConfig.MenuItemConfig> slotItems = new HashMap<>();

        // 先处理所有物品，确定每个槽位最终显示哪个（按最高优先级）
        for (MenuConfig.MenuItemConfig itemConfig : sortedItems) {
            if (!checkViewRequirement(itemConfig, player)) {
                continue;
            }

            // 处理单个槽位
            if (itemConfig.getSlot() >= 0) {
                int slot = itemConfig.getSlot();
                if (slot < config.getSize()) {
                    if (!slotItems.containsKey(slot) || itemConfig.getPriority() > slotItems.get(slot).getPriority()) {
                        slotItems.put(slot, itemConfig);
                    }
                }
            }

            // 处理多个槽位
            if (!itemConfig.getSlots().isEmpty()) {
                for (int slot : itemConfig.getSlots()) {
                    if (slot < config.getSize()) {
                        if (!slotItems.containsKey(slot) || itemConfig.getPriority() > slotItems.get(slot).getPriority()) {
                            slotItems.put(slot, itemConfig);
                        }
                    }
                }
            }
        }

        // 为每个槽位创建菜单项并设置点击事件
        for (Map.Entry<Integer, MenuConfig.MenuItemConfig> entry : slotItems.entrySet()) {
            int slot = entry.getKey();
            MenuConfig.MenuItemConfig itemConfig = entry.getValue();
            if (itemConfig.isUpdate()) {
                menu.putUpdateItem(slot, itemConfig);
            }
            ItemStack itemStack = createItemStack(itemConfig, player);
            if (!itemStack.isEmpty()) {
                menu.setItem(slot, itemStack);
                // 设置点击事件
                menu.setAction(slot, clickType -> {
                    if (clickType.equals(ClickTypes.SHIFT_CLICK_LEFT.get())) {
                        handleCommands(itemConfig.getShiftLeftClickCommands(), player);
                    } else if (clickType.equals(ClickTypes.SHIFT_CLICK_RIGHT.get())) {
                        handleCommands(itemConfig.getShiftRightClickCommands(), player);
                    } else if (clickType.equals(ClickTypes.CLICK_LEFT.get())) {
                        handleCommands(itemConfig.getLeftClickCommands(), player);
                    } else if (clickType.equals(ClickTypes.CLICK_RIGHT.get())) {
                        handleCommands(itemConfig.getRightClickCommands(), player);
                    } else if (clickType.equals(ClickTypes.CLICK_MIDDLE.get())) {
                        handleCommands(itemConfig.getMiddleClickCommands(), player);
                    }
                });
            }
        }
        return menu;
    }

    // 创建物品堆
    public static ItemStack createItemStack(MenuConfig.MenuItemConfig config, ServerPlayer player) {
        String materialStr = config.getMaterial();
        if (!materialStr.contains(":")) {
            materialStr = "minecraft:" + materialStr;
        }

        ResourceKey itemKey = ResourceKey.resolve(materialStr.toLowerCase());
        ItemType itemType = RegistryTypes.ITEM_TYPE.get()
                .findValue(itemKey)
                .orElse(ItemTypes.STONE.get());

        DataContainer container = ItemStack.of(itemType).toContainer();

        // 创建 components 视图
        DataQuery dataQuery = DataQuery.of("components");
        DataView dataView = container.createView(dataQuery);
        // 处理简单的 nbtString
        if (config.getNbtString() != null) {
            for (Map.Entry<String, String> entry : config.getNbtString().entrySet()) {
                String value = Placeholder.parseString(entry.getValue(), player);
                dataView.set(DataQuery.of(entry.getKey()), value);
            }
        }

        // 处理 nbtInt
        for (Map.Entry<String, Integer> entry : config.getNbtInt().entrySet()) {
            dataView.set(DataQuery.of(entry.getKey()), entry.getValue());
        }

        // 处理 nbtDouble
        for (Map.Entry<String, Double> entry : config.getNbtDouble().entrySet()) {
            dataView.set(DataQuery.of(entry.getKey()), entry.getValue());
        }

        // 处理 nbtFloat
        for (Map.Entry<String, Float> entry : config.getNbtFloat().entrySet()) {
            dataView.set(DataQuery.of(entry.getKey()), entry.getValue());
        }

        if (config.getNbtForm() != null) {
            ConfigurationNode nbtFormNode = config.getNbtForm();
            // 检查是否有有效数据
            if (!nbtFormNode.virtual() && !nbtFormNode.childrenMap().isEmpty()) {
                for (Map.Entry<Object, ? extends ConfigurationNode> entry : nbtFormNode.childrenMap().entrySet()) {
                    String componentKey = entry.getKey().toString();
                    ConfigurationNode componentNode = entry.getValue();

                    // 创建组件子视图
                    DataView componentView = dataView.createView(DataQuery.of(componentKey));

                    // 遍历组件内的所有属性
                    for (Map.Entry<Object, ? extends ConfigurationNode> propEntry : componentNode.childrenMap().entrySet()) {
                        String propertyKey = propEntry.getKey().toString();
                        ConfigurationNode propertyNode = propEntry.getValue();

                        // 根据节点类型获取值
                        if (propertyNode.isList()) {
                            List<String> list = new ArrayList<>();
                            for (ConfigurationNode item : propertyNode.childrenList()) {
                                String s = Placeholder.parseString(item.getString(""), player);
                                list.add(s);
                            }
                            componentView.set(DataQuery.of(propertyKey), list);
                        } else {
                            Object value = propertyNode.raw();
                            if (value != null) {
                                componentView.set(DataQuery.of(propertyKey), value);
                            }
                        }
                    }
                }
            }
        }

        ItemStack stack = ItemStack.builder()
                .fromContainer(container)
                .quantity(config.getCount())
                .build();

        // 解析显示名称
        String displayName = Placeholder.parseString(config.getDisplayName(), player);
        Component displayComponent = ColorUtils.toComponent(displayName);
        stack.offer(Keys.CUSTOM_NAME, displayComponent);

        // 解析lore
        List<String> loreList = new ArrayList<>();
        for (String line : config.getLore()) {
            loreList.add(Placeholder.parseString(line, player));
        }
        String[] loreArray = loreList.toArray(new String[0]);
        if (loreArray.length > 0) {
            List<Component> loreComponents = ColorUtils.toComponentList(Arrays.asList(loreArray));
            stack.offer(Keys.LORE, loreComponents);
        }

        // 处理附魔
        if (!config.getEnchantments().isEmpty()) {
            stack = applyEnchantmentsToItem(stack, config.getEnchantments(), config.isHideEnchantments());
        }

        return stack;
    }

    /**
     * 应用附魔到物品
     */
    private static ItemStack applyEnchantmentsToItem(ItemStack itemStack, List<String> enchantmentList, boolean hideEnchantments) {
        List<Enchantment> enchantments = new ArrayList<>();

        for (String enchantStr : enchantmentList) {
            String[] parts = enchantStr.split(";");
            if (parts.length == 2) {
                try {
                    String enchantName = parts[0].toLowerCase();
                    if (!enchantName.contains(":")) {
                        enchantName = "minecraft:" + enchantName;
                    }

                    ResourceKey enchantKey = ResourceKey.resolve(enchantName);
                    Optional<EnchantmentType> enchantType = RegistryTypes.ENCHANTMENT_TYPE.get()
                            .findValue(enchantKey);

                    if (enchantType.isPresent()) {
                        int level = Integer.parseInt(parts[1]);
                        Enchantment enchant = Enchantment.of(enchantType.get(), level);
                        enchantments.add(enchant);
                    }
                } catch (Exception e) {
                    // 忽略无效附魔
                }
            }
        }

        ItemStack.Builder builder = ItemStack.builder().from(itemStack);

        if (!enchantments.isEmpty()) {
            builder.add(Keys.APPLIED_ENCHANTMENTS, enchantments);
        }

        if (hideEnchantments) {
            builder.add(Keys.HIDE_ENCHANTMENTS, true);
        }

        return builder.build();
    }

    // 检查视图要求
    private static boolean checkViewRequirement(MenuConfig.MenuItemConfig config, ServerPlayer player) {
        Map<String, Map<String, String>> requirements = config.getRequirements();
        // 如果没有配置条件，直接显示
        if (requirements == null || requirements.isEmpty()) {
            return true;
        }

        for (Map.Entry<String, Map<String, String>> entry : requirements.entrySet()) {
            String conditionName = entry.getKey();
            Map<String, String> conditionData = entry.getValue();

            String type = conditionData.get("type");
            String input = conditionData.get("input");
            String output = conditionData.get("output");

            // 解析占位符
            String inputStr = Placeholder.parseString(input, player);
            String outputStr = Placeholder.parseString(output, player);

            // 根据条件类型进行判断
            switch (type) {
                // 字符串相等判断
                case "equals":
                    if (!inputStr.equals(outputStr)) {
                        return false;
                    }
                    break;
                case ">":
                    try {
                        double inputNum = Double.parseDouble(inputStr);
                        double outputNum = Double.parseDouble(outputStr);
                        if (!(inputNum > outputNum)) {
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        return false;
                    }
                    break;
                case ">=":
                    try {
                        double inputNum = Double.parseDouble(inputStr);
                        double outputNum = Double.parseDouble(outputStr);
                        if (!(inputNum >= outputNum)) {
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        return false;
                    }
                    break;
                case "<":
                    try {
                        double inputNum = Double.parseDouble(inputStr);
                        double outputNum = Double.parseDouble(outputStr);
                        if (!(inputNum < outputNum)) {
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        return false;
                    }
                    break;
                case "<=":
                    try {
                        double inputNum = Double.parseDouble(inputStr);
                        double outputNum = Double.parseDouble(outputStr);
                        if (!(inputNum <= outputNum)) {
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        return false;
                    }
                    break;
                case "==":
                    try {
                        double inputNum = Double.parseDouble(inputStr);
                        double outputNum = Double.parseDouble(outputStr);
                        if (inputNum != outputNum) {
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        return false;
                    }
                    break;

                // 字符串包含判断
                case "contains":
                    if (!inputStr.contains(outputStr)) {
                        return false;
                    }
                    break;

                // 字符串开头判断
                case "starts with":
                    if (!inputStr.startsWith(outputStr)) {
                        return false;
                    }
                    break;

                // 字符串结尾判断
                case "ends with":
                    if (!inputStr.endsWith(outputStr)) {
                        return false;
                    }
                    break;

                default:
                    Lelmenus.instance.logger.warn("未知的条件类型 [{}]: {}", conditionName, type);
                    break;
            }
        }
        return true;
    }

    public static Map<String, String> getRegisteredCommands() {
        Map<String, String> commands = new HashMap<>();
        for (Map.Entry<String, MenuConfig> entry : configManager.menus.entrySet()) {
            String menuName = entry.getKey();
            MenuConfig config = entry.getValue();
            if (config.getOpenCommand() != null && !config.getOpenCommand().isEmpty()) {
                commands.put(config.getOpenCommand(), menuName);
            }
        }
        return commands;
    }


    // 处理命令
    private static void handleCommands(List<String> commands, ServerPlayer player) {
        for (String command : commands) {
            if (command.startsWith("[refresh]")) {
                ChestMenu openMenu = ChestMenu.getOpenMenu(player);
                openMenu.refreshMenu(player);
            } else if (command.startsWith("[close]")) {
                ChestMenu.closeMenu(player);
            } else if (command.startsWith("[player]")) {
                String cmd = command.substring(8).trim();
                cmd = Placeholder.parseString(cmd, player);
                executeCommand(player, cmd);
            } else if (command.startsWith("[console]")) {
                String cmd = command.substring(9).trim();
                cmd = Placeholder.parseString(cmd, player);
                executeConsoleCommand(cmd);
            } else if (command.startsWith("[message]")) {
                String msg = command.substring(9).trim();
                player.sendMessage(Component.text(Placeholder.parseString(msg, player)));
            }
        }
    }

    private static void executeCommand(ServerPlayer player, String command) {
        try {
            Sponge.server().commandManager().process(player, command);
        } catch (CommandException e) {
            // 忽略命令异常
        }
    }

    private static void executeConsoleCommand(String command) {
        try {
            Sponge.server().commandManager().process(command);
        } catch (CommandException e) {
            // 忽略命令异常
        }
    }
}