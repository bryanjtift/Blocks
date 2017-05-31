package me.HeyAwesomePeople.Blocks.offlinemethods;

import me.HeyAwesomePeople.Blocks.Blocks;
import me.HeyAwesomePeople.Blocks.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.UUID;

public class RemoveOffline extends BukkitRunnable {

    private Blocks plugin;

    private Player receiver;
    private String targetUsername;
    private Integer amountToRemove;
    private String type;

    private UUID target;

    public RemoveOffline(Blocks plugin, Player p, String username, Integer amountToRemove, String type) {
        this.plugin = plugin;
        this.receiver = p;
        this.targetUsername = username;
        this.amountToRemove = amountToRemove;
        this.type = type;
    }

    @Override
    public void run() {
        checkForUUID();
        if (target == null) return; //TODO PLAYER NOT FOUND

        if (plugin.redis.isInCache(target)) {
            if (type.equalsIgnoreCase("B")) {
                String msgForTarget = plugin.blockMethods.removeBlocks(receiver, amountToRemove, true);
            }
            if (type.equalsIgnoreCase("C")) {
                String msgForTarget = plugin.blockMethods.removeCubes(receiver, amountToRemove, true);
            }
            reportSuccess(plugin.redis.getBlocks(target), plugin.redis.getCubes(target));
            debugMsg("QUERIED REDIS");
        } else {
            try {
                Integer[] data = plugin.mysql.retrieveData(target);
                if (data[2] == 0) return; //TODO PLAYER NOT FOUND
                debugMsg("QUERIED MYSQL");
                if (type.equalsIgnoreCase("B")) {
                    plugin.mysql.uploadData(target, data[0] - amountToRemove, data[1]);
                    reportSuccess(data[0] - amountToRemove, data[1]);
                }
                if (type.equalsIgnoreCase("C")) {
                    plugin.mysql.uploadData(target, data[0], data[1] - amountToRemove);
                    reportSuccess(data[0], data[1] - amountToRemove);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


    }

    private void debugMsg(String s) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage("Debug: " + s);
            }
        }.runTask(plugin);
    }

    private void reportSuccess(final Integer blocks, final Integer cubes) {
        new BukkitRunnable() {
            @Override
            public void run() {
                receiver.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.add.success_sender." + (type.equalsIgnoreCase("C") ? "cubes" : "blocks"))
                        .replace("%blocks%", amountToRemove + "")
                        .replace("%cubes%", amountToRemove + "")
                        .replace("%player%", targetUsername)
                        .replace("%newblocks%", blocks + "")
                        .replace("%newcubes%", cubes + "")));
            }
        }.runTask(plugin);
    }

    private void checkForUUID() {
        try {
            target = UUIDFetcher.getUUIDOf(targetUsername);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
