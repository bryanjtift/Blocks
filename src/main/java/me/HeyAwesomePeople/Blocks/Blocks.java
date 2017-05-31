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

import java.io.File;

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

}
