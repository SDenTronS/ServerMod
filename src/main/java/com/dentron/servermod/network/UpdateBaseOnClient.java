package com.dentron.servermod.network;

import com.dentron.servermod.screens.NoneBaseGUI;
import com.dentron.servermod.tileentities.BaseTile;
import com.dentron.servermod.timers.TimerUpdate;
import com.dentron.servermod.utils.CapUtils;
import com.dentron.servermod.utils.Utils;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UpdateBaseOnClient implements IMessage {
    private BlockPos pos;
    private Integer timer;
    private byte teamID;


    public UpdateBaseOnClient(){

    }

    public UpdateBaseOnClient(BlockPos tilePos, Integer timer, byte teamID){
        this.pos = tilePos;
        this.timer = timer;
        this.teamID = teamID;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        timer = buf.readInt();
        teamID = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        buf.writeInt(timer);
        buf.writeByte(teamID);
    }

    public static class Handler implements IMessageHandler<UpdateBaseOnClient, IMessage>{

        @Override
        public IMessage onMessage(UpdateBaseOnClient message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                BaseTile client_te = (BaseTile) Minecraft.getMinecraft().world.getTileEntity(message.pos);

                if (client_te != null) {
                    client_te.getTimer().setTicks(message.timer);
                    client_te.setTeamColor(message.teamID);
                }
                    });

            return null;
        }
    }
}
