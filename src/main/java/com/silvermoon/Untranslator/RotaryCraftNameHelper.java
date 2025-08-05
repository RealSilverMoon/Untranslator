package com.silvermoon.Untranslator;

import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.ModRegistry.ModOreList;
import Reika.RotaryCraft.Auxiliary.OldTextureLoader;
import Reika.RotaryCraft.Items.Tools.ItemEngineUpgrade;
import Reika.RotaryCraft.Registry.GearboxTypes;
import Reika.RotaryCraft.Registry.ItemRegistry;
import Reika.RotaryCraft.Registry.MachineRegistry;
import Reika.RotaryCraft.RotaryNames;
import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.item.ItemStack;

import java.util.Locale;

import static Reika.RotaryCraft.Registry.MachineRegistry.MAGNETIC;
import static Reika.RotaryCraft.RotaryNames.*;

public class RotaryCraftNameHelper {

    public String getRotaryCraftName(ItemStack stack) {
        ItemRegistry entry = ItemRegistry.getEntry(stack);
        if (entry != null) {
            if (entry.hasMultiValuedName())
                return this.getMultiValuedName(entry, stack.getItemDamage());
        }
            return "";
    }

    public String getMultiValuedName(ItemRegistry entry,int dmg) {
        if (entry.isCharged())
            return getSecondBaseName(entry)+" ("+String.format("%d", dmg)+" kJ)";
        return switch (entry) {
            case SLIDE -> getSecondBaseName(entry) + " (" + dmg + ")";
            case SPRING, STRONGCOIL -> getSecondBaseName(entry) + " (" + String.format("%d", dmg) + " kJ)";
            case BUCKET -> getBucketName(dmg);
            case RAILGUN ->
                getSecondBaseName(entry) + " (" + String.format("%d", (int) ReikaMathLibrary.intpow(2, dmg)) + " kg)";
            case UPGRADE -> LanguageRegistry.instance().getStringLocalization(ItemEngineUpgrade.Upgrades.list[dmg].getOriginalName(), "en_US");
            case MODEXTRACTS -> getModExtractName(dmg);
            case MODINGOTS -> getModIngotName(dmg);
            case SHAFTCRAFT -> LanguageRegistry.instance().getStringLocalization(RotaryNames.shaftPartNames[dmg], "en_US");
            case MISCCRAFT -> LanguageRegistry.instance().getStringLocalization(RotaryNames.miscPartNames[dmg], "en_US");
            case BORECRAFT -> LanguageRegistry.instance().getStringLocalization(RotaryNames.borerPartNames[dmg], "en_US");
            case ENGINECRAFT -> LanguageRegistry.instance().getStringLocalization(RotaryNames.enginePartNames[dmg], "en_US");
            case EXTRACTS -> LanguageRegistry.instance().getStringLocalization(RotaryNames.extractNames[dmg], "en_US");
            case COMPACTS -> LanguageRegistry.instance().getStringLocalization(RotaryNames.compactNames[dmg], "en_US");
            case POWDERS -> LanguageRegistry.instance().getStringLocalization(RotaryNames.powderNames[dmg], "en_US");
            case MODINTERFACE -> LanguageRegistry.instance().getStringLocalization(RotaryNames.interfaceNames[dmg], "en_US");
            case GEARCRAFT -> getGearPartName(dmg);
            case FLYWHEELCRAFT -> LanguageRegistry.instance().getStringLocalization("flywheel."+entry.name().toLowerCase(Locale.ENGLISH)) + " " + getSecondBaseName(entry);
            case SHAFT, GEARBOX -> getSecondBaseName(entry);
            case ENGINE -> getEngineName(dmg);
            case FLYWHEEL -> LanguageRegistry.instance().getStringLocalization("flywheel."+entry.name().toLowerCase(Locale.ENGLISH)) + " " + (OldTextureLoader.instance.loadOldTextures() ?
                OldTextureLoader.instance.getMagnetostaticName() : LanguageRegistry.instance().getStringLocalization(MachineRegistry.FLYWHEEL.getOriginalName(),"en_US"));
            case ADVGEAR -> getAdvGearName(dmg);
            case MACHINE -> getMachineName(dmg);
            case CANOLA -> getCanolaName(dmg);
            default -> "";
        };
    }
    public String getSecondBaseName(ItemRegistry itemRegistry){
        return LanguageRegistry.instance().getStringLocalization(itemRegistry.getOriginalName(), "en_US");
    }
    public String getBucketName(int i) {
        String liq = LanguageRegistry.instance().getStringLocalization(liquidNames[i], "en_US");
        String item = LanguageRegistry.instance().getStringLocalization("item.rcbucket", "en_US");
        return liq + " " + item;
    }
    public String getGearPartName(int dmg) {
        ItemStack is = ItemRegistry.GEARCRAFT.getStackOfMetadata(dmg);
        GearboxTypes material = GearboxTypes.getMaterialFromCraftingItem(is);
        String s = switch (GearboxTypes.GearPart.list[is.getItemDamage() % 16]) {
            case SHAFT -> "crafting.shaft";
            case GEAR -> "crafting.gear";
            case UNIT2 -> "crafting.gear2x";
            case UNIT4 -> "crafting.gear4x";
            case UNIT8 -> "crafting.gear8x";
            case UNIT16 -> "crafting.gear16x";
            case BEARING -> "crafting.bearing";
            case SHAFTCORE -> "crafting.shaftcore";
        };

        return LanguageRegistry.instance().getStringLocalization("material." + material.name().toLowerCase(Locale.ENGLISH),"en_US")
            + " " + LanguageRegistry.instance().getStringLocalization(s,"en_US");

    }
    public String getEngineName(int i) {
        return LanguageRegistry.instance().getStringLocalization(engineNames[i],"en_US");
    }
    public String getAdvGearName(int i) {
        return LanguageRegistry.instance().getStringLocalization(advGearItemNames[i],"en_US");
    }
    public String getMachineName(int i){
        MachineRegistry machine = MachineRegistry.machineList.get(i);
        return OldTextureLoader.instance.loadOldTextures() && machine == MAGNETIC ?
            OldTextureLoader.instance.getMagnetostaticName() : LanguageRegistry.instance().getStringLocalization(machine.getOriginalName(),"en_US");
    }
    public String getCanolaName(int i){
        return LanguageRegistry.instance().getStringLocalization(canolaNames[i],"en_US");
    }
    public String getModExtractName(int i) {
        String base = ModOreList.oreList[i / 4].displayName;
        return switch (i % 4) {
            case 0 -> base + " Dust";
            case 1 -> base + " Slurry";
            case 2 -> base + " Solution";
            case 3 -> base + " Flakes";
            default -> base;
        };
    }
    public String getModIngotName(int i) {
        return ModOreList.oreList[i].displayName + " " + ModOreList.oreList[i].getTypeName();
    }
}
