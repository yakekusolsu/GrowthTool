package dev.yakekusolsu.growthtools.command;

import dev.yakekusolsu.growthtools.integration.IntegrationManager;
import dev.yakekusolsu.growthtools.service.DiagnosticsService;
import dev.yakekusolsu.growthtools.service.MessageService;
import java.util.Map;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public final class CompatibilityCommandHandler {
    public static final String INTEGRATIONS_PERMISSION = "growthtools.admin.integrations";
    public static final String DOCTOR_PERMISSION = "growthtools.admin.doctor";
    private final Plugin plugin;
    private final IntegrationManager integrations;
    private final DiagnosticsService diagnostics;
    private final MessageService messages;
    public CompatibilityCommandHandler(Plugin plugin, IntegrationManager integrations,
            DiagnosticsService diagnostics, MessageService messages) {
        this.plugin = plugin; this.integrations = integrations;
        this.diagnostics = diagnostics; this.messages = messages;
    }
    public void integrations(CommandSender sender, String[] args) {
        if (!sender.hasPermission(INTEGRATIONS_PERMISSION)) {
            messages.send(sender, "command.no-permission"); return;
        }
        if (args.length != 1) { messages.send(sender, "command.usage"); return; }
        String report = integrations.snapshots().stream()
                .map(value -> value.pluginName() + ": " + value.state() + " - " + value.detail())
                .collect(java.util.stream.Collectors.joining("\n"));
        messages.send(sender, "integrations.report", Map.of("report", report));
    }
    public void doctor(CommandSender sender, String[] args) {
        if (!sender.hasPermission(DOCTOR_PERMISSION)) {
            messages.send(sender, "command.no-permission"); return;
        }
        if (args.length == 1) {
            diagnostics.report().whenComplete((report, error) -> onMain(() -> {
                if (error == null) messages.send(sender, "doctor.report", Map.of("report", report));
                else messages.send(sender, "doctor.failed");
            }));
        } else if (args.length == 2 && "export".equalsIgnoreCase(args[1])) {
            diagnostics.export().whenComplete((file, error) -> onMain(() -> {
                if (error == null) messages.send(sender, "doctor.exported",
                        Map.of("file", file.toAbsolutePath().toString()));
                else messages.send(sender, "doctor.failed");
            }));
        } else messages.send(sender, "doctor.usage");
    }
    private void onMain(Runnable action) {
        if (plugin.isEnabled()) plugin.getServer().getScheduler().runTask(plugin, action);
    }
}
