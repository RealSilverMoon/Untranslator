package com.silvermoon.Untranslator;

import static com.silvermoon.Untranslator.Util.i18n;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.GregTech_API;
import gregtech.api.enums.Materials;
import unicodefontfixer.ConfigManager;
import unicodefontfixer.FontRendererEx;
import unicodefontfixer.GuiLanguageEx;
import unicodefontfixer.UnicodeFontFixer;

@SideOnly(Side.CLIENT)
public class TooltipEventHandler {

    public TooltipEventHandler() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static String status = "none";

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
        String localizedName = getSecondName(event.itemStack);
        if (localizedName.isEmpty()) return;
        event.toolTip.add(localizedName);
    }

    public String getSecondName(ItemStack stack) {
        String key = stack.getUnlocalizedName();
        // NEI GT Fluid
        if (stack.getItem() != null && stack.getItem().delegate.name()
            .equals("gregtech:gt.GregTech_FluidDisplay")) return EnumChatFormatting.GOLD + key;
        // IC2
        key = key + (key.startsWith("ic2") ? "" : ".name");
        // Core method
        String name = LanguageRegistry.instance()
            .getStringLocalization(key, status);
        // Replace GT Materials
        if (name.contains("%material") && Loader.isModLoaded("gregtech")) {
            if (Util.containsGTKeyword(name)) name = name.replace("%material ", "");
            name = getDefaultLocalizedNameForItem(name, stack.getItemDamage() % 1000);
        }
        return name.isEmpty() ? name : EnumChatFormatting.AQUA + name;
    }

    public static String getDefaultLocalizedNameForItem(String aFormat, int aMaterialID) {
        if (aMaterialID >= 0 && aMaterialID < 1000) {
            Materials aMaterial = GregTech_API.sGeneratedMaterials[aMaterialID];
            if (aMaterial != null) return aMaterial.getDefaultLocalizedNameForItem(aFormat);
        }
        return aFormat;
    }

    public void changeStatus() {
        switch (status) {
            case "none" -> status = "en_US";
            case "en_US" -> status = "ja_JP";
            case "ja_JP" -> status = "none";
        }
        Config.saveStatusChange();
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

            @Override
            public void initGui() {
                super.initGui();
                GuiOptionButton btnUnicode = (GuiOptionButton) this.buttonList.get(0);
                GuiOptionButton btnDone = (GuiOptionButton) this.buttonList.get(1);
                doubleButton = new GuiButton(94, this.width / 2 + 2, this.height - 28, 150, 20, "");
                btnFixFont = new GuiButton(93, this.width / 2 + 2, this.height - 52, 150, 20, "");
                btnDone.yPosition = this.height - 28;
                btnDone.xPosition = this.width / 2 - 150 - 2;
                btnUnicode.yPosition = this.height - 52;
                btnUnicode.xPosition = this.width / 2 - 150 - 2;
                this.buttonList.add(doubleButton);
                this.buttonList.add(btnFixFont);
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

            @Override
            public void initGui() {
                super.initGui();
                GuiOptionButton btnUnicode = (GuiOptionButton) this.buttonList.get(0);
                GuiOptionButton btnDone = (GuiOptionButton) this.buttonList.get(1);
                doubleButton = new GuiButton(94, this.width / 2 + 2, this.height - 52, 150, 20, "");
                btnDone.yPosition = this.height - 28;
                btnDone.xPosition = this.width / 2 - 75;
                btnUnicode.yPosition = this.height - 52;
                btnUnicode.xPosition = this.width / 2 - 150 - 2;
                this.buttonList.add(doubleButton);
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
            }

            @Override
            protected void actionPerformed(GuiButton btn) {
                if (btn.enabled && btn.id == 94) {
                    changeStatus();
                } else super.actionPerformed(btn);
            }
        };
    }
}
