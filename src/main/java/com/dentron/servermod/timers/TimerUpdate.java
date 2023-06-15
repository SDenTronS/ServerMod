package com.dentron.servermod.timers;


import com.dentron.servermod.network.UpdateNoneBaseGUI;
import com.dentron.servermod.utils.ModConstants;
import com.dentron.servermod.utils.Utils;
import com.dentron.servermod.worlddata.ModWorldData;
import com.dentron.servermod.ServerMod;
import com.dentron.servermod.network.UpdateBaseOnClient;
import com.dentron.servermod.tileentities.BaseTile;
import com.dentron.servermod.utils.CapUtils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;


@Mod.EventBusSubscriber(modid = ServerMod.MODID)
public class TimerUpdate {
    public static World overworld;
    private static HashMap<Byte, BlockPos> basePoses = new HashMap<>();
    private static int tick = 0;

    @SubscribeEvent
    public static void updateTimers(TickEvent.WorldTickEvent event){
        if ((event.side != Side.SERVER) || (event.phase != TickEvent.Phase.START) || (event.world != overworld)) {
            return;
        }

        if (tick < 20){
            tick++;
            return;
        }

        tick = 0;


        for (BlockPos pos : basePoses.values()) {
            BaseTile tile = (BaseTile) overworld.getTileEntity(pos);
            if (tile == null){
                continue;
            }

            ModTimer timer = tile.getTimer();

            if (!timer.is_times_up()){
                timer.tick();
            }
            else {
                updatePoses();
                deactivateBase(pos);
            }
        }
    }

    public static void updateWorld(WorldServer world){
        overworld = world;
    }

    public static void updatePoses(){
        ModWorldData data = ModWorldData.forWorld(CapUtils.DATA_WORLD);
        basePoses = new HashMap<>();
        for (BlockPos tilePos : data.basePoses){
            BaseTile tile = (BaseTile) overworld.getTileEntity(tilePos);
            if ((tile != null) && !tile.getTimer().is_times_up()){
                basePoses.put(tile.getTeamColor(), tilePos);
            }
        }

        basePoses.remove((byte) 0);
    }

    public static boolean teamHasActiveBase(byte teamID){
        return basePoses.containsKey(teamID);
    }

    public static void deactivateBase(BlockPos pos){
        TileEntity tile = overworld.getTileEntity(pos);
        if (tile instanceof BaseTile){
            BaseTile te = (BaseTile) tile;
            notifyPlayers(te.getTeamColor(), pos);
            te.setTeamColor((byte) 0);
            ServerMod.network.sendToAll(new UpdateBaseOnClient(pos, 0, (byte) 0));
         }
    }

    public static void notifyPlayers(byte teamID, BlockPos pos){
        List<UUID> players = CapUtils.getTeamPlayers(teamID);
        for (UUID uuid : players){
            EntityPlayerMP player = Utils.getPlayerByUUID(uuid);

            if (player == null){
                return;
            }

            player.sendMessage(new TextComponentTranslation("messages.player.base_deactivate", pos.getX(), pos.getY(), pos.getZ()).setStyle(ModConstants.RED_BOLD));
            if (!teamHasActiveBase(teamID)){
                ServerMod.network.sendTo(new UpdateNoneBaseGUI(true), player);
            }
        }
    }
}
