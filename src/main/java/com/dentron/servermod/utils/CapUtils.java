package com.dentron.servermod.utils;

import com.dentron.servermod.teams.ModPlayerStatsHandler;
import com.dentron.servermod.teams.PlayerStatsProvider;
import com.dentron.servermod.tileentities.BaseTile;
import com.dentron.servermod.timers.TimerUpdate;
import com.dentron.servermod.worlddata.ModWorldData;
import com.dentron.servermod.worlddata.TeamsWorldData;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.UUID;

public class CapUtils {

    public static WorldServer DATA_WORLD;

    public static TeamsWorldData.TeamObject getTeam(EntityPlayer player){
        byte teamID = getTeamID(player);
        return TeamsWorldData.getTeam(teamID);
    }

    public static byte getTeamID(EntityPlayer player){
        ModPlayerStatsHandler cap = player.getCapability(PlayerStatsProvider.PLAYER_STATS_CAP, null);
        return cap.getTeamID();
    }

    public static WorldServer getDataWorld(){
        return DATA_WORLD;
    }

    public static ModPlayerStatsHandler getStatsCapability(EntityPlayer player){
        return player.getCapability(PlayerStatsProvider.PLAYER_STATS_CAP, null);
    }

    public static List<UUID> getTeamPlayers(byte teamID){
        return TeamsWorldData.getTeam(teamID).getPlayers();
    }

    public static List<BlockPos> getTeamPosition(byte teamID){
        return TeamsWorldData.getTeam(teamID).getPosition();
    }


    public static boolean hasActiveBase(byte teamID){
        World world = DATA_WORLD;
        for (BlockPos pos : ModWorldData.getPositions(DATA_WORLD)){
            TileEntity tile = world.getTileEntity(pos);
            if (tile == null){
                continue;
            }

            BaseTile te = (BaseTile) tile;
            if (!te.getTimer().is_times_up() && (te.getTeamColor() == teamID)){
                return true;
            }
         }
        return false;
    }


}
