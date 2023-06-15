package com.dentron.servermod;

import com.dentron.servermod.blocks.BaseBlock;
import com.dentron.servermod.blocks.CrateBlock;
import com.dentron.servermod.items.BaseSoulItem;
import com.dentron.servermod.network.ActivateBase;
import com.dentron.servermod.network.UpdateBaseOnClient;
import com.dentron.servermod.network.UpdateNoneBaseGUI;
import com.dentron.servermod.renderer.BaseTileRenderer;
import com.dentron.servermod.tileentities.BaseTile;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemColored;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@Mod.EventBusSubscriber(modid = ServerMod.MODID)
public class Registration {

    public static Block BASE_BLOCK;
    public static Block CRATE_BLOCK;
    public static Item BASE_SOUL;

    public static void init() {
        BASE_BLOCK = new BaseBlock("base_block");
        BASE_SOUL = new BaseSoulItem("base_soul").setMaxStackSize(1);
        CRATE_BLOCK = new CrateBlock("crate_block");

        GameRegistry.registerTileEntity(BaseTile.class, BASE_BLOCK.getRegistryName());
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(BASE_BLOCK, CRATE_BLOCK);

    }

    @SubscribeEvent
    public static void registerItemBlocks(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new ItemBlock(BASE_BLOCK).setRegistryName(BASE_BLOCK.getRegistryName()));


        event.getRegistry().register((new ItemColored(CRATE_BLOCK, true))
                .setSubtypeNames(new String[] {"science", "army", "resource"})
                .setRegistryName(CRATE_BLOCK.getRegistryName())
        );
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerRenders(ModelRegistryEvent event) {
        registerRender(Item.getItemFromBlock(BASE_BLOCK));
        registerRender(BASE_SOUL);
        registerRender(CRATE_BLOCK, CrateBlock.EnumType.SCIENCE.getMeta(), "science_crate");
        registerRender(CRATE_BLOCK, CrateBlock.EnumType.ARMY.getMeta(), "army_crate");
        registerRender(CRATE_BLOCK, CrateBlock.EnumType.RESOURCE.getMeta(), "resource_crate");

        ModelLoader.setCustomStateMapper(CRATE_BLOCK, (new StateMap.Builder()).withName(CrateBlock.TYPE).build());
        ClientRegistry.bindTileEntitySpecialRenderer(BaseTile.class, new BaseTileRenderer());
    }

    public static void registerRender(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

    public static void registerRender(Block block, int metadata, String identifier){
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), metadata, new ModelResourceLocation(
                new ResourceLocation(ServerMod.MODID, identifier), "inventory")
        );
    }


    public static void registerPackets(SimpleNetworkWrapper network){
        network.registerMessage(ActivateBase.Handler.class, ActivateBase.class, 1, Side.SERVER);
        network.registerMessage(UpdateBaseOnClient.Handler.class, UpdateBaseOnClient.class, 4, Side.CLIENT);
        network.registerMessage(UpdateNoneBaseGUI.Handler.class, UpdateNoneBaseGUI.class, 5, Side.CLIENT);
    }


    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(BASE_SOUL);
    }
}

