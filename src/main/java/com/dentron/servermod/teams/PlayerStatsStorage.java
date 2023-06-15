package com.dentron.servermod.teams;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;


public class PlayerStatsStorage implements Capability.IStorage<ModPlayerStatsHandler> {

    @Nullable
    @Override
    public NBTBase writeNBT(Capability<ModPlayerStatsHandler> capability, ModPlayerStatsHandler instance, EnumFacing side) {
        NBTTagCompound data = new NBTTagCompound();
        data.setByte("teamID", instance.getTeamID());
        data.setByte("lives", instance.getLives());
        return data;
    }

    @Override
    public void readNBT(Capability<ModPlayerStatsHandler> capability, ModPlayerStatsHandler instance, EnumFacing side, NBTBase nbt) {
        instance.setTeamID(((NBTTagCompound) nbt).getByte("teamID"));
        instance.setLives(((NBTTagCompound) nbt).getByte("lives"));
    }
}
