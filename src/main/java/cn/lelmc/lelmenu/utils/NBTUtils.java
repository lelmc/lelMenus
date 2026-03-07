package cn.lelmc.lelmenu.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class NBTUtils {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * 将ItemStack转换为NBT Map格式，方便写入配置文件
     */
    public static Map<String, Object> itemStackToNBTMap(ItemStack itemStack) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 获取物品的DataContainer
        DataContainer container = itemStack.toContainer();
        // 遍历所有数据
        for (Map.Entry<DataQuery, Object> entry : container.values(false).entrySet()) {
            String key = entry.getKey().asString('.');
            Object value = entry.getValue();

            // 跳过一些内部数据
            if (key.startsWith("sponge:")) {
                continue;
            }

            // 处理不同类型的值
            Object processedValue = processValue(value);
            if (processedValue != null) {
                result.put(key, processedValue);
            }
        }

        return result;
    }

    /**
     * 递归处理值，将DataView等转换为Map
     */
    private static Object processValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof DataView view) {
            // 如果是DataView，转换为Map
            Map<String, Object> map = new LinkedHashMap<>();
            for (Map.Entry<DataQuery, Object> entry : view.values(false).entrySet()) {
                String key = entry.getKey().asString('.');
                Object processed = processValue(entry.getValue());
                if (processed != null) {
                    map.put(key, processed);
                }
            }
            return map.isEmpty() ? null : map;

        } else if (value instanceof List<?> list) {
            // 如果是List，处理每个元素
            List<Object> processedList = list.stream()
                    .map(NBTUtils::processValue)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            return processedList.isEmpty() ? null : processedList;

        } else if (value instanceof Byte || value instanceof Short ||
                value instanceof Integer || value instanceof Long ||
                value instanceof Float || value instanceof Double ||
                value instanceof Boolean || value instanceof String) {
            // 基本类型直接返回
            return value;

        } else if (value instanceof byte[]) {
            // 字节数组转换为字符串表示
            return "byte[" + ((byte[]) value).length + "]";

        } else if (value instanceof int[]) {
            // 整数数组
            return "int[" + ((int[]) value).length + "]";

        } else {
            // 其他类型转换为字符串
            return value.toString();
        }
    }

    /**
     * 将NBT Map转换为HOCON格式的字符串
     */
    public static String toHoconString(Map<String, Object> nbtMap) {
        StringBuilder sb = new StringBuilder();

        // 处理整数NBT
        Map<String, Integer> intMap = new LinkedHashMap<>();
        Map<String, String> stringMap = new LinkedHashMap<>();
        Map<String, Double> doubleMap = new LinkedHashMap<>();
        Map<String, Float> floatMap = new LinkedHashMap<>();
        Map<String, Object> formMap = new LinkedHashMap<>();

        // 分类NBT数据
        for (Map.Entry<String, Object> entry : nbtMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Integer) {
                intMap.put(key, (Integer) value);
            } else if (value instanceof String) {
                stringMap.put(key, (String) value);
            } else if (value instanceof Double) {
                doubleMap.put(key, (Double) value);
            } else if (value instanceof Float) {
                floatMap.put(key, (Float) value);
            } else {
                formMap.put(key, value);
            }
        }

        // 输出整数NBT
        if (!intMap.isEmpty()) {
            sb.append("    nbt_int: {\n");
            for (Map.Entry<String, Integer> entry : intMap.entrySet()) {
                sb.append("        \"").append(entry.getKey()).append("\": ").append(entry.getValue()).append("\n");
            }
            sb.append("    }\n");
        }

        // 输出字符串NBT
        if (!stringMap.isEmpty()) {
            sb.append("    nbt_string: {\n");
            for (Map.Entry<String, String> entry : stringMap.entrySet()) {
                sb.append("        \"").append(entry.getKey()).append("\": \"").append(escapeString(entry.getValue())).append("\"\n");
            }
            sb.append("    }\n");
        }

        // 输出双精度NBT
        if (!doubleMap.isEmpty()) {
            sb.append("    nbt_double: {\n");
            for (Map.Entry<String, Double> entry : doubleMap.entrySet()) {
                sb.append("        \"").append(entry.getKey()).append("\": ").append(entry.getValue()).append("\n");
            }
            sb.append("    }\n");
        }

        // 输出单精度NBT
        if (!floatMap.isEmpty()) {
            sb.append("    nbt_float: {\n");
            for (Map.Entry<String, Float> entry : floatMap.entrySet()) {
                sb.append("        \"").append(entry.getKey()).append("\": ").append(entry.getValue()).append("\n");
            }
            sb.append("    }\n");
        }

        // 输出复合NBT
        if (!formMap.isEmpty()) {
            sb.append("    nbt_form: ");
            appendComplexObject(sb, formMap, 4);
            sb.append("\n");
        }

        return sb.toString();
    }

    private static void appendComplexObject(StringBuilder sb, Object obj, int indent) {
        if (obj instanceof Map<?, ?> map) {
            if (map.isEmpty()) {
                sb.append("{}");
                return;
            }
            sb.append("{\n");
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                indent(sb, indent + 4);
                sb.append("\"").append(entry.getKey()).append("\": ");
                appendComplexObject(sb, entry.getValue(), indent + 4);
                sb.append("\n");
            }
            indent(sb, indent);
            sb.append("}");
        } else if (obj instanceof List<?> list) {
            if (list.isEmpty()) {
                sb.append("[]");
                return;
            }
            sb.append("[\n");
            for (Object item : list) {
                indent(sb, indent + 4);
                appendComplexObject(sb, item, indent + 4);
                sb.append(",\n");
            }
            sb.delete(sb.length() - 2, sb.length()); // 移除最后一个逗号
            sb.append("\n");
            indent(sb, indent);
            sb.append("]");
        } else if (obj instanceof String) {
            sb.append("\"").append(escapeString((String) obj)).append("\"");
        } else {
            sb.append(obj);
        }
    }

    private static void indent(StringBuilder sb, int spaces) {
        for (int i = 0; i < spaces; i++) {
            sb.append(' ');
        }
    }

    private static String escapeString(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 将NBT Map转换为JSON字符串（用于调试）
     */
    public static String toJsonString(Map<String, Object> nbtMap) {
        JsonElement json = JsonParser.parseString(GSON.toJson(nbtMap));
        return GSON.toJson(json);
    }
}