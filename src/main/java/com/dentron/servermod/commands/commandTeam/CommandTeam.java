package com.dentron.servermod.commands.commandTeam;

import com.dentron.servermod.SMEventHandler;
import com.dentron.servermod.ServerMod;
import com.dentron.servermod.network.SendInvitationWithOD;
import com.dentron.servermod.utils.CapUtils;
import com.dentron.servermod.utils.Messages;
import com.dentron.servermod.utils.Utils;
import com.dentron.servermod.worlddata.ModWorldData;
import com.dentron.servermod.utils.ModConstants;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraftforge.server.permission.DefaultPermissionLevel;

import javax.annotation.Nullable;
import java.util.*;
import java.util.List;

public class CommandTeam extends CommandBase {
    private final List<String> onlinePlayerArgs = Lists.newArrayList("invite", "makeLeader", "kick");
    private final List<String> colorArgs = Lists.newArrayList("create", "stats");
    private final List<String> anyPlayerArgs = Lists.newArrayList("accept", "decline");

    @Override
    public String getName() {
        return "teams";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.teams.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

        if (args.length < 1){
            throw new WrongUsageException("commands.teams.usage");
        }

        EntityPlayerMP commandSender = getCommandSenderAsPlayer(sender);
        byte senderTeamId = CapUtils.getTeamID(commandSender);
        boolean isSenderTeamLeader = Utils.isTeamLeader(commandSender);
        boolean commandSenderInTeam = senderTeamId != 0;
        boolean teamIsExist = ModWorldData.getTeams(CapUtils.DATA_WORLD).hasKey(String.valueOf(senderTeamId));
        boolean teamHasActiveBase = CapUtils.hasActiveBase(senderTeamId);

        if (args.length == 1){
            switch (args[0]){
                case "create":
                    throw new WrongUsageException("commands.teams.create.color");
                case "invite":
                    throw new WrongUsageException("commands.teams.invite.player");
                case "kick":
                    throw new WrongUsageException("commands.teams.kick.player");
                case "makeLeader":
                    throw new WrongUsageException("commands.teams.makeLeader.player");
                case "stats":
                    throw new WrongUsageException("commands.teams.stats.color");
                case "accept":
                    throw new WrongUsageException("commands.teams.accept.uuid");
                case "decline":
                    throw new WrongUsageException("commands.teams.decline.uuid");
                case "setSpawnPointOnRandomGen":
                    if (!commandSenderInTeam) {
                        throw new CommandException("commands.teams.noTeam");
                    }
                    break;
                case "leave":
                    if (!commandSenderInTeam){
                        throw new CommandException("commands.teams.noTeam");
                    } else if (isSenderTeamLeader) {
                        throw new CommandException("commands.teams.leave.leader");
                    }
            }
        }



        if (args.length == 2){
            switch (args[0]){
                case "create":
                    if (commandSenderInTeam) {
                        throw new CommandException("commands.teams.inTeam");
                    }
                    break;
                case "invite":
                case "kick":
                    if (!commandSenderInTeam){
                        throw new CommandException("commands.teams.noTeam");
                    } else if (!isSenderTeamLeader) {
                        throw new CommandException("commands.teams.permission");
                    }
                    break;
                case  "makeLeader":
                    if (!commandSenderInTeam) {
                        throw new CommandException("commands.teams.inTeam");
                    } else if (!isSenderTeamLeader) {
                        throw new CommandException("commands.teams.permission");
                    }
                    break;
                case "accept":{
                    if (commandSenderInTeam) {
                        throw new CommandException("commands.teams.inTeam");
                    }
                    break;
                }
                case "stats":
                    if (!teamIsExist) {
                        throw new CommandException("commands.teams.team.exist");
                    }
                    break;
            }
        }

        if (onlinePlayerArgs.contains(args[0])){
            boolean isPlayerOnline = Lists.newArrayList(server.getPlayerList().getOnlinePlayerNames()).contains(args[1]);
            if (!isPlayerOnline){
                throw new PlayerNotFoundException("commands.generic.player.notFound", args[1]);
            }

            EntityPlayerMP target = server.getPlayerList().getPlayerByUsername(args[1]);

            switch (args[0]){
                case "invite":
                    this.invitePlayer(target, commandSender);
                    break;
                case "makeLeader":
                    this.makeLeader(target, commandSender); // exception ~player is leader~
                    break;
                case "kick":
                    this.kickPlayer(target, commandSender);
                    break;
            }
            return;

        } else if (colorArgs.contains(args[0])){
            Object obj = ModConstants.COLORS.get(args[1]);

            if (obj == null){
                throw new CommandException("commands.teams.color.noSuchColor");
            }

            byte colorID = (byte) obj;

            switch (args[0]) {
                case "create":
                    this.createTeam(commandSender, colorID);
                    break;
                case "stats":
                    this.sendStatsToPlayer(colorID, commandSender);
                    break;
            }
            return;

        } else if (anyPlayerArgs.contains(args[0])){
            boolean playerHasSuchInvitation = InvitationsBuffer.playerHasSuchInvitation(UUID.fromString(args[1]), commandSender);

            if (!playerHasSuchInvitation){
                throw new CommandException("commands.teams.uuid.noSuchUUID");
            }

            switch (args[0]){
                case "accept":
                    this.acceptInviation(commandSender, UUID.fromString(args[1]));
                    break;
                case "decline":
                    this.removeIntvitation(commandSender, UUID.fromString(args[1]), true);
                    break;
            }
            return;

        } else {
            switch (args[0]){
                case "invitations":
                    if (args.length == 1) {
                        this.sendInvitationsToPlayer(commandSender, 0);
                        return;
                    }

                    try {
                        int page = parseInt(args[1], 1, InvitationsBuffer.getPlayerInvitations(commandSender).size());
                        this.sendInvitationsToPlayer(commandSender, page - 1);
                    } catch (NumberInvalidException e){
                        throw new CommandNotFoundException();
                    }
                    return;
                case "leave":
                    this.leaveTeam(commandSender);
                    return;
                case "setSpawnPointOnRandomGen":
                    this.setSpawnPointOnRandomGen(commandSender);
                    return;
            }
        }

        throw new WrongUsageException("commands.teams.usage");
    }


    @Override
    public List<String> getTabCompletions(MinecraftServer server,  ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        EntityPlayerMP commandSender;

        try {
            commandSender = getCommandSenderAsPlayer(sender);
        } catch (PlayerNotFoundException e) {
            throw new RuntimeException(e);
        }


        byte senderTeamId = CapUtils.getTeamID(commandSender);
        boolean isSenderTeamLeader = Utils.isTeamLeader(commandSender);
        boolean commandSenderInTeam = senderTeamId != 0;
        if (args.length >= 3){
            return Collections.emptyList();
        }

        if (args.length == 1){
            if (isSenderTeamLeader){
                return getListOfStringsMatchingLastWord(args, "kick", "leave", "stats", "makeLeader", "invitations", "invite", "setSpawnPointOnRandomGen");
            } else if (commandSenderInTeam) {
                return getListOfStringsMatchingLastWord(args,  "leave", "stats", "invitations", "setSpawnPointOnRandomGen");
            } else {
                return getListOfStringsMatchingLastWord(args, "create", "stats", "invitations");
            }
        } else {
            if (onlinePlayerArgs.contains(args[0])){
                return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
            } else if (colorArgs.contains(args[0])) {
                switch (args[0]){
                    case "create":
                        return getListOfStringsMatchingLastWord(args, getAvaliableColors());
                    case "stats":
                        return getListOfStringsMatchingLastWord(args, getUnavaliableColor());
                }
            } else if (args[0].equals("invitations") || args[0].equals("leave")) {
                return Collections.emptyList();
            } else {
                return getListOfStringsMatchingLastWord(args, InvitationsBuffer.getPlayerInvitations(commandSender));
            }
        }

        return Collections.emptyList();
    }

    public List<String> getAvaliableColors(){
        Set<String> existingTeams = getUnavaliableColor();
        Set<String> allColors = Sets.newHashSet(ModConstants.COLORS.keySet());


        List<String> toReturn = Lists.newArrayList(Sets.difference(allColors, existingTeams));
        toReturn.remove("white");
        return toReturn;
    }

    public Set<String> getUnavaliableColor(){
        NBTTagCompound teams = ModWorldData.getTeams(CapUtils.DATA_WORLD);
        Set<String> existingTeams = new HashSet<>();

        for (byte i = 1; i < 16; i++){
            if (teams.hasKey(String.valueOf(i))){
                existingTeams.add(ModConstants.COLORS_BYTES.get(i));
            }
        }

        return existingTeams;
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return DefaultPermissionLevel.ALL.ordinal();
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    // Command Execution

    public void acceptInviation(EntityPlayerMP player, UUID invitaion){
        EntityPlayerMP sender = Utils.getPlayerByUUID(invitaion);
        byte teamID = CapUtils.getTeamID(sender);
        Utils.sendMessageToTeam(Messages.getEntryOrLeaveMessage(player.getName(), true), teamID);
        this.putPlayerInTeam(player, teamID);
        this.removeIntvitation(player, invitaion, false);
    }

    public void removeIntvitation(EntityPlayerMP player, UUID invitation, boolean resend) {
        int index = InvitationsBuffer.removeInvitation(player, invitation);
        if (InvitationsBuffer.getPlayerInvitations(player).isEmpty() || !resend) {
            ServerMod.network.sendTo(new SendInvitationWithOD("", 0, 0, (byte) 0, UUID.randomUUID(), true), player);
        } else {
            this.sendInvitationsToPlayer(player, index);
        }
    }

    public void setSpawnPointOnRandomGen(EntityPlayerMP player) throws CommandException {
        List<BlockPos> list = ModWorldData.getRandomGenPositions(CapUtils.DATA_WORLD);

        if (list.isEmpty()){
            throw new CommandException("commands.team.spawnpoint.notGen");
        }

        byte teamID = CapUtils.getTeamID(player);
        BlockPos position = list.get(teamID - 1);
        player.setSpawnPoint(position, true);
        player.sendMessage(Messages.getSuccessCommandMessage());
    }

    public void createTeam(EntityPlayerMP player, byte color) throws CommandException {
        if (getUnavaliableColor().contains(ModConstants.COLORS_BYTES.get(color))){
            throw new CommandException("commands.teams.color.unavailable", getAvaliableColors());
        }

        this.putPlayerInTeam(player, color);
        Utils.sendMessageToAll(Messages.getCreateMessage(player.getName(), color));
    }

    private void putPlayerInTeam(EntityPlayerMP player, byte color){
        ModWorldData.getTeamOrCreate(color);
        ModWorldData.addPlayer(color, player.getUniqueID());
        CapUtils.getStatsCapability(player).setTeamID(color);
        SMEventHandler.updateDisplayName(player, false);
        recountTeamAdvancements(color);
    }

    public void invitePlayer(EntityPlayerMP target, EntityPlayerMP sender) throws CommandException {
        if (target.equals(sender)){
            throw new CommandException("commands.teams.incorrectTarget");
        }

        if (InvitationsBuffer.playerHasSuchInvitation(sender.getUniqueID(), target)){
            throw new CommandException("commands.teams.invite.more");
        }

        if (InvitationsBuffer.getPlayerInvitations(target).size() >= 99){
            throw new CommandException("commands.teams.invite.limit");
        }

        InvitationsBuffer.createInvitations(sender.getUniqueID(), target.getUniqueID());
        this.sendInvitationsToPlayer(target, InvitationsBuffer.getPlayerInvitations(target).size() - 1);
    }

    public void makeLeader(EntityPlayerMP newLeader, EntityPlayerMP oldLeader) throws CommandException {
        if (Utils.isTeamLeader(newLeader)){
            throw new CommandException("commands.teams.incorrectTarget");
        } else if (!Utils.playerInSameTeam(newLeader, CapUtils.getTeamID(oldLeader))) {
            throw new CommandException("commands.teams.notSameTeam");
        }

        byte teamID = CapUtils.getTeamID(oldLeader);
        List<UUID> players = CapUtils.getTeamPlayers(teamID);
        int i = players.indexOf(newLeader.getUniqueID());
        ModWorldData.swapElementWithFirst(i, teamID);
        Utils.sendMessageToTeam(Messages.getNewLeaderMessage(newLeader.getName()), teamID);
        SMEventHandler.updateDisplayName(newLeader, false);
        SMEventHandler.updateDisplayName(oldLeader, false);
    }

    public void kickPlayer(EntityPlayerMP target, EntityPlayerMP sender) throws CommandException {
        byte teamID = CapUtils.getTeamID(sender);

        if (target.equals(sender)){
            throw new CommandException("commands.teams.incorrectTarget");
        } else if (!Utils.playerInSameTeam(target, teamID)) {
            throw new CommandException("commands.teams.notSameTeam");
        }
        ModWorldData.toDefaultTeam(target);

        Utils.sendMessageToTeam(Messages.getEntryOrLeaveMessage(target.getName(), false), teamID);
        target.sendMessage(new TextComponentTranslation("commands.teams.kick.message.toPlayer", sender.getName()).setStyle(new Style().setColor(TextFormatting.RED)));
        SMEventHandler.updateDisplayName(target, false);
        recountTeamAdvancements(teamID);
    }

    public void sendStatsToPlayer(byte color, EntityPlayerMP target){
        target.sendMessage(Messages.getStatsMessage(color));
    }

    public void sendInvitationsToPlayer(EntityPlayerMP target, Integer index){
        List<UUID> invitations = InvitationsBuffer.getPlayerInvitations(target);
        if (invitations.isEmpty()){
            target.sendMessage(new TextComponentTranslation("commands.teams.invitations.output")
                    .setStyle(new Style().setColor(TextFormatting.GOLD).setBold(true)));
            return;
        }

        UUID sender = invitations.get(index);

        EntityPlayerMP player = Utils.getPlayerByUUID(sender);

//        if (!server.getPlayerList().getPlayers().contains(player)){
//            player = Utils.loadPlayer(sender, CapUtils.DATA_WORLD);
//        }

        byte playerTeam = CapUtils.getTeamID(player);

        ServerMod.network.sendTo(new SendInvitationWithOD(player.getName(), index + 1, invitations.size(), playerTeam, sender, false), target);
    }

    public void leaveTeam(EntityPlayerMP player){
        byte teamID = ModWorldData.toDefaultTeam(player);
        SMEventHandler.updateDisplayName(player, false);
        Utils.sendMessageToTeam(Messages.getEntryOrLeaveMessage(player.getName(), false), teamID);
        recountTeamAdvancements(teamID);
        player.sendMessage(Messages.getSuccessCommandMessage());
    }

    private void recountTeamAdvancements(byte teamId){
        ModWorldData.setTeamAdvancementAmount(teamId, Utils.recountTeamAdvancements(teamId, ModWorldData.getTeam(teamId).getAdvAmount()));
    }

}