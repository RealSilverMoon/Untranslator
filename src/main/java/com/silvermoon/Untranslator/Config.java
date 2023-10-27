package com.silvermoon.Untranslator;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class Config {

    public static Configuration config;
    public static Property configStatus;

    public static void synchronizeConfiguration(File configFile) {
        config = new Configuration(configFile);
        config.load();
        configStatus = config.get(Configuration.CATEGORY_GENERAL, "language", "none", "Second language");
        TooltipEventHandler.status = configStatus.getString();
        saveConfig();
    }

    public static void saveStatusChange() {
        configStatus.set(TooltipEventHandler.status);
        saveConfig();
    }

    public static void saveConfig() {
        if (config.hasChanged()) {
            config.save();
        }
    }
}
