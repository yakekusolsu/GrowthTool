package dev.yakekusolsu.growthtools.command;

import dev.yakekusolsu.growthtools.ability.AbilityRegistry;
import dev.yakekusolsu.growthtools.api.ability.AbilityDefinition;
import dev.yakekusolsu.growthtools.api.ability.AbilityId;
import dev.yakekusolsu.growthtools.service.MessageService;
import dev.yakekusolsu.growthtools.ability.AbilityService;
import dev.yakekusolsu.growthtools.service.GrowthToolItemService;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.command.CommandSender;

public final class AbilityCommandHandler {
    public static final String PERMISSION = "growthtools.admin.ability";
    private final AbilityRegistry registry;
    private final MessageService messages;
    private final AbilityService abilities;
    private final GrowthToolItemService items;

    public AbilityCommandHandler(AbilityRegistry registry, AbilityService abilities,
            GrowthToolItemService items, MessageService messages) {
        this.registry = registry;
        this.abilities = abilities;
        this.items = items;
        this.messages = messages;
    }

    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            messages.send(sender, "command.no-permission");
            return;
        }
        if (args.length == 2 && "list".equalsIgnoreCase(args[1])) {
            String abilities = registry.getAll().stream()
                    .sorted(Comparator.comparing(AbilityDefinition::id))
                    .map(definition -> definition.id() + " (L" + definition.unlockLevel()
                            + ", " + (definition.enabled() ? "enabled" : "disabled") + ')')
                    .collect(java.util.stream.Collectors.joining("\n"));
            messages.send(sender, "ability.list", Map.of("abilities", abilities));
            return;
        }
        if (args.length == 3 && "info".equalsIgnoreCase(args[1])) {
            AbilityDefinition definition;
            try {
                definition = registry.get(AbilityId.parse(args[2])).orElse(null);
            } catch (IllegalArgumentException exception) {
                definition = null;
            }
            if (definition == null) {
                messages.send(sender, "ability.not-found", Map.of("ability", args[2]));
                return;
            }
            messages.send(sender, "ability.info", placeholders(definition));
            return;
        }
        if (args.length == 2 && "debug".equalsIgnoreCase(args[1])) {
            if (!(sender instanceof org.bukkit.entity.Player player)) {
                messages.send(sender, "command.player-only");
                return;
            }
            var data = items.read(player.getInventory().getItemInMainHand());
            if (data.isEmpty()) {
                messages.send(sender, "command.inspect-not-growth-tool");
                return;
            }
            String details = registry.getAll().stream()
                    .filter(definition -> definition.supportedToolTypes().contains(data.get().type()))
                    .sorted(Comparator.comparing(AbilityDefinition::id))
                    .map(definition -> definition.id() + ": "
                            + (!definition.enabled() ? "disabled"
                            : data.get().level() >= definition.unlockLevel() ? "unlocked" : "locked")
                            + ", cooldown=" + abilities.remainingCooldownMillis(
                                    definition.id(), data.get().toolId()) + "ms")
                    .collect(java.util.stream.Collectors.joining("\n"));
            messages.send(sender, "ability.debug", Map.of("details", details));
            return;
        }
        messages.send(sender, "ability.usage");
    }

    public List<String> complete(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PERMISSION)) return List.of();
        if (args.length == 2) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            return List.of("list", "info", "debug").stream()
                    .filter(value -> value.startsWith(prefix)).toList();
        }
        if (args.length == 3 && "info".equalsIgnoreCase(args[1])) {
            String prefix = args[2].toLowerCase(Locale.ROOT);
            return registry.getAll().stream().map(definition -> definition.id().toString())
                    .filter(value -> value.startsWith(prefix)).sorted().toList();
        }
        return List.of();
    }

    private static Map<String, String> placeholders(AbilityDefinition definition) {
        return Map.of(
                "id", definition.id().toString(),
                "name", definition.displayName(),
                "description", definition.description(),
                "trigger", definition.trigger().name(),
                "enabled", Boolean.toString(definition.enabled()),
                "level", Integer.toString(definition.unlockLevel()),
                "cooldown", Long.toString(definition.cooldown().toMillis()),
                "types", definition.supportedToolTypes().toString(),
                "settings", definition.settings().toString());
    }
}
