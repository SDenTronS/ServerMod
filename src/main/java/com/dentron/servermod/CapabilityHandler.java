package com.dentron.servermod;

import com.dentron.servermod.teams.ModPlayerStatsHandler;
import com.dentron.servermod.teams.PlayerStatsProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CapabilityHandler {

    public static final ResourceLocation PLAYER_STATS_CAP = new ResourceLocation(ServerMod.MODID, "stats");


    @SubscribeEvent
    public void attachCapability(AttachCapabilitiesEvent<Entity> event){
        if (event.getObject() instanceof EntityPlayer){
            event.addCapability(PLAYER_STATS_CAP, new PlayerStatsProvider());
        }
    }

    @SubscribeEvent
    public void clonePlayer(PlayerEvent.Clone event){
        final ModPlayerStatsHandler original = getHandler(event.getOriginal());
        final ModPlayerStatsHandler clone = getHandler(event.getEntity());
        clone.setTeamID(original.getTeamID());
        clone.setLives(original.getLives());
    }


    public static ModPlayerStatsHandler getHandler(Entity entity) {

        if (entity.hasCapability(PlayerStatsProvider.PLAYER_STATS_CAP, EnumFacing.DOWN))
            return entity.getCapability(PlayerStatsProvider.PLAYER_STATS_CAP, EnumFacing.DOWN);
        return null;
    }
}
