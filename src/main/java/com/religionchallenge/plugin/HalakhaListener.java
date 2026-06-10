package com.religionchallenge.plugin;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class HalakhaListener implements Listener {

    private static final Set<Material> NON_KOSHER = Set.of(
            Material.PORKCHOP,
            Material.COOKED_PORKCHOP,
            Material.ROTTEN_FLESH,
            Material.RABBIT,
            Material.COOKED_RABBIT,
            Material.PUFFERFISH,
            Material.TROPICAL_FISH,
            Material.OMINOUS_BOTTLE
    );

    private static final Set<EntityType> TRADERS = Set.of(
            EntityType.VILLAGER, EntityType.WANDERING_TRADER
    );

    private static final Set<Material> SEEDS = Set.of(
            Material.WHEAT_SEEDS, Material.BEETROOT_SEEDS,
            Material.PUMPKIN_SEEDS, Material.MELON_SEEDS,
            Material.TORCHFLOWER_SEEDS, Material.PITCHER_POD,
            Material.CARROT, Material.POTATO, Material.NETHER_WART,
            Material.SWEET_BERRIES, Material.BAMBOO, Material.COCOA_BEANS,
            Material.BONE_MEAL
    );

    private static final Set<Material> COOKING_BLOCKS = Set.of(
            Material.FURNACE, Material.SMOKER, Material.BLAST_FURNACE
    );

    private static final Set<Material> WRITING_BLOCKS = Set.of(
            Material.ENCHANTING_TABLE, Material.LECTERN,
            Material.GRINDSTONE, Material.CARTOGRAPHY_TABLE
    );

    private static final Set<Material> CONTAINERS = Set.of(
            Material.CHEST, Material.TRAPPED_CHEST, Material.BARREL,
            Material.DISPENSER, Material.DROPPER, Material.HOPPER
    );

    private static final Set<Material> KINDLE_ITEMS = Set.of(
            Material.FLINT_AND_STEEL, Material.FIRE_CHARGE
    );

    private static final Set<EntityType> PASSIVE_MOBS = Set.of(
            EntityType.ARMADILLO, EntityType.AXOLOTL, EntityType.BAT,
            EntityType.BEE, EntityType.CAMEL, EntityType.CAT,
            EntityType.CHICKEN, EntityType.COD, EntityType.COW,
            EntityType.DOLPHIN, EntityType.DONKEY, EntityType.FROG,
            EntityType.GLOW_SQUID, EntityType.GOAT, EntityType.HORSE,
            EntityType.LLAMA, EntityType.MOOSHROOM, EntityType.MULE,
            EntityType.OCELOT, EntityType.PANDA, EntityType.PARROT,
            EntityType.PIG, EntityType.PUFFERFISH, EntityType.RABBIT,
            EntityType.SALMON, EntityType.SHEEP, EntityType.SKELETON_HORSE,
            EntityType.SNIFFER, EntityType.SQUID, EntityType.STRIDER,
            EntityType.TADPOLE, EntityType.TROPICAL_FISH, EntityType.TURTLE,
            EntityType.VILLAGER, EntityType.WANDERING_TRADER, EntityType.WOLF,
            EntityType.ZOMBIE_HORSE
    );

    private final Map<UUID, Integer> warnings = new HashMap<>();

    private boolean isShabbat(World world) {
        long time = world.getFullTime();
        long day = time / 24000;
        int tick = (int) (time % 24000);
        int dayOfWeek = (int) (day % 7);
        if (dayOfWeek < 0) dayOfWeek += 7;

        if (dayOfWeek == 6 && tick >= 12000) return true;
        if (dayOfWeek == 7 && tick < 13800) return true;
        return false;
    }

    private void handleViolation(Player player) {
        UUID uuid = player.getUniqueId();
        int count = warnings.getOrDefault(uuid, 0) + 1;
        warnings.put(uuid, count);

        if (count == 1) {
            player.sendMessage(ChatColor.GOLD + "Hatarah: Has violat una llei halàquica. La propera vegada seràs colpejat!");
        } else if (count >= 2) {
            Location loc = player.getLocation();
            player.getWorld().strikeLightning(loc);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (isShabbat(event.getPlayer().getWorld())) {
            event.setCancelled(true);
            handleViolation(event.getPlayer());
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isShabbat(event.getPlayer().getWorld())) {
            event.setCancelled(true);
            handleViolation(event.getPlayer());
        }
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        if (NON_KOSHER.contains(event.getItem().getType())) {
            event.setCancelled(true);
            handleViolation(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        if (!isShabbat(event.getPlayer().getWorld())) return;

        Material blockType = event.getClickedBlock().getType();
        ItemStack item = event.getItem();
        Player player = event.getPlayer();

        if (isCookingStation(blockType)
                || isWritingStation(blockType)
                || isPlanting(blockType, item)
                || isKindling(item)
                || blockType == Material.CRAFTING_TABLE
                || blockType == Material.SMITHING_TABLE
                || blockType == Material.STONECUTTER
                || Tag.ANVIL.isTagged(blockType)
                || Tag.CAMPFIRES.isTagged(blockType)
                || Tag.SHULKER_BOXES.isTagged(blockType)
                || Tag.DOORS.isTagged(blockType)
                || Tag.TRAPDOORS.isTagged(blockType)
                || Tag.FENCE_GATES.isTagged(blockType)
                || Tag.BUTTONS.isTagged(blockType)
                || Tag.PRESSURE_PLATES.isTagged(blockType)
                || CONTAINERS.contains(blockType)) {
            event.setCancelled(true);
            handleViolation(player);
        }
    }

    @EventHandler
    public void onShear(PlayerShearEntityEvent event) {
        if (isShabbat(event.getPlayer().getWorld())) {
            event.setCancelled(true);
            handleViolation(event.getPlayer());
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!PASSIVE_MOBS.contains(event.getEntityType())) return;
        if (!isShabbat(event.getEntity().getWorld())) return;

        Player player = null;
        Entity damager = event.getDamager();
        if (damager instanceof Player) {
            player = (Player) damager;
        } else if (damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager;
            if (projectile.getShooter() instanceof Player) {
                player = (Player) projectile.getShooter();
            }
        }

        if (player != null) {
            event.setCancelled(true);
            handleViolation(player);
        }
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (isShabbat(event.getPlayer().getWorld())) {
            event.setCancelled(true);
            handleViolation(event.getPlayer());
        }
    }

    private boolean isCookingStation(Material block) {
        return COOKING_BLOCKS.contains(block);
    }

    private boolean isWritingStation(Material block) {
        return WRITING_BLOCKS.contains(block);
    }

    private boolean isPlanting(Material block, ItemStack item) {
        if (item == null) return false;
        Material type = item.getType();
        if (!SEEDS.contains(type)) return false;
        return block == Material.FARMLAND
                || block == Material.SOUL_SAND
                || block == Material.SOUL_SOIL
                || block == Material.GRASS_BLOCK
                || block == Material.DIRT
                || block == Material.COARSE_DIRT
                || block == Material.ROOTED_DIRT;
    }

    private boolean isKindling(ItemStack item) {
        if (item == null) return false;
        return KINDLE_ITEMS.contains(item.getType());
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!isShabbat(event.getPlayer().getWorld())) return;
        if (TRADERS.contains(event.getRightClicked().getType())) {
            event.setCancelled(true);
            handleViolation(event.getPlayer());
        }
    }
}
