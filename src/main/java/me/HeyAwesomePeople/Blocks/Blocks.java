package me.HeyAwesomePeople.Blocks;

import me.HeyAwesomePeople.Blocks.database.MySQL;
import me.HeyAwesomePeople.Blocks.database.Redis;
import net.minecraft.server.v1_11_R1.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.SQLException;
import java.util.UUID;

public class Blocks extends JavaPlugin {

    public Redis redis;
    public OnlineMethods blockMethods;
    public MySQL mysql;

    @Override
    public void onEnable() {
        readyConfig();
    }

    private void readyConfig() {
        if (!new File(this.getDataFolder() + File.separator + "config.yml").exists()) {
            saveDefaultConfig();
        }
        reloadConfig();

        getServer().getPluginManager().registerEvents(new CommandHandler(this), this);

        if (getConfig().getBoolean("banknote_enabled"))
            getServer().getPluginManager().registerEvents(new BankNoteRedeemer(this), this);

        redis = new Redis();
        mysql = new MySQL(this);
        blockMethods = new OnlineMethods(this);
    }

    @Override
    public void onDisable() { }

    ItemStack getBankNote(Player signer, Integer blocks) {
        ItemStack i = new ItemStack(Material.getMaterial(getConfig().getString("withdrawl_item.type")));

        net.minecraft.server.v1_11_R1.ItemStack craftItem = CraftItemStack.asNMSCopy(i);
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInt("blocks", blocks);
        craftItem.setTag(compound);

        ItemStack item = CraftItemStack.asBukkitCopy(craftItem);
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', getConfig().getString("withdrawl_item.name")
                .replace("%blocks%", blocks + "")
                .replace("%signer%", signer.getName())));
        im.setLore(Utils.customColorLore(getConfig().getStringList("withdrawl_item.lore"), blocks, signer.getName()));
        item.setItemMeta(im);

        return item;
    }

    boolean isBankNote(ItemStack item) {
        net.minecraft.server.v1_11_R1.ItemStack craftItem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound compound = craftItem.getTag();
        return compound != null && compound.hasKey("blocks");
    }

    Integer getBankNoteWorth(ItemStack item) {
        net.minecraft.server.v1_11_R1.ItemStack craftItem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound compound = craftItem.getTag();
        return compound != null ? compound.getInt("blocks") : 0;
    }

    // ********** MYSQL METHODS ************ //

    void getMySQLCurrency(final Player getter, final String username) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    UUID id = UUIDFetcher.getUUIDOf(username);

                    if (id == null) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                getter.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player_not_found")));
                            }
                        }.runTask(Blocks.this);
                        return;
                    }
                    if (redis.isInCache(id)) {
                        Integer[] pData = blockMethods.getPlayerBlocksAndShards(id);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                getter.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.check_balance_of_other_player")
                                        .replace("%shards%", pData[0] + "")
                                        .replace("%blocks%", pData[1] + "")
                                        .replace("%leftovershards%", pData[2] + "")
                                        .replace("%player%", username)));
                            }
                        }.runTask(Blocks.this);
                    } else {
                        Integer mysql = Blocks.this.mysql.retrieveSingleData(id);
                        if (mysql != -1) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    getter.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.check_balance_of_other_player")
                                            .replace("%shards%", mysql + "")
                                            .replace("%blocks%", (mysql / getConfig().getInt("blocks_multiple")) + "")
                                            .replace("%leftovershards%", (mysql % getConfig().getInt("blocks_multiple")) + "")
                                            .replace("%player%", username)));
                                }
                            }.runTask(Blocks.this);
                        } else {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    getter.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player_not_found")));
                                }
                            }.runTask(Blocks.this);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(this);
    }

    void addOfflinePlayerBlocks(Player setter, String username, Integer blocksToAdd) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    UUID id = UUIDFetcher.getUUIDOf(username);
                    if (id == null) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                setter.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player_not_found")));
                            }
                        }.runTask(Blocks.this);
                        return;
                    }

                    if (redis.isInCache(id)) {
                        Integer update = redis.getCurrency(id) + (blocksToAdd * getConfig().getInt("blocks_multiple"));
                        redis.updateCurrency(id, update);
                        Integer newBlocks = update / getConfig().getInt("blocks_multiple");

                        setter.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.add.success.blocks")
                                .replace("%player%", username)
                                .replace("%newshards%", update + "")
                                .replace("%blocks%", blocksToAdd + "")
                                .replace("%newblocks%", newBlocks + "")));
                    } else {
                        Integer mysql = Blocks.this.mysql.retrieveSingleData(id);
                        if (mysql != -1) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    Integer update = mysql + (blocksToAdd * getConfig().getInt("blocks_multiple"));
                                    try {
                                        Blocks.this.mysql.uploadToMySQL(id, update);
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                    Integer newBlocks = update / getConfig().getInt("blocks_multiple");

                                    setter.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.add.success.blocks")
                                            .replace("%player%", username)
                                            .replace("%newshards%", update + "")
                                            .replace("%blocks%", blocksToAdd + "")
                                            .replace("%newblocks%", newBlocks + "")));
                                }
                            }.runTask(Blocks.this);
                        } else {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    setter.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player_not_found")));
                                }
                            }.runTask(Blocks.this);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.runTaskAsynchronously(this);
    }

    void addOfflinePlayerShards(Player setter, String username, Integer shardsToAdd) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    UUID id = UUIDFetcher.getUUIDOf(username);
                    if (id == null) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                setter.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player_not_found")));
                            }
                        }.runTask(Blocks.this);
                        return;
                    }

                    if (redis.isInCache(id)) {
                        Integer newShards = redis.getCurrency(id) + shardsToAdd;
                        redis.updateCurrency(id, newShards);

                        setter.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.add.success.shards")
                                .replace("%player%", username)
                                .replace("%newshards%", newShards + "")
                                .replace("%shards%", shardsToAdd + "")
                                .replace("%newblocks%", (newShards / getConfig().getInt("blocks_multiple")) + "")));
                    } else {
                        Integer mysql = Blocks.this.mysql.retrieveSingleData(id);
                        if (mysql != -1) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    Integer update = mysql + shardsToAdd;
                                    try {
                                        Blocks.this.mysql.uploadToMySQL(id, update);
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                    Integer newBlocks = update / getConfig().getInt("blocks_multiple");

                                    setter.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.add.success.blocks")
                                            .replace("%player%", username)
                                            .replace("%newshards%", update + "")
                                            .replace("%shards%", shardsToAdd + "")
                                            .replace("%newblocks%", newBlocks + "")));
                                }
                            }.runTask(Blocks.this);
                        } else {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    setter.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player_not_found")));
                                }
                            }.runTask(Blocks.this);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.runTaskAsynchronously(this);
    }

    void removeOfflinePlayerShards(Player setter, String username, Integer shardsToRemove) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    UUID id = UUIDFetcher.getUUIDOf(username);
                    if (id == null) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                setter.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player_not_found")));
                            }
                        }.runTask(Blocks.this);
                        return;
                    }

                    if (redis.isInCache(id)) {
                        Integer newShards = redis.getCurrency(id) - shardsToRemove;
                        redis.updateCurrency(id, ((newShards <= 0) ? 0 : newShards));

                        setter.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.add.success.shards")
                                .replace("%player%", username)
                                .replace("%newshards%", ((newShards <= 0) ? 0 : newShards) + "")
                                .replace("%shards%", shardsToRemove + "")
                                .replace("%newblocks%", (((newShards <= 0) ? 0 : newShards) / getConfig().getInt("blocks_multiple")) + "")));
                    } else {
                        Integer mysql = Blocks.this.mysql.retrieveSingleData(id);
                        if (mysql != -1) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    Integer update = mysql - shardsToRemove;
                                    try {
                                        Blocks.this.mysql.uploadToMySQL(id, ((update <= 0) ? 0 : update));
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                    Integer newBlocks = update / getConfig().getInt("blocks_multiple");

                                    setter.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.add.success.shards")
                                            .replace("%player%", username)
                                            .replace("%newshards%", ((update <= 0) ? 0 : update) + "")
                                            .replace("%shards%", shardsToRemove + "")
                                            .replace("%newblocks%", ((newBlocks <= 0) ? 0 : newBlocks) + "")));
                                }
                            }.runTask(Blocks.this);
                        } else {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    setter.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player_not_found")));
                                }
                            }.runTask(Blocks.this);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.runTaskAsynchronously(this);
    }

    void removeOfflinePlayerBlocks(Player setter, String username, Integer blocksToRemove) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    UUID id = UUIDFetcher.getUUIDOf(username);
                    if (id == null) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                setter.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player_not_found")));
                            }
                        }.runTask(Blocks.this);
                        return;
                    }

                    if (redis.isInCache(id)) {
                        Integer update = redis.getCurrency(id) - (blocksToRemove * getConfig().getInt("blocks_multiple"));
                        redis.updateCurrency(id, ((update <= 0) ? 0 : update));
                        Integer newBlocks = ((update <= 0) ? 0 : update) / getConfig().getInt("blocks_multiple");

                        setter.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.add.success.blocks")
                                .replace("%player%", username)
                                .replace("%newshards%", ((update <= 0) ? 0 : update) + "")
                                .replace("%blocks%", blocksToRemove + "")
                                .replace("%newblocks%", newBlocks + "")));
                    } else {
                        Integer mysql = Blocks.this.mysql.retrieveSingleData(id);
                        if (mysql != -1) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    Integer update = mysql - (blocksToRemove * getConfig().getInt("blocks_multiple"));
                                    try {
                                        Blocks.this.mysql.uploadToMySQL(id, ((update <= 0) ? 0 : update));
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                    Integer newBlocks = ((update <= 0) ? 0 : update) / getConfig().getInt("blocks_multiple");

                                    setter.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.add.success.blocks")
                                            .replace("%player%", username)
                                            .replace("%newshards%", ((update <= 0) ? 0 : update) + "")
                                            .replace("%blocks%", blocksToRemove + "")
                                            .replace("%newblocks%", newBlocks + "")));
                                }
                            }.runTask(Blocks.this);
                        } else {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    setter.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.player_not_found")));
                                }
                            }.runTask(Blocks.this);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.runTaskAsynchronously(this);
    }


}
