package com.dentron.servermod.utils;

import com.dentron.servermod.worlddata.TeamsWorldData;
import com.google.common.collect.Lists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class Messages {
    public final static Style msgStyle = new Style().setColor(TextFormatting.GOLD).setBold(true);

    public static ITextComponent getInvitationMsg(int page, int amountOfPages, String senderName, byte senderTeam, UUID invitation){
        ITextComponent teamMsg = new TextComponentString(ModConstants.COLORS_FROM_BYTES.get(senderTeam));
        int teamLength = teamMsg.getFormattedText().length();



        Style team = ModConstants.COLORS_TEXT_STYLE.get(senderTeam).setBold(true);
        Style acceptStyle = new Style().setColor(TextFormatting.DARK_GREEN).setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/teams accept " + invitation));
        Style declineStyle = new Style().setColor(TextFormatting.DARK_RED).setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/teams decline " + invitation));
        Style defaultBold = new Style().setColor(TextFormatting.WHITE).setBold(true);

        ITextComponent turner = new TextComponentString("[<-]").setStyle(getCurrentSwitchStyle(page, amountOfPages, false))
                .appendSibling(new TextComponentString(String.format(" -%2s of %-2s- ", page, amountOfPages)).setStyle(defaultBold))
                .appendSibling(new TextComponentString("[->]").setStyle(getCurrentSwitchStyle(page, amountOfPages, true)));


        teamMsg.setStyle(team);
        String teamFormat = StringUtils.leftPad("Team: ", (48 - teamLength) / 2);
        String senderFormat = StringUtils.leftPad("Sender: ", (46 - senderName.length()) / 2);

        return new TextComponentString("\n---------  INVITATION  ---------\n").setStyle(msgStyle)
                .appendSibling(new TextComponentString(senderFormat)).appendSibling(new TextComponentString(senderName + "\n").setStyle(defaultBold))
                .appendSibling(new TextComponentString(teamFormat).appendSibling(teamMsg)
                        .appendSibling(new TextComponentString("\n\n         [DECLINE]").setStyle(declineStyle))
                        .appendSibling(new TextComponentString("   ").setStyle(new Style().setBold(false).setColor(TextFormatting.DARK_GRAY)))
                        .appendSibling(new TextComponentString("[ACCEPT]\n").setStyle(acceptStyle))
                        .appendSibling(new TextComponentString("------ ").appendSibling(turner)).appendSibling(new TextComponentString(" ------")));
    }

    private static Style getCurrentSwitchStyle(int currentPage, int maxPages, boolean forwardDirection){
        Style deactiveSwitch = new Style().setColor(TextFormatting.DARK_GRAY).setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/teams invitations %s", currentPage)));

        if ((currentPage == maxPages && forwardDirection) || (currentPage == 1 && !forwardDirection)){
            return deactiveSwitch;
        }

        int nextPage = forwardDirection ? currentPage + 1 : currentPage - 1;

        return new Style().setColor(TextFormatting.DARK_GREEN).setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/teams invitations %s", nextPage)));
    }

    public static ITextComponent getCreateMessage(String playerName, byte teamID){
        String teamMsg = getTeamFormatMessage(teamID, true).getFormattedText();

        return new TextComponentTranslation("commands.create.output", playerName, teamMsg).setStyle(new Style().setColor(TextFormatting.WHITE).setBold(true));
    }

    public static ITextComponent getTeamFormatMessage(byte teamID, boolean boldIn){
        Style style = ModConstants.COLORS_TEXT_STYLE.get(teamID).setBold(boldIn);
        return new TextComponentTranslation(ModConstants.COLORS_LOCALIZE_KEYS.get(teamID)).setStyle(style);
    }

    public static ITextComponent getBaseDestroyMessage(byte teamID){
        Style RED_OBFUSCATED = new Style().setObfuscated(true).setBold(true).setColor(TextFormatting.DARK_RED);
        ITextComponent team = getTeamFormatMessage(teamID, true);


        return new TextComponentString("TT").setStyle(RED_OBFUSCATED)
                .appendSibling(new TextComponentTranslation("messages.player.base_destroyed", team).setStyle(new Style().setBold(true).setColor(TextFormatting.DARK_RED).setObfuscated(false)))
                .appendSibling(new TextComponentString("TT"));
    }

    public static ITextComponent getBasePosMessage(byte teamID, BlockPos pos){
        int radius = ModConstants.BASE_MSG_RADIUS;
        TextComponentTranslation component;
        if (pos != null){
            int X = -radius + (int) (Math.random() * (2 * radius));
            int MAX_Z = (int) (Math.sqrt(Math.pow(radius, 2) - Math.pow(X, 2)));
            int Z = -MAX_Z + (int) (Math.random() * (2 * MAX_Z + 1));
            component = new TextComponentTranslation("messages.events.base_pos", radius - 1, pos.getX() + X, pos.getZ() + Z);
            TeamsWorldData.setPosition(teamID, new BlockPos(X, 0, Z));
        }
        else {
            component = new TextComponentTranslation("messages.events.null_base");
            TeamsWorldData.setPosition(teamID, BlockPos.ORIGIN);
        }

        ITextComponent team = getTeamFormatMessage(teamID, true);

        return (new TextComponentString("-------------------------------").setStyle(msgStyle)).appendText("\n")
                .appendSibling(new TextComponentTranslation("messages.events.team_complete", team, ModConstants.ADVANCEMENTS_AMOUNT)).appendText("\n")
                .appendSibling(component).appendText("\n")
                .appendSibling(new TextComponentString("-------------------------------"));
    }

    public static ITextComponent getEntryOrLeaveMessage(String playerName, boolean onlyJoined){
        Style forName = new Style().setColor(TextFormatting.WHITE).setBold(true);
        String name = new TextComponentString(playerName).setStyle(forName).getFormattedText();

        return onlyJoined ? new TextComponentTranslation("messages.teams.entry", name).setStyle(msgStyle) : new TextComponentTranslation("messages.teams.leave", name).setStyle(msgStyle);
    }

    public static ITextComponent getNewLeaderMessage(String newLeaderName){
        Style forName = new Style().setColor(TextFormatting.WHITE).setBold(true);
        String name = new TextComponentString(newLeaderName).setStyle(forName).getFormattedText();


        return new TextComponentTranslation("messages.teams.newLeader", name).setStyle(msgStyle);
    }

    public static ITextComponent getStatsMessage(byte teamID){
        Style forArgs = new Style().setColor(TextFormatting.WHITE).setBold(true);

        List<String> team = CapUtils.getTeamPlayers(teamID).stream().map((o) -> Utils.getPlayerByUUID(o).getName()).collect(Collectors.toList());


        String playersNames = StringUtils.join(team, ", ");
        String thirdArg = new TextComponentString(playersNames).setStyle(forArgs).getFormattedText();
        String fourthArg = new TextComponentString(String.valueOf(TeamsWorldData.getTeam(teamID).getAdv_amount())).setStyle(forArgs).getFormattedText();

        ITextComponent firstComponent = new TextComponentString("---------  STATISTICS  ---------\n");
        ITextComponent secondComponent = new TextComponentTranslation("messages.stat.team", getTeamFormatMessage(teamID, true).getFormattedText() + "\n");
        ITextComponent thirdComponent = new TextComponentTranslation("messages.stat.players", thirdArg + "\n");
        ITextComponent fourthComponent = new TextComponentTranslation("messages.stat.achievments", fourthArg + "\n");

        List<BlockPos> positions = CapUtils.getTeamPosition(teamID);
        positions.remove(BlockPos.ORIGIN);

        positions = positions.stream().filter((o) -> !o.equals(BlockPos.ORIGIN)).collect(Collectors.toList());
        ITextComponent fifthComponent = new TextComponentTranslation("messages.stat.position");

        List<String> teamPositions = positions.stream().map((o) -> String.format(" - [x = %s; z = %s]", o.getX(), o.getZ())).collect(Collectors.toList());
        teamPositions.forEach((o) -> fifthComponent.appendSibling(new TextComponentString('\n' + o +
                (teamPositions.get(teamPositions.size() - 1).equals(o) ? "" : ",")).setStyle(forArgs)));

        if (teamPositions.isEmpty()){
            fifthComponent.appendSibling(new TextComponentTranslation("messages.emote.lock").setStyle(forArgs));
        }

        ITextComponent sixthComponent = new TextComponentString("\n--- ----------- ----------- ---");

        return firstComponent.setStyle(msgStyle).appendSibling(secondComponent).appendSibling(thirdComponent)
                .appendSibling(fourthComponent).appendSibling(fifthComponent).appendSibling(sixthComponent);
    }
}
