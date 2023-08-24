package com.dentron.servermod.utils;

import com.dentron.servermod.SMEventHandler;
import com.dentron.servermod.tileentities.BaseTile;
import com.dentron.servermod.worlddata.ModWorldData;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import net.minecraft.advancements.*;
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
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.*;


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

    public static void sendMessageToTeam(ITextComponent msg, byte teamId){
        List<UUID> team = CapUtils.getTeamPlayers(teamId);
        for (UUID uuid : team){
            if (!isPlayerOnline(uuid)){
                continue;
            }

            EntityPlayerMP player = getPlayerByUUID(uuid);
            player.sendMessage(msg);
        }
    }

    public static byte getCurrentBaseColor(BlockPos pos){
        TileEntity tile = CapUtils.DATA_WORLD.getTileEntity(pos);
        if (tile instanceof BaseTile){
            BaseTile te = (BaseTile) tile;
            return te.getTeamColor();
        }

        return 0;
    }

    public static EntityPlayerMP getPlayerByUUID(UUID uuid){
        EntityPlayerMP player;
        if (isPlayerOnline(uuid)){
            player = SMEventHandler.server.getPlayerList().getPlayerByUUID(uuid);
        } else {
            player = loadPlayer(uuid, CapUtils.DATA_WORLD);
        }
        return player;
    }

    public static boolean isPlayerOnline(UUID uuid){
        return SMEventHandler.server.getEntityFromUuid(uuid) != null;
    }

    public static boolean isTeamLeader(EntityPlayerMP player){
        byte teamId = CapUtils.getTeamID(player);
        if (teamId == 0){
            return false;
        }

        List<UUID> teamPlayers = CapUtils.getTeamPlayers(teamId);
        UUID playerUUID = player.getUniqueID();

        return teamPlayers.indexOf(playerUUID) == 0;
    }

    public static boolean playerInSameTeam(EntityPlayerMP player, byte teamId){
        return CapUtils.getTeamID(player) == teamId;
    }

    @SuppressWarnings("unchecked")
    public static HashSet<Advancement> getPlayerCompletedAdvancements(EntityPlayerMP player){
        Field progress = ObfuscationReflectionHelper.findField(PlayerAdvancements.class, "field_192758_f");

        HashSet<Advancement> set = new HashSet<>();

        try {
            Map<Advancement, AdvancementProgress> map = (Map<Advancement, AdvancementProgress>) progress.get(player.getAdvancements());

            for (Map.Entry<Advancement, AdvancementProgress> entry : map.entrySet()){
                Advancement key = entry.getKey();
                if (entry.getValue().isDone() && !key.getId().toString().contains("minecraft:recipes")){
                    set.add(key);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }


        return set;
    }

    public static int recountTeamAdvancements(byte teamID, int oldValue){
        List<UUID> team = CapUtils.getTeamPlayers(teamID);
        HashSet<Advancement> advancementsSet = new HashSet<>();

        for (UUID uuid : team){
            EntityPlayerMP player = getPlayerByUUID(uuid);
            advancementsSet = Sets.newHashSet(Sets.union(advancementsSet, getPlayerCompletedAdvancements(player)));
        }

        return Math.max(advancementsSet.size(), oldValue);
    }
}
