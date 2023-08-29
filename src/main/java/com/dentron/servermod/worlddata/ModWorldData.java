package com.dentron.servermod.worlddata;

import com.dentron.servermod.ServerMod;
import com.dentron.servermod.utils.CapUtils;
import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.ArrayList;
import java.util.List;

public class ModWorldData extends WorldSavedData {
    private static final String DATA_NAME = ServerMod.MODID + "_ModWorldData";
    private final List<BlockPos> basePoses = new ArrayList<>();
    private NBTTagCompound baseData = new NBTTagCompound();
    private NBTTagCompound randomGenData = new NBTTagCompound();

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
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList positions = new NBTTagList();

        basePoses.forEach(pos -> positions.appendTag(new NBTTagLong(pos.toLong())));

        compound.setTag("positions", positions);
        compound.setTag("baseData", baseData);
        compound.setTag("randomGen", randomGenData);
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
            saver.baseData.removeTag(String.valueOf(pos.toLong()));
            saver.markDirty();
        }
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
        String keyId = String.valueOf(teamId);
        String keyPos = String.valueOf(pos.toLong());

        ModWorldData saver = forWorld(world);
        NBTTagCompound data = saver.baseData.getCompoundTag(keyPos);
        data.setInteger(keyId, data.getInteger(keyId) + 1);

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
}
