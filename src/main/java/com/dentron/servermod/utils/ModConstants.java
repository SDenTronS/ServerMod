package com.dentron.servermod.utils;


import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

import java.util.*;

public class ModConstants {
    public static final int INVITATIONS_CHATLINE_ID = 1581;
    public static final int MAX_TEAM_MEMBERS = 999;
    public static final int BASE_TIMER = 5400;
    public static final double PERCENT_OF_BORDER_DIAMETR = 20;
    public static final double PERCENT_RADIUS_INACCURASY = 22.5;
    public static final double ANGLE_STEP = 5;  // must be divider 360
    public static final byte PLAYER_LIVES = (byte) 15;
    public static final byte ADVANCEMENTS_AMOUNT = 30;
    public static final int BASE_MSG_RADIUS = 5000 + (1);
    public static final Style RED_BOLD = new Style().setBold(true).setColor(TextFormatting.RED);
    public static final Style WHITE_BOLD = new Style().setBold(true).setColor(TextFormatting.WHITE);

    public static final BiMap<String, Byte> COLORS = HashBiMap.create();
    static {
        String[] values = new String[] {"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple",
                "blue", "brown", "green", "red", "black"};

        for (byte i = 0; i <= 15; i++){
            COLORS.put(values[i], i);
        }
    }

    public static final BiMap<Byte, String> COLORS_BYTES = COLORS.inverse();

    public static final HashMap<Byte, String> COLORS_FROM_BYTES = new HashMap<>();
    static {
        String[] values = new String[] {"White", "Orange", "Magenta", "Light blue", "Yellow", "Lime", "Pink", "Gray", "Light gray", "Cyan", "Purple",
                "Blue", "Brown", "Green", "Red", "Black"};

        for (byte i = 0; i <= 15; i++){
            COLORS_FROM_BYTES.put(i, values[i]);
        }
    }


    public static final HashMap<Byte, Style> COLORS_TEXT_STYLE = new HashMap<>();
    static {
        Style[] values = new Style[] {new Style().setColor(TextFormatting.WHITE), new Style().setColor(TextFormatting.GOLD), new Style().setColor(TextFormatting.DARK_PURPLE),
                new Style().setColor(TextFormatting.AQUA), new Style().setColor(TextFormatting.YELLOW), new Style().setColor(TextFormatting.GREEN), new Style().setColor(TextFormatting.LIGHT_PURPLE),
                new Style().setColor(TextFormatting.DARK_GRAY), new Style().setColor(TextFormatting.GRAY), new Style().setColor(TextFormatting.DARK_AQUA), new Style().setColor(TextFormatting.DARK_PURPLE),
                new Style().setColor(TextFormatting.BLUE), new Style().setColor(TextFormatting.DARK_RED), new Style().setColor(TextFormatting.DARK_GREEN), new Style().setColor(TextFormatting.RED),
                new Style().setColor(TextFormatting.BLACK)};
        for (byte i = 0; i <= 15; i++){
            COLORS_TEXT_STYLE.put(i, values[i]);
        }
    }

    public static final HashMap<Byte, String> COLORS_LOCALIZE_KEYS = new HashMap<>();
    static {
        String[] values = new String[] {"messages.color.white", "messages.color.orange", "messages.color.magenta", "messages.color.light_blue",
                "messages.color.yellow", "messages.color.lime", "messages.color.pink", "messages.color.grey", "messages.color.light_grey",
                "messages.color.cyan", "messages.color.purple", "messages.color.blue", "messages.color.brown", "messages.color.green",
                "messages.color.red", "messages.color.black"};


        for (byte i = 0; i <= 15; i++){
            COLORS_LOCALIZE_KEYS.put(i, values[i]);
        }
    }

    //------------------------------------------------------ AMMO

    public static final List<List<String>> WEAPONS = new ArrayList<>();

    static {
        WEAPONS.add(Utils.buildWeaponList("hbm:gun_ks23", "hbm:ammo_4gauge", 16));
        WEAPONS.add(Utils.buildWeaponList("hbm:gun_revolver_gold", "hbm:gun_revolver_gold_ammo", 24));
        WEAPONS.add(Utils.buildWeaponList("hbm:gun_bolter", "hbm:ammo_75bolt", 4));
        WEAPONS.add(Utils.buildWeaponList("hbm:gun_rpg", "hbm:ammo_rocket_he", 4));
        WEAPONS.add(Utils.buildWeaponList("hbm:gun_mp40", "hbm:ammo_9mm_ap", 128));
        WEAPONS.add(Utils.buildWeaponList("hbm:gun_vortex", "hbm:ammo_556_ap", 160));
    }

    public static final HashMap<String, Integer> INGOT_CONSTANTS = new HashMap<>();

    static {
        createConstant("hbm:ingot_steel", 20);
        createConstant("hbm:ingot_reiium",8);
    }

    public static void createConstant(String item_name, Integer size){
        INGOT_CONSTANTS.put(item_name, size);
    }
}
