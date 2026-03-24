package cn.lelmc.lelmenu.config;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigSerializable
public class MenuConfig {

    @Setting
    private Map<String, MenuItemConfig> items = new HashMap<>();

    @Setting
    private int rows = 3;

    @Setting("open_command")
    private String openCommand = "menu";

    @Setting("update_interval")
    private int updateInterval = 0;

    @Setting("menu_title")
    private String menuTitle = "&c&l菜单";

    @Setting
    private Map<String, Map<String, String>> requirements = new HashMap<>();

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public String getOpenCommand() {
        return openCommand;
    }

    public void setOpenCommand(String openCommand) {
        this.openCommand = openCommand;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
    }

    public String getMenuTitle() {
        return menuTitle;
    }

    public void setMenuTitle(String menuTitle) {
        this.menuTitle = menuTitle;
    }

    public Map<String, MenuItemConfig> getItems() {
        return items;
    }

    public void setItems(Map<String, MenuItemConfig> items) {
        this.items = items;
    }

    public int getSize() {
        return rows * 9;
    }

    public Map<String, Map<String, String>> getRequirements() {
        return requirements;
    }

    public void setRequirements(Map<String, Map<String, String>> requirements) {
        this.requirements = requirements;
    }

    @ConfigSerializable
    public static class MenuItemConfig {

        @Setting
        private String material = "minecraft:stone";

        @Setting("display_name")
        private String displayName = "";

        @Setting
        private List<String> lore = new ArrayList<>();

        @Setting
        private int count = 1;

        @Setting
        private int slot = -1;

        @Setting
        private List<Integer> slots = new ArrayList<>();

        @Setting("hide_enchantments")
        private boolean hideEnchantments = false;

        @Setting
        private List<String> enchantments = new ArrayList<>();

        @Setting("nbt_int")
        private ConfigurationNode nbtInt;

        @Setting("nbt_string")
        private ConfigurationNode nbtString;

        @Setting("nbt_double")
        private ConfigurationNode nbtDouble;

        @Setting("nbt_float")
        private ConfigurationNode nbtFloat;

        @Setting("nbt_form")
        private ConfigurationNode nbtForm;


        @Setting
        private boolean update = false;

        @Setting("left_click_commands")
        private List<String> leftClickCommands = new ArrayList<>();

        @Setting("shift_left_click_commands")
        private List<String> shiftLeftClickCommands = new ArrayList<>();

        @Setting("right_click_commands")
        private List<String> rightClickCommands = new ArrayList<>();

        @Setting("shift_right_click_commands")
        private List<String> shiftRightClickCommands = new ArrayList<>();

        @Setting("middle_click_requirement")
        private List<String> middleClickCommands = new ArrayList<>();

        @Setting
        private Map<String, Map<String, String>> requirements = new HashMap<>();

        @Setting
        private int priority = 0;

        // 临时存储解析后的 NBT 数据
        private final transient Map<String, Integer> parsedNbtInt = new HashMap<>();
        private final transient Map<String, String> parsedNbtString = new HashMap<>();
        private final transient Map<String, Double> parsedNbtDouble = new HashMap<>();
        private final transient Map<String, Float> parsedNbtFloat = new HashMap<>();

        // 解析 NBT 数据的方法
        public void parseNbtData() {
            parseNbtInt();
            parseNbtString();
            parseNbtDouble();
            parseNbtFloat();
        }

        private void parseNbtInt() {
            parsedNbtInt.clear();
            if (nbtInt == null || nbtInt.virtual()) return;

            if (nbtInt.isMap()) {
                for (Map.Entry<Object, ? extends ConfigurationNode> entry : nbtInt.childrenMap().entrySet()) {
                    String key = entry.getKey().toString();
                    int value = entry.getValue().getInt();
                    parsedNbtInt.put(key, value);
                }
            } else {
                ConfigurationNode parent = nbtInt.parent();
                if (parent != null) {
                    for (Map.Entry<Object, ? extends ConfigurationNode> entry : parent.childrenMap().entrySet()) {
                        String key = entry.getKey().toString();
                        if (!key.equals("nbt_int") && entry.getValue() == nbtInt) {
                            parsedNbtInt.put(key, nbtInt.getInt());
                            break;
                        }
                    }
                }
            }
        }

        private void parseNbtString() {
            parsedNbtString.clear();
            if (nbtString == null || nbtString.virtual()) return;

            if (nbtString.isMap()) {
                // 多值格式
                for (Map.Entry<Object, ? extends ConfigurationNode> entry : nbtString.childrenMap().entrySet()) {
                    String key = entry.getKey().toString();
                    String value = entry.getValue().getString("");
                    parsedNbtString.put(key, value);
                }
            } else {
                ConfigurationNode parent = nbtString.parent();
                if (parent != null) {
                    for (Map.Entry<Object, ? extends ConfigurationNode> entry : parent.childrenMap().entrySet()) {
                        String key = entry.getKey().toString();
                        if (!key.equals("nbt_string") && entry.getValue() == nbtString) {
                            parsedNbtString.put(key, nbtString.getString(""));
                            break;
                        }
                    }
                }
            }
        }

