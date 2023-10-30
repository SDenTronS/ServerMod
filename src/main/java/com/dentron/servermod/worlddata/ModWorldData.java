package com.dentron.servermod.worlddata;

import com.dentron.servermod.ServerMod;
import com.dentron.servermod.utils.CapUtils;
import com.dentron.servermod.utils.ModConstants;
import com.dentron.servermod.utils.Utils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ModWorldData extends WorldSavedData {
    private static final String DATA_NAME = ServerMod.MODID + "_WorldData";
    private final List<BlockPos> basePoses = new ArrayList<>();
    private NBTTagCompound baseData = new NBTTagCompound();
    private NBTTagCompound randomGenData = new NBTTagCompound();
    private NBTTagCompound teamsData = new NBTTagCompound();
    public ModWorldData(String name){
        super(name);
    }

    public ModWorldData() {
        super(DATA_NAME);
    }


    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagList positions = (NBTTagList) nbt.getTag("positions");

        for (int i = 0; i < positions.tagCount(); i++){
            BlockPos pos = BlockPos.fromLong(((NBTTagLong) positions.get(i)).getLong());
            basePoses.add(pos);
        }


        baseData = nbt.getCompoundTag("baseData");
        randomGenData = nbt.getCompoundTag("randomGen");
        teamsData = nbt.getCompoundTag("teamsData");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList positions = new NBTTagList();

        basePoses.forEach(pos -> positions.appendTag(new NBTTagLong(pos.toLong())));

        compound.setTag("positions", positions);
        compound.setTag("baseData", baseData);
        compound.setTag("randomGen", randomGenData);
        compound.setTag("teamsData", teamsData);
        return compound;
    }

    public static List<BlockPos> getPositions(WorldServer world){
        ModWorldData data = forWorld(world);
        return data.basePoses;
    }

    public static NBTTagCompound getBaseData(WorldServer world){
        ModWorldData data = forWorld(world);
        return data.baseData;
    }

    public static NBTTagCompound getRandomGenData(WorldServer world){
        ModWorldData data = forWorld(world);
        return data.randomGenData;
    }

    public static ModWorldData forWorld(WorldServer world){
        MapStorage storage = world.getMapStorage();
        ModWorldData saver = (ModWorldData) storage.getOrLoadData(ModWorldData.class, DATA_NAME);

        if (saver == null)
        {
            saver = new ModWorldData();
            storage.setData(DATA_NAME, saver);
        }
        return saver;
    }





    ///             ------------------------------ METHODS FOR BASES DATA ------------------------------





    public static List<BlockPos> getRandomGenPositions(WorldServer world){
        NBTTagList list = (NBTTagList) getRandomGenData(world).getTag("positions");
        List<BlockPos> toReturn = new ArrayList<>();

        if (list.hasNoTags()) return toReturn;

        for (int i = 0; i < list.tagCount(); i++){
            BlockPos pos = BlockPos.fromLong(((NBTTagLong) list.get(i)).getLong());
            toReturn.add(pos);
        }

        return toReturn;
    }

    public static void putPos(BlockPos pos){
        ModWorldData saver = forWorld(CapUtils.DATA_WORLD);

        saver.basePoses.add(pos);
        saver.baseData.setTag(String.valueOf(pos.toLong()), getEmptyActivationList());
        saver.markDirty();
    }

    public static void removePos(BlockPos pos){
        ModWorldData saver = forWorld(CapUtils.DATA_WORLD);

        saver.basePoses.remove(pos);
        saver.baseData.removeTag(String.valueOf(pos.toLong()));
        saver.markDirty();
    }

    public static void writeRandomGen(List<BlockPos> positions, WorldServer world){
        ModWorldData data = forWorld(world);
        NBTTagList toWrite = new NBTTagList();

        positions.forEach(pos -> toWrite.appendTag(new NBTTagLong(pos.toLong())));
        data.randomGenData.setTag("positions", toWrite);

        data.markDirty();
    }

    public static void lockWritingRandomGen(WorldServer world){
        ModWorldData data = forWorld(world);
        data.randomGenData.setBoolean("locked", true);
        data.markDirty();
    }

    public static void activate_base(byte teamId, BlockPos pos, WorldServer world){
        String keyPos = String.valueOf(pos.toLong());

        ModWorldData saver = forWorld(world);
        NBTTagList data = (NBTTagList) saver.baseData.getTag(keyPos);

        int newValue = Utils.getBaseActivationByTeam(pos, teamId) + 1;
        data.set(teamId - 1, new NBTTagInt(newValue));

        saver.markDirty();
    }

    private static NBTTagList getEmptyActivationList(){
        NBTTagList list = new NBTTagList();

        for (int i = 0; i < 15; i++){
            list.appendTag(new NBTTagInt(0));
        }

        return list;
    }





    ///             ------------------------------ METHODS FOR TEAMS DATA ------------------------------





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

        ModWorldData data = forWorld(CapUtils.DATA_WORLD);
        data.teamsData.removeTag(String.valueOf(teamID));
        data.markDirty();
    }

    public static byte toDefaultTeam(EntityPlayerMP player){
        byte teamID = CapUtils.getTeamID(player);
        removePlayer(teamID, player.getUniqueID());
        CapUtils.getStatsCapability(player).setTeamID((byte) 0);

        if (getTeam(teamID).getPlayers().isEmpty()){
            removeTeam(teamID);
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
        ModWorldData data = ModWorldData.forWorld(CapUtils.DATA_WORLD);
        data.teamsData.setTag(String.valueOf(teamID), team.deserialize());
        data.markDirty();
    }

    private static void createTeam(String id ,WorldServer world){
        ModWorldData data = ModWorldData.forWorld(CapUtils.DATA_WORLD);
        data.teamsData.setTag(id, new TeamObject().deserialize());
        data.markDirty();
    }

    public static NBTTagCompound getTeams(WorldServer world){
        return ModWorldData.forWorld(world).teamsData;
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
