package com.dentron.servermod.network;

import com.dentron.servermod.commands.commandTeam.InvitationsBuffer;
import com.dentron.servermod.utils.Messages;
import com.dentron.servermod.utils.ModConstants;
import com.dentron.servermod.utils.Utils;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import java.util.UUID;

public class SendInvitationWithOD implements IMessage { // OD - Optional Deletion
    private int page;
    private int maxPages;
    private String senderName;
    private byte senderTeam;
    private boolean delete;
    private UUID invitation;

    public SendInvitationWithOD(){
    }

    public SendInvitationWithOD(String senderName, int page, int maxPages, byte senderTeam, UUID invitation, boolean delete){
        this.senderName = senderName;
        this.page = page;
        this.maxPages = maxPages;
        this.senderTeam = senderTeam;
        this.delete = delete;
        this.invitation = invitation;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        page = buf.readInt();
        maxPages = buf.readInt();
        senderName = ByteBufUtils.readUTF8String(buf);
        invitation = UUID.fromString(ByteBufUtils.readUTF8String(buf));
        senderTeam = buf.readByte();
        delete = buf.readBoolean();

    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(page);
        buf.writeInt(maxPages);
        ByteBufUtils.writeUTF8String(buf, senderName);
        ByteBufUtils.writeUTF8String(buf, invitation.toString());
        buf.writeByte(senderTeam);
        buf.writeBoolean(delete);
    }

    public static class Handler implements IMessageHandler<SendInvitationWithOD, IMessage> {

        @Override
        public IMessage onMessage(SendInvitationWithOD message, MessageContext ctx) {
            GuiIngame gui = Minecraft.getMinecraft().ingameGUI;
            GuiNewChat chat = gui.getChatGUI();
            if (message.delete){
                chat.deleteChatLine(ModConstants.INVITATIONS_CHATLINE_ID);
                return null;
            }

            chat.printChatMessageWithOptionalDeletion(Messages.getInvitationMsg(message.page, message.maxPages, message.senderName, message.senderTeam, message.invitation), ModConstants.INVITATIONS_CHATLINE_ID);
            return null;
        }
    }
}
