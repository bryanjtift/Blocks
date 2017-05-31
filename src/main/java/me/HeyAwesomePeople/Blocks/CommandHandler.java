package me.HeyAwesomePeople.Blocks;

import me.HeyAwesomePeople.Blocks.offlinemethods.AddOffline;
import me.HeyAwesomePeople.Blocks.offlinemethods.CheckBalanceOffline;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandHandler implements Listener {

    private Blocks plugin;

    CommandHandler(Blocks plugin) {
        this.plugin = plugin;
    }

    private void noArguments(Player p) {
        if (p.hasPermission("blocks.admin")) {
            for (String s : Utils.colorLore(plugin.getConfig().getStringList("messages.commandAdmin"))) {
                p.sendMessage(s);
            }
        }
        if (p.hasPermission("blocks.user")) {
            for (String s : Utils.colorLore(plugin.getConfig().getStringList("messages.command"))) {
                p.sendMessage(s);
            }
        }
    }

    private void checkSelfBalance(Player p) {
        if (!p.hasPermission("blocks.user")) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.command_msgs.no_permission")));
            return;
        }
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.balance.self")
                .replace("%blocks%", plugin.redis.getBlocks(p.getUniqueId()) + "")
                .replace("%cubes%", plugin.redis.getCubes(p.getUniqueId()) + "")));
    }

    private void checkOtherBalance(Player p, String user) {
        if (!p.hasPermission("blocks.user")) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.command_msgs.no_permission")));
            return;
        }
        if (Bukkit.getPlayer(user) == null) {
            new CheckBalanceOffline(plugin, p, user);
        } else {
            Player target = Bukkit.getPlayer(user);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.balance.other")
                    .replace("%blocks%", plugin.redis.getBlocks(target.getUniqueId()) + "")
                    .replace("%cubes%", plugin.redis.getCubes(target.getUniqueId()) + "")
                    .replace("%player%", target.getName())));
        }
    }

    private void handleWithdraw(Player p, String amount) {
        if (!p.hasPermission("blocks.user")) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.command_msgs.no_permission")));
            return;
        }
        if (!plugin.getConfig().getBoolean("banknote_enabled")) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.banknote_disabled")));
            return;
        }
        if (!Utils.isInt(amount)) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.withdraw.not_a_number")));
            return;
        }
        plugin.blockMethods.withdrawBlocks(p, Integer.parseInt(amount));
    }

    private void addToPlayer(Player p, String username, String amountToAdd, String type) {
        if (Utils.isInt(amountToAdd)) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.add.not_a_number")));
            return;
        }
        if (Bukkit.getPlayer(username) == null) {
            if (type.equalsIgnoreCase("C")
                    || type.equalsIgnoreCase("B")) {
                new AddOffline(plugin, p, username, Integer.parseInt(amountToAdd), type);
            } else {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.command_msgs.invalid_currency_type")));
            }
        } else {
            Player target = Bukkit.getPlayer(username);
            if (type.equalsIgnoreCase("C")) {
                plugin.blockMethods.addCubes(target, Integer.parseInt(amountToAdd), true);
            } else if (type.equalsIgnoreCase("B")) {
                plugin.blockMethods.addBlocks(target, Integer.parseInt(amountToAdd), true);
            } else {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.command_msgs.invalid_currency_type")));
            }
        }
    }

    private void removeFromPlayer(Player p, String username, String amountToRemove, String type) {
        if (Utils.isInt(amountToRemove)) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.add.not_a_number")));
            return;
        }
        if (Bukkit.getPlayer(username) == null) {
            if (type.equalsIgnoreCase("C")
                    || type.equalsIgnoreCase("B")) {

            } else {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.command_msgs.invalid_currency_type")));
            }
        } else {
            Player target = Bukkit.getPlayer(username);
            if (type.equalsIgnoreCase("C")) {
                plugin.blockMethods.removeCubes(target, Integer.parseInt(amountToRemove), true);
            } else if (type.equalsIgnoreCase("B")) {
                plugin.blockMethods.removeBlocks(target, Integer.parseInt(amountToRemove), true);
            } else {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.command_msgs.invalid_currency_type")));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        String[] args = e.getMessage().substring(1).split(" ");

        if (args[0].equalsIgnoreCase(plugin.getConfig().getString("custom_command"))) {
            e.setCancelled(true);
            if (args.length == 1) {
                noArguments(p);
            } else if (args.length == 2) {
                if (args[1].equalsIgnoreCase("balance")) {
                    checkSelfBalance(p);
                }
            } else if (args.length == 3) {
                if (args[1].equalsIgnoreCase("balance")) {
                    checkOtherBalance(p, args[2]);
                }
                if (args[1].equalsIgnoreCase("withdraw")) {
                    handleWithdraw(p, args[2]);
                }
            } else if (args.length == 5) {
                if (args[1].equalsIgnoreCase("add")) {
                    addToPlayer(p, args[2], args[3], args[4]);
                }
                if (args[1].equalsIgnoreCase("remove")) {

                }
            }


            if (args.length == 3) {
            } else if (args.length == 5) {
                if (!p.hasPermission("blocks.admin")) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.noPermission")));
                    return;
                }
                else if (args[1].equalsIgnoreCase("remove")) {
                    if (Utils.isInt(args[3])) {
                        if (Bukkit.getPlayer(args[2]) == null) {
                            if (args[4].equalsIgnoreCase("F")) {
                                plugin.removeOfflinePlayerShards(p, args[2], Integer.parseInt(args[3]));
                            } else if (args[4].equalsIgnoreCase("B")) {
                                plugin.removeOfflinePlayerBlocks(p, args[2], Integer.parseInt(args[3]));
                            } else {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.invalid_currency_type")));
                            }
                        } else {
                            if (args[4].equalsIgnoreCase("F")) {
                                plugin.blockMethods.removeShards(Bukkit.getPlayer(args[2]), p, Integer.parseInt(args[3]), true);
                            } else if (args[4].equalsIgnoreCase("B")) {
                                plugin.blockMethods.removeBlocks(Bukkit.getPlayer(args[2]), p, Integer.parseInt(args[3]), true);
                            } else {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.invalid_currency_type")));
                            }
                        }
                    } else {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.remove.not_a_number")));
                    }
                } else {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.invalid_arguments")));
                }
            } else {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.invalid_arguments")));
            }
        }

    }

}
