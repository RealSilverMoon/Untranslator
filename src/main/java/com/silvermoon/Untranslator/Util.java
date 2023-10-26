package com.silvermoon.Untranslator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.util.StatCollector;

public class Util {

    public static String i18n(String info) {
        return StatCollector.translateToLocal(info);
    }

    public static boolean containsGTKeyword(String input) {
        String regex = "Cable|Frame|Fluid Pipe|Block of";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        return matcher.find();
    }
}
