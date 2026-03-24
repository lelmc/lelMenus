package cn.lelmc.lelmenu.utils;

import io.github.miniplaceholders.api.MiniPlaceholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class Placeholder {

    public static String parseString(String text, ServerPlayer player) {
        if (text == null || text.isEmpty()) return text;
        try {
            TagResolver resolver = MiniPlaceholders.audienceGlobalPlaceholders();
            Component parsed = MiniMessage.miniMessage().deserialize(
                    text,
                    player,
                    resolver
            );
            return PlainTextComponentSerializer.plainText().serialize(parsed).toLowerCase();
        } catch (Exception e) {
            return text.toLowerCase();
        }
    }

    /**
     * 解析并返回原始类型的值
     *
     * @param text   带占位符的文本
     * @param player 玩家
     * @return 解析后的原始值（可以是 String, Integer, Double, Boolean 等）
     */
    public static Object parseObject(String text, ServerPlayer player) {
        if (text == null || text.isEmpty()) return text;

        // 如果不是占位符格式，直接返回原文本
        if (!text.startsWith("<") || !text.endsWith(">")) {
            return text;
        }

        try {
            // 先获取解析后的字符串
            String parsed = parseString(text, player);

            // 尝试解析为整数
            try {
                if (parsed.matches("-?\\d+")) {
                    long longValue = Long.parseLong(parsed);
                    if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
                        return (int) longValue;
                    }
                    return longValue;
                }
            } catch (NumberFormatException ignored) {
            }

            // 尝试解析为浮点数
            try {
                if (parsed.matches("-?\\d+\\.\\d+")) {
                    return Double.parseDouble(parsed);
                }
            } catch (NumberFormatException ignored) {
            }

            // 尝试解析为布尔值
            if (parsed.equalsIgnoreCase("true") || parsed.equalsIgnoreCase("false")) {
                return Boolean.parseBoolean(parsed);
            }

            // 默认返回字符串
            return parsed;

        } catch (Exception e) {
            return text;
        }
    }

    /**
     * 解析并返回指定类型的值
     */
    public static <T> T parseAs(String text, ServerPlayer player, Class<T> type) {
        Object value = parseObject(text, player);

        if (type == Integer.class && value instanceof Number) {
            return type.cast(((Number) value).intValue());
        } else if (type == Double.class && value instanceof Number) {
            return type.cast(((Number) value).doubleValue());
        } else if (type == Boolean.class && value instanceof Boolean) {
            return type.cast(value);
        } else if (type == String.class) {
            return type.cast(value.toString());
        }

        // 尝试转换
        try {
            if (type == Integer.class) {
                return type.cast(Integer.parseInt(value.toString()));
            } else if (type == Double.class) {
                return type.cast(Double.parseDouble(value.toString()));
            } else if (type == Boolean.class) {
                return type.cast(Boolean.parseBoolean(value.toString()));
            }
        } catch (Exception ignored) {
        }

        return null;
    }
}