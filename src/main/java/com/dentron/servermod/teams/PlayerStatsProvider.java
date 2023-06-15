package com.dentron.servermod.teams;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerStatsProvider implements ICapabilitySerializable<NBTBase> {
    @CapabilityInject(ModPlayerStatsHandler.class)
    public static final Capability<ModPlayerStatsHandler> PLAYER_STATS_CAP = null;

    private ModPlayerStatsHandler instance = PLAYER_STATS_CAP.getDefaultInstance();


    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == PLAYER_STATS_CAP;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return hasCapability(capability, facing) ? PLAYER_STATS_CAP.cast(this.instance) : null;
    }

    @Override
    public NBTBase serializeNBT() {
        return PLAYER_STATS_CAP.getStorage().writeNBT(PLAYER_STATS_CAP, this.instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        PLAYER_STATS_CAP.getStorage().readNBT(PLAYER_STATS_CAP, this.instance, null, nbt);
    }
}
