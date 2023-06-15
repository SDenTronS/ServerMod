package com.dentron.servermod.network;

import com.dentron.servermod.screens.NoneBaseGUI;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UpdateNoneBaseGUI implements IMessage {
    private boolean flag;

    public UpdateNoneBaseGUI(){

    }

    public UpdateNoneBaseGUI(boolean flag){
        this.flag = flag;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        flag = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(flag);
    }

    public static class Handler implements IMessageHandler<UpdateNoneBaseGUI, IMessage>{
        @Override
        public IMessage onMessage(UpdateNoneBaseGUI message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                NoneBaseGUI.changeVisibility(message.flag);
            });
            return null;
        }
    }
}
