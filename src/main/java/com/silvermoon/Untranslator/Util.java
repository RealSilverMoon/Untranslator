package com.silvermoon.Untranslator;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.util.StatCollector;
import net.minecraftforge.common.config.Configuration;

import cpw.mods.fml.relauncher.FMLInjectionData;

public class Util {

    public static String i18n(String info) {
        return StatCollector.translateToLocal(info);
    }

    public static void loadGTLangFile() {
        TooltipEventHandler.GTSecondLangFile = null;
        File GT_Lang = new File((File) FMLInjectionData.data()[6], "GregTech_" + TooltipEventHandler.status + ".lang");
        if (GT_Lang.exists()) {
            TooltipEventHandler.GTSecondLangFile = new Configuration(GT_Lang);
            TooltipEventHandler.GTSecondLangFile.load();
        }
    }
}
