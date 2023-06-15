package com.dentron.servermod.commands.commandTeam;

import com.dentron.servermod.teams.ModPlayerStatsHandler;
import com.dentron.servermod.teams.PlayerStatsProvider;
import com.dentron.servermod.worlddata.TeamsWorldData;
import com.dentron.servermod.utils.ModConstants;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardSaveData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.server.permission.DefaultPermissionLevel;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandTeam extends CommandBase {
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
        if ((args.length <= 1) || (args.length == 2 && args[0].equals("add"))) {
            throw new WrongUsageException("commands.teams.usage");
        }

        EntityPlayerMP player = getPlayer(server, sender, args[1]);
        ModPlayerStatsHandler cap = player.getCapability(PlayerStatsProvider.PLAYER_STATS_CAP, null);
        byte player_team_id = cap.getTeamID();

        if (!args[0].equals("add")) {
            TeamsWorldData.removePlayer(player_team_id, player.getUniqueID(), server.getWorld(player.dimension));
            cap.setTeamID((byte) 0);
            return;
        }

        byte teamID = ModConstants.COLORS.get(args[2]);

        if (player_team_id != 0) {
            sender.sendMessage(new TextComponentString("The player is already on the team").setStyle(new Style().setColor(TextFormatting.RED)));
            return;
        }

        TeamsWorldData.putPlayer(teamID, player.getUniqueID(), server.getWorld(player.dimension));
        cap.setTeamID(teamID);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1){
            return getListOfStringsMatchingLastWord(args, "add", "kick");
        } else if (args.length == 2) {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        } else {
            return (args.length == 3 && args[0].equals("add")) ? getListOfStringsMatchingLastWord(args, ModConstants.COLORS.keySet()) : Collections.emptyList();
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return DefaultPermissionLevel.OP.ordinal();
    }
}
