package com.dentron.servermod.worlddata;

import com.dentron.servermod.ServerMod;
import com.dentron.servermod.utils.CapUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ModWorldData extends WorldSavedData {
    private static final String DATA_NAME = ServerMod.MODID + "_ModWorldData";
    private final List<BlockPos> basePoses = new ArrayList<>();

    private final NBTTagCompound baseData = new NBTTagCompound();
    private NBTTagCompound randomGenData = new NBTTagCompound();

    public ModWorldData() {
        super(DATA_NAME);
    }

    public ModWorldData(String name){
        super(name);
    }


    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagCompound saveData = nbt.getCompoundTag("positions");
        for (int i = 0; saveData.hasKey("basepos"+i); i++){
            basePoses.add(BlockPos.fromLong(saveData.getLong("basepos" + i)));
        }

        for (BlockPos pos : basePoses){
            String basePos = String.valueOf(pos.toLong());
            baseData.setTag(basePos, nbt.getTag(basePos));
        }

        randomGenData = nbt.getCompoundTag("randomGen");
    }

    public static List<BlockPos> getPositions(WorldServer world){
        ModWorldData data = forWorld(world);
        return data.basePoses;
    }

    public static NBTTagCompound getBaseData(WorldServer world){
        ModWorldData data = forWorld(world);
        return data.baseData;
    }

    public static NBTTagCompound getRandomGen(WorldServer world){
        ModWorldData data = forWorld(world);
        return data.randomGenData;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound saveData = new NBTTagCompound();
        for (int i = 0; i < basePoses.size(); i++ ) {
            saveData.setLong("basepos" + i, basePoses.get(i).toLong());
        }
        for (BlockPos pos : basePoses){
            String basePos = String.valueOf(pos.toLong());
            compound.setTag(basePos, baseData.getTag(basePos));
        }

        compound.setTag("randomGen", randomGenData);
        compound.setTag("positions", saveData);
        return compound;
    }

    public void putPos(BlockPos pos){
        ModWorldData saver = (ModWorldData) CapUtils.DATA_WORLD.getMapStorage().getOrLoadData(ModWorldData.class, DATA_NAME);
        if (saver != null) {
            saver.basePoses.add(pos);
            saver.baseData.setTag(String.valueOf(pos.toLong()), new NBTTagCompound());
            saver.markDirty();
        }
    }

    public void removePos(BlockPos pos){
        ModWorldData saver = (ModWorldData) CapUtils.DATA_WORLD.getMapStorage().getOrLoadData(ModWorldData.class, DATA_NAME);
        if (saver != null) {
            saver.basePoses.remove(pos);
            saver.markDirty();
        }
    }

    public static void writeRandomGen(List<BlockPos> positions, WorldServer world){
        ModWorldData data = forWorld(world);
        for (byte i = 1; i <= 15; i++){
            data.randomGenData.setLong(String.valueOf(i), positions.get(i).toLong());
        }

        data.markDirty();
    }

    public static void activate_base(byte teamId, BlockPos pos, WorldServer world){
        String basePos = String.valueOf(pos.toLong());
        ModWorldData saver = forWorld(world);
        BaseData data = BaseData.serialize(saver.baseData.getCompoundTag(basePos));
        data.activate(teamId);
        saver.baseData.setTag(basePos, data.deserialize());
        saver.markDirty();
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


    private static class BaseData{
        private final NBTTagCompound team_activations;

        public BaseData(NBTTagCompound team_activations){
            this.team_activations = team_activations;
        }

        public void activate(byte teamID){
            String id = String.valueOf(teamID);
            if (!team_activations.hasKey(id)){
                team_activations.setInteger(id, 1);
                return;
            }

            int activations = team_activations.getInteger(id);
            team_activations.setInteger(id, activations + 1);
        }

        public static BaseData serialize(NBTTagCompound nbt){
            return new BaseData(nbt);
        }

        public NBTTagCompound deserialize(){
            return team_activations;
        }

    }
}
