package com.dentron.servermod.blocks;

import com.dentron.servermod.Registration;
import com.dentron.servermod.SMEventHandler;
import com.dentron.servermod.ServerMod;
import com.dentron.servermod.network.UpdateBaseOnClient;
import com.dentron.servermod.network.UpdateNoneBaseGUI;
import com.dentron.servermod.screens.ScreenHandler;
import com.dentron.servermod.tileentities.BaseTile;
import com.dentron.servermod.utils.Utils;
import com.dentron.servermod.worlddata.ModWorldData;
import com.dentron.servermod.timers.TimerUpdate;
import com.dentron.servermod.utils.CapUtils;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class BaseBlock extends Block {
    private final AxisAlignedBB AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.625D, 1.0D);


    public BaseBlock(String name) {
        super(Material.IRON);

        this.setRegistryName(name);
        this.setUnlocalizedName(name);

        setSoundType(SoundType.METAL);
        setHardness(100.0f);
        setResistance(40.0f);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return this.AABB;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean canDropFromExplosion(Explosion explosionIn) {
        return false;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getByNameOrId("servermod:base_soul");
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new BaseTile();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote){
            BaseTile server_tile = (BaseTile) worldIn.getTileEntity(pos);
            ServerMod.network.sendTo(new UpdateBaseOnClient(pos, server_tile.getTime(), server_tile.getTeamColor()), (EntityPlayerMP) playerIn);
        }
        else {
            BaseTile client_tile = (BaseTile) worldIn.getTileEntity(pos);
            playerIn.openGui(ServerMod.instance, ScreenHandler.BASE_GUI, worldIn, pos.getX(), pos.getY(), pos.getZ());
            client_tile.is_GUI_Open = true;
        }
        return true;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)  {
        if (!worldIn.isRemote) {
            changePoses(pos, true);
            if (placer.dimension != DimensionType.OVERWORLD.getId()){
                changePoses(pos, false);
            }
        }
        if (placer.dimension != DimensionType.OVERWORLD.getId()) {
            worldIn.setBlockState(pos, net.minecraft.init.Blocks.AIR.getDefaultState(), worldIn.isRemote ? 11 : 3);
            worldIn.getPlayerEntityByName(placer.getName()).inventory.addItemStackToInventory(new ItemStack(Registration.BASE_BLOCK));
        }
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World worldIn, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        if (!worldIn.isRemote) {
            changePoses(pos, false);
            notifyBaseDestroy(Utils.getBaseOwner(pos));
        }
        return super.removedByPlayer(state, worldIn, pos, player, willHarvest);
    }

    private void changePoses( BlockPos pos, boolean flag){
        ModWorldData data = ModWorldData.forWorld(CapUtils.DATA_WORLD);
        if (flag) {
            data.putPos(pos);
        }
        else {
            data.removePos(pos);
        }
        TimerUpdate.updatePoses();
    }

    public static void notifyBaseDestroy(byte teamID){
        List<UUID> players = CapUtils.getTeamPlayers(teamID);
        Utils.sendMessageToAll(Utils.getBaseDestroyMessage(teamID));

        for (UUID uuid : players){
            EntityPlayerMP player = Utils.getPlayerByUUID(uuid);

            if (player == null){
                return;
            }

            if (!CapUtils.hasActiveBase(teamID)){
                ServerMod.network.sendTo(new UpdateNoneBaseGUI(true), player);
            }
        }
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return super.canPlaceBlockAt(worldIn, pos);
    }
}
