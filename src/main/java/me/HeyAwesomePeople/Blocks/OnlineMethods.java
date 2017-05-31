package me.HeyAwesomePeople.Blocks;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

class OnlineMethods {

    private Blocks plugin;

    OnlineMethods(Blocks plugin) {
        this.plugin = plugin;
    }

    public String addCubes(Player p, Integer cubesToAdd, boolean toPlayer) {
        Integer newCubes = plugin.redis.getCubes(p.getUniqueId()) + cubesToAdd;
        plugin.redis.setCubes(p.getUniqueId(), newCubes);

        if (toPlayer)
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.add.success_receiver.cubes")
                    .replace("%newcubes%", newCubes + "")
                    .replace("%cubes%", cubesToAdd + "")));
        return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.add.success_sender.cubes")
                .replace("%player%", p.getName())
                .replace("%newcubes%", newCubes + "")
                .replace("%cubes%", cubesToAdd + ""));
    }

    public String addBlocks(Player p, Integer blocksToAdd, boolean toPlayer) {
        Integer newBlocks = plugin.redis.getBlocks(p.getUniqueId()) + blocksToAdd;
        plugin.redis.setBlocks(p.getUniqueId(), newBlocks);

        if (toPlayer)
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.add.success_receiver.blocks")
                    .replace("%newblocks%", newBlocks + "")
                    .replace("%blocks%", blocksToAdd + "")));
        return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.add.success_sender.blocks")
                .replace("%player%", p.getName())
                .replace("%newcubes%", newBlocks + "")
                .replace("%cubes%", blocksToAdd + ""));
    }

    public String removeCubes(Player p, Integer cubesToRemove, boolean toPlayer) {
        Integer newCubes = plugin.redis.getCubes(p.getUniqueId()) - cubesToRemove;
        newCubes = (newCubes <= 0) ? 0 : newCubes;
        plugin.redis.setCubes(p.getUniqueId(), newCubes);

        if (toPlayer)
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.remove.success_receiver.cubes")
                    .replace("%newcubes%", newCubes + "")
                    .replace("%cubes%", cubesToRemove + "")));
        return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.remove.success_sender.cubes")
                .replace("%player%", p.getName())
                .replace("%newcubes%", newCubes + "")
                .replace("%cubes%", cubesToRemove + ""));
    }

    public String removeBlocks(Player p, Integer blocksToRemove, boolean toPlayer) {
        Integer newBlocks = plugin.redis.getBlocks(p.getUniqueId()) - blocksToRemove;
        newBlocks = (newBlocks <= 0) ? 0 : newBlocks;
        plugin.redis.setBlocks(p.getUniqueId(), newBlocks);

        if (toPlayer)
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.remove.success_receiver.blocks")
                    .replace("%newblocks%", newBlocks + "")
                    .replace("%blocks%", blocksToRemove + "")));
        return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.remove.success_sender.blocks")
                .replace("%player%", p.getName())
                .replace("%newcubes%", newBlocks + "")
                .replace("%cubes%", blocksToRemove + ""));
    }

    public void withdrawBlocks(Player p, Integer toWithdraw) {
        Integer blocks = plugin.redis.getBlocks(p.getUniqueId());
        Integer newBlocks = blocks - toWithdraw;
        this.removeBlocks(p, toWithdraw, false);

        if (blocks >= toWithdraw) {
            p.getInventory().addItem(plugin.getBankNote(p, blocks));
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.withdraw.success")
                    .replace("%blocks%", toWithdraw + "")
                    .replace("%newblocks%", newBlocks + "")));
        } else {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.withdraw.not_enough_blocks")
                    .replace("%towithdraw%", toWithdraw + "")
                    .replace("%availableblocks%", blocks + "")));
        }
    }

}
