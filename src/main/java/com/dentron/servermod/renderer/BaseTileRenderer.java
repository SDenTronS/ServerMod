package com.dentron.servermod.renderer;

import com.dentron.servermod.tileentities.BaseTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

public class BaseTileRenderer extends TileEntitySpecialRenderer<BaseTile> {
    private EntityItem entityItem = new EntityItem(Minecraft.getMinecraft().world, 0D, 0D, 0D);

    @Override
    public void render(BaseTile te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GL11.glPushMatrix();
        {
            GL11.glTranslated((float) x + 0.5F, (float) y - 0.1F + te.getCurrent_render_y(), (float) z + 0.5F);
            GL11.glScalef(1.5F, 1.5F, 1.5F);
            GL11.glRotatef(te.getCurrent_render_rotate(), 0F, 1F, 0F);
            entityItem.hoverStart = 0F;
            entityItem.setItem(new ItemStack(Blocks.WOOL, 1, te.getTeamColor()));
            Minecraft.getMinecraft().getRenderManager().renderEntity(entityItem, 0.0D, 0.2D, 0.0D, 0.0F, 0.0F, false);
        }
        GL11.glPopMatrix();
    }
}
