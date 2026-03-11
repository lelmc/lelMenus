package cn.lelmc.lelmenu.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class NBTUtils {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final DataQuery COMPONENTS_QUERY = DataQuery.of("components");

    /**
     * 将ItemStack转换为NBT Map格式，专门处理components路径
     */
    public static Map<String, Object> itemStackToNBTMap(ItemStack itemStack) {
        // 获取物品的DataContainer
        DataContainer container = itemStack.toContainer();

        // 获取components视图
        Optional<DataView> componentsView = container.getView(COMPONENTS_QUERY);

        // 直接返回components下的内容
        return componentsView.map(NBTUtils::processDataView).orElseGet(LinkedHashMap::new);

    }

    /**
     * 递归处理DataView，保持原始嵌套结构
     */
    private static Map<String, Object> processDataView(DataView view) {
        Map<String, Object> result = new LinkedHashMap<>();

        for (Map.Entry<DataQuery, Object> entry : view.values(false).entrySet()) {
            String key = entry.getKey().asString('.');
            Object value = entry.getValue();

            // 跳过sponge内部数据
            if (key.startsWith("sponge:")) {
                continue;
            }

            Object processedValue = processValue(value);
            if (processedValue != null) {
                result.put(key, processedValue);
            }
        }

        return result;
    }

    /**
     * 判断是否为基本类型
     */
    private static boolean isPrimitiveType(Object value) {
        return value instanceof Byte || value instanceof Short ||
                value instanceof Integer || value instanceof Long ||
                value instanceof Float || value instanceof Double ||
                value instanceof Boolean || value instanceof String ||
                value instanceof Character;
    }

    /**
     * 递归处理值，保持数据结构
     */
    private static Object processValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof DataView view) {
            // DataView转换为Map
            return processDataView(view);

        } else if (value instanceof List<?> list) {
            // 处理列表，保持每个元素的类型
            return list.stream()
                    .map(NBTUtils::processValue)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } else if (value instanceof Map<?, ?> map) {
            // 处理Map
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                result.put(String.valueOf(entry.getKey()), processValue(entry.getValue()));
            }
            return result;

        } else if (isPrimitiveType(value)) {
            // 基本类型直接返回
            return value;

        } else if (value instanceof byte[] bytes) {
            // 字节数组转为列表
            List<Byte> list = new ArrayList<>();
            for (byte b : bytes) list.add(b);
            return list;

        } else if (value instanceof int[] ints) {
            // 整数数组转为列表
            List<Integer> list = new ArrayList<>();
            for (int i : ints) list.add(i);
            return list;

        } else if (value instanceof long[] longs) {
            // 长整数数组转为列表
            List<Long> list = new ArrayList<>();
            for (long l : longs) list.add(l);
            return list;

        } else if (value instanceof short[] shorts) {
            // 短整数数组转为列表
            List<Short> list = new ArrayList<>();
            for (short s : shorts) list.add(s);
            return list;

        } else if (value instanceof float[] floats) {
            // 浮点数数组转为列表
            List<Float> list = new ArrayList<>();
            for (float f : floats) list.add(f);
            return list;

        } else if (value instanceof double[] doubles) {
            // 双精度数组转为列表
            List<Double> list = new ArrayList<>();
            for (double d : doubles) list.add(d);
            return list;

        } else if (value instanceof boolean[] booleans) {
            // 布尔数组转为列表
            List<Boolean> list = new ArrayList<>();
            for (boolean b : booleans) list.add(b);
            return list;

        } else {
            // 其他类型转为字符串
            return value.toString();
        }
    }

    /**
     * 将NBT Map转换为HOCON格式的字符串，保持原始嵌套结构
     */
    public static String toHoconString(Map<String, Object> nbtMap) {
        StringBuilder sb = new StringBuilder();
        appendHoconObject(sb, nbtMap, 0);
        return sb.toString();
    }

    private static void appendHoconObject(StringBuilder sb, Object obj, int indent) {
        if (obj instanceof Map<?, ?> map) {
            if (map.isEmpty()) {
                sb.append("{}");
                return;
            }

            sb.append("{\n");

            // 处理所有条目
            List<Map.Entry<?, ?>> entries = new ArrayList<>(map.entrySet());
            for (int i = 0; i < entries.size(); i++) {
                Map.Entry<?, ?> entry = entries.get(i);

                indent(sb, indent + 2);

                // 键（如果包含特殊字符就加引号）
                String key = String.valueOf(entry.getKey());
                if (needsQuotes(key)) {
                    sb.append("\"").append(escapeString(key)).append("\"");
                } else {
                    sb.append(key);
                }

                sb.append(" = ");

                // 值
                appendHoconObject(sb, entry.getValue(), indent + 2);

                // 如果不是最后一个，添加换行
                if (i < entries.size() - 1) {
                    sb.append("\n");
                }
            }

            sb.append("\n");
            indent(sb, indent);
            sb.append("}");

        } else if (obj instanceof List<?> list) {
            if (list.isEmpty()) {
                sb.append("[]");
                return;
            }

            sb.append("[");

            // 判断是否所有元素都是简单类型
            boolean allSimple = list.stream().allMatch(NBTUtils::isSimpleValue);

            if (allSimple && list.size() <= 5) {
                // 简单类型且数量少，放在一行
                for (int i = 0; i < list.size(); i++) {
                    appendHoconObject(sb, list.get(i), indent);
                    if (i < list.size() - 1) {
                        sb.append(", ");
                    }
                }
                sb.append("]");
            } else {
                // 复杂类型，每个元素一行
                sb.append("\n");
                for (int i = 0; i < list.size(); i++) {
                    indent(sb, indent + 2);
                    appendHoconObject(sb, list.get(i), indent + 2);
                    if (i < list.size() - 1) {
                        sb.append(",\n");
                    }
                }
                sb.append("\n");
                indent(sb, indent);
                sb.append("]");
            }

        } else if (obj instanceof String) {
            sb.append("\"").append(escapeString((String) obj)).append("\"");
        } else if (obj instanceof Double || obj instanceof Float) {
            // 浮点数
            sb.append(obj);
        } else if (obj instanceof Number) {
            // 整数
            sb.append(obj);
        } else if (obj instanceof Boolean) {
            sb.append(obj);
        } else {
            sb.append(obj);
        }
    }

    /**
     * 判断是否为简单类型（用于输出格式化）
     */
    private static boolean isSimpleValue(Object obj) {
        return obj instanceof String || obj instanceof Number || obj instanceof Boolean;
    }

    private static boolean needsQuotes(String key) {
        // 如果键包含特殊字符，需要加引号
        return !key.matches("[a-zA-Z0-9_\\-]+");
    }

    private static void indent(StringBuilder sb, int spaces) {
        sb.append(" ".repeat(Math.max(0, spaces)));
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
        return GSON.toJson(nbtMap);
    }

    /**
     * 获取完整的物品信息（包括基本属性）
     */
    public static Map<String, Object> getFullItemInfo(ItemStack itemStack) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 基本属性
        result.put("id", itemStack.type().key(org.spongepowered.api.registry.RegistryTypes.ITEM_TYPE).asString());
        result.put("count", itemStack.quantity());

        // components数据
        DataContainer container = itemStack.toContainer();
        Optional<DataView> componentsView = container.getView(COMPONENTS_QUERY);
        componentsView.ifPresent(dataView -> result.put("components", processDataView(dataView)));
        return result;
    }
}