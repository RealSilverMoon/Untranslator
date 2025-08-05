package com.silvermoon.Untranslator;

import static com.silvermoon.Untranslator.Util.i18n;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Locale;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import com.google.common.collect.Lists;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.Materials;
import unicodefontfixer.ConfigManager;
import unicodefontfixer.FontRendererEx;
import unicodefontfixer.GuiLanguageEx;
import unicodefontfixer.UnicodeFontFixer;

@SideOnly(Side.CLIENT)
public class TooltipEventHandler {

    public TooltipEventHandler() {
        MinecraftForge.EVENT_BUS.register(this);
        EN_US.loadLocaleDataFiles(
            Minecraft.getMinecraft()
                .getResourceManager(),
            Lists.newArrayList("en_US"));
        JA_JP.loadLocaleDataFiles(
            Minecraft.getMinecraft()
                .getResourceManager(),
            Lists.newArrayList("ja_JP"));
        if(Loader.isModLoaded("RotaryCraft")) rotaryCraftNameHelper=new RotaryCraftNameHelper();
    }

    public static String status = "none";
    public static String location = "name";
    public static final Locale EN_US = new Locale();
    public static final Locale JA_JP = new Locale();
    public static Configuration GTSecondLangFile;
    private RotaryCraftNameHelper rotaryCraftNameHelper;

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTooltip(ItemTooltipEvent event) {
        if (status.equals("none")) return;
        if (status.equals(
            Minecraft.getMinecraft()
                .getLanguageManager()
                .getCurrentLanguage()
                .getLanguageCode()))
            return;
        if (event == null || event.itemStack == null || event.itemStack.getItem() == null) return;
        String localizedName = getSecondName(event.itemStack).replaceAll("molten\\.|item\\.|tile\\.|Empty ", "");
        if (localizedName.isEmpty()) return;
        switch (location) {
            case "name" -> {
                if (event.itemStack.getItem().delegate.name()
                    .equals("ae2fc:fluid_drop")) {
                    event.toolTip.add(localizedName);
                    return;
                }
                if (event.itemStack.getDisplayName()
                    .contains(localizedName)) {
                    event.toolTip.set(
                        0,
                        event.toolTip.get(0)
                            .replaceFirst("§o", ""));
                    return;
                }
                event.itemStack
                    .setStackDisplayName(event.itemStack.getDisplayName() + " §r(%§r)".replace("%", localizedName));
            }
            case "below" -> {
                removeName(event.itemStack, localizedName);
                event.toolTip.add(1, localizedName);
            }
            case "bottom" -> {
                removeName(event.itemStack, localizedName);
                event.toolTip.add(localizedName);
            }
        }
    }

    public String getSecondName(ItemStack stack) {
        String key = stack.getUnlocalizedName();
        String secondName = "";
        // NEI GT Fluid,GT,GT++
        if (stack.getItem() != null) {
            String name = stack.getItem().delegate.name();
            if (name.equals("gregtech:gt.GregTech_FluidDisplay")) {
                String nbtKey = "";
                if (stack.stackTagCompound != null) {
                    nbtKey = stack.stackTagCompound.getString("mFluidMaterialName");
                }
                return EnumChatFormatting.GOLD + (nbtKey.isEmpty() ? key : nbtKey);
            } else if (name.equals("ae2fc:fluid_drop")) {
                String nbtKey = "";
                if (stack.stackTagCompound != null) {
                    nbtKey = stack.stackTagCompound.getString("Fluid");
                }
                return EnumChatFormatting.GOLD + (nbtKey.isEmpty() ? key : nbtKey);
            } else if (name.contains("gregtech:")) {
                secondName = GTSecondLangFile == null ? i18n("Untranslator.error")
                    : GTSecondLangFile.get("LanguageFile", key + ".name", "")
                        .getString();
            } else if (name.contains("miscutils:")) {
                secondName = GTSecondLangFile == null ? i18n("Untranslator.error")
                    : GTSecondLangFile.get("LanguageFile", "gtplusplus." + key + ".name", "")
                        .getString();
            }else if(name.contains("RotaryCraft") && rotaryCraftNameHelper!=null){
                secondName=rotaryCraftNameHelper.getRotaryCraftName(stack);
            }
        }
        // Core method
        if (secondName.isEmpty()) secondName = LanguageRegistry.instance()
            .getStringLocalization(key, status);
        if (secondName.isEmpty()) secondName = LanguageRegistry.instance()
            .getStringLocalization(key + ".name", status);
        // Replace GT Materials
        if (secondName.contains("%material") && Loader.isModLoaded("gregtech")) {
            secondName = replaceMaterials(secondName, stack.getItemDamage() % 1000);
        }
        if (secondName.isEmpty()) {
            switch (status) {
                case "en_US" -> secondName = EN_US.formatMessage(key, new Object[0]);
                case "ja_JP" -> secondName = JA_JP.formatMessage(key, new Object[0]);
            }
        }
        return secondName.isEmpty() ? secondName : EnumChatFormatting.AQUA + secondName;
    }

