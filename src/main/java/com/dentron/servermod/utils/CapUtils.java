package com.dentron.servermod.utils;

import com.dentron.servermod.teams.ModPlayerStatsHandler;
import com.dentron.servermod.teams.PlayerStatsProvider;
import com.dentron.servermod.tileentities.BaseTile;
import com.dentron.servermod.worlddata.ModWorldData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;
import java.util.UUID;

public class CapUtils {

    public static WorldServer DATA_WORLD;

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
        return ModWorldData.getTeam(teamID).getPlayers();
    }

    public static List<BlockPos> getTeamPositions(byte teamID){
        return ModWorldData.getTeam(teamID).getPositions();
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
