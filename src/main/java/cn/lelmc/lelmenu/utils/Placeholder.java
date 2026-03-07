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
            return PlainTextComponentSerializer.plainText().serialize(parsed);
        } catch (Exception e) {
            return text;
        }
    }
}