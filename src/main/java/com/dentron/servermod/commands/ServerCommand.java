package com.dentron.servermod.commands;

import com.dentron.servermod.SMEventHandler;
import com.dentron.servermod.commands.commandTeam.InvitationsBuffer;
import com.dentron.servermod.utils.CapUtils;
import com.dentron.servermod.utils.Messages;
import com.dentron.servermod.utils.ModConstants;
import com.dentron.servermod.utils.Utils;
import com.dentron.servermod.worlddata.TeamsWorldData;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.*;
import net.minecraft.world.border.WorldBorder;
import net.minecraftforge.server.permission.DefaultPermissionLevel;


import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class ServerCommand extends CommandBase {
    private static List<BlockPos> positions;
    private static WorldBorder border;
    private static double spawn_radius;


    @Override
    public String getName() {
        return "servermod";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return null;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0){
            throw new WrongUsageException("");
        }

        if (args[0].equals("loaded")){
            msgConstantLoaded((EntityPlayer) sender);
            return;
        }

        if (args[0].equals("randomTeleport")){
            randomTeleportTeams(server);
            return;
        }

        if (args[0].equals("withdrawTeam") && args.length == 2){
            withdrawTeam(args[1], getCommandSenderAsPlayer(sender));
            return;
        }

        if (args[0].equals("wtcp")){
            BlockPos senderPos = sender.getPosition();
            EntityPlayerMP player = (EntityPlayerMP) sender.getEntityWorld().getClosestPlayer(senderPos.getX(), senderPos.getY(), senderPos.getZ(), 10, false);
            TeamsWorldData.toDefaultTeam(player);
            SMEventHandler.updateDisplayName(player, false);
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1){
            return getListOfStringsMatchingLastWord(args, "loaded", "withdrawTeam");
        }

        if (args[0].equals("withdrawTeam")){
            return getListOfStringsMatchingLastWord(args, getAllPlayerNamesInTeams().keySet());
        }

        return Collections.emptyList();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return DefaultPermissionLevel.OP.ordinal();
    }

    public void msgConstantLoaded(EntityPlayer player){
        for (Integer id : SMEventHandler.POOL.keySet()) {
            Item item = Item.getItemById(id);
            player.sendMessage(new TextComponentString("[Constant item] - " + "Item: " + item.getUnlocalizedName() + ", Id: " + id + ", Size: " + SMEventHandler.POOL.get(id)));
        }
    }

    public void withdrawTeam(String playerName, EntityPlayerMP sender) throws PlayerNotFoundException {
        HashMap<String, UUID> players = getAllPlayerNamesInTeams();
        if (!players.containsKey(playerName)){
            throw new PlayerNotFoundException("commands.generic.player.notFound", playerName);
        }


        UUID targetUUID = players.get(playerName);
        EntityPlayerMP player = Utils.getPlayerByUUID(targetUUID);
        boolean isTeamLeader = Utils.isTeamLeader(player);

        byte teamID = TeamsWorldData.toDefaultTeam(player);

        if (Utils.isPlayerOnline(targetUUID)){
            player.sendMessage(new TextComponentTranslation("commands.teams.kick.message.toPlayer", sender.getName()).setStyle(new Style().setColor(TextFormatting.RED)));
            SMEventHandler.updateDisplayName(player, false);
        }


        if (!isTeamLeader){
            return;
        }

        InvitationsBuffer.removeSentInvitations(targetUUID);
        List<UUID> team = CapUtils.getTeamPlayers(teamID);

        if (team.isEmpty()){
            TeamsWorldData.removeTeam(teamID);
            return;
        }

        TeamsWorldData.setTeamAdvancementAmount(teamID, Utils.recountTeamAdvancements(teamID, TeamsWorldData.getTeam(teamID).getAdv_amount()));
        UUID newLeader = team.get(0);
        player = Utils.getPlayerByUUID(newLeader);
        if (Utils.isPlayerOnline(newLeader)){
            SMEventHandler.updateDisplayName(player, false);
        }
        Utils.sendMessageToTeam(Messages.getNewLeaderMessage(player.getName()), teamID);

    }

    public void randomTeleportTeams(MinecraftServer server) throws CommandException {
            positions = new ArrayList<>();
            WorldServer OVERWORLD = server.getWorld(DimensionType.OVERWORLD.getId());
            border = OVERWORLD.getWorldBorder();
            double WOLRD_RADIUS = (border.getDiameter() * (1 - ModConstants.PERCENT_OF_BORDER_DIAMETR / 100)) / 2;
            double firstX = -WOLRD_RADIUS + (Math.random() * (2 * WOLRD_RADIUS + 1)) + border.getCenterX();
            double firstZ = -WOLRD_RADIUS + (Math.random() * (2 * WOLRD_RADIUS + 1)) + border.getCenterZ();
            positions.add(OVERWORLD.getTopSolidOrLiquidBlock(new BlockPos(firstX, 0, firstZ)));
            spawn_radius = border.getDiameter() * (ModConstants.PERCENT_OF_BORDER_DIAMETR / 100);

            for (byte i = 1; i <= 15; i++){
                int last_index = positions.size() - 1;
                BlockPos nextPos = getNextPos(OVERWORLD, positions.get(last_index).getX(), positions.get(last_index).getZ());
                positions.add(nextPos);
            }

            System.out.println(positions.stream().map(BlockPos::getX).collect(Collectors.toList()));
            System.out.println(positions.stream().map(BlockPos::getZ).collect(Collectors.toList()));
    }


    private BlockPos getNextPos(WorldServer world, double previousX, double previousZ) throws CommandException {
        double angle = getRandomAngleFromAvaliable(previousX, previousZ);
        double SPAWN_RADIUS = spawn_radius;

        double X = Math.cos(Math.toRadians(angle)) * (SPAWN_RADIUS + 1) + previousX;
        double Z = Math.sin(Math.toRadians(angle)) * (SPAWN_RADIUS + 1) + previousZ;

        return world.getTopSolidOrLiquidBlock(new BlockPos(X, 0, Z));
    }

    private List<String> getAvaliableArcs(double centralX, double centralZ) throws CommandException {
        List<Double> avaliableAngles = new ArrayList<>();

        for (double angle = 0; angle <= 360; angle += ModConstants.ANGLE_STEP){
            if (isAvalible(angle, centralX, centralZ)){
                avaliableAngles.add(angle);
            }
        }
        System.out.println(avaliableAngles);

        return collectAngles(avaliableAngles);
    }

    private int getRandomAngleFromAvaliable(double centralX, double centralZ) throws CommandException {
        List<String> avaliableArcs = getAvaliableArcs(centralX, centralZ);



        String arc = avaliableArcs.get((int) (Math.random() * (avaliableArcs.size())));
        List<Double> angles = stringToArc(arc);

        int toReturn = (int) (Math.random() * (angles.get(1) - angles.get(0) + 1) + angles.get(0));
        System.out.println(avaliableArcs + " " + toReturn);
        return toReturn;
    }

    private List<String> collectAngles(List<Double> avaliableAngles) throws CommandException {
        if (avaliableAngles.isEmpty()){
            throw new CommandException("commands.server.random.lack");
        }

        List<String> collection = new ArrayList<>();

        ListIterator<Double> iter = avaliableAngles.listIterator();
        double start = iter.next();
        double step = ModConstants.ANGLE_STEP;

        while (iter.hasNext()){
            double next = iter.next();
            if (start + step != next){
                collection.add(arcToString(start, avaliableAngles.get(iter.previousIndex() - 1)));
                start = next;
                step = 0;
            }

            step += ModConstants.ANGLE_STEP;
        }

        collection.add(arcToString(start, avaliableAngles.get(avaliableAngles.size() - 1)));
        return collection;
    }

    private String arcToString(double start, double finish){
        return String.valueOf(start) + ':' + finish;
    }

    private List<Double> stringToArc(String str){
        String[] arc = str.split(":");
        return Arrays.stream(arc).map(Double::valueOf).collect(Collectors.toList());
    }


    private boolean isAvalible(double angle, double X, double Z){
        boolean flag = true;
        double SPAWN_RADIUS = spawn_radius;

        double checkX = Math.cos(Math.toRadians(angle)) * SPAWN_RADIUS + X;
        double checkZ = Math.sin(Math.toRadians(angle)) * SPAWN_RADIUS + Z;
        double borderEdgeX1 = border.getCenterX() + border.getDiameter() / 2;
        double borderEdgeX2 = border.getCenterX() - border.getDiameter() / 2;
        double borderEdgeZ1 = border.getCenterZ() + border.getDiameter() / 2;
        double borderEdgeZ2 = border.getCenterZ() - border.getDiameter() / 2;

        boolean borderCheck = (borderEdgeX1 > checkX) && (borderEdgeX2 < checkX) && (borderEdgeZ1 > checkZ) && (borderEdgeZ2 < checkZ);

        for (BlockPos pos : positions){
            double posX = pos.getX();
            double posZ = pos.getZ();

            if ((Math.hypot(checkX - posX, checkZ - posZ) < spawn_radius) || !borderCheck){
                flag = false;
                break;
            }
        }

        return flag;
    }

    private HashMap<String, UUID> getAllPlayerNamesInTeams(){
        HashMap<String, UUID> toRetutn = new HashMap<>();
        for (byte i = 1; i < 16; i++){
           for (UUID uuid : CapUtils.getTeamPlayers(i)){
               toRetutn.put(Utils.getPlayerByUUID(uuid).getName(), uuid);
           }
        }
        return toRetutn;
    }
}
