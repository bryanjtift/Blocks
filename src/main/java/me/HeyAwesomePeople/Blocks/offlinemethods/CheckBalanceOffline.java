package me.HeyAwesomePeople.Blocks.offlinemethods;

import me.HeyAwesomePeople.Blocks.Blocks;
import me.HeyAwesomePeople.Blocks.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.UUID;

public class CheckBalanceOffline extends BukkitRunnable {

    private Blocks plugin;

    private Player receiver;
    private String targetUsername;
    private UUID target;

    public CheckBalanceOffline(Blocks plugin, Player receiver, String target) {
        this.plugin = plugin;
        this.receiver = receiver;
        this.targetUsername = target;

        this.runTaskAsynchronously(plugin);
    }

    @Override
    public void run() {
        checkForUUID();
        if (target == null) return; //TODO PLAYER NOT FOUND

        if (plugin.redis.isInCache(target)) {
            debugMsg("QUERIED REDIS");
            reportBalance(plugin.redis.getBlocks(target), plugin.redis.getCubes(target));
        } else {
            try {
                Integer[] data = plugin.mysql.retrieveData(target);
                if (data[2] == 0) return; //TODO PLAYER NOT FOUND
                debugMsg("QUERIED MYSQL");
                reportBalance(data[0], data[1]);
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

    private void reportBalance(final Integer blocks, final Integer cubes) {
        new BukkitRunnable() {
            @Override
            public void run() {
                receiver.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.balance.other")
                        .replace("%blocks%", blocks + "")
                        .replace("%cubes%", cubes + "")
                        .replace("%player%", targetUsername)));
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
