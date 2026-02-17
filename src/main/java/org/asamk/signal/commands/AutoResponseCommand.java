package org.asamk.signal.commands;

import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import org.asamk.signal.autoresponse.AutoResponseConfig;
import org.asamk.signal.autoresponse.AutoResponseConfigManager;
import org.asamk.signal.commands.exceptions.CommandException;
import org.asamk.signal.commands.exceptions.IOErrorException;
import org.asamk.signal.manager.Manager;
import org.asamk.signal.output.OutputWriter;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AutoResponseCommand implements JsonRpcLocalCommand {

    @Override
    public String getName() {
        return "autoResponse";
    }

    @Override
    public void attachToSubparser(final Subparser subparser) {
        subparser.help("Configure AI-powered auto-response for incoming messages");
        
        subparser.addArgument("--enable")
                .action(Arguments.storeTrue())
                .help("Enable auto-response");
        
        subparser.addArgument("--disable")
                .action(Arguments.storeTrue())
                .help("Disable auto-response");
        
        subparser.addArgument("--api-key")
                .help("Set the API key for AI provider (e.g., OpenAI API key)");
        
        subparser.addArgument("--api-endpoint")
                .help("Set the API endpoint URL (default: https://api.openai.com/v1/chat/completions)");
        
        subparser.addArgument("--model")
                .help("Set the AI model to use (e.g., gpt-3.5-turbo, gpt-4)");
        
        subparser.addArgument("--system-prompt")
                .help("Set the system prompt that guides AI behavior");
        
        subparser.addArgument("--fallback-message")
                .help("Set the fallback message when AI is unavailable");
        
        subparser.addArgument("--allow-sender")
                .nargs("*")
                .help("Add phone number(s) to allowed senders list");
        
        subparser.addArgument("--block-sender")
                .nargs("*")
                .help("Add phone number(s) to blocked senders list");
        
        subparser.addArgument("--respond-to-groups")
                .action(Arguments.storeTrue())
                .help("Enable auto-responses to group messages");
        
        subparser.addArgument("--no-respond-to-groups")
                .action(Arguments.storeTrue())
                .help("Disable auto-responses to group messages");
        
        subparser.addArgument("--show-config")
                .action(Arguments.storeTrue())
                .help("Display current auto-response configuration");
    }

    @Override
    public void handleCommand(
            final Namespace ns,
            final Manager m,
            final OutputWriter outputWriter
    ) throws CommandException {
        // Get data path from environment or use default
        var dataPath = getDataPath();
        var configManager = new AutoResponseConfigManager(dataPath, m.getSelfNumber());

        var config = configManager.load();
        boolean modified = false;

        // Handle enable/disable
        if (Boolean.TRUE.equals(ns.getBoolean("enable"))) {
            config.setEnabled(true);
            modified = true;
            println(outputWriter, "Auto-response enabled");
        }
        
        if (Boolean.TRUE.equals(ns.getBoolean("disable"))) {
            config.setEnabled(false);
            modified = true;
            println(outputWriter, "Auto-response disabled");
        }

        // Handle API settings
        String apiKey = ns.getString("api-key");
        if (apiKey != null) {
            config.setApiKey(apiKey);
            modified = true;
            println(outputWriter, "API key updated");
        }

        String apiEndpoint = ns.getString("api-endpoint");
        if (apiEndpoint != null) {
            config.setApiEndpoint(apiEndpoint);
            modified = true;
            println(outputWriter, "API endpoint updated: " + apiEndpoint);
        }

        String model = ns.getString("model");
        if (model != null) {
            config.setModel(model);
            modified = true;
            println(outputWriter, "Model updated: " + model);
        }

        String systemPrompt = ns.getString("system-prompt");
        if (systemPrompt != null) {
            config.setSystemPrompt(systemPrompt);
            modified = true;
            println(outputWriter, "System prompt updated");
        }

        String fallbackMessage = ns.getString("fallback-message");
        if (fallbackMessage != null) {
            config.setFallbackMessage(fallbackMessage);
            modified = true;
            println(outputWriter, "Fallback message updated");
        }

        // Handle sender lists
        List<String> allowedSenders = ns.getList("allow-sender");
        if (allowedSenders != null && !allowedSenders.isEmpty()) {
            config.getAllowedSenders().addAll(allowedSenders);
            modified = true;
            println(outputWriter, "Added to allowed senders: " + String.join(", ", allowedSenders));
        }

        List<String> blockedSenders = ns.getList("block-sender");
        if (blockedSenders != null && !blockedSenders.isEmpty()) {
            config.getBlockedSenders().addAll(blockedSenders);
            modified = true;
            println(outputWriter, "Added to blocked senders: " + String.join(", ", blockedSenders));
        }

        // Handle group responses
        if (Boolean.TRUE.equals(ns.getBoolean("respond-to-groups"))) {
            config.setRespondToGroups(true);
            modified = true;
            println(outputWriter, "Group responses enabled");
        }
        
        if (Boolean.TRUE.equals(ns.getBoolean("no-respond-to-groups"))) {
            config.setRespondToGroups(false);
            modified = true;
            println(outputWriter, "Group responses disabled");
        }

        // Save config if modified
        if (modified) {
            try {
                configManager.save(config);
                println(outputWriter, "Configuration saved to: " + configManager.getConfigPath());
            } catch (IOException e) {
                throw new IOErrorException("Failed to save configuration: " + e.getMessage(), e);
            }
        }

        // Show config if requested or if no changes made
        if (Boolean.TRUE.equals(ns.getBoolean("show-config")) || !modified) {
            showConfig(config, configManager, outputWriter);
        }
    }

    private File getDataPath() {
        // Use XDG_DATA_HOME if set, otherwise use default
        String xdgDataHome = System.getenv("XDG_DATA_HOME");
        if (xdgDataHome != null && !xdgDataHome.isBlank()) {
            return new File(new File(xdgDataHome, "signal-cli"), "data");
        }
        
        // Fallback to ~/.local/share/signal-cli/data
        String userHome = System.getProperty("user.home");
        return new File(new File(new File(new File(userHome, ".local"), "share"), "signal-cli"), "data");
    }

    private void println(OutputWriter outputWriter, String message) {
        if (outputWriter instanceof org.asamk.signal.output.PlainTextWriter writer) {
            writer.println(message);
        }
    }

    private void showConfig(AutoResponseConfig config, AutoResponseConfigManager configManager, OutputWriter outputWriter) {
        if (!(outputWriter instanceof org.asamk.signal.output.PlainTextWriter writer)) {
            return;
        }
        
        writer.println("\n=== Auto-Response Configuration ===");
        writer.println("Config file: " + configManager.getConfigPath());
        writer.println("Enabled: " + config.isEnabled());
        writer.println("AI Provider: " + config.getAiProvider());
        writer.println("API Endpoint: " + config.getApiEndpoint());
        writer.println("Model: " + config.getModel());
        writer.println("API Key: " + (config.getApiKey().isBlank() ? "(not set)" : "***" + config.getApiKey().substring(Math.max(0, config.getApiKey().length() - 4))));
        writer.println("Respond to Groups: " + config.isRespondToGroups());
        writer.println("Fallback Message: " + config.getFallbackMessage());
        writer.println("Use Fallback on Error: " + config.isUseFallbackOnError());
        writer.println("Send Typing Indicator: " + config.isSendTypingIndicator());
        writer.println("Send Greeting on First Message: " + config.isSendGreetingOnFirstMessage());
        writer.println("Identify as Bot: " + config.isIdentifyAsBot());
        writer.println("Use Natural Delay: " + config.isUseNaturalDelay());
        
        if (!config.getAllowedSenders().isEmpty()) {
            writer.println("Allowed Senders: " + String.join(", ", config.getAllowedSenders()));
        } else {
            writer.println("Allowed Senders: (all, except blocked)");
        }
        
        if (!config.getBlockedSenders().isEmpty()) {
            writer.println("Blocked Senders: " + String.join(", ", config.getBlockedSenders()));
        } else {
            writer.println("Blocked Senders: (none)");
        }
        
        writer.println("\nSystem Prompt:");
        writer.println("  " + config.getSystemPrompt());
        
        if (config.isIdentifyAsBot()) {
            writer.println("\nBot Signature:");
            writer.println("  " + config.getBotSignature());
        }
    }
}
