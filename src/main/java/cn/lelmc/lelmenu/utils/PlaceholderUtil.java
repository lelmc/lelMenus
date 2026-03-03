package cn.lelmc.lelmenu.utils;

import io.github.miniplaceholders.api.MiniPlaceholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class PlaceholderUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    // 检查 MiniPlaceholders 是否可用
    public static boolean isMiniPlaceholdersAvailable() {
        try {
            Class.forName("io.github.miniplaceholders.api.MiniPlaceholders");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * 解析文本中的占位符，返回 Component
     * 支持 MiniMessage 格式和传统 & 颜色代码
     */
    public static Component parsePlaceholders(String text, ServerPlayer player) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        try {
            String miniMessageText = convertLegacyToMiniMessage(text);

            if (isMiniPlaceholdersAvailable()) {
                TagResolver resolver = MiniPlaceholders.audienceGlobalPlaceholders();
                return MINI_MESSAGE.deserialize(miniMessageText, resolver);
            } else {
                return MINI_MESSAGE.deserialize(miniMessageText);
            }
        } catch (Exception e) {
            return LEGACY_SERIALIZER.deserialize(text.replace('&', '§'));
        }
    }

    /**
     * 解析文本中的占位符，返回字符串（用于 NBT 等需要字符串的地方）
     */
    public static String parsePlaceholdersToString(String text, ServerPlayer player) {
        Component component = parsePlaceholders(text, player);
        return LEGACY_SERIALIZER.serialize(component);
    }

    /**
     * 将传统 & 颜色代码转换为 MiniMessage 格式
     */
    private static String convertLegacyToMiniMessage(String text) {
        if (text == null) return "";

        // 简单的转换规则
        return text
                .replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&k", "<obfuscated>")
                .replace("&l", "<bold>")
                .replace("&m", "<strikethrough>")
                .replace("&n", "<underlined>")
                .replace("&o", "<italic>")
                .replace("&r", "<reset>");
    }
}