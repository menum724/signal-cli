package org.asamk.signal.autoresponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Manages loading and saving auto-response configuration.
 */
public class AutoResponseConfigManager {

    private static final Logger logger = LoggerFactory.getLogger(AutoResponseConfigManager.class);
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private final File configFile;

    public AutoResponseConfigManager(File dataPath, String accountNumber) {
        this.configFile = new File(dataPath, accountNumber + ".auto-response.json");
    }

    /**
     * Load configuration from file, or return default config if file doesn't exist.
     */
    public AutoResponseConfig load() {
        if (!configFile.exists()) {
            logger.debug("Auto-response config file not found, using defaults: {}", configFile);
            return new AutoResponseConfig();
        }

        try {
            AutoResponseConfig config = objectMapper.readValue(configFile, AutoResponseConfig.class);
            logger.info("Loaded auto-response config from: {}", configFile);
            return config;
        } catch (IOException e) {
            logger.error("Failed to load auto-response config from: " + configFile, e);
            return new AutoResponseConfig();
        }
    }

    /**
     * Save configuration to file.
     */
    public void save(AutoResponseConfig config) throws IOException {
        try {
            // Ensure parent directory exists
            File parent = configFile.getParentFile();
            if (parent != null && !parent.exists()) {
                Files.createDirectories(parent.toPath());
            }

            objectMapper.writeValue(configFile, config);
            logger.info("Saved auto-response config to: {}", configFile);
        } catch (IOException e) {
            logger.error("Failed to save auto-response config to: " + configFile, e);
            throw e;
        }
    }

    /**
     * Get the config file path.
     */
    public Path getConfigPath() {
        return configFile.toPath();
    }

    /**
     * Check if config file exists.
     */
    public boolean exists() {
        return configFile.exists();
    }
}