    public static String replaceMaterials(String aFormat, int aMaterialID) {
        if (aMaterialID >= 0 && aMaterialID < 1000) {
            Materials aMaterial = GregTechAPI.sGeneratedMaterials[aMaterialID];
            return aFormat.replaceAll(
                "%material |%material",
                aMaterial == null ? ""
                    : GTSecondLangFile.get("LanguageFile", "Material." + aMaterial.mName.toLowerCase(), "")
                        .getString());
        }
        return aFormat;
    }

    public void removeName(ItemStack itemStack, String localizedName) {
        if (!itemStack.hasDisplayName()) return;
        if (!itemStack.getDisplayName()
            .contains(localizedName)) return;
        String s = itemStack.getDisplayName()
            .replace(" §r(%§r)".replace("%", localizedName), "");
        if (s.equals(
            itemStack.getItem()
                .getItemStackDisplayName(itemStack))) {
            itemStack.stackTagCompound.removeTag("display");
        } else {
            itemStack.setStackDisplayName(s);
        }
    }

    public void changeStatus() {
        switch (status) {
            case "none" -> status = "en_US";
            case "en_US" -> status = "ja_JP";
            case "ja_JP" -> status = "none";
        }
        Util.loadGTLangFile();
        Config.saveChange();
    }

    public void changeLocation() {
        switch (location) {
            case "name" -> location = "below";
            case "below" -> location = "bottom";
            case "bottom" -> location = "name";
        }
        Config.saveChange();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    @SideOnly(Side.CLIENT)
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui instanceof GuiLanguage) {
            if (Loader.isModLoaded("UnicodeFontFixer")) {
                tryUseUnicodeGUI(event);
            } else tryUseMyGUI(event);
        }
    }

    /**
     * @author youyihj
     */
    @Optional.Method(modid = "UnicodeFontFixer")
    private void tryUseUnicodeGUI(GuiOpenEvent e) {
        e.gui = new GuiLanguageEx((GuiLanguage) e.gui) {

            public GuiButton doubleButton;
            public GuiButton locationButton;

            @Override
            public void initGui() {
                super.initGui();
                GuiOptionButton btnUnicode = (GuiOptionButton) this.buttonList.get(0);
                GuiOptionButton btnDone = (GuiOptionButton) this.buttonList.get(1);
                doubleButton = new GuiButton(94, this.width / 2 + 2, this.height - 28, 150, 20, "");
                locationButton = new GuiButton(95, this.width / 2 + 154, this.height - 28, 40, 20, "");
                btnFixFont = new GuiButton(93, this.width / 2 + 2, this.height - 52, 150, 20, "");
                btnDone.yPosition = this.height - 28;
                btnDone.xPosition = this.width / 2 - 150 - 2;
                btnUnicode.yPosition = this.height - 52;
                btnUnicode.xPosition = this.width / 2 - 150 - 2;
                this.buttonList.add(doubleButton);
                this.buttonList.add(btnFixFont);
                this.buttonList.add(locationButton);
            }

            @Override
            public void drawScreen(int mouseX, int mouseY, float partialTicks) {
                GuiSlot langlist = ObfuscationReflectionHelper
                    .getPrivateValue(GuiLanguage.class, this, "field_146450_f");
                langlist.drawScreen(mouseX, mouseY, partialTicks);
                this.drawCenteredString(
                    this.fontRendererObj,
                    I18n.format("options.language"),
                    this.width / 2,
                    16,
                    16777215);
                updateButtonText();
                for (Object o : this.buttonList) ((GuiButton) o).drawButton(this.mc, mouseX, mouseY);
                for (Object o : this.labelList) ((GuiLabel) o).func_146159_a(this.mc, mouseX, mouseY);
            }

            private void updateButtonText() {
                btnFixFont.displayString = i18n("options.unicodefontfixer.fixDerpyFont") + ": "
                    + i18n(
                        "options.unicodefontfixer.fixDerpyFont."
                            + UnicodeFontFixer.instance.configManager.fixDerpyFont.getString());
                doubleButton.displayString = i18n("Untranslator.display_second") + i18n("Untranslator." + status);
                locationButton.displayString = i18n("Untranslator." + location);
            }

            @Override
            protected void actionPerformed(GuiButton btn) {
                if (btn.enabled && btn.id == 93) {
                    ConfigManager cm = UnicodeFontFixer.instance.configManager;
                    if (cm.fixDerpyFont.getString()
                        .equals("always")) {
                        cm.fixDerpyFont.set("moderate");
                        FontRendererEx.policy = 2;
                    } else if (cm.fixDerpyFont.getString()
                        .equals("moderate")) {
                            cm.fixDerpyFont.set("disabled");
                            FontRendererEx.policy = 0;
                        } else {
                            cm.fixDerpyFont.set("always");
                            FontRendererEx.policy = 1;
                        }
                    cm.update();
                } else if (btn.enabled && btn.id == 94) {
                    changeStatus();
                } else if (btn.enabled && btn.id == 95) {
                    changeLocation();
                } else super.actionPerformed(btn);
            }
        };
    }

    private void tryUseMyGUI(GuiOpenEvent e) {
        e.gui = new GuiLanguage(
            ObfuscationReflectionHelper
                .getPrivateValue(GuiLanguage.class, (GuiLanguage) e.gui, new String[] { "field_146453_a" }),
            ObfuscationReflectionHelper
                .getPrivateValue(GuiLanguage.class, (GuiLanguage) e.gui, new String[] { "field_146451_g" }),
            ObfuscationReflectionHelper
                .getPrivateValue(GuiLanguage.class, (GuiLanguage) e.gui, new String[] { "field_146454_h" })) {

            public GuiButton doubleButton;
            public GuiButton locationButton;

            @Override
            public void initGui() {
                super.initGui();
                GuiOptionButton btnUnicode = (GuiOptionButton) this.buttonList.get(0);
                GuiOptionButton btnDone = (GuiOptionButton) this.buttonList.get(1);
                doubleButton = new GuiButton(94, this.width / 2 + 2, this.height - 52, 150, 20, "");
                locationButton = new GuiButton(95, this.width / 2 + 154, this.height - 52, 40, 20, "");
                btnDone.yPosition = this.height - 28;
                btnDone.xPosition = this.width / 2 - 75;
                btnUnicode.yPosition = this.height - 52;
                btnUnicode.xPosition = this.width / 2 - 150 - 2;
                this.buttonList.add(doubleButton);
                this.buttonList.add(locationButton);
            }

            @Override
            public void drawScreen(int mouseX, int mouseY, float partialTicks) {
                GuiSlot langlist = ObfuscationReflectionHelper
                    .getPrivateValue(GuiLanguage.class, this, "field_146450_f");
                langlist.drawScreen(mouseX, mouseY, partialTicks);
                this.drawCenteredString(
                    this.fontRendererObj,
                    I18n.format("options.language"),
                    this.width / 2,
                    16,
                    16777215);
                updateButtonText();
                for (Object o : this.buttonList) ((GuiButton) o).drawButton(this.mc, mouseX, mouseY);
                for (Object o : this.labelList) ((GuiLabel) o).func_146159_a(this.mc, mouseX, mouseY);
            }

            private void updateButtonText() {
                doubleButton.displayString = i18n("Untranslator.display_second") + i18n("Untranslator." + status);
                locationButton.displayString = i18n("Untranslator." + location);
            }

            @Override
            protected void actionPerformed(GuiButton btn) {
                if (btn.enabled && btn.id == 94) {
                    changeStatus();
                } else if (btn.enabled && btn.id == 95) {
                    changeLocation();
                } else super.actionPerformed(btn);
            }
        };
    }
}