        private void parseNbtDouble() {
            parsedNbtDouble.clear();
            if (nbtDouble == null || nbtDouble.virtual()) return;

            if (nbtDouble.isMap()) {
                // 多值格式
                for (Map.Entry<Object, ? extends ConfigurationNode> entry : nbtDouble.childrenMap().entrySet()) {
                    String key = entry.getKey().toString();
                    double value = entry.getValue().getDouble();
                    parsedNbtDouble.put(key, value);
                }
            } else {
                // 单值格式
                ConfigurationNode parent = nbtDouble.parent();
                if (parent != null) {
                    for (Map.Entry<Object, ? extends ConfigurationNode> entry : parent.childrenMap().entrySet()) {
                        String key = entry.getKey().toString();
                        if (!key.equals("nbt_double") && entry.getValue() == nbtDouble) {
                            parsedNbtDouble.put(key, nbtDouble.getDouble());
                            break;
                        }
                    }
                }
            }
        }

        private void parseNbtFloat() {
            parsedNbtFloat.clear();
            if (nbtFloat == null || nbtFloat.virtual()) return;

            if (nbtFloat.isMap()) {
                // 多值格式
                for (Map.Entry<Object, ? extends ConfigurationNode> entry : nbtFloat.childrenMap().entrySet()) {
                    String key = entry.getKey().toString();
                    float value = (float) entry.getValue().getDouble();
                    parsedNbtFloat.put(key, value);
                }
            } else {
                // 单值格式
                ConfigurationNode parent = nbtFloat.parent();
                if (parent != null) {
                    for (Map.Entry<Object, ? extends ConfigurationNode> entry : parent.childrenMap().entrySet()) {
                        String key = entry.getKey().toString();
                        if (!key.equals("nbt_float") && entry.getValue() == nbtFloat) {
                            parsedNbtFloat.put(key, (float) nbtFloat.getDouble());
                            break;
                        }
                    }
                }
            }
        }

        public String getMaterial() {
            return material;
        }

        public void setMaterial(String material) {
            this.material = material;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public List<String> getLore() {
            return lore;
        }

        public void setLore(List<String> lore) {
            this.lore = lore;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getSlot() {
            return slot;
        }

        public void setSlot(int slot) {
            this.slot = slot;
        }

        public List<Integer> getSlots() {
            return slots;
        }

        public void setSlots(List<Integer> slots) {
            this.slots = slots;
        }

        public boolean isHideEnchantments() {
            return hideEnchantments;
        }

        public void setHideEnchantments(boolean hideEnchantments) {
            this.hideEnchantments = hideEnchantments;
        }

        public List<String> getEnchantments() {
            return enchantments;
        }

        public void setEnchantments(List<String> enchantments) {
            this.enchantments = enchantments;
        }

        public Map<String, Integer> getNbtInt() {
            return parsedNbtInt;
        }

        public Map<String, String> getNbtString() {
            return parsedNbtString;
        }

        public Map<String, Double> getNbtDouble() {
            return parsedNbtDouble;
        }

        public Map<String, Float> getNbtFloat() {
            return parsedNbtFloat;
        }

        public void setNbtInt(ConfigurationNode nbtInt) {
            this.nbtInt = nbtInt;
        }

        public ConfigurationNode getNbtForm() {
            return nbtForm;
        }

        public void setNbtForm(ConfigurationNode nbtForm) {
            this.nbtForm = nbtForm;
        }

        public boolean isUpdate() {
            return update;
        }

        public void setUpdate(boolean update) {
            this.update = update;
        }

        public List<String> getLeftClickCommands() {
            return leftClickCommands;
        }

        public void setLeftClickCommands(List<String> leftClickCommands) {
            this.leftClickCommands = leftClickCommands;
        }

        public List<String> getRightClickCommands() {
            return rightClickCommands;
        }

        public void setRightClickCommands(List<String> rightClickCommands) {
            this.rightClickCommands = rightClickCommands;
        }

        public List<String> getShiftLeftClickCommands() {
            return shiftLeftClickCommands;
        }

        public void setShiftLeftClickCommands(List<String> shiftLeftClickCommands) {
            this.shiftLeftClickCommands = shiftLeftClickCommands;
        }

        public List<String> getShiftRightClickCommands() {
            return shiftRightClickCommands;
        }

        public void setShiftRightClickCommands(List<String> shiftRightClickCommands) {
            this.shiftRightClickCommands = shiftRightClickCommands;
        }

        public List<String> getMiddleClickCommands() {
            return middleClickCommands;
        }

        public void setMiddleClickCommands(List<String> middleClickCommands) {
            this.middleClickCommands = middleClickCommands;
        }

        public Map<String, Map<String, String>> getRequirements() {
            return requirements;
        }

        public void setRequirements(Map<String, Map<String, String>> requirements) {
            this.requirements = requirements;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }
    }
}