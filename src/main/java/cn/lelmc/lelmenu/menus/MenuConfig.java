package cn.lelmc.lelmenu.menus;

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
        private Map<String, Integer> nbtInt = new HashMap<>();

        @Setting("nbt_string")
        private Map<String, String> nbtString = new HashMap<>();

        @Setting("nbt_double")
        private Map<String, Double> nbtDouble = new HashMap<>();

        @Setting("nbt_float")
        private Map<String, Float> nbtFloat = new HashMap<>();

        @Setting("nbt_form")
        private ConfigurationNode nbtForm;  // 直接使用 ConfigurationNode


        @Setting
        private boolean update = false;

        @Setting("left_click_commands")
        private List<String> leftClickCommands = new ArrayList<>();

        @Setting("right_click_commands")
        private List<String> rightClickCommands = new ArrayList<>();

        @Setting
        private Map<String, Map<String, String>> requirements = new HashMap<>();

        @Setting
        private int priority = 0;

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
            return nbtInt;
        }

        public void setNbtInt(Map<String, Integer> nbtInt) {
            this.nbtInt = nbtInt;
        }

        public Map<String, String> getNbtString() {
            return nbtString;
        }

        public void setNbtString(Map<String, String> nbtString) {
            this.nbtString = nbtString;
        }

        public Map<String, Double> getNbtDouble() {
            return nbtDouble;
        }

        public void setNbtDouble(Map<String, Double> nbtDouble) {
            this.nbtDouble = nbtDouble;
        }

        public Map<String, Float> getNbtFloat() {
            return nbtFloat;
        }

        public void setNbtFloat(Map<String, Float> nbtFloat) {
            this.nbtFloat = nbtFloat;
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