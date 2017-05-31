package me.HeyAwesomePeople.Blocks;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BankNoteRedeemer implements Listener {

    private Blocks plugin;

    BankNoteRedeemer(Blocks plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK
                || e.getAction() == Action.RIGHT_CLICK_AIR) {
            if (plugin.isBankNote(p.getInventory().getItemInMainHand())) {
                Integer blocks = plugin.getBankNoteWorth(p.getInventory().getItemInMainHand());
                plugin.blockMethods.addBlocks(p, blocks, false);
                e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.redeemed_note")
                        .replace("%blocks%", blocks + "")));
                p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                p.updateInventory();
            }
        }
    }

    @EventHandler
    public void onRightClickPlayer(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() instanceof Player) {
            Player clicked = (Player) e.getRightClicked();
            if (plugin.isBankNote(e.getPlayer().getInventory().getItemInMainHand())) {
                Integer blocks = plugin.getBankNoteWorth(e.getPlayer().getInventory().getItemInMainHand());
                plugin.blockMethods.addBlocks(clicked, blocks, false);
                e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.gave_player_money")
                        .replace("%blocks%", blocks + "")
                        .replace("%player%", clicked.getName())));
                clicked.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.received_player_money_from_banknote")
                        .replace("%blocks%", blocks + "")
                        .replace("%player%", e.getPlayer().getName())));
                e.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                e.getPlayer().updateInventory();
            }
        }
    }

}
