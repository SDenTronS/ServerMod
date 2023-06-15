package com.dentron.servermod.blocks;

import com.dentron.servermod.SMEventHandler;
import com.dentron.servermod.utils.ModConstants;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;

import java.util.ArrayList;
import java.util.List;

public class CrateBlock extends BlockFalling {
    public static final PropertyEnum<CrateBlock.EnumType> TYPE = PropertyEnum.<CrateBlock.EnumType>create("type", CrateBlock.EnumType.class);

    public CrateBlock(String name) {
        super(Material.WOOD);
        this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, EnumType.SCIENCE));

        this.setRegistryName(name);
        this.setUnlocalizedName(name);

        setResistance(64000F);
        setHardness(100F);
    }

    @Override
    protected boolean canSilkHarvest() {
        return false;
    }

    @Override
    public boolean canDropFromExplosion(Explosion explosionIn) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(TYPE, CrateBlock.EnumType.byMetadata(meta));
    }

    public int getMetaFromState(IBlockState state)
    {
        return ((CrateBlock.EnumType)state.getValue(TYPE)).getMeta();
    }

    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {TYPE});
    }


    public static enum EnumType implements IStringSerializable{
        SCIENCE(0, "science_crate"),
        ARMY(1, "army_crate"),
        RESOURCE(2, "resource_crate");

        private static final CrateBlock.EnumType[] META_LOOKUP = new CrateBlock.EnumType[values().length];
        private final int meta;
        private final String name;


        private EnumType(int meta, String name)
        {
            this.meta = meta;
            this.name = name;
        }

        public int getMeta()
        {
            return this.meta;
        }

        public String toString()
        {
            return this.name;
        }

        public static CrateBlock.EnumType byMetadata(int meta)
        {
            if (meta < 0 || meta >= META_LOOKUP.length)
            {
                meta = 0;
            }

            return META_LOOKUP[meta];
        }


        @Override
        public String getName() {
            return this.name;
        }

        static
        {
            for (CrateBlock.EnumType crate$enumtype : values())
            {
                META_LOOKUP[crate$enumtype.getMeta()] = crate$enumtype;
            }
        }
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        List<ItemStack> drop = new ArrayList<>();
        if (!SMEventHandler.isHTMLoaded()){
            return drop;
        }

        switch (state.getValue(TYPE)){
            case SCIENCE:
                drop = getScienceDrop();
                break;
            case ARMY:
                drop = getArmyDrop();
                break;
            case RESOURCE:
                drop = getResourceDrop();
                break;
            default:
                break;
        }

        return drop;
    }


    private static List<ItemStack> getScienceDrop(){
        List<ItemStack> drop = new ArrayList<>();


        Item htm_recipe = Item.getByNameOrId("hbm:assembly_template");
        for (int i = 0; i <= 7; i++){
            ItemStack item = new ItemStack(htm_recipe, 1, 0);
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger("type", (int) (Math.random() * 384));
            item.setTagCompound(nbt);
            drop.add(item);
        }

        return drop;
    }

    private static List<ItemStack> getResourceDrop(){
        List<ItemStack> drop = new ArrayList<>();

        List<Integer> pool = new ArrayList<>();
        for (int i = 0; i <= 7; i++){
            Integer poolID = Lists.newArrayList(SMEventHandler.POOL.keySet()).get((int) (Math.random() * SMEventHandler.POOL.size()));
            pool.add(poolID);
        }

        for (Integer id : pool){
            Integer itemID = id + (int) (Math.random() * (SMEventHandler.POOL.get(id) + 1));

            Item item = Item.getItemById(itemID);
            ItemStack stack = new ItemStack(item, (int) (Math.random() * (33)));
            drop.add(stack);
        }

        return drop;
    }

    private static List<ItemStack> getArmyDrop(){
        List<ItemStack> drop = new ArrayList<>();

        List<String> equipment = ModConstants.WEAPONS.get((int) (Math.random() * ModConstants.WEAPONS.size()));
        Item weapon = Item.getByNameOrId(equipment.get(0));
        Item ammo = Item.getByNameOrId(equipment.get(1));
        Item grenade = Item.getByNameOrId("hbm:grenade_strong");

        drop.add(new ItemStack(weapon));
        drop.add(new ItemStack(ammo, Integer.valueOf(equipment.get(2))));
        drop.add(new ItemStack(grenade, 5));

        return drop;
    }



}
