package com.zonty.farmingplugin.zontyFamingPlugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Set;
import java.util.Random;

public class DestructionListener implements Listener {

    // A set of crops
    private static final Set<Material> CROPS = Set.of(
            Material.WHEAT,
            Material.CARROTS,
            Material.POTATOES,
            Material.BEETROOTS
    );

    //
    // PLAYER'S CROP DESTRUCTION HANDLER
    //


    @EventHandler
    public void onBreakEvent(BlockBreakEvent event) {
        Player player = event.getPlayer();
        NamespacedKey key = new NamespacedKey("farmplugin", "toggled");
        PersistentDataContainer container = player.getPersistentDataContainer();
        if (container.getOrDefault(key, PersistentDataType.INTEGER, 1) == 0) {
            return; // No crops will drop for the players who have the plugin disabled ^^
        }

        Block block = event.getBlock();
        Material type = block.getType();

        if (!CROPS.contains(type)) return;

        event.setDropItems(false);

        ItemStack tool = player.getInventory().getItemInMainHand();
        Material toolType = tool.getType();

        int baseCrops = 0;
        int seeds = 0;

        // Just do nothing if the item isn't a hoe
        if (!(toolType.name().endsWith("_HOE"))) {
            return;
        }

        // Set the respective drop values accordingly
        switch (toolType) {
            case WOODEN_HOE:
            case STONE_HOE:
                baseCrops = 1;
                seeds = 1;
                break;

            case IRON_HOE:
            case GOLDEN_HOE:
                baseCrops = 2;
                seeds = 1;
                break;

            case DIAMOND_HOE:
            case NETHERITE_HOE:
                baseCrops = 3;
                seeds = 1;
                break;
        }

        // Handle fortune enchant
        int fortuneLevel = tool.getEnchantmentLevel(Enchantment.FORTUNE);
        int bonusCrops = 0;

        if (fortuneLevel > 0) {
            Random r = new Random();
            bonusCrops = r.nextInt(fortuneLevel + 1);
        }

        int totalCrops = baseCrops + bonusCrops;


        // Handle the dropping
        World world = block.getWorld();
        Location loc = block.getLocation();

        Material cropItem;
        Material seedItem;

        switch (type) {
            case WHEAT:
                cropItem = Material.WHEAT;
                seedItem = Material.WHEAT_SEEDS;
                break;
            case CARROTS:
                cropItem = Material.CARROT;
                seedItem = Material.CARROT; // no real seeds; carrot itself acts as seed
                break;
            case POTATOES:
                cropItem = Material.POTATO;
                seedItem = Material.POTATO; // same as carrots
                break;
            case BEETROOTS:
                cropItem = Material.BEETROOT;
                seedItem = Material.BEETROOT_SEEDS;
                break;
            default:
                return;
        }

        if (totalCrops > 0)
            world.dropItemNaturally(loc, new ItemStack(cropItem, totalCrops));

        if (seeds > 0)
            world.dropItemNaturally(loc, new ItemStack(seedItem, seeds));

        // It came as a suprise, but apparently,
        // even though the event is not canceled but adjusted,
        // the durability of hoes is not expended when you
        // break the listed crops. Hence I had to implement this manually.

        int unbreaking = tool.getEnchantmentLevel(Enchantment.UNBREAKING);
        boolean reduceDurability = true;

        if (unbreaking > 0) {
            double chanceToIgnore = 100.0 / (unbreaking + 1);
            if (Math.random() * 100 < chanceToIgnore) {
                reduceDurability = false;
            }
        }

        if (reduceDurability) {
            Damageable meta = (Damageable) tool.getItemMeta();
            meta.setDamage(meta.getDamage() + 1);
            tool.setItemMeta(meta);
        }
    }

    //
    // CIRCUMVENTION PREVENTION
    //

    // The logic for the following handlers is the same:
    // I go across every event that can feasibly get a crop destroyed.
    // I make the event ignore the crop blocks and just manually replace them by air.

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> {
            if (CROPS.contains(block.getType())) {
                block.setType(Material.AIR);
                return true;
            }
            return false;
        });
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> {
            if (CROPS.contains(block.getType())) {
                block.setType(Material.AIR);
                return true;
            }
            return false;
        });
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        if (CROPS.contains(event.getBlock().getType())) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (CROPS.contains(event.getBlock().getType())) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (CROPS.contains(block.getType())) {
                block.setType(Material.AIR);
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (CROPS.contains(block.getType())) {
                block.setType(Material.AIR);
            }
        }
    }

    // Hereon because I thought it'd be funny to have crops immune to
    // something that would certainly destroy them, I just start canceling events.
    // Also it's easier than handling destruction logic lol

    @EventHandler
    public void onFarmlandTrample(BlockFadeEvent event) {
        if (event.getBlock().getType() == Material.FARMLAND) {
            if (event.getNewState().getType() == Material.DIRT) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerTrample(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) {
            Block block = event.getClickedBlock();

            if (block != null && block.getType() == Material.FARMLAND) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onWaterFlow(BlockFromToEvent event) {
        Block to = event.getToBlock();

        if (CROPS.contains(to.getType())) {
            event.setCancelled(true);
        }
    }

}
