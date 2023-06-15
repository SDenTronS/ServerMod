package com.dentron.servermod.screens;

import com.dentron.servermod.SMEventHandler;
import com.dentron.servermod.ServerMod;
import com.dentron.servermod.teams.PlayerStatsProvider;
import com.dentron.servermod.timers.TimerUpdate;
import com.dentron.servermod.utils.CapUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.Display;


@SideOnly(Side.CLIENT)
public class NoneBaseGUI extends Gui{

    private final ResourceLocation texture = new ResourceLocation(ServerMod.MODID, "textures/gui/widgets1.png");
    private final int imageWidth = 9;
    private final int imageHeight = 9;
    private final Minecraft mc;
    private static boolean visible;

    public NoneBaseGUI(){
        mc = Minecraft.getMinecraft();
        visible = true;

    }
    public static void toggleVisibility(){
        visible=!visible;
    }

    public static void changeVisibility(boolean flag) {
        visible = flag;
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRender(final RenderGameOverlayEvent.Post event) {


        String newTitle = SMEventHandler.getAndResetChangedWindowTitle();
        if (newTitle != null)
            Display.setTitle(newTitle);

        if (!visible ||  event.isCanceled() ||  mc.gameSettings.debugCamEnable
                ||  event.getType()!=RenderGameOverlayEvent.ElementType.POTION_ICONS
                ||  mc.player.isSpectator() || mc.player.isCreative())
            return;



        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        ScaledResolution screensize = new ScaledResolution(mc);

        int height = screensize.getScaledHeight();


        int leftPos = 4;
        int topPos = height - imageHeight - 4;


        mc.getTextureManager().bindTexture(texture);
        this.drawTexturedModalRect(leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }
}
