/*
 *  Copyright 2013 eccentric_nz.
 */
package me.eccentric_nz.nonspecificodyssey;

import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * @author eccentric_nz
 */
public class NonSpecificOdysseyListener implements Listener {

    private final NonSpecificOdyssey plugin;
    private final List<UUID> travellers = new ArrayList<>();
    private final List<String> hasClicked = new ArrayList<>();
    private final HashMap<UUID, NonSpecificOdysseyMoveSession> moveSessions = new HashMap<>();
    String firstline;

    public NonSpecificOdysseyListener(NonSpecificOdyssey plugin) {
        this.plugin = plugin;
        firstline = plugin.getConfig().getString("firstline");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerSuffocate(EntityDamageByBlockEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player player && event.getCause().equals(DamageCause.SUFFOCATION)) {
            UUID uuid = player.getUniqueId();
            if (travellers.contains(uuid)) {
                Location location = player.getLocation();
                double y = location.getWorld().getHighestBlockYAt(location);
                location.setY(y);
                player.teleport(location);
                //suffocators.remove(uuid);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!travellers.contains(event.getPlayer().getUniqueId())) {
            return;
        }
        Player p = event.getPlayer();
        Location loc = p.getLocation(); // Grab Location
        /**
         * Copyright (c) 2011, The Multiverse Team All rights reserved. Check
         * the Player has actually moved a block to prevent unneeded
         * calculations... This is to prevent huge performance drops on high
         * player count servers.
         */
        NonSpecificOdysseyMoveSession tms = getNonSpecificOdysseyMoveSession(p);
        tms.setStaleLocation(loc);
        // If the location is stale, ie: the player isn't actually moving xyz coords, they're looking around
        if (!tms.isStaleLocation()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block b = event.getClickedBlock();
        if (b != null && Tag.SIGNS.isTagged(b.getType())) {
            Sign sign = (Sign) b.getState();
            String nsoline = ChatColor.stripColor(sign.getLine(0));
            if (nsoline.equalsIgnoreCase("[" + firstline + "]")) {
                Player p = event.getPlayer();
                if (p.hasPermission("nonspecificodyssey.sign")) {
                    String name = p.getName();
                    if (!hasClicked.contains(name)) {
                        hasClicked.add(name);
                        World w = b.getWorld();
                        if (p.isSneaking() && p.isOp()) {
                            b.setType(Material.AIR);
                            w.dropItemNaturally(b.getLocation(), new ItemStack(Material.OAK_SIGN, 1));
                            hasClicked.remove(name);
                        } else {
                            // check the other lines
                            String world_line = ChatColor.stripColor(sign.getLine(2));
                            World the_world;
                            if (plugin.getServer().getWorld(world_line) != null) {
                                the_world = plugin.getServer().getWorld(world_line);
                            } else {
                                the_world = w;
                            }
                            Location the_location;
                            Biome biome = checkBiomeLine(sign.getLine(3).toUpperCase());
                            if (biome != null) {
                                the_location = plugin.getCommando().searchBiome(p, biome, the_world);
                            } else {
                                the_location = plugin.getCommando().randomOverworldLocation(the_world);
                            }
                            Location random = the_location;
                            if (random != null) {
                                p.sendMessage(ChatColor.GOLD + "[" + plugin.getPluginName() + "] " + ChatColor.RESET + "Standby for random teleport...");
                                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                                    plugin.getCommando().movePlayer(p, random, w);
                                }, 40L);
                            } else {
                                p.sendMessage(ChatColor.GOLD + "[" + plugin.getPluginName() + "] " + ChatColor.RESET + "Location finding timed out, most likely the biome couldn't be found!");
                            }
                            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                                hasClicked.remove(name);
                            }, 80L);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        String nsoline = ChatColor.stripColor(event.getLine(0));
        if (nsoline.equalsIgnoreCase("[" + firstline + "]") && !player.hasPermission("nonspecificodyssey.admin")) {
            event.setLine(0, "");
            player.sendMessage(ChatColor.GOLD + "[" + plugin.getPluginName() + "] " + ChatColor.RESET + "You do not have permission to create teleport signs!");
        }
    }

    public Biome checkBiomeLine(String str) {
        int start = (str.startsWith(String.valueOf(ChatColor.COLOR_CHAR))) ? 2 : 0;
        int end = (str.startsWith(String.valueOf(ChatColor.COLOR_CHAR))) ? 12 : 15;
        for (Biome b : Biome.values()) {
            if (b.toString().length() > 15) {
                if (b.toString().substring(0, end).equals(str.substring(start))) {
                    return b;
                }
            }
        }
        try {
            return Biome.valueOf(str);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public List<UUID> getTravellers() {
        return travellers;
    }

    private NonSpecificOdysseyMoveSession getNonSpecificOdysseyMoveSession(Player p) {
        if (moveSessions.containsKey(p.getUniqueId())) {
            return moveSessions.get(p.getUniqueId());
        }
        NonSpecificOdysseyMoveSession session = new NonSpecificOdysseyMoveSession(p);
        moveSessions.put(p.getUniqueId(), session);
        return session;
    }
}
