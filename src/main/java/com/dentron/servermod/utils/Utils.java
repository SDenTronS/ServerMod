package com.dentron.servermod.utils;

import com.dentron.servermod.SMEventHandler;
import com.dentron.servermod.ServerMod;
import com.dentron.servermod.tileentities.BaseTile;
import com.dentron.servermod.worlddata.ModWorldData;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Utils {

    public static List<String> buildWeaponList(String weapon, String ammo, Integer ammo_size){
        List<String> list = new ArrayList<>();
        list.add(weapon);
        list.add(ammo);
        list.add(String.valueOf(ammo_size));
        return list;

    }

    public static BlockPos getTeamBasePos(byte teamId){
        String id = String.valueOf(teamId);
        ModWorldData saver = ModWorldData.forWorld(CapUtils.getDataWorld());
        List<BlockPos> basePoses = saver.basePoses;
        NBTTagCompound baseData = saver.baseData;
        int max_activations = 1;
        BlockPos toReturn = null;

        for (BlockPos basePos : basePoses){
            String pos = String.valueOf(basePos.toLong());
            NBTTagCompound data = baseData.getCompoundTag(pos);

            int activations = data.getInteger(id);

            if (activations >= max_activations){
                toReturn = basePos;
            }
        }

        return toReturn;
    }

    public static EntityPlayerMP loadPlayer(UUID player_uuid, World world) {

        GameProfile profile = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerProfileCache().getProfileByUUID(player_uuid);
        EntityPlayerMP member = new EntityPlayerMP(FMLCommonHandler.instance().getMinecraftServerInstance(), (WorldServer) world, profile , new PlayerInteractionManager(world));
        FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().setPlayerManager(new WorldServer[] {(WorldServer) world});
        member.deserializeNBT(FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerNBT(member));
        member.mountEntityAndWakeUp();

        return member;

    }

    public static void sendMessageToAll(ITextComponent msg){
        List<EntityPlayerMP> players = SMEventHandler.server.getPlayerList().getPlayers();

        for (EntityPlayerMP player : players){
            player.sendMessage(msg);
        }
    }

    public static ITextComponent getBasePosMessage(byte teamID, BlockPos pos){
        String local_key = ModConstants.COLORS_LOCALIZE_KEYS.get(teamID);
        int radius = ModConstants.BASE_MSG_RADIUS;
        TextComponentTranslation component;
        if (pos != null){
            int X = -radius + (int) (Math.random() * (2 * radius));
            int MAX_Z = (int) (Math.sqrt(Math.pow(radius, 2) - Math.pow(X, 2)));
            int Z = -MAX_Z + (int) (Math.random() * (2 * MAX_Z + 1));
            component = new TextComponentTranslation("messages.events.base_pos", radius - 1, pos.getX() + X, pos.getZ() + Z);
        }
        else {
            component = new TextComponentTranslation("messages.events.null_base");
        }

        ITextComponent team = new TextComponentTranslation(local_key).setStyle(ModConstants.COLORS_TEXT_STYLE.get(teamID).setBold(true));
        Style GOLD_BOLD = new Style().setBold(true).setColor(TextFormatting.YELLOW);

        return (new TextComponentString("------------------------------").setStyle(GOLD_BOLD)).appendText("\n")
                .appendSibling(new TextComponentTranslation("messages.events.team_complete", team, ModConstants.ADVANCEMENTS_AMOUNT)).appendText("\n")
                .appendSibling(component).appendText("\n")
                .appendSibling(new TextComponentString("------------------------------"));
    }

    public static byte getBaseOwner(BlockPos pos){
        TileEntity tile = CapUtils.DATA_WORLD.getTileEntity(pos);
        if (tile instanceof BaseTile){
            BaseTile te = (BaseTile) tile;
            return te.getTeamColor();
        }

        return 0;
    }

    public static ITextComponent getBaseDestroyMessage(byte teamID){
        Style RED_OBFUSCATED = new Style().setObfuscated(true).setBold(true).setColor(TextFormatting.DARK_RED);
        String local_key = ModConstants.COLORS_LOCALIZE_KEYS.get(teamID);
        ITextComponent team = new TextComponentTranslation(local_key).setStyle(ModConstants.COLORS_TEXT_STYLE.get(teamID));


        return new TextComponentString("TT").setStyle(RED_OBFUSCATED)
                .appendSibling(new TextComponentTranslation("messages.player.base_destroyed", team).setStyle(new Style().setBold(true).setColor(TextFormatting.DARK_RED).setObfuscated(false)))
                .appendSibling(new TextComponentString("TT"));
    }

    public static EntityPlayerMP getPlayerByUUID(UUID uuid){
        EntityPlayerMP player = null;
        try {
            player = SMEventHandler.server.getPlayerList().getPlayerByUUID(uuid);
        }
        catch (NullPointerException ignored){}
        return player;
    }
}
