package com.dentron.servermod.worlddata;

import com.dentron.servermod.ServerMod;
import com.dentron.servermod.utils.CapUtils;
import com.dentron.servermod.utils.ModConstants;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nonnull;
import java.util.*;

public class TeamsWorldData extends WorldSavedData {
    private static final String DATA_NAME = ServerMod.MODID + "_TeamsData";
    private NBTTagCompound WORLD_TEAMS = new NBTTagCompound();

    public TeamsWorldData(String name){
        super(name);
    }

    public TeamsWorldData(){
        super(DATA_NAME);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.WORLD_TEAMS = nbt;
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        return WORLD_TEAMS;
    }

    private static TeamsWorldData getData(WorldServer world){
        MapStorage storage = world.getMapStorage();
        TeamsWorldData saver = (TeamsWorldData) storage.getOrLoadData(TeamsWorldData.class, DATA_NAME);

        if (saver == null)
        {
            saver = new TeamsWorldData();
            storage.setData(DATA_NAME, saver);
        }
        return saver;
    }

    public static void setTeamAdvancementAmount(byte teamID, int advAmount){
        TeamObject team = getTeam(teamID);
        team.advAmount = advAmount;
        updateTeam(team, teamID);
    }

    public static void removeTeam(byte teamID){
        List<UUID> team = CapUtils.getTeamPlayers(teamID);
        if (!team.isEmpty()){
            throw new RuntimeException("team is not empty");
        }

        TeamsWorldData data = getData(CapUtils.DATA_WORLD);
        data.WORLD_TEAMS.removeTag(String.valueOf(teamID));
        data.markDirty();
    }

    public static byte toDefaultTeam(EntityPlayerMP player){
        byte teamID = CapUtils.getTeamID(player);
        TeamsWorldData.removePlayer(teamID, player.getUniqueID());
        CapUtils.getStatsCapability(player).setTeamID((byte) 0);

        if (getTeam(teamID).getPlayers().isEmpty()){
            TeamsWorldData.removeTeam(teamID);
        }
        return teamID;
    }

    public static TeamObject getTeam(byte teamID){
        NBTTagCompound team = getTeams(CapUtils.DATA_WORLD).getCompoundTag(String.valueOf(teamID));
        return !team.hasNoTags() ? TeamObject.serialize(team) : new TeamObject();
    }

    public static void swapElementWithFirst(int index, byte teamID){
        TeamObject team = getTeam(teamID);

        List<UUID> players = team.getPlayers();
        Collections.swap(players, 0, index);
        team.setPlayers(players);

        updateTeam(team, teamID);
    }

    public static TeamObject getTeamOrCreate(byte teamID){
        TeamObject team = getTeam(teamID);
        if (team.equals(new TeamObject())){
            createTeam(String.valueOf(teamID), CapUtils.DATA_WORLD);
            return new TeamObject();
        }

        return team;
    }

    public static boolean addPlayer(byte teamID, UUID player_uuid){
        TeamObject team = getTeamOrCreate(teamID);

        if (team.getPlayers().size() < TeamObject.MAX_MEMBERS) {
            team.addPlayer(player_uuid);
            updateTeam(team, teamID);
            return true;
        }
        return false;
    }

    public static void addPosition(byte teamID, BlockPos pos){
        TeamObject team = getTeamOrCreate(teamID);
        team.addPosition(pos);
        updateTeam(team, teamID);
    }

    public static void removePlayer(byte teamID, UUID player_uuid){
        TeamObject team = getTeamOrCreate(teamID);
        team.removePlayer(player_uuid);
        updateTeam(team, teamID);
    }

    public static void updateTeam(TeamObject team, byte teamID){
        TeamsWorldData data = getData(CapUtils.DATA_WORLD);
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

    public static boolean addAdvancement(byte teamID){
        TeamObject team = getTeam(teamID);
        team.advAmount++;
        updateTeam(team, teamID);

        int step = team.advAmount / ModConstants.ADVANCEMENTS_AMOUNT;

        return team.getPositions().size() < step;
    }

    public static class TeamObject {
        public static int MAX_MEMBERS = ModConstants.MAX_TEAM_MEMBERS;
        private int advAmount;
        private NBTTagList players;
        private NBTTagList position;

        public TeamObject(){
            this.players = new NBTTagList();
            this.advAmount = 0;
            this.position = new NBTTagList();
        }

        public TeamObject(NBTTagList players, int adv_amount, NBTTagList position){
            this.players = players;
            this.advAmount = adv_amount;
            this.position = position;
        }

        public int getAdvAmount() {
            return advAmount;
        }

        public List<BlockPos> getPositions(){
            List<BlockPos> positions = new ArrayList<>();

            for (int i = 0; i < position.tagCount(); i++){
                BlockPos pos = BlockPos.fromLong(((NBTTagLong) position.get(i)).getLong());
                positions.add(pos);
            }

            return positions;
        }

        public void addPlayer(UUID player){
            List<UUID> players = getPlayers();
            players.add(player);
            setPlayers(players);
        }

        public void addPosition(BlockPos pos){
            List<BlockPos> positions = getPositions();
            positions.add(pos);
            setPosition(positions);
        }

        public void removePlayer(UUID player){
            List<UUID> players = getPlayers();
            players.remove(player);
            setPlayers(players);
        }

        public void setPosition(List<BlockPos> position){
            NBTTagList positions = new NBTTagList();
            position.forEach(pos -> positions.appendTag(new NBTTagLong(pos.toLong())));

            this.position = positions;
        }

        public List<UUID> getPlayers(){
            List<UUID> members = new ArrayList<>();

            for (int i = 0; i < players.tagCount(); i++){
                UUID uuid = UUID.fromString(((NBTTagString) players.get(i)).getString());
                members.add(uuid);
            }

            return members;
        }

        public void setPlayers(List<UUID> players){
            NBTTagList members = new NBTTagList();
            players.forEach(uuid -> members.appendTag(new NBTTagString(uuid.toString())));

            this.players = members;
        }


        public static TeamObject serialize(NBTTagCompound nbt){
            return new TeamObject((NBTTagList) nbt.getTag("members"), nbt.getInteger("adv"), (NBTTagList) nbt.getTag("positions"));
        }

        public NBTTagCompound deserialize(){
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger("adv", advAmount);
            nbt.setTag("members", players);
            nbt.setTag("positions", position);
            return nbt;
        }

    }
}