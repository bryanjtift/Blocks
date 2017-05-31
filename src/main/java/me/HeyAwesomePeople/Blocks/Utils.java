package me.HeyAwesomePeople.Blocks;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

class Utils {

    static List<String> customColorLore(List<String> input, Integer blocks, String signer) {
        List<String> list = new ArrayList<>();
        for (String s : input) {
            list.add(ChatColor.translateAlternateColorCodes('&', s
                    .replace("%blocks%", blocks + "")
                    .replace("%signer%", signer)));
        }
        return list;
    }

    static List<String> colorLore(List<String> input) {
        List<String> list = new ArrayList<>();
        for (String s : input) {
            list.add(ChatColor.translateAlternateColorCodes('&', s));
        }
        return list;
    }

    static boolean isInt(String s) {
        try {
            //noinspection ResultOfMethodCallIgnored
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

}
