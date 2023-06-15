package com.dentron.servermod.commands;

import com.dentron.servermod.SMEventHandler;
import com.dentron.servermod.utils.CapUtils;
import com.dentron.servermod.worlddata.TeamsWorldData;
import jdk.nashorn.internal.ir.BlockStatement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.*;
import net.minecraft.world.border.WorldBorder;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.server.permission.DefaultPermissionLevel;

import java.util.List;
import java.util.UUID;

public class ServerCommand extends CommandBase {
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

        if (args[0].equals("clearRad")){

        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return DefaultPermissionLevel.OP.ordinal();
    }

    public static void msgConstantLoaded(EntityPlayer player){
        for (Integer id : SMEventHandler.POOL.keySet()) {
            Item item = Item.getItemById(id);
            player.sendMessage(new TextComponentString("[Constant item] - " + "Item: " + item.getUnlocalizedName() + ", Id: " + id + ", Size: " + SMEventHandler.POOL.get(id)));
        }
    }

    public static void randomTeleportTeams(MinecraftServer server){
        for (byte i = 0; i <= 15; i++){
            List<UUID> players = CapUtils.getTeamPlayers(i);
            if (players.isEmpty()){
                continue;
            }

            WorldServer OVERWORLD = server.getWorld(DimensionType.OVERWORLD.getId());
            BlockPos target = getLandTarget(OVERWORLD);


            for (UUID uuid : players){
                EntityPlayer player = CapUtils.DATA_WORLD.getPlayerEntityByUUID(uuid);
                if (player == null){
                    continue;
                }
                player = server.getPlayerList().getPlayerByUUID(uuid);
                player.setPositionAndUpdate(target.getX(), target.getY() + 3, target.getZ());
            }
        }
    }

    public static BlockPos getLandTarget(WorldServer world){
        WorldBorder border = world.getWorldBorder();
        double WOLRD_RADIUS = border.getDiameter() / 2 ;
        boolean flag = true;
        BlockPos target = BlockPos.ORIGIN;

        while (flag) {
            double X = -WOLRD_RADIUS + (Math.random() * (2 * WOLRD_RADIUS + 1)) + border.getCenterX();
            double Z = -WOLRD_RADIUS + (Math.random() * (2 * WOLRD_RADIUS + 1)) + border.getCenterZ();
            target = BlockPos.ORIGIN.add(X, 255, Z);

            target = world.getTopSolidOrLiquidBlock(target);


            Block block = world.getBlockState(target).getBlock();
            if (!block.equals(Blocks.WATER) && !block.equals(Blocks.LAVA)){
                flag = false;
            }
        }

        return target;
    }

    public static void clearRadiation(MinecraftServer server){
        List<EntityPlayerMP> players = server.getPlayerList().getPlayers();

        for (EntityPlayerMP player : players){
            player.setGameType(GameType.CREATIVE);
            player.setHealth(0);
            player.setGameType(GameType.SURVIVAL);
        }
    }
}
