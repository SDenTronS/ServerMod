package com.dentron.servermod.worlddata;

import com.dentron.servermod.ServerMod;
import com.dentron.servermod.utils.CapUtils;
import com.dentron.servermod.utils.ModConstants;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

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
        team.adv_amount = advAmount;
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

        if (getTeam(teamID).players.isEmpty()){
            TeamsWorldData.removeTeam(teamID);
        }
        return teamID;
    }

    public static TeamObject getTeam(byte teamID){
        NBTTagCompound team = getTeams(CapUtils.DATA_WORLD).getCompoundTag(String.valueOf(teamID));
        return TeamObject.serialize(team);
    }

    public static void setNewLeader(int UUIDindex, byte teamID){
        TeamObject team = getTeam(teamID);
        Collections.swap(team.players, 0, UUIDindex);
        updateTeam(team, teamID);
    }

    public static TeamObject getTeamOrCreate(byte TeamID){
        WorldServer world = CapUtils.DATA_WORLD;
        NBTTagCompound teams = getTeams(world);
        String id = String.valueOf(TeamID);
        if (!teams.hasKey(id)) {
            createTeam(id, world);
        }
        return TeamObject.serialize(teams.getCompoundTag(id));
    }

    public static boolean putPlayer(byte teamID, UUID player_uuid){
        TeamObject team = getTeamOrCreate(teamID);
        if (team.players.size() < TeamObject.MAX_MEMBERS) {
            team.players.add(player_uuid);
            updateTeam(team, teamID);
            return true;
        }
        return false;
    }

    public static void setPosition(byte teamID, BlockPos pos){
        TeamObject team = getTeamOrCreate(teamID);
        team.addPosition(pos);
        updateTeam(team, teamID);
    }

    public static void removePlayer(byte teamID, UUID player_uuid){
        TeamObject team = getTeamOrCreate(teamID);
        team.players.remove(player_uuid);
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
        team.adv_amount++;
        updateTeam(team, teamID);

        int step = team.adv_amount / ModConstants.ADVANCEMENTS_AMOUNT;

        return team.position.size() < step;
    }

    public static class TeamObject {
        public static int MAX_MEMBERS = ModConstants.MAX_TEAM_MEMBERS;
        private int adv_amount;
        private final List<UUID> players;
        private final List<BlockPos> position;


        public TeamObject(){
            this.players = new ArrayList<>();
            this.adv_amount = 0;
            this.position = new ArrayList<>();
        }

        public TeamObject(List<UUID> players, int adv_amount, List<BlockPos> position){
            this.players = players;
            this.adv_amount = adv_amount;
            this.position = position;
        }

        public void addPosition(BlockPos pos){
            position.add(pos);
        }

        public int getAdv_amount() {
            return adv_amount;
        }

        public List<BlockPos> getPosition(){
            return this.position;
        }

        public List<UUID> getPlayers(){
            return this.players;
        }


        public static TeamObject serialize(NBTTagCompound nbt){
            List<UUID> members = new ArrayList<>();
            for (int i = 0; nbt.hasKey("member" + i); i++){
                UUID uuid = UUID.fromString(nbt.getString("member" + i));
                members.add(uuid);
            }

            List<BlockPos> positions = new ArrayList<>();
            for (int i = 0; nbt.hasKey("position" + i); i++){
                BlockPos pos = BlockPos.fromLong(nbt.getLong("position" + i));
                positions.add(pos);
            }


            return new TeamObject(members, nbt.getInteger("adv"), positions);
        }

        public NBTTagCompound deserialize(){
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger("adv", adv_amount);
            for (ListIterator<UUID> iter = players.listIterator(); iter.hasNext(); ) {
                nbt.setString("member" + iter.nextIndex(), iter.next().toString());
            }

            for (ListIterator<BlockPos> iter = position.listIterator(); iter.hasNext(); ){
                nbt.setLong("position" + iter.nextIndex(), iter.next().toLong());
            }

            return nbt;
        }

    }
}