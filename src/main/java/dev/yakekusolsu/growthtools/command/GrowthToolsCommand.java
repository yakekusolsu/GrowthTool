package dev.yakekusolsu.growthtools.command;

import dev.yakekusolsu.growthtools.model.GrowthToolData;
import dev.yakekusolsu.growthtools.model.GrowthToolType;
import dev.yakekusolsu.growthtools.service.GrowthToolItemService;
import dev.yakekusolsu.growthtools.service.InitialToolMaterialProvider;
import dev.yakekusolsu.growthtools.service.MessageService;
import dev.yakekusolsu.growthtools.service.PluginReloadService;
import dev.yakekusolsu.growthtools.service.ToolAdministrationService;
import dev.yakekusolsu.growthtools.service.ToolRegistryService;
import dev.yakekusolsu.growthtools.ability.AbilityRegistry;
import dev.yakekusolsu.growthtools.ability.AbilityService;
import dev.yakekusolsu.growthtools.integration.IntegrationManager;
import dev.yakekusolsu.growthtools.service.DiagnosticsService;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class GrowthToolsCommand implements CommandExecutor, TabCompleter {
    private static final String RELOAD_PERMISSION = "growthtools.admin.reload";
    private static final String GIVE_PERMISSION = "growthtools.admin.give";
    private static final String INSPECT_PERMISSION = "growthtools.admin.inspect";

    private final JavaPlugin plugin;
    private final MessageService messages;
    private final PluginReloadService reloadService;
    private final GrowthToolItemService itemService;
    private final InitialToolMaterialProvider materialProvider;
    private final ToolRegistryService registryService;
    private final AdminCommandHandler adminHandler;
    private final ToolAdministrationService administrationService;
    private final AbilityCommandHandler abilityHandler;
    private final CompatibilityCommandHandler compatibilityHandler;

    public GrowthToolsCommand(
            JavaPlugin plugin,
            MessageService messages,
            PluginReloadService reloadService,
            GrowthToolItemService itemService,
            InitialToolMaterialProvider materialProvider,
            ToolRegistryService registryService,
            ToolAdministrationService administrationService,
            AbilityRegistry abilityRegistry,
            AbilityService abilityService,
            IntegrationManager integrations,
            DiagnosticsService diagnostics) {
        this.plugin = plugin;
        this.messages = messages;
        this.reloadService = reloadService;
        this.itemService = itemService;
        this.materialProvider = materialProvider;
        this.registryService = registryService;
        this.administrationService = administrationService;
        adminHandler = new AdminCommandHandler(administrationService, messages);
        abilityHandler = new AbilityCommandHandler(
                abilityRegistry, abilityService, itemService, messages);
        compatibilityHandler = new CompatibilityCommandHandler(
                plugin, integrations, diagnostics, messages);
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {
        if (args.length == 0) {
            messages.send(sender, "command.usage");
            return true;
        }

        return Subcommand.parse(args[0])
                .map(subcommand -> execute(sender, subcommand, args))
                .orElseGet(() -> {
                    messages.send(sender, "command.unknown-subcommand");
                    return true;
                });
    }

    private boolean execute(CommandSender sender, Subcommand subcommand, String[] commandArguments) {
        switch (subcommand) {
            case VERSION -> version(sender, commandArguments);
            case RELOAD -> reload(sender, commandArguments);
            case GIVE -> give(sender, commandArguments);
            case INSPECT -> inspect(sender, commandArguments);
            case DEBUG, REPAIR, REGENERATE_ID ->
                    adminHandler.execute(sender, subcommand, commandArguments);
            case ABILITY -> abilityHandler.execute(sender, commandArguments);
            case INTEGRATIONS -> compatibilityHandler.integrations(sender, commandArguments);
            case DOCTOR -> compatibilityHandler.doctor(sender, commandArguments);
        }
        return true;
    }

    private void version(CommandSender sender, String[] args) {
        if (args.length != 1) {
            messages.send(sender, "command.usage");
            return;
        }
        messages.send(sender, "command.version",
                Map.of("version", plugin.getPluginMeta().getVersion()));
    }

    private void reload(CommandSender sender, String[] args) {
        if (!sender.hasPermission(RELOAD_PERMISSION)) {
            messages.send(sender, "command.no-permission");
            return;
        }
        if (args.length != 1) {
            messages.send(sender, "command.usage");
            return;
        }
        try {
            reloadService.reload();
            messages.send(sender, "command.reload-success");
        } catch (IllegalStateException exception) {
            messages.send(sender, "command.reload-failed");
        }
    }

    private void give(CommandSender sender, String[] args) {
        if (!sender.hasPermission(GIVE_PERMISSION)) {
            messages.send(sender, "command.no-permission");
            return;
        }
        if (args.length != 3) {
            messages.send(sender, "command.give-usage");
            return;
        }

        Player target = plugin.getServer().getPlayerExact(args[1]);
        if (target == null) {
            messages.send(sender, "command.player-not-found", Map.of("player", args[1]));
            return;
        }
        GrowthToolType type = GrowthToolType.parse(args[2]).orElse(null);
        if (type == null) {
            messages.send(sender, "command.invalid-type", Map.of("type", args[2]));
            return;
        }

        ItemStack item = new ItemStack(materialProvider.get(type));
        GrowthToolData data = itemService.create(item);
        target.getInventory().addItem(item).values()
                .forEach(leftover -> target.getWorld().dropItemNaturally(target.getLocation(), leftover));
        messages.send(sender, "command.give-success", Map.of(
                "player", target.getName(), "type", type.id()));
        registryService.observe(data, target, true);
        registryService.audit("TOOL_CREATED", data.toolId(), sender instanceof Player player
                        ? player.getUniqueId() : null,
                "Created for " + target.getName(), System.currentTimeMillis(), true);
    }

    private void inspect(CommandSender sender, String[] args) {
        if (!sender.hasPermission(INSPECT_PERMISSION)) {
            messages.send(sender, "command.no-permission");
            return;
        }
        if (!(sender instanceof Player player)) {
            messages.send(sender, "command.player-only");
            return;
        }
        if (args.length != 1) {
            messages.send(sender, "command.inspect-usage");
            return;
        }

        administrationService.inspect(player, false);
    }

    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(java.util.Locale.ROOT);
            return Arrays.stream(Subcommand.values())
                    .filter(value -> canUse(sender, value))
                    .map(Subcommand::argument)
                    .filter(value -> value.startsWith(prefix))
                    .toList();
        }
        if (args.length == 2 && "give".equalsIgnoreCase(args[0])
                && sender.hasPermission(GIVE_PERMISSION)) {
            String prefix = args[1].toLowerCase(java.util.Locale.ROOT);
            return plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase(java.util.Locale.ROOT).startsWith(prefix))
                    .sorted()
                    .toList();
        }
        if (args.length == 3 && "give".equalsIgnoreCase(args[0])
                && sender.hasPermission(GIVE_PERMISSION)) {
            String prefix = args[2].toLowerCase(java.util.Locale.ROOT);
            return Arrays.stream(GrowthToolType.values())
                    .map(GrowthToolType::id)
                    .filter(type -> type.startsWith(prefix))
                    .toList();
        }
        if (args.length == 2 && "doctor".equalsIgnoreCase(args[0])
                && sender.hasPermission(CompatibilityCommandHandler.DOCTOR_PERMISSION)) {
            return "export".startsWith(args[1].toLowerCase(java.util.Locale.ROOT))
                    ? List.of("export") : List.of();
        }
        List<String> adminCompletions = adminHandler.complete(sender, args);
        if (!adminCompletions.isEmpty()) {
            return adminCompletions;
        }
        List<String> abilityCompletions = abilityHandler.complete(sender, args);
        if (!abilityCompletions.isEmpty()) {
            return abilityCompletions;
        }
        return List.of();
    }

    private static boolean canUse(CommandSender sender, Subcommand subcommand) {
        return switch (subcommand) {
            case VERSION -> true;
            case RELOAD -> sender.hasPermission(RELOAD_PERMISSION);
            case GIVE -> sender.hasPermission(GIVE_PERMISSION);
            case INSPECT -> sender.hasPermission(INSPECT_PERMISSION);
            case DEBUG -> sender.hasPermission(AdminCommandHandler.DEBUG_PERMISSION);
            case REPAIR -> sender.hasPermission(AdminCommandHandler.REPAIR_PERMISSION);
            case REGENERATE_ID -> sender.hasPermission(AdminCommandHandler.REGENERATE_PERMISSION);
            case ABILITY -> sender.hasPermission(AbilityCommandHandler.PERMISSION);
            case INTEGRATIONS -> sender.hasPermission(
                    CompatibilityCommandHandler.INTEGRATIONS_PERMISSION);
            case DOCTOR -> sender.hasPermission(CompatibilityCommandHandler.DOCTOR_PERMISSION);
        };
    }
}
