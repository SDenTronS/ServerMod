package com.dentron.servermod;


import com.dentron.servermod.network.UpdateNoneBaseGUI;
import com.dentron.servermod.teams.ModPlayerStatsHandler;
import com.dentron.servermod.utils.Utils;
import com.dentron.servermod.worlddata.TeamsWorldData;
import com.dentron.servermod.timers.TimerUpdate;
import com.dentron.servermod.utils.CapUtils;
import com.dentron.servermod.utils.ModConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ServerMod.MODID)
public class SMEventHandler {
    public static HashMap<Integer, Integer> POOL = new HashMap<>();
    private static String changedWindowTitle;
    private static boolean HTMLoaded = false;
    public static MinecraftServer server;


    @SubscribeEvent
    public static void onReceivingAdvancement(AdvancementEvent event){
        EntityPlayer player = event.getEntityPlayer();
        byte teamID = CapUtils.getTeamID(player);

        if (event.getAdvancement().getId().toString().contains("minecraft:recipes") || teamID == 0){
            return;
        }

        TeamsWorldData.TeamObject team = CapUtils.getTeam(player);
        boolean flag = true;

        for (UUID uuid : team.getPlayers()){
            EntityPlayerMP teammate = (EntityPlayerMP) event.getEntityPlayer().world.getPlayerEntityByUUID(uuid);

            if (teammate ==  player){
                continue;
            }

            if (teammate == null) {
                teammate = Utils.loadPlayer(uuid, player.getEntityWorld());
            }

            if (teammate.getAdvancements().getProgress(event.getAdvancement()).isDone()) {
                flag = false;
            }
        }

        if (flag){
            boolean needMsg = TeamsWorldData.addAdvancement(teamID, CapUtils.getDataWorld());

            if (needMsg){
                sendCoordinatesToAll(teamID);
            }
        }
    }

    public static void sendCoordinatesToAll(byte teamID){
        BlockPos pos = Utils.getTeamBasePos(teamID);
        Utils.sendMessageToAll(Utils.getBasePosMessage(teamID, pos));
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event){
        Entity entity = event.getEntity();
        if (entity instanceof EntityPlayer){
            EntityPlayer player = (EntityPlayer) entity;
            ModPlayerStatsHandler cap = CapUtils.getStatsCapability(player);

            byte teamID = cap.getTeamID();

            if (TimerUpdate.teamHasActiveBase(teamID)){
                return;
            }

            if (!cap.is_lives_over()){
                cap.reduceLives();
            }
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event){
        EntityPlayer player = event.player;
        ModPlayerStatsHandler cap = CapUtils.getStatsCapability(player);

        byte teamID = cap.getTeamID();

        if (TimerUpdate.teamHasActiveBase(teamID)){
            return;
        }

        if (!cap.is_lives_over()) {
            player.sendMessage(new TextComponentTranslation("messages.player.remaining_live", cap.lives).setStyle(ModConstants.WHITE_BOLD));
        }
        else if (cap.lives == 0){
            player.sendMessage(new TextComponentTranslation("messages.player.full_death").setStyle(ModConstants.RED_BOLD));
            player.sendMessage(new TextComponentTranslation("messages.events.change_dimension").setStyle(new Style().setColor(TextFormatting.DARK_GREEN).setItalic(true)));
            changeGameType(player, GameType.ADVENTURE);
            cap.reduceLives();
        }

    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event){
        EntityPlayer player = event.player;
        ModPlayerStatsHandler cap = CapUtils.getStatsCapability(player);

        if (cap.is_lives_over()){
            if (event.toDim != DimensionType.NETHER.getId()) {
                player.sendMessage(new TextComponentTranslation("messages.events.change_dimension").setStyle(new Style().setColor(TextFormatting.DARK_GREEN).setItalic(true)));
                changeGameType(player, GameType.ADVENTURE);
            }
            else {
                changeGameType(player, GameType.SURVIVAL);
            }

        }
    }


//    @SubscribeEvent
//    public static void onWorldLoad(WorldEvent.Load event){
//        server = FMLCommonHandler.instance().getMinecraftServerInstance();
//        CapUtils.DATA_WORLD = server.getWorld(DimensionType.OVERWORLD.getId());
//        TimerUpdate.updateWorld(CapUtils.DATA_WORLD);
//        TimerUpdate.updatePoses();
//        loadConstants();
//    }

    public static void loadData(){
        HTMLoaded = Loader.isModLoaded("hbm");
        server = FMLCommonHandler.instance().getMinecraftServerInstance();
        CapUtils.DATA_WORLD = server.getWorld(DimensionType.OVERWORLD.getId());
        TimerUpdate.updateWorld(CapUtils.DATA_WORLD);
        TimerUpdate.updatePoses();
        loadConstants();
    }

    public static void changeGameType(EntityPlayer player, GameType type){
        if (!(player.isCreative() || player.isSpectator())){
            player.setGameType(type);
        }
    }

    public static boolean isHTMLoaded(){
        return HTMLoaded;
    }

    public static void loadConstants(){
        for (String name : ModConstants.INGOT_CONSTANTS.keySet()){
            try {
                Item item = Item.getByNameOrId(name);
                Integer id = Item.getIdFromItem(item);
                POOL.put(id, ModConstants.INGOT_CONSTANTS.get(name));
            }
            catch (NullPointerException ignored){}
        }
    }


    @SideOnly(Side.SERVER)
    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event){
        EntityPlayer entity = event.player;
        EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(entity.getUniqueID());
        boolean flag = TimerUpdate.teamHasActiveBase(CapUtils.getTeamID(player));
        ServerMod.network.sendTo(new UpdateNoneBaseGUI(!flag), player);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onConnectedToServerEvent(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        Minecraft mc=Minecraft.getMinecraft();
        String serverName = (event.isLocal() ? "local game" : mc.getCurrentServerData().serverName);
        if (serverName == null)
            serverName="unknown server";
        changedWindowTitle = mc.getSession().getUsername() + " on "+serverName;
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onDisconnectFromServerEvent(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        Minecraft mc=Minecraft.getMinecraft();
        changedWindowTitle=mc.getSession().getUsername() + " not connected";
    }

    public static String getAndResetChangedWindowTitle() {
        String result=changedWindowTitle;
        changedWindowTitle=null;
        return result;
    }
}
