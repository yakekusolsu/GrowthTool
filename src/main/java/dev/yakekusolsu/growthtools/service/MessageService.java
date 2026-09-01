package dev.yakekusolsu.growthtools.service;

import dev.yakekusolsu.growthtools.config.PluginConfiguration;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

public final class MessageService {
    private static final LegacyComponentSerializer SERIALIZER =
            LegacyComponentSerializer.legacyAmpersand();

    private final PluginConfiguration configuration;

    public MessageService(PluginConfiguration configuration) {
        this.configuration = configuration;
    }

    public void send(CommandSender recipient, String key) {
        send(recipient, key, Map.of());
    }

    public void send(CommandSender recipient, String key, Map<String, String> placeholders) {
        String text = configuration.message("prefix") + render(key, placeholders);
        Component component = SERIALIZER.deserialize(text);
        recipient.sendMessage(component);
    }

    public Component component(String key, Map<String, String> placeholders) {
        return SERIALIZER.deserialize(render(key, placeholders));
    }

    private String render(String key, Map<String, String> placeholders) {
        String text = configuration.message(key);
        for (Map.Entry<String, String> placeholder : placeholders.entrySet()) {
            text = text.replace('{' + placeholder.getKey() + '}', placeholder.getValue());
        }
        return text;
    }
}
