package com.dentron.servermod.screens;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class GenericMenu extends Container {
    private TileEntity te;

    public GenericMenu() {}

    public GenericMenu(InventoryPlayer inventory, TileEntity te) {
        this.te = te;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return te == null || isWithinUsableDistance(te.getWorld(), te.getPos(), playerIn, te.getBlockType());
    }
    public static boolean isWithinUsableDistance(World world, BlockPos pos, EntityPlayer player, Block block) {
        return world.getBlockState(pos).getBlock() == block && player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }
}
