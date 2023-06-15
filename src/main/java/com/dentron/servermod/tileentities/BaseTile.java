package com.dentron.servermod.tileentities;

import com.dentron.servermod.timers.ModTimer;
import com.dentron.servermod.utils.ModConstants;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

public class BaseTile extends TileEntity implements ITickable {
    private Float current_render_rotate = 0F;
    private Integer tick = 0;
    private ModTimer timer = new ModTimer(0);
    public boolean is_GUI_Open = false;
    private byte teamColor = 0;



    public Float getCurrent_render_rotate() {
        return current_render_rotate;
    }

    public Float getCurrent_render_y() {
        double angle_rad = 4 * (current_render_rotate * Math.PI) / 180;
        return 0.1F * (float) Math.sin(angle_rad);
    }

    @Override
    public void update() {
        if (!world.isRemote){
            return;
        }

        this.current_render_rotate += 0.5F;
        this.current_render_rotate = current_render_rotate % 360;
        if (!timer.is_times_up() && is_GUI_Open) {
            if (tick >= 20) {
                timer.tick();
                tick = 0;
            } else {
                tick++;
            }
        }
        this.markDirty();

    }



    public void activate_base(){
        timer.setTicks(ModConstants.BASE_TIMER);
    }

    public byte getTeamColor() {
        return teamColor;
    }
    public void setTeamColor(byte TeamID) {
        this.teamColor = TeamID;
        this.markDirty();
    }


    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("time", timer.getTicks());
        compound.setByte("color", teamColor);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        timer.setTicks(compound.getInteger("time"));
        teamColor = compound.getByte("color");
        }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 1, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }


    public ModTimer getTimer(){
        return timer;
    }

    public Integer getTime(){
        return timer.getTicks();
    }
}

