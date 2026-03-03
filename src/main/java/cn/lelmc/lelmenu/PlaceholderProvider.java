package cn.lelmc.lelmenu;

import io.github.miniplaceholders.api.Expansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.statistic.Statistics;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlaceholderProvider {

    private static final long startTime = System.currentTimeMillis();
    private static final long downtimeMillis = 0;

    // 用于记录唯一玩家
    private static final Set<UUID> uniquePlayers = ConcurrentHashMap.newKeySet();

    // 经济服务缓存
    private static EconomyService economyService = null;
    private static Currency defaultCurrency = null;

    private static Tag createTag(String value) {
        return Tag.selfClosingInserting(Component.text(value));
    }

    public static void registerExpansion() {
        // 注册 player 占位符
        registerPlayerExpansion();

        // 注册 server 占位符
        registerServerExpansion();

        // 注册 economy 占位符
        registerEconomyExpansion();
    }

    private static void registerPlayerExpansion() {
        final Expansion playerExpansion = Expansion.builder("player")
                .audiencePlaceholder(ServerPlayer.class, "uuid", (p, ctx, queue) ->
                        createTag(p.uniqueId().toString()))
                .audiencePlaceholder(ServerPlayer.class, "name", (p, ctx, queue) ->
                        createTag(p.name()))

                // 前缀后缀
                .audiencePlaceholder(ServerPlayer.class, "prefix", (p, ctx, queue) ->
                        createTag(p.option("prefix").orElse("")))
                .audiencePlaceholder(ServerPlayer.class, "suffix", (p, ctx, queue) ->
                        createTag(p.option("suffix").orElse("")))

                // 飞行状态
                .audiencePlaceholder(ServerPlayer.class, "can_fly", (p, ctx, queue) ->
                        createTag(String.valueOf(p.get(Keys.CAN_FLY).orElse(false))))
                .audiencePlaceholder(ServerPlayer.class, "flying", (p, ctx, queue) ->
                        createTag(String.valueOf(p.get(Keys.IS_FLYING).orElse(false))))
                .audiencePlaceholder(ServerPlayer.class, "fly_speed", (p, ctx, queue) ->
                        createTag(String.valueOf(p.get(Keys.FLYING_SPEED).orElse(1.0))))

                // 世界和位置
                .audiencePlaceholder(ServerPlayer.class, "world", (p, ctx, queue) ->
                        createTag(p.world().properties().name()))
                .audiencePlaceholder(ServerPlayer.class, "x", (p, ctx, queue) ->
                        createTag(String.valueOf(p.location().position().x())))
                .audiencePlaceholder(ServerPlayer.class, "y", (p, ctx, queue) ->
                        createTag(String.valueOf(p.location().position().y())))
                .audiencePlaceholder(ServerPlayer.class, "z", (p, ctx, queue) ->
                        createTag(String.valueOf(p.location().position().z())))
                .audiencePlaceholder(ServerPlayer.class, "direction", (p, ctx, queue) ->
                        createTag(getCardinal(p)))

                // 网络信息
                .audiencePlaceholder(ServerPlayer.class, "ping", (p, ctx, queue) ->
                        createTag(String.valueOf(p.connectionState().latency())))
                .audiencePlaceholder(ServerPlayer.class, "language", (p, ctx, queue) ->
                        createTag(p.locale().getDisplayName()))

                // 生命值和饱食度
                .audiencePlaceholder(ServerPlayer.class, "health", (p, ctx, queue) ->
                        createTag(String.valueOf(Math.round(p.get(Keys.HEALTH).orElse(20.0)))))
                .audiencePlaceholder(ServerPlayer.class, "max_health", (p, ctx, queue) ->
                        createTag(String.valueOf(Math.round(p.get(Keys.MAX_HEALTH).orElse(20.0)))))
                .audiencePlaceholder(ServerPlayer.class, "food", (p, ctx, queue) ->
                        createTag(String.valueOf(p.get(Keys.FOOD_LEVEL).orElse(20))))
                .audiencePlaceholder(ServerPlayer.class, "saturation", (p, ctx, queue) ->
                        createTag(String.valueOf(Math.round(p.get(Keys.SATURATION).orElse(5.0)))))

                // 游戏模式和经验
                .audiencePlaceholder(ServerPlayer.class, "gamemode", (p, ctx, queue) ->
                        createTag(p.gameMode().get().asComponent().examinableName()))
                .audiencePlaceholder(ServerPlayer.class, "exp_total", (p, ctx, queue) ->
                        createTag(String.valueOf(p.get(Keys.EXPERIENCE).orElse(0))))
                .audiencePlaceholder(ServerPlayer.class, "exp", (p, ctx, queue) ->
                        createTag(String.valueOf(p.get(Keys.EXPERIENCE_SINCE_LEVEL).orElse(0))))
                .audiencePlaceholder(ServerPlayer.class, "exp_to_next", (p, ctx, queue) ->
                        createTag(String.valueOf(p.get(Keys.EXPERIENCE_FROM_START_OF_LEVEL).orElse(0))))
                .audiencePlaceholder(ServerPlayer.class, "level", (p, ctx, queue) ->
                        createTag(String.valueOf(p.get(Keys.EXPERIENCE_LEVEL).orElse(0))))

                // 空气值
                .audiencePlaceholder(ServerPlayer.class, "max_air", (p, ctx, queue) ->
                        createTag(String.valueOf(p.get(Keys.MAX_AIR).orElse(300))))
                .audiencePlaceholder(ServerPlayer.class, "remaining_air", (p, ctx, queue) ->
                        createTag(String.valueOf(p.get(Keys.REMAINING_AIR).orElse(300))))

                // 手持物品
                .audiencePlaceholder(ServerPlayer.class, "item_in_main_hand", (p, ctx, queue) ->
                        createTag(getDisplayName(p.itemInHand(HandTypes.MAIN_HAND))))
                .audiencePlaceholder(ServerPlayer.class, "item_in_off_hand", (p, ctx, queue) ->
                        createTag(getDisplayName(p.itemInHand(HandTypes.OFF_HAND))))

                // 移动速度
                .audiencePlaceholder(ServerPlayer.class, "walk_speed", (p, ctx, queue) ->
                        createTag(String.valueOf(p.get(Keys.WALKING_SPEED).orElse(1.0))))

                // 游戏时间
                .audiencePlaceholder(ServerPlayer.class, "time_played_seconds", (p, ctx, queue) ->
                        createTag(String.valueOf(getTime(p, TimeUnit.SECONDS, true))))
                .audiencePlaceholder(ServerPlayer.class, "time_played_minutes", (p, ctx, queue) ->
                        createTag(String.valueOf(getTime(p, TimeUnit.MINUTES, true))))
                .audiencePlaceholder(ServerPlayer.class, "time_played_ticks", (p, ctx, queue) ->
                        createTag(String.valueOf(getTime(p, null, true))))
                .audiencePlaceholder(ServerPlayer.class, "time_played_hours", (p, ctx, queue) ->
                        createTag(String.valueOf(getTime(p, TimeUnit.HOURS, true))))
                .audiencePlaceholder(ServerPlayer.class, "time_played_days", (p, ctx, queue) ->
                        createTag(String.valueOf(getTime(p, TimeUnit.DAYS, true))))
                .audiencePlaceholder(ServerPlayer.class, "time_played", (p, ctx, queue) ->
                        createTag(formatTime(p)))

                // 首次加入时间
                .audiencePlaceholder(ServerPlayer.class, "first_join", (p, ctx, queue) ->
                        createTag(p.get(Keys.FIRST_DATE_JOINED)
                                .map(Instant::toString)
                                .orElse("unknown")))

                .version("1.0.0")
                .build();

        playerExpansion.register();
    }

    public static String getDisplayName(ItemStack snapshot) {
        return snapshot.get(Keys.CUSTOM_NAME)
                .map(PlainTextComponentSerializer.plainText()::serialize)
                .orElseGet(() -> snapshot.type().asComponent().toString());
    }

    private static void registerServerExpansion() {
        final Expansion serverExpansion = Expansion.builder("server")
                // 服务器基本信息
                .globalPlaceholder("online",
                        createTag(String.valueOf(Sponge.server().onlinePlayers().size())))
                .globalPlaceholder("max_players",
                        createTag(String.valueOf(Sponge.server().maxPlayers())))
                .globalPlaceholder("unique_players",
                        createTag(String.valueOf(uniquePlayers.size())))
                .globalPlaceholder("motd",
                        createTag(Sponge.server().motd().toString()))

                // 运行时间
                .globalPlaceholder("uptime", (a, c) -> {
                    long um = getUptimeMillis();
                    long dm = getDowntimeMillis();
                    NumberFormat fmt = NumberFormat.getPercentInstance();
                    fmt.setMaximumFractionDigits(2);
                    fmt.setMinimumFractionDigits(2);
                    return createTag(fmt.format((um / ((double) dm + (double) um))));
                })
                .globalPlaceholder("uptime_total",
                        createTag(Duration.ofMillis(getUptimeMillis()).toString()))

                // 内存信息
                .globalPlaceholder("ram_used", (a, c) -> {
                    int MB = 1048576;
                    Runtime runtime = Runtime.getRuntime();
                    return createTag(String.valueOf((runtime.totalMemory() - runtime.freeMemory()) / MB));
                })
                .globalPlaceholder("ram_free", (a, c) -> {
                    int MB = 1048576;
                    Runtime runtime = Runtime.getRuntime();
                    return createTag(String.valueOf(runtime.freeMemory() / MB));
                })
                .globalPlaceholder("ram_total", (a, c) -> {
                    int MB = 1048576;
                    Runtime runtime = Runtime.getRuntime();
                    return createTag(String.valueOf(runtime.totalMemory() / MB));
                })
                .globalPlaceholder("ram_max", (a, c) -> {
                    int MB = 1048576;
                    Runtime runtime = Runtime.getRuntime();
                    return createTag(String.valueOf(runtime.maxMemory() / MB));
                })

                // CPU 信息
                .globalPlaceholder("cores", (a, c) ->
                        createTag(String.valueOf(Runtime.getRuntime().availableProcessors())))

                // TPS
                .globalPlaceholder("tps", (a, c) ->
                        createTag(String.valueOf(Sponge.server().ticksPerSecond())))

                // 时间相关
                .globalPlaceholder("time_world", (a, c) ->
                        createTag(String.valueOf(System.currentTimeMillis())))

                .version("1.0.0")
                .build();

        serverExpansion.register();
    }

    private static void registerEconomyExpansion() {
        final Expansion economyExpansion = Expansion.builder("economy")
                // 基础经济信息
                .audiencePlaceholder(ServerPlayer.class, "balance", (p, ctx, queue) -> {
                    EconomyService service = getEconomyService();
                    if (service == null) {
                        return createTag("0.00");
                    }
                    UniqueAccount account = service.findOrCreateAccount(p.uniqueId()).orElse(null);
                    if (account == null) {
                        return createTag("0.00");
                    }
                    BigDecimal balance = account.balance(defaultCurrency);
                    return createTag(balance.toPlainString());
                })
                .audiencePlaceholder(ServerPlayer.class, "balance_formatted", (p, ctx, queue) -> {
                    EconomyService service = getEconomyService();
                    if (service == null) {
                        return createTag("$0.00");
                    }
                    UniqueAccount account = service.findOrCreateAccount(p.uniqueId()).orElse(null);
                    if (account == null) {
                        return createTag("$0.00");
                    }
                    BigDecimal balance = account.balance(defaultCurrency);
                    return createTag(defaultCurrency.format(balance).toString());
                })
                .audiencePlaceholder(ServerPlayer.class, "currency_name", (p, ctx, queue) -> {
                    EconomyService service = getEconomyService();
                    if (service == null || defaultCurrency == null) {
                        return createTag("未知货币");
                    }
                    return createTag(getDisplayName(defaultCurrency.displayName()));
                })
                .audiencePlaceholder(ServerPlayer.class, "currency_symbol", (p, ctx, queue) -> {
                    EconomyService service = getEconomyService();
                    if (service == null || defaultCurrency == null) {
                        return createTag("$");
                    }
                    return createTag(getDisplayName(defaultCurrency.symbol()));
                })

                // 排行榜相关（需要额外实现）
                .globalPlaceholder("baltop_1", (a, c) -> createTag(getBaltopName(1)))
                .globalPlaceholder("baltop_2", (a, c) -> createTag(getBaltopName(2)))
                .globalPlaceholder("baltop_3", (a, c) -> createTag(getBaltopName(3)))
                .globalPlaceholder("baltop_4", (a, c) -> createTag(getBaltopName(4)))
                .globalPlaceholder("baltop_5", (a, c) -> createTag(getBaltopName(5)))

                .globalPlaceholder("baltop_1_balance", (a, c) -> createTag(getBaltopBalance(1)))
                .globalPlaceholder("baltop_2_balance", (a, c) -> createTag(getBaltopBalance(2)))
                .globalPlaceholder("baltop_3_balance", (a, c) -> createTag(getBaltopBalance(3)))
                .globalPlaceholder("baltop_4_balance", (a, c) -> createTag(getBaltopBalance(4)))
                .globalPlaceholder("baltop_5_balance", (a, c) -> createTag(getBaltopBalance(5)))

                .version("1.0.0")
                .build();

        economyExpansion.register();
    }

    private static EconomyService getEconomyService() {
        if (economyService != null && Sponge.server().serviceProvider().economyService().isEmpty()) {
            economyService = null;
        }

        if (economyService == null) {
            Optional<EconomyService> optService = Sponge.server().serviceProvider().economyService();
            if (optService.isPresent()) {
                economyService = optService.get();
                defaultCurrency = economyService.defaultCurrency();
            }
        }

        return economyService;
    }

    private static String getBaltopBalance(int rank) {
        EconomyService service = getEconomyService();
        if (service == null) {
            return "0.00";
        }

        try {
            var accounts = service.virtualAccounts();
            List<AbstractMap.SimpleEntry<Object, BigDecimal>> balances = new ArrayList<>();

            for (var account : accounts) {
                BigDecimal balance = account.balance(defaultCurrency);
                if (balance.compareTo(BigDecimal.ZERO) > 0) {
                    balances.add(new AbstractMap.SimpleEntry<>(account.identifier(), balance));
                }
            }

            balances.sort((a, b) -> b.getValue().compareTo(a.getValue()));

            if (rank <= balances.size()) {
                BigDecimal balance = balances.get(rank - 1).getValue();
                return balance.toPlainString();
            }
        } catch (Exception e) {
            // 如果无法获取排行榜，返回 0
        }

        return "0.00";
    }

    private static String getBaltopName(int rank) {
        EconomyService service = getEconomyService();
        if (service == null) {
            return "无";
        }

        try {
            var accounts = service.virtualAccounts();
            List<Map.Entry<String, BigDecimal>> balances = new ArrayList<>();

            for (var account : accounts) {
                BigDecimal balance = account.balance(defaultCurrency);
                if (balance.compareTo(BigDecimal.ZERO) > 0) {
                    // 使用 identifier() 返回 String
                    balances.add(new AbstractMap.SimpleEntry<>(account.identifier(), balance));
                }
            }

            balances.sort((a, b) -> b.getValue().compareTo(a.getValue()));

            if (rank <= balances.size()) {
                String identifier = balances.get(rank - 1).getKey();
                try {
                    UUID uuid = UUID.fromString(identifier);
                    Optional<ServerPlayer> player = Sponge.server().player(uuid);
                    return player.map(ServerPlayer::name).orElse(identifier.substring(0, 8));
                } catch (Exception e) {
                    return identifier;
                }
            }
        } catch (Exception e) {
            // 如果无法获取排行榜，返回未知
        }

        return "未知";
    }

    private static String getCardinal(ServerPlayer p) {
        double rotation = (p.rotation().y() + 180) % 360;
        if (rotation < 0) rotation += 360;

        if (rotation >= 337.5 || rotation < 22.5) return "南";
        if (rotation >= 22.5 && rotation < 67.5) return "西南";
        if (rotation >= 67.5 && rotation < 112.5) return "西";
        if (rotation >= 112.5 && rotation < 157.5) return "西北";
        if (rotation >= 157.5 && rotation < 202.5) return "北";
        if (rotation >= 202.5 && rotation < 247.5) return "东北";
        if (rotation >= 247.5 && rotation < 292.5) return "东";
        if (rotation >= 292.5) return "东南";
        return "未知";
    }

    private static long getTime(ServerPlayer p, TimeUnit unit, boolean raw) {
        // 尝试通过统计信息获取游戏时间
        var stats = p.statistics();

        // 获取玩家的游戏时间统计（以刻为单位）
        var ticks = stats.get().get(Statistics.PLAY_TIME.get());

        // Minecraft 中 1 秒 = 20 tick
        long seconds = ticks / 20;

        if (unit == null) return ticks;

        return switch (unit) {
            case SECONDS -> seconds;
            case MINUTES -> seconds / 60;
            case HOURS -> seconds / 3600;
            case DAYS -> seconds / 86400;
        };
    }

    private static String formatTime(ServerPlayer p) {
        long totalSeconds = getTime(p, TimeUnit.SECONDS, false);

        long d = totalSeconds / 86400;
        long h = (totalSeconds % 86400) / 3600;
        long m = (totalSeconds % 3600) / 60;
        double s = totalSeconds % 60;

        NumberFormat f = NumberFormat.getInstance(Locale.getDefault());
        f.setMaximumFractionDigits(2);

        StringBuilder out = new StringBuilder();
        if (d > 0) out.append(f.format(d)).append(" d ");
        if (h > 0) out.append(f.format(h)).append(" h ");
        if (m > 0) out.append(f.format(m)).append(" m ");
        if (s > 0) out.append(f.format(s)).append(" s");

        return out.toString().trim();
    }

    private static long getUptimeMillis() {
        return System.currentTimeMillis() - startTime;
    }

    private static long getDowntimeMillis() {
        return downtimeMillis;
    }

    private static String getDisplayName(Component component) {
        LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacySection();
        return legacySerializer.serialize(component);
    }

    private enum TimeUnit {
        SECONDS, MINUTES, HOURS, DAYS
    }
}
