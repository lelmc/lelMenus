package cn.lelmc.lelmenu.commands;

import cn.lelmc.lelmenu.utils.NBTUtils;
import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.plugin.PluginContainer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class NBTCommand {

    private final PluginContainer plugin;

    Parameter.Value<String> formatParam = Parameter.choices("hocon", "json", "compact")
            .key("format")
            .optional()
            .build();

    @Inject
    public NBTCommand(PluginContainer plugin) {
        this.plugin = plugin;
    }

    public Command.Parameterized createCommand() {

        return Command.builder()
                .addParameter(formatParam)
                .permission("lelmenu.nbt")
                .executor(this::execute)
                .build();
    }

    private CommandResult execute(CommandContext context) {
        if (!(context.cause().root() instanceof ServerPlayer player)) {
            context.sendMessage(Component.text("只有玩家可以使用此命令", NamedTextColor.RED));
            return CommandResult.success();
        }
        // 获取手中的物品
        ItemStack itemInHand = player.itemInHand(HandTypes.MAIN_HAND);
        if (itemInHand.isEmpty()) {
            player.sendMessage(Component.text("你手中没有物品！", NamedTextColor.RED));
            return CommandResult.success();
        }
        // 获取输出格式（默认hocon）
        String format = context.one(formatParam.key()).orElse("hocon");
        // 获取物品的NBT信息
        Map<String, Object> nbtMap = NBTUtils.itemStackToNBTMap(itemInHand);

        // 获取物品基本信息
        String itemId = itemInHand.type().key(RegistryTypes.ITEM_TYPE).asString();
        int amount = itemInHand.quantity();

        // 构建输出
        StringBuilder output = new StringBuilder();
        output.append("# 物品信息\n");
        output.append("# 类型: ").append(itemId).append("\n");
        output.append("# 数量: ").append(amount).append("\n");
        output.append("# 生成时间: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\n\n");

        if ("json".equalsIgnoreCase(format)) {
            output.append(NBTUtils.toJsonString(nbtMap));
        } else if ("compact".equalsIgnoreCase(format)) {
            // 紧凑格式，只输出NBT部分
            String hoconStr = NBTUtils.toHoconString(nbtMap);
            // 移除开头的缩进和 "nbt_form:" 标记
            String compactStr = hoconStr.replaceFirst("^\\s*nbt_form:\\s*", "");
            output.append(compactStr);
        } else {
            // 默认hocon格式
            output.append(NBTUtils.toHoconString(nbtMap));
        }

        String result = output.toString();

        // 发送到聊天
        player.sendMessage(Component.text("=== 物品NBT信息 ===", NamedTextColor.GREEN));
        for (String line : result.split("\n")) {
            player.sendMessage(Component.text(line, NamedTextColor.WHITE));
        }

        // 同时保存到文件
        try {
            saveToFile(player, result, itemId);
        } catch (Exception e) {
            player.sendMessage(Component.text("保存文件失败: " + e.getMessage(), NamedTextColor.RED));
        }

        return CommandResult.success();
    }

    private void saveToFile(ServerPlayer player, String content, String itemId) throws Exception {
        // 创建nbt导出目录
        Path nbtDir = Paths.get("config", plugin.metadata().id(), "nbt_exports");
        if (!Files.exists(nbtDir)) {
            Files.createDirectories(nbtDir);
        }

        // 生成文件名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = String.format("%s_%s_%s.conf",
                player.name(),
                itemId.replace(":", "_"),
                timestamp);

        Path filePath = nbtDir.resolve(filename);

        // 写入文件
        Files.writeString(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        player.sendMessage(Component.text("NBT信息已保存到: " + filePath, NamedTextColor.GREEN));
    }
}