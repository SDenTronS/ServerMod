package com.dentron.servermod;

import com.dentron.servermod.commands.ServerCommand;
import com.dentron.servermod.commands.commandTeam.CommandTeam;
import com.dentron.servermod.screens.NoneBaseGUI;
import com.dentron.servermod.screens.ScreenHandler;
import com.dentron.servermod.teams.ModPlayerStatsHandler;
import com.dentron.servermod.teams.PlayerStatsStorage;
import com.dentron.servermod.utils.CapUtils;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;

@Mod(modid = ServerMod.MODID, name = ServerMod.NAME, version = ServerMod.VERSION)
public class ServerMod
{

    public static final String MODID = "servermod";
    public static final String NAME = "Server Mod";
    public static final String VERSION = "1.12.2-2.0";

    @Mod.Instance(ServerMod.MODID)
    public static ServerMod instance;
    public static SimpleNetworkWrapper network;
    private static Logger logger;

    private ScreenHandler guiHandler = new ScreenHandler();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        ServerMod.network = NetworkRegistry.INSTANCE.newSimpleChannel(ServerMod.MODID);
        Registration.init();
        Registration.registerPackets(ServerMod.network);
        CapabilityManager.INSTANCE.register(ModPlayerStatsHandler.class, new PlayerStatsStorage(), ModPlayerStatsHandler::new);
        MinecraftForge.EVENT_BUS.register(new CapabilityHandler());
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, guiHandler);
        // some example code
        logger.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    @EventHandler
    @SideOnly(Side.CLIENT)
    public void initClient(FMLInitializationEvent event){
        MinecraftForge.EVENT_BUS.register(new NoneBaseGUI());
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event){
        event.registerServerCommand(new CommandTeam());
        event.registerServerCommand(new ServerCommand());
    }

    @EventHandler
    public void serverStarted(FMLServerStartedEvent event){
        SMEventHandler.loadData();
    }

}
