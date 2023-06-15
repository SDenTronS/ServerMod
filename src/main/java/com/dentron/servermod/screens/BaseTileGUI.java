package com.dentron.servermod.screens;

import com.dentron.servermod.ServerMod;
import com.dentron.servermod.network.ActivateBase;
import com.dentron.servermod.tileentities.BaseTile;
import com.dentron.servermod.utils.ModConstants;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.IOException;


public class BaseTileGUI extends GuiScreen {
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(ServerMod.MODID, "textures/gui/base_gui.png");
    private final BaseTile tile;
    private int leftPos, topPos;
    private final int imageWidth = 226;
    private final int imageHeight = 56;
    private GuiButton activateButton;

    public BaseTileGUI(BaseTile tile){
        this.tile = tile;

    }

    @Override
    public void initGui() {
        super.initGui();
        leftPos = (width - imageWidth) / 2;
        topPos = (height - imageHeight) / 2;

        TextComponentTranslation buttonText = new TextComponentTranslation("gui.servermod.activate_button");

        addButton(activateButton = new GuiButton(0, width / 2 - 90 / 2, topPos + 29, 90 , 20, buttonText.getFormattedText()));
        activateButton.enabled = true;

    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(GUI_TEXTURE);
        drawTexturedModalRect(leftPos, topPos, 0, 0, imageWidth, imageHeight);


        int maxFill = 204;
        int pbHeight = 6;
        drawTexturedModalRect(leftPos + 11, topPos + 18, 0, 250,Math.round((float) maxFill / ModConstants.BASE_TIMER * tile.getTime()), pbHeight); // PROGRESS BAR


        String remaining_time = tile.getTimer().toString();


        fontRenderer.drawString(remaining_time, (width - fontRenderer.getStringWidth(remaining_time)) / 2, topPos + 7, 4210752);


        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == activateButton.id){
            ServerMod.network.sendToServer(new ActivateBase(tile.getPos()));
        }
        super.actionPerformed(button);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        tile.is_GUI_Open = false;
        super.onGuiClosed();
    }

}
