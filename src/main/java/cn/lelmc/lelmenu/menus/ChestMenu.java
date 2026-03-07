package cn.lelmc.lelmenu.menus;

import cn.lelmc.lelmenu.Lelmenus;
import cn.lelmc.lelmenu.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.item.inventory.*;
import org.spongepowered.api.item.inventory.menu.ClickType;
import org.spongepowered.api.item.inventory.menu.InventoryMenu;
import org.spongepowered.api.item.inventory.menu.handler.SlotClickHandler;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.plugin.PluginContainer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ChestMenu {
    private final int rows;
    private final Component title;
    private final Map<Integer, ItemStack> items = new HashMap<>();
    private final Map<Integer, Consumer<ClickType<?>>> clickActions = new HashMap<>();
    private final Map<Integer, MenuConfig.MenuItemConfig> updateItems = new HashMap<>();
    private String menuName;
    private int updateInterval = 0;
    private ScheduledTask updateTask;
    private ViewableInventory inventory;

    public ChestMenu(String title, int rows) {
        this.title = ColorUtils.toComponent(title);
        this.rows = rows;
    }

    public void refreshMenu(ServerPlayer player) {
        ChestMenu newMenu = MenuLoader.loadMenu(menuName, player);
        if (newMenu == null) {
            return;
        }
        newMenu.items.forEach((slot, itemStack) -> inventory
                .slot(slot).ifPresent(slotObj -> slotObj.set(itemStack)));
    }

    public void putUpdateItem(int slot, MenuConfig.MenuItemConfig item) {
        updateItems.put(slot, item);
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public void setUpdateInterval(int seconds) {
        this.updateInterval = seconds;
    }

    public void setItem(int slot, ItemStack item) {
        items.put(slot, item);
    }

    public void setAction(int slot, Consumer<ClickType<?>> action) {
        clickActions.put(slot, action);
    }

    public void open(ServerPlayer player) {
        try {
            ContainerType containerType = getContainerTypeForRows(rows);
            this.inventory = ViewableInventory.builder()
                    .type(containerType)
                    .completeStructure()
                    .carrier(player)
                    .plugin(Lelmenus.instance.container)
                    .build();

            InventoryMenu menu = this.inventory.asMenu();
            menu.setReadOnly(true);
            menu.setTitle(title);

            // 填充物品
            this.items.forEach((slot, itemStack) ->
                    this.inventory.slot(slot).ifPresent(slotObj ->
                            slotObj.set(itemStack)));
            // 注册点击处理器
            menu.registerSlotClick(new MenuClickHandler(Lelmenus.instance.container, menu, this.inventory, player, clickActions));
            // 打开菜单
            menu.open(player);

            menu.registerClose((cause, container) -> updateTask.cancel());
            // 设置自动更新任务
            if (updateInterval > 0 && !updateItems.isEmpty()) {
                startUpdateTask(player);
            }

        } catch (Exception e) {
            Lelmenus.instance.logger.error("无法打开菜单: {}", menuName, e);
        }
    }

    private void startUpdateTask(ServerPlayer player) {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        updateTask = Sponge.server().scheduler().executor(Lelmenus.instance.container)
                .scheduleAtFixedRate(() -> updateItems
                        .forEach((slot, item) -> inventory
                                .slot(slot)
                                .ifPresent(slotObj -> {
                                    ItemStack peek = MenuLoader.updateItemDisplay(item, player, slotObj.peek());
                                    updateSlotWithMarkDirty(slot, peek);
                                })), updateInterval, updateInterval, TimeUnit.SECONDS).task();
    }

    private void updateSlotWithMarkDirty(int slot, ItemStack newStack) {
        this.inventory.slot(slot).ifPresent(slotObj -> {
            try {
                slotObj.set(newStack);
                Object fabricSlot = slotObj.getClass().getMethod("inventoryAdapter$getFabric").invoke(slotObj);
                fabricSlot.getClass().getMethod("fabric$markDirty").invoke(fabricSlot);
            } catch (Exception e) {
                Lelmenus.instance.logger.error("无法调用 markDirty", e);
            }
        });
    }

    private ContainerType getContainerTypeForRows(int rows) {
        return switch (rows) {
            case 1 -> ContainerTypes.GENERIC_9X1.get();
            case 2 -> ContainerTypes.GENERIC_9X2.get();
            case 4 -> ContainerTypes.GENERIC_9X4.get();
            case 5 -> ContainerTypes.GENERIC_9X5.get();
            case 6 -> ContainerTypes.GENERIC_9X6.get();
            default -> ContainerTypes.GENERIC_9X3.get();
        };
    }

    // 内部点击处理器类
    private record MenuClickHandler(PluginContainer plugin,
                                    InventoryMenu menu,
                                    ViewableInventory primary,
                                    ServerPlayer player,
                                    Map<Integer, Consumer<ClickType<?>>> clickActions)
            implements SlotClickHandler {

        @Override
        public boolean handle(Cause cause, Container container, Slot slot,
                              int slotIndex, ClickType<?> clickType) {
            // 只处理主菜单的点击
            if (slot.viewedSlot().parent() == this.primary) {
                Consumer<ClickType<?>> action = clickActions.get(slotIndex);
                if (action != null) {
                    action.accept(clickType);
                }
                return false;
            }
            return true;
        }
    }
}