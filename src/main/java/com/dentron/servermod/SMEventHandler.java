package com.dentron.servermod;


import com.dentron.servermod.commands.commandTeam.InvitationsBuffer;
import com.dentron.servermod.network.UpdateNoneBaseGUI;
import com.dentron.servermod.teams.ModPlayerStatsHandler;
import com.dentron.servermod.utils.Messages;
import com.dentron.servermod.utils.Utils;
import com.dentron.servermod.worlddata.TeamsWorldData;
import com.dentron.servermod.timers.TimerUpdate;
import com.dentron.servermod.utils.CapUtils;
import com.dentron.servermod.utils.ModConstants;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.util.*;

import static com.dentron.servermod.utils.Utils.isTeamLeader;

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

        boolean flag = true;

        for (UUID uuid : CapUtils.getTeamPlayers(teamID)){
            EntityPlayerMP teammate = Utils.getPlayerByUUID(uuid);

            if (teammate.getUniqueID() == player.getUniqueID()){
                continue;
            }

            if (teammate.getAdvancements().getProgress(event.getAdvancement()).isDone()) {
                flag = false;
            }
        }

        if (flag){
            boolean needMsg = TeamsWorldData.addAdvancement(teamID);

            if (needMsg){
                sendCoordinatesToAll(teamID);
            }
        }
    }

    public static void sendCoordinatesToAll(byte teamID){
        BlockPos pos = Utils.getTeamBasePos(teamID);
        Utils.sendMessageToAll(Messages.getBasePosMessage(teamID, pos));
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
        InvitationsBuffer.resetInvitations();
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


    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayer entity = event.player;
        EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(entity.getUniqueID());
        boolean flag = TimerUpdate.teamHasActiveBase(CapUtils.getTeamID(player));
        ServerMod.network.sendTo(new UpdateNoneBaseGUI(!flag), player);

        updateDisplayName(player, true);
    }

    public static void updateDisplayName(EntityPlayerMP player, boolean updateOthersForPlayer){
        SPacketPlayerListItem packetToAll = getUpdateNamePacket(Collections.singletonList(player));
        server.getPlayerList().sendPacketToAllPlayers(packetToAll);

        if (!updateOthersForPlayer){return;}

        List<EntityPlayerMP> playersList = Lists.newArrayList(server.getPlayerList().getPlayers());
        playersList.remove(player);

        if (!playersList.isEmpty()) {
            SPacketPlayerListItem packetToPlayer = getUpdateNamePacket(playersList);
            player.connection.sendPacket(packetToPlayer);
        }
    }

    @SuppressWarnings("unchecked")
    public static SPacketPlayerListItem getUpdateNamePacket(Iterable<EntityPlayerMP> playersIn) {
        SPacketPlayerListItem packet = new SPacketPlayerListItem(SPacketPlayerListItem.Action.UPDATE_DISPLAY_NAME, playersIn);

        try {
            Field fieldDisplayName = ObfuscationReflectionHelper.findField(SPacketPlayerListItem.AddPlayerData.class, "field_179965_e");
            Field fieldPlayers = ObfuscationReflectionHelper.findField(SPacketPlayerListItem.class, "field_179769_b");

            List<SPacketPlayerListItem.AddPlayerData> list = (List<SPacketPlayerListItem.AddPlayerData>) fieldPlayers.get(packet);

            Iterator<EntityPlayerMP> iter = playersIn.iterator();

            for (SPacketPlayerListItem.AddPlayerData data : list){
                ITextComponent displayName = getDisplayName(iter.next());

                fieldDisplayName.set(data, displayName);
            }

        } catch (Exception e){
            e.printStackTrace();
        }

        return packet;
    }

    private static ITextComponent getDisplayName(EntityPlayerMP player){
        TextFormatting color = ModConstants.COLORS_TEXT_STYLE.get(CapUtils.getTeamID(player)).getColor();

        ITextComponent displayName = new TextComponentString(player.getName());
        Style style = displayName.getStyle().setColor(color);
        displayName.setStyle(style);

        if (isTeamLeader(player)){
            displayName.appendText(" ");
            Style starStyle = new Style().setColor(TextFormatting.YELLOW);
            displayName.appendSibling(new TextComponentTranslation("nickname.emote.star").setStyle(starStyle));
        }

        return displayName;
    }



//    @SubscribeEvent
//    public static void onChatMessage(ClientChatEvent event){
//        String playerName = event.
//        String msg = event.getMessage();
//
//        TextFormatting color = ModConstants.COLORS_TEXT_STYLE.get(CapUtils.getTeamID(event.getPlayer())).getColor();
//
//        String formattedName = new TextComponentString(playerName).setStyle(new Style().setColor(color)).getFormattedText();
//        msg = "<" + formattedName + "> " + msg;
//
//        event.setComponent(new TextComponentString(msg));
//    }

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

//    @SubscribeEvent
//    public void test(RenderGameOverlayEvent event){
//        event.getType().equals(RenderGameOverlayEvent.ElementType.CHAT);
//        event.getResolution().
//
//        ClickEvent action =
//    }

    public static String getAndResetChangedWindowTitle() {
        String result=changedWindowTitle;
        changedWindowTitle=null;
        return result;
    }
}
