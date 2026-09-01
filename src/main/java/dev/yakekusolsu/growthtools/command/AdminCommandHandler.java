package dev.yakekusolsu.growthtools.command;

import dev.yakekusolsu.growthtools.service.MessageService;
import dev.yakekusolsu.growthtools.service.ToolAdministrationService;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Parses Phase 4 administrator commands and delegates all work to its service. */
public final class AdminCommandHandler {
    public static final String DEBUG_PERMISSION = "growthtools.admin.debug";
    public static final String REPAIR_PERMISSION = "growthtools.admin.repair";
    public static final String REGENERATE_PERMISSION = "growthtools.admin.regenerateid";

    private final ToolAdministrationService administrationService;
    private final MessageService messages;

    public AdminCommandHandler(
            ToolAdministrationService administrationService, MessageService messages) {
        this.administrationService = administrationService;
        this.messages = messages;
    }

    public void execute(CommandSender sender, Subcommand subcommand, String[] args) {
        switch (subcommand) {
            case DEBUG -> debug(sender, args);
            case REPAIR -> withPlayer(sender, args, REPAIR_PERMISSION,
                    administrationService::repair);
            case REGENERATE_ID -> withPlayer(sender, args, REGENERATE_PERMISSION,
                    administrationService::regenerateId);
            default -> throw new IllegalArgumentException("Not an administrator subcommand");
        }
    }

    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 2 && "debug".equalsIgnoreCase(args[0])
                && sender.hasPermission(DEBUG_PERMISSION)) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            return List.of("tool", "registry", "database", "add-level").stream()
                    .filter(value -> value.startsWith(prefix))
                    .toList();
        }
        if (args.length == 3 && "debug".equalsIgnoreCase(args[0])
                && "add-level".equalsIgnoreCase(args[1])
                && sender.hasPermission(DEBUG_PERMISSION)) {
            String prefix = args[2].toLowerCase(Locale.ROOT);
            return List.of("1", "5", "10").stream()
                    .filter(value -> value.startsWith(prefix))
                    .toList();
        }
        return List.of();
    }

    private void debug(CommandSender sender, String[] args) {
        if (!sender.hasPermission(DEBUG_PERMISSION)) {
            messages.send(sender, "command.no-permission");
            return;
        }
        if (args.length == 2 && "tool".equalsIgnoreCase(args[1])) {
            if (sender instanceof Player player) {
                administrationService.inspect(player, true);
            } else {
                messages.send(sender, "command.player-only");
            }
            return;
        }
        if (args.length == 2 && "database".equalsIgnoreCase(args[1])) {
            administrationService.debugDatabase(sender);
            return;
        }
        if (args.length == 3 && "add-level".equalsIgnoreCase(args[1])) {
            addLevel(sender, args[2]);
            return;
        }
        if (args.length == 3 && "registry".equalsIgnoreCase(args[1])) {
            try {
                administrationService.debugRegistry(sender, UUID.fromString(args[2]));
            } catch (IllegalArgumentException exception) {
                messages.send(sender, "admin.invalid-uuid");
            }
            return;
        }
        messages.send(sender, "admin.debug-usage");
    }

    private void addLevel(CommandSender sender, String value) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "command.player-only");
            return;
        }
        try {
            int levels = Integer.parseInt(value);
            if (levels <= 0) {
                throw new NumberFormatException("Level count is not positive");
            }
            administrationService.addDebugLevels(player, levels);
        } catch (NumberFormatException exception) {
            messages.send(sender, "admin.debug-level-invalid");
        }
    }

    private void withPlayer(
            CommandSender sender,
            String[] args,
            String permission,
            java.util.function.Consumer<Player> operation) {
        if (!sender.hasPermission(permission)) {
            messages.send(sender, "command.no-permission");
        } else if (args.length != 1) {
            messages.send(sender, "command.usage");
        } else if (sender instanceof Player player) {
            operation.accept(player);
        } else {
            messages.send(sender, "command.player-only");
        }
    }
}
