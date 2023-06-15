package com.dentron.servermod.worlddata;

import com.dentron.servermod.ServerMod;
import com.dentron.servermod.utils.ModConstants;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nonnull;
import java.util.*;

public class TeamsWorldData extends WorldSavedData {
    private static final String DATA_NAME = ServerMod.MODID + "_TeamsData";
    private NBTTagCompound WORLD_TEAMS = new NBTTagCompound();
    public TeamsWorldData(String name) {
        super(name);
    }

    public TeamsWorldData(){
        super(DATA_NAME);
    }


    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        WORLD_TEAMS = nbt;
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        return WORLD_TEAMS;
    }

    public static TeamsWorldData getData(WorldServer world){
        MapStorage storage = world.getMapStorage();
        TeamsWorldData saver = (TeamsWorldData) storage.getOrLoadData(TeamsWorldData.class, DATA_NAME);

        if (saver == null)
        {
            saver = new TeamsWorldData();
            storage.setData(DATA_NAME, saver);
        }
        return saver;


    }

    public static TeamObject getTeam(byte teamID, WorldServer world){
        NBTTagCompound team = getTeams(world).getCompoundTag(String.valueOf(teamID));
        return TeamObject.serialize(team);
    }

    public static TeamObject getTeamOrCreate(byte TeamID, WorldServer world){
        NBTTagCompound teams = getTeams(world);
        String id = String.valueOf(TeamID);
        if (!teams.hasKey(id)) {
            createTeam(id, world);
        }
        return TeamObject.serialize(teams.getCompoundTag(id));
    }

    public static boolean putPlayer(byte teamID, UUID player_uuid, WorldServer world){
        TeamObject team = getTeamOrCreate(teamID, world);
        if (team.players.size() < TeamObject.MAX_MEMBERS) {
            team.addPlayer(player_uuid);
            updateTeam(team, teamID, world);
            return true;
        }
        return false;
    }

    public static void removePlayer(byte teamID, UUID player_uuid, WorldServer world){
        TeamObject team = getTeamOrCreate(teamID, world);
        team.removePlayer(player_uuid);
        updateTeam(team, teamID, world);
    }

    public static void updateTeam(TeamObject team, byte teamID, WorldServer world){
        TeamsWorldData data = getData(world);
        data.WORLD_TEAMS.setTag(String.valueOf(teamID), team.deserialize());
        data.markDirty();
    }

    private static void createTeam(String id ,WorldServer world){
        TeamsWorldData data = getData(world);
        data.WORLD_TEAMS.setTag(id, new TeamObject().deserialize());
        data.markDirty();
    }

    public static NBTTagCompound getTeams(WorldServer world){
        TeamsWorldData data = TeamsWorldData.getData(world);
        return data.WORLD_TEAMS;
    }

    public static boolean addAdvancement(byte teamID, WorldServer world){
        TeamObject team = getTeam(teamID, world);
        team.addAdvancement();
        team.improveScore(ModConstants.Score.ADVANCEMENT);

        boolean flag = false;
        if (team.adv_amount >= ModConstants.ADVANCEMENTS_AMOUNT){
            team.adv_amount = 0;
            flag = true;
        }

        updateTeam(team, teamID, world);
        return flag;
    }

    public static class TeamObject {
        public static int MAX_MEMBERS = ModConstants.MAX_TEAM_MEMBERS;
        private int score;
        private byte adv_amount;
        private final List<UUID> players;


        public TeamObject(){
            this.score = 0;
            this.players = new ArrayList<>();
            this.adv_amount = 0;
        }

        public TeamObject(int score, List<UUID> players, byte adv_amount){
            this.score = score;
            this.players = players;
            this.adv_amount = adv_amount;
        }

        public void improveScore(int amount){
            this.score += amount;
        }
        public void addAdvancement(){
            this.adv_amount++;
        }
        public void addPlayer(UUID uuid){
            players.add(uuid);
        }

        public void removePlayer(UUID uuid){
            players.remove(uuid);
        }


        public List<UUID> getPlayers(){
            return this.players;
        }


        public static TeamObject serialize(NBTTagCompound nbt){
            List<UUID> members = new ArrayList<>();
            for (int i = 0; i <= MAX_MEMBERS; i++){
                String key = "member" + i;
                if (nbt.hasKey(key)) {
                    members.add(UUID.fromString(nbt.getString(key)));
                }
            }

            return new TeamObject(nbt.getInteger("score"), members, nbt.getByte("adv"));
        }

        public NBTTagCompound deserialize(){
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger("score", score);
            nbt.setByte("adv", adv_amount);
            for (int i = 0; i < players.size(); i++){
                nbt.setString("member" + i, players.get(i).toString());
            }
            return nbt;
        }

    }
}