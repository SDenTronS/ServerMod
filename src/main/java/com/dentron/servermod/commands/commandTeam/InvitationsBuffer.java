package com.dentron.servermod.commands.commandTeam;

import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.*;

public class InvitationsBuffer {
    private static HashMap<UUID, List<UUID>> BUFFER = new HashMap<>();


    public static void createInvitations(UUID sender, UUID target){
        List<UUID> list = BUFFER.get(target);
        if (list == null){
            list = new ArrayList<>();
        }

        list.add(sender);
        BUFFER.put(target, list);
    }

    public static void removeSentInvitations(UUID sender){
        for (Map.Entry<UUID, List<UUID>> entry : BUFFER.entrySet()){
            entry.getValue().remove(sender);
        }
    }

    public static void resetInvitations(){
        BUFFER = new HashMap<>();
    }

    public static int removeInvitation(EntityPlayerMP player, UUID invitation){
        int toRetutn = BUFFER.get(player.getUniqueID()).indexOf(invitation);
        BUFFER.get(player.getUniqueID()).remove(invitation);
        return toRetutn;
    }

    public static boolean playerHasSuchInvitation(UUID sender, EntityPlayerMP player){
        UUID playerUUID = player.getUniqueID();

        return BUFFER.get(playerUUID) != null && BUFFER.get(playerUUID).contains(sender);
    }

    public static List<UUID> getPlayerInvitations(EntityPlayer player){
        UUID playerUUID = player.getUniqueID();
        return BUFFER.containsKey(playerUUID) ? BUFFER.get(playerUUID) : new ArrayList<>();
    }
}
