package cn.lelmc.lelmenu.menus;

import cn.lelmc.lelmenu.Lelmenus;
import cn.lelmc.lelmenu.config.ConfigManager;
import cn.lelmc.lelmenu.config.MenuConfig;
import cn.lelmc.lelmenu.utils.ColorUtils;
import cn.lelmc.lelmenu.utils.NbtBuilder;
import cn.lelmc.lelmenu.utils.Placeholder;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.menu.ClickTypes;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MenuLoader {

    public static ConfigManager configManager;

    static Pattern DELAY_PATTERN = Pattern.compile("^\\[delay;(\\d+)([tms]*)]");

    public static void setConfigManager(ConfigManager configManager) {
        MenuLoader.configManager = configManager;
    }

    public static ChestMenu loadMenu(String menuName, ServerPlayer player) {
        MenuConfig config = configManager.getMenu(menuName);
        if (config == null) {
            return null;
        }

        if (checkViewRequirement(config.getRequirements(), player)) {
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
            if (checkViewRequirement(itemConfig.getRequirements(), player)) {
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
                        handleCommands(itemConfig.getShiftLeftClickCommands(), player, menu);
                    } else if (clickType.equals(ClickTypes.SHIFT_CLICK_RIGHT.get())) {
                        handleCommands(itemConfig.getShiftRightClickCommands(), player, menu);
                    } else if (clickType.equals(ClickTypes.CLICK_LEFT.get())) {
                        handleCommands(itemConfig.getLeftClickCommands(), player, menu);
                    } else if (clickType.equals(ClickTypes.CLICK_RIGHT.get())) {
                        handleCommands(itemConfig.getRightClickCommands(), player, menu);
                    } else if (clickType.equals(ClickTypes.CLICK_MIDDLE.get())) {
                        handleCommands(itemConfig.getMiddleClickCommands(), player, menu);
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
        DataView view = dataView.createView(DataQuery.of("minecraft:custom_data"));

        // nbtString
        for (Map.Entry<String, String> entry : config.getNbtString().entrySet()) {
            String value = Placeholder.parseString(entry.getValue(), player);
            view.set(DataQuery.of(entry.getKey()), value);
        }

        // nbtInt
        for (Map.Entry<String, Integer> entry : config.getNbtInt().entrySet()) {
            String value = Placeholder.parseString(String.valueOf(entry.getValue()), player);
            view.set(DataQuery.of(entry.getKey()), Integer.valueOf(value));
        }

        // nbtDouble
        for (Map.Entry<String, Double> entry : config.getNbtDouble().entrySet()) {
            String value = Placeholder.parseString(String.valueOf(entry.getValue()), player);
            view.set(DataQuery.of(entry.getKey()), Double.valueOf(value));
        }

        // nbtFloat
        for (Map.Entry<String, Float> entry : config.getNbtFloat().entrySet()) {
            String value = Placeholder.parseString(String.valueOf(entry.getValue()), player);
            view.set(DataQuery.of(entry.getKey()), Float.valueOf(value));
        }

        // 处理 nbtForm
        if (config.getNbtForm() != null) {
            ConfigurationNode nbtFormNode = config.getNbtForm();
            if (!nbtFormNode.virtual() && !nbtFormNode.childrenMap().isEmpty()) {
                for (Map.Entry<Object, ? extends ConfigurationNode> entry : nbtFormNode.childrenMap().entrySet()) {
                    String componentKey = entry.getKey().toString();
                    ConfigurationNode componentNode = entry.getValue();

                    DataView componentView = dataView.createView(DataQuery.of(componentKey));
                    NbtBuilder nbtBuilder = new NbtBuilder();

                    for (Map.Entry<Object, ? extends ConfigurationNode> propEntry : componentNode.childrenMap().entrySet()) {
                        String propertyKey = propEntry.getKey().toString();
                        ConfigurationNode propertyNode = propEntry.getValue();

                        Object rawValue = propertyNode.raw();
                        switch (rawValue) {
                            case null -> {
                            }
                            case String strValue -> {
                                if (strValue.startsWith("<") && strValue.endsWith(">")) {
                                    nbtBuilder.setPlaceholder(propertyKey, strValue, NbtBuilder.ValueType.AUTO);
                                } else {
                                    nbtBuilder.set(propertyKey, strValue);
                                }
                            }
                            case Integer i -> nbtBuilder.set(propertyKey, i);
                            case Double v -> nbtBuilder.set(propertyKey, v);
                            case Boolean b -> nbtBuilder.set(propertyKey, b);
                            case Long l -> nbtBuilder.set(propertyKey, l.intValue());
                            default -> nbtBuilder.set(propertyKey, rawValue.toString());
                        }

                    }
                    nbtBuilder.build(componentView, player);
                }
            }
        }

        ItemStack stack = ItemStack.builder()
                .fromContainer(container)
                .quantity(config.getCount())
                .build();

        ItemStack itemStack = updateItemDisplay(config, player, stack);

        // 处理附魔
        if (!config.getEnchantments().isEmpty()) {
            itemStack = applyEnchantmentsToItem(itemStack, config.getEnchantments(), config.isHideEnchantments());
        }

        return itemStack;
    }

    public static ItemStack updateItemDisplay(MenuConfig.MenuItemConfig config, ServerPlayer player, ItemStack stack) {
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
    private static boolean checkViewRequirement(Map<String, Map<String, String>> requirements, ServerPlayer player) {
        if (requirements == null || requirements.isEmpty()) {
            return false;
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
                        return true;
                    }
                    break;
                case ">":
                    try {
                        double inputNum = Double.parseDouble(inputStr);
                        double outputNum = Double.parseDouble(outputStr);
                        if (!(inputNum > outputNum)) {
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        return true;
                    }
                    break;
                case ">=":
                    try {
                        double inputNum = Double.parseDouble(inputStr);
                        double outputNum = Double.parseDouble(outputStr);
                        if (!(inputNum >= outputNum)) {
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        return true;
                    }
                    break;
                case "<":
                    try {
                        double inputNum = Double.parseDouble(inputStr);
                        double outputNum = Double.parseDouble(outputStr);
                        if (!(inputNum < outputNum)) {
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        return true;
                    }
                    break;
                case "<=":
                    try {
                        double inputNum = Double.parseDouble(inputStr);
                        double outputNum = Double.parseDouble(outputStr);
                        if (!(inputNum <= outputNum)) {
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        return true;
                    }
                    break;
                case "==":
                    try {
                        double inputNum = Double.parseDouble(inputStr);
                        double outputNum = Double.parseDouble(outputStr);
                        if (inputNum != outputNum) {
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        return true;
                    }
                    break;
                case "!=":
                    if (inputStr.equals(outputStr)) {
                        return true;
                    }
                    break;

                // 字符串包含判断
                case "contains":
                    if (!inputStr.contains(outputStr)) {
                        return true;
                    }
                    break;

                // 字符串开头判断
                case "starts with":
                    if (!inputStr.startsWith(outputStr)) {
                        return true;
                    }
                    break;

                // 字符串结尾判断
                case "ends with":
                    if (!inputStr.endsWith(outputStr)) {
                        return true;
                    }
                    break;

                case "permission":
                    boolean b = Boolean.parseBoolean(output);
                    if (player.hasPermission(input) != b) {
                        return true;
                    }
                    break;

                default:
                    Lelmenus.instance.logger.warn("未知的条件类型 [{}]: {}", conditionName, type);
                    break;
            }
        }
        return false;
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
    private static void handleCommands(List<String> commands, ServerPlayer player, ChestMenu menu) {
        if (commands == null || commands.isEmpty()) return;
        processCommands(commands, 0, player, menu);
    }

    private static void processCommands(List<String> commands, int index, ServerPlayer player, ChestMenu menu) {
        if (index >= commands.size()) return;

        String command = commands.get(index);
        Matcher delayMatcher = DELAY_PATTERN.matcher(command);

        if (delayMatcher.matches()) {
            long delayTicks = Long.parseLong(delayMatcher.group(1));
            Sponge.server().scheduler().submit(Task.builder()
                    .execute(() -> processCommands(commands, index + 1, player, menu))
                    .delay(Ticks.of(delayTicks))
                    .plugin(Lelmenus.instance.container)
                    .build());
        } else {
            executeCommand(command, player, menu);
            processCommands(commands, index + 1, player, menu);
        }
    }

    private static void executeCommand(String command, ServerPlayer player, ChestMenu menu) {
        if (command.startsWith("[refresh]")) {
            menu.refreshMenu(player);
        } else if (command.startsWith("[close]")) {
            player.closeInventory();
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

    private static void executeCommand(ServerPlayer player, String command) {
        try (CauseStackManager.StackFrame frame = Sponge.server().causeStackManager().pushCauseFrame()) {
            frame.pushCause(player);
            frame.addContext(EventContextKeys.PLAYER, player);
            frame.addContext(EventContextKeys.SUBJECT, player);
            frame.addContext(EventContextKeys.AUDIENCE, player);
            Sponge.server().commandManager().process(command);
        } catch (CommandException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void executeConsoleCommand(String command) {
        try {
            SystemSubject systemSubject = Sponge.systemSubject();
            Sponge.server().commandManager().process(systemSubject, systemSubject, command);
        } catch (CommandException e) {
            throw new RuntimeException(e);
        }
    }

}