package com.dentron.servermod.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class LevelUtils {
    public static void addScheduledTask(World w, Runnable r) {
        if (w.isRemote)
            Minecraft.getMinecraft().addScheduledTask(r);
        else
            ((WorldServer) w).addScheduledTask(r);
    }
}
