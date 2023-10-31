package com.silvermoon.Untranslator;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class Config {

    public static Configuration config;
    public static Property configStatus;
    public static Property configLocation;

    public static void synchronizeConfiguration(File configFile) {
        config = new Configuration(configFile);
        config.load();
        configStatus = config.get(Configuration.CATEGORY_GENERAL, "language", "none", "Second language");
        TooltipEventHandler.status = configStatus.getString();
        configLocation = config.get(Configuration.CATEGORY_GENERAL, "location", "name", "Second location");
        TooltipEventHandler.location = configLocation.getString();
        saveConfig();
    }

    public static void saveChange() {
        configStatus.set(TooltipEventHandler.status);
        configLocation.set(TooltipEventHandler.location);
        saveConfig();
    }

    public static void saveConfig() {
        if (config.hasChanged()) {
            config.save();
        }
    }
}
