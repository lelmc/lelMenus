package cn.lelmc.lelmenu.utils;

import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public class NbtBuilder {

    private final Map<String, NbtValue<?>> values = new HashMap<>();

    public void set(String key, int value) {
        values.put(key, new NbtValue.IntValue(value));
    }

    public void set(String key, String value) {
        values.put(key, new NbtValue.StringValue(value));
    }

    public void set(String key, boolean value) {
        values.put(key, new NbtValue.BooleanValue(value));
    }

    public void set(String key, double value) {
        values.put(key, new NbtValue.DoubleValue(value));
    }

    public void setPlaceholder(String key, String placeholder, ValueType type) {
        values.put(key, new NbtValue.PlaceholderValue(placeholder, type));
    }

    public void build(DataView view, ServerPlayer player) {
        for (Map.Entry<String, NbtValue<?>> entry : values.entrySet()) {
            entry.getValue().apply(view, entry.getKey(), player);
        }
    }

    // NBT值类型抽象
    private interface NbtValue<T> {
        void apply(DataView view, String key, ServerPlayer player);

        class IntValue implements NbtValue<Integer> {
            private final int value;

            IntValue(int value) {
                this.value = value;
            }

            @Override
            public void apply(DataView view, String key, ServerPlayer player) {
                view.set(DataQuery.of(key), value);
            }
        }

        class StringValue implements NbtValue<String> {
            private final String value;

            StringValue(String value) {
                this.value = value;
            }

            @Override
            public void apply(DataView view, String key, ServerPlayer player) {
                view.set(DataQuery.of(key), value);
            }
        }

        class BooleanValue implements NbtValue<Boolean> {
            private final boolean value;

            BooleanValue(boolean value) {
                this.value = value;
            }

            @Override
            public void apply(DataView view, String key, ServerPlayer player) {
                view.set(DataQuery.of(key), value);
            }
        }

        class DoubleValue implements NbtValue<Double> {
            private final double value;

            DoubleValue(double value) {
                this.value = value;
            }

            @Override
            public void apply(DataView view, String key, ServerPlayer player) {
                view.set(DataQuery.of(key), value);
            }
        }

        class PlaceholderValue implements NbtValue<String> {
            private final String placeholder;
            private final ValueType expectedType;

            PlaceholderValue(String placeholder, ValueType expectedType) {
                this.placeholder = placeholder;
                this.expectedType = expectedType;
            }

            @Override
            public void apply(DataView view, String key, ServerPlayer player) {
                String parsed = Placeholder.parseString(placeholder, player);
                Object value = expectedType.convert(parsed);
                view.set(DataQuery.of(key), value);
            }
        }
    }

    public enum ValueType {
        INTEGER {
            @Override
            public Object convert(String value) {
                return Integer.parseInt(value);
            }
        },
        LONG {
            @Override
            public Object convert(String value) {
                return Long.parseLong(value);
            }
        },
        DOUBLE {
            @Override
            public Object convert(String value) {
                return Double.parseDouble(value);
            }
        },
        BOOLEAN {
            @Override
            public Object convert(String value) {
                return Boolean.parseBoolean(value);
            }
        },
        STRING {
            @Override
            public Object convert(String value) {
                return value;
            }
        },
        AUTO {
            @Override
            public Object convert(String value) {
                // 自动判断类型
                if (value.matches("-?\\d+")) {
                    try {
                        long longVal = Long.parseLong(value);
                        if (longVal >= Integer.MIN_VALUE && longVal <= Integer.MAX_VALUE) {
                            return (int) longVal;
                        }
                        return longVal;
                    } catch (NumberFormatException ignored) {
                    }
                }
                if (value.matches("-?\\d+\\.\\d+")) {
                    try {
                        return Double.parseDouble(value);
                    } catch (NumberFormatException ignored) {
                    }
                }
                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                    return Boolean.parseBoolean(value);
                }
                return value;
            }
        };

        public abstract Object convert(String value);
    }
}