package cn.lelmc.lelmenu.menus;

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
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ChestMenu {

    private static final Map<UUID, ChestMenu> openMenus = new HashMap<>();
    private static PluginContainer plugin;

    private final Component title;
    private final int rows;  // 行数
    private final Map<Integer, ItemStack> items = new HashMap<>();
    private final Map<Integer, Consumer<ClickType<?>>> clickActions = new HashMap<>();
    private int updateInterval = 0;
    private String menuName;
    private ScheduledTask updateTask;

    public ChestMenu(String title, int rows) {
        this.title = ColorUtils.toComponent(title);
        this.rows = rows;
    }

    public static void setPlugin(PluginContainer pluginContainer) {
        plugin = pluginContainer;
    }

    public static void closeMenu(ServerPlayer player) {
        ChestMenu menu = openMenus.remove(player.uniqueId());
        if (menu != null && menu.updateTask != null) {
            menu.updateTask.cancel();
        }
        player.closeInventory();
    }

    public static boolean hasMenuOpen(ServerPlayer player) {
        return openMenus.containsKey(player.uniqueId());
    }

    public static ChestMenu getOpenMenu(ServerPlayer player) {
        return openMenus.get(player.uniqueId());
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
            // 根据行数选择容器类型
            ContainerType containerType = getContainerTypeForRows(rows);

            ViewableInventory inventory = ViewableInventory.builder()
                    .type(containerType)
                    .completeStructure()
                    .carrier(player)
                    .plugin(plugin)
                    .build();

            InventoryMenu menu = inventory.asMenu();
            menu.setReadOnly(true);
            menu.setTitle(title);

            // 填充物品
            items.forEach((slot, itemStack) -> {
                inventory.slot(slot).ifPresent(slotObj -> {
                    slotObj.set(itemStack);
                });
            });

            // 注册点击处理器
            menu.registerSlotClick(new MenuClickHandler(plugin, menu, inventory, player, clickActions));

            // 打开菜单
            menu.open(player);
            openMenus.put(player.uniqueId(), this);

            // 设置自动更新任务
            if (updateInterval > 0) {
                startUpdateTask(player);
            }

        } catch (Exception e) {
            plugin.logger().error("无法打开菜单: {}", menuName, e);
        }
    }

    private void startUpdateTask(ServerPlayer player) {
        updateTask = Sponge.server().scheduler().executor(plugin)
                .scheduleAtFixedRate(() -> {
                    if (player.isOnline() && openMenus.containsKey(player.uniqueId())) {
                        // 重新加载菜单并刷新
                        MenuLoader loader = new MenuLoader(new ConfigManager(plugin));
                        ChestMenu newMenu = loader.loadMenu(menuName, player);
                        if (newMenu != null) {
                            // 刷新当前菜单
                            player.closeInventory();
                            newMenu.open(player);
                        }
                    } else {
                        // 如果玩家不在线或菜单已关闭，停止任务
                        if (updateTask != null) {
                            updateTask.cancel();
                        }
                    }
                }, updateInterval, updateInterval, TimeUnit.SECONDS).task();
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
                return false; // 取消事件，防止物品被移动
            }
            return true; // 允许其他操作
        }
    }
}