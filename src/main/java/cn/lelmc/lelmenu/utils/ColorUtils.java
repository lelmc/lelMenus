package cn.lelmc.lelmenu.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;
import java.util.stream.Collectors;

public class ColorUtils {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    /**
     * 将包含 & 颜色代码的字符串转换为 Component
     */
    public static Component toComponent(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        String formatted = text.replace('&', '§');
        return LEGACY_SERIALIZER.deserialize(formatted)
                .style(style -> style.decoration(TextDecoration.ITALIC, false));
    }

    /**
     * 将包含 & 颜色代码的字符串列表转换为 Component 列表
     */
    public static List<Component> toComponentList(List<String> texts) {
        return texts.stream()
                .map(ColorUtils::toComponent)
                .collect(Collectors.toList());
    }

    /**
     * 将包含 & 颜色代码的字符串数组转换为 Component 数组
     */
    public static Component[] toComponentArray(String... texts) {
        return java.util.Arrays.stream(texts)
                .map(ColorUtils::toComponent)
                .toArray(Component[]::new);
    }

    /**
     * 将包含 & 颜色代码的字符串转换为格式化后的字符串（&替换为§）
     */
    public static String format(String text) {
        if (text == null) return "";
        return text.replace('&', '§');
    }
}