package com.dentron.servermod.network;

import com.dentron.servermod.SMEventHandler;
import com.dentron.servermod.ServerMod;
import com.dentron.servermod.teams.ModPlayerStatsHandler;
import com.dentron.servermod.tileentities.BaseTile;
import com.dentron.servermod.timers.TimerUpdate;
import com.dentron.servermod.utils.CapUtils;
import com.dentron.servermod.utils.LevelUtils;
import com.dentron.servermod.utils.ModConstants;
import com.dentron.servermod.utils.Utils;
import com.dentron.servermod.worlddata.ModWorldData;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ActivateBase implements IMessage {
    private BlockPos pos;

    public ActivateBase() {
    }

    public ActivateBase(BlockPos pos){
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
    }

    public static class Handler implements IMessageHandler<ActivateBase, IMessage> {
        @Override
        public IMessage onMessage(ActivateBase message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().player;
            ModPlayerStatsHandler cap = CapUtils.getStatsCapability(player);
            byte teamID = CapUtils.getTeamID(player);

            if (cap.is_lives_over()) {
                player.closeScreen();
                player.sendMessage(new TextComponentTranslation("messages.player.dead_activate").setStyle(ModConstants.RED_BOLD));
                return null;
            }

            if (teamID == 0){
                player.closeScreen();
                player.sendMessage(new TextComponentTranslation("messages.team.is_null").setStyle(ModConstants.RED_BOLD));
                return null;
            }


            LevelUtils.addScheduledTask(player.world, () -> {
                BlockPos pos = message.pos;
                World world = player.world;
                TileEntity tile = world.getTileEntity(pos);
                WorldServer worldServer = CapUtils.getDataWorld();

                activate_base(worldServer, teamID, tile);
                    });



            BlockPos tilePos = message.pos;
            ServerMod.network.sendToDimension(new UpdateBaseOnClient(
                    tilePos, ModConstants.BASE_TIMER,
                    teamID),
                    0);
            return null;
        }
    }

    public static void activate_base(WorldServer world, byte teamID, TileEntity tile){
        if (tile instanceof BaseTile) {
            BaseTile te = (BaseTile) tile;
            byte te_color = te.getTeamColor();
            if (te.getTimer().is_times_up()){
                ModWorldData.activate_base(teamID, tile.getPos(), world);
            }

            notifyTeam(teamID);
            te.activate_base();
            te.setTeamColor(teamID);
            TimerUpdate.updatePoses();

            if (te_color != 0 && te_color != teamID){
                notifyCapture(te_color, teamID, te.getPos());
            }

        }
    }


    public static void notifyTeam(byte teamID){
        List<UUID> team = CapUtils.getTeamPlayers(teamID);
        for (UUID uuid : team){
            if (!Utils.isPlayerOnline(uuid)){
                return;
            }

            EntityPlayerMP player = Utils.getPlayerByUUID(uuid);

            ServerMod.network.sendTo(new UpdateNoneBaseGUI(false), player);
        }
    }

    public static void notifyCapture(byte teamID, byte capturer, BlockPos pos){
        List<UUID> team = CapUtils.getTeamPlayers(teamID);
        for (UUID uuid : team){
            if (!Utils.isPlayerOnline(uuid)){
                return;
            }

            EntityPlayerMP player = Utils.getPlayerByUUID(uuid);

            String local_key = ModConstants.COLORS_LOCALIZE_KEYS.get(capturer);
            ITextComponent team_part = new TextComponentTranslation(local_key).setStyle(ModConstants.COLORS_TEXT_STYLE.get(capturer).setBold(true));
            player.sendMessage(new TextComponentTranslation("messages.player.base_captured", team_part,  pos.getX(), pos.getY(), pos.getZ()).setStyle(ModConstants.RED_BOLD));

            if (!CapUtils.hasActiveBase(teamID)){
                ServerMod.network.sendTo(new UpdateNoneBaseGUI(true), player);
            }

        }
    }



}
