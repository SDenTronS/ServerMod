package com.dentron.servermod.screens;

import com.dentron.servermod.tileentities.BaseTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class ScreenHandler implements IGuiHandler {
    public static final int BASE_GUI = 1;

    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        switch (ID){
            case BASE_GUI:
                return new GenericMenu(player.inventory, te);
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        switch (ID){
            case BASE_GUI:
                return new BaseTileGUI((BaseTile) te);
            default:
                return null;
        }
    }
}
