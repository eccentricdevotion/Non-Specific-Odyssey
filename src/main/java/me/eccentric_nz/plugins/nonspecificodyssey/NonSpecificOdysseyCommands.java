package me.eccentric_nz.plugins.nonspecificodyssey;

import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NonSpecificOdysseyCommands implements CommandExecutor {

    private NonSpecificOdyssey plugin;
    Random rand = new Random();
    private String plugin_name = "¤6[Non-Specific Odyssey]¤r ";

    public NonSpecificOdysseyCommands(NonSpecificOdyssey plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }
        // If the player typed /randomteleport then do the following...
        // check there is the right number of arguments
        if (cmd.getName().equalsIgnoreCase("randomteleport")) {
            if (player == null) {
                sender.sendMessage(plugin_name + "This command can only be run by a player!");
                return true;
            }
            if (!player.hasPermission("nonspecificodyssey.use")) {
                sender.sendMessage(plugin_name + "You do not have permission to run this command!");
                return true;
            }
            // get system time
            long cooldownPeriod = (long) plugin.getConfig().getInt("cooldown_time") * 1000;
            long systime = System.currentTimeMillis();
            long playerTime;
            if (plugin.rtpcooldown.containsKey(player.getName())) {
                playerTime = plugin.rtpcooldown.get(player.getName());
            } else {
                playerTime = systime - (cooldownPeriod + 1);
            }
            if (player.hasPermission("nonspecificodyssey.bypass") || (systime - playerTime) >= cooldownPeriod) {
                World pworld = player.getWorld();
                if (!player.hasPermission("nonspecificodyssey.use." + pworld.getName())) {
                    sender.sendMessage(plugin_name + "You do not have permission to random teleport in this world!");
                    return true;
                }
                Environment e = pworld.getEnvironment();
                Location random;
                if (args.length == 0) {
                    switch (pworld.getEnvironment()) {
                        case NETHER:
                            if (plugin.getConfig().getBoolean("nether") && player.hasPermission("nonspecificodyssey.nether")) {
                                random = randomNetherLocation(pworld);
                            } else {
                                player.sendMessage(plugin_name + "You cannot random teleport to the Nether");
                                return true;
                            }
                            break;
                        case THE_END:
                            if (plugin.getConfig().getBoolean("end") && player.hasPermission("nonspecificodyssey.end")) {
                                random = randomTheEndLocation(pworld);
                            } else {
                                player.sendMessage(plugin_name + "You cannot random teleport to The End");
                                return true;
                            }
                        //break;
                        default:
                            random = randomOverworldLocation(pworld);
                            break;
                    }
                    // teleport within this world only
                    sender.sendMessage(plugin_name + "Teleporting...");
                    movePlayer(player, random, pworld);
                    if (plugin.getConfig().getBoolean("cooldown")) {
                        plugin.rtpcooldown.put(player.getName(), systime);
                    }
                    return true;
                }
                if (args.length > 0) {
                    // teleport to the specified world
                    World world = plugin.getServer().getWorld(args[0]);
                    if (world == null) {
                        sender.sendMessage(plugin_name + "Could not find the world '" + world + "'. Are you sure you typed it correctly?");
                        return true;
                    }
                    if (!player.hasPermission("nonspecificodyssey.use." + args[0])) {
                        sender.sendMessage(plugin_name + "You do not have permission to random teleport to this world!");
                        return true;
                    }
                    switch (world.getEnvironment()) {
                        case NETHER:
                            if (plugin.getConfig().getBoolean("nether") && player.hasPermission("nonspecificodyssey.nether")) {
                                random = randomNetherLocation(world);
                            } else {
                                player.sendMessage(plugin_name + "You cannot random teleport to the Nether");
                                return true;
                            }
                            break;
                        case THE_END:
                            if (plugin.getConfig().getBoolean("end") && player.hasPermission("nonspecificodyssey.end")) {
                                random = randomTheEndLocation(world);
                            } else {
                                player.sendMessage(plugin_name + "You cannot random teleport to The End");
                                return true;
                            }
                        default:
                            random = randomOverworldLocation(world);
                            break;
                    }
                    sender.sendMessage(plugin_name + "Teleporting to " + world + "...");
                    movePlayer(player, random, world);
                    plugin.rtpcooldown.put(player.getName(), systime);
                    return true;
                }
            } else {
                long secs = Math.round((cooldownPeriod - (systime - playerTime)) / 1000);
                sender.sendMessage(plugin_name + "Your random teleport cooldown period still has " + secs + " seconds to go.");
                return true;
            }
        }
        if (cmd.getName().equalsIgnoreCase("nsoadmin")) {
            if (!player.hasPermission("nonspecificodyssey.admin")) {
                sender.sendMessage(plugin_name + "You do not have permission to change the config!");
                return true;
            }
            if (args.length < 1) {
                sender.sendMessage(plugin_name + "Not enough command arguments!");
                return false;
            }
            if (args[0].equalsIgnoreCase("cooldown") || args[0].equalsIgnoreCase("no_damage") || args[0].equalsIgnoreCase("nether") || args[0].equalsIgnoreCase("end")) {
                String option = args[0].toLowerCase();
                boolean bool = !plugin.getConfig().getBoolean(option);
                plugin.getConfig().set(option, bool);
                plugin.saveConfig();
                sender.sendMessage(plugin_name + option + " was set to: " + bool);
                return true;
            }
            if (args[0].equalsIgnoreCase("cooldown_time") || args[0].equalsIgnoreCase("no_damage_time") || args[0].equalsIgnoreCase("max")) {
                if (args.length < 2) {
                    sender.sendMessage(plugin_name + "Not enough command arguments!");
                    return false;
                }
                String option = args[0].toLowerCase();
                int amount = Integer.parseInt(args[1]);
                plugin.getConfig().set(option, amount);
                plugin.saveConfig();
                sender.sendMessage(plugin_name + option + " was set to: " + amount);
                return true;
            }
        }
        return false;
    }

    private Location randomOverworldLocation(World w) {
        boolean danger = true;
        Location random = null;
        // get max_radius from config
        while (danger == true) {
            int x = randomX();
            int z = randomZ();
            int y = 255;
            int highest = w.getHighestBlockYAt(x, z);
            if (highest > 3) {
                int chkBlock = w.getBlockAt(x, highest, z).getRelative(BlockFace.DOWN).getTypeId();
                if (chkBlock != 8 && chkBlock != 9 && chkBlock != 10 && chkBlock != 11 && chkBlock != 51) {
                    random = w.getBlockAt(x, highest, z).getLocation();
                    danger = false;
                    break;
                }
            }
        }
        return random;
    }

    private Location randomNetherLocation(World nether) {
        boolean danger = true;
        Location random = null;
        while (danger == true) {
            int x = randomX();
            int z = randomZ();
            int y = 100;
            Block startBlock = nether.getBlockAt(x, y, z);
            while (startBlock.getTypeId() != 0) {
                startBlock = startBlock.getRelative(BlockFace.DOWN);
            }
            int air = 0;
            while (startBlock.getTypeId() == 0 && startBlock.getLocation().getBlockY() > 30) {
                startBlock = startBlock.getRelative(BlockFace.DOWN);
                air++;
            }
            int id = startBlock.getTypeId();
            if ((id == 87 || id == 88 || id == 89 || id == 112 || id == 113 || id == 114) && air >= 4) {
                random = startBlock.getLocation();
                int randomLocY = random.getBlockY();
                random.setY(randomLocY + 1);
                danger = false;
                break;
            }
        }
        return random;
    }

    private Location randomTheEndLocation(World end) {
        boolean danger = true;
        Location random = null;
        while (danger == true) {
            int x = rand.nextInt(240);
            int z = rand.nextInt(240);
            x = x - 120;
            z = z - 120;
            // get the spawn point
            Location endSpawn = end.getSpawnLocation();
            int highest = end.getHighestBlockYAt(endSpawn.getBlockX() + x, endSpawn.getBlockZ() + z);
            if (highest > 40) {
                Block currentBlock = end.getBlockAt(x, highest, z);
                random = currentBlock.getLocation();
                danger = false;
                break;
            }
        }
        return random;
    }

    private void movePlayer(Player p, Location l, World from) {

        final Player thePlayer = p;
        final Location theLocation = l;
        final World to = theLocation.getWorld();
        final boolean allowFlight = thePlayer.getAllowFlight();
        final boolean crossWorlds = from != to;

        // try loading chunk
        World world = l.getWorld();
        Chunk chunk = world.getChunkAt(l);
        if (!world.isChunkLoaded(chunk)) {
            world.loadChunk(chunk);
        }
        //world.refreshChunk(chunk.getX(), chunk.getZ());

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                thePlayer.teleport(theLocation);
            }
        }, 10L);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                thePlayer.teleport(theLocation);
                if (plugin.getConfig().getBoolean("no_damage")) {
                    thePlayer.setNoDamageTicks(plugin.getConfig().getInt("no_damage_time") * 20);
                }
                if (thePlayer.getGameMode() == GameMode.CREATIVE || (allowFlight && crossWorlds)) {
                    thePlayer.setAllowFlight(true);
                }
            }
        }, 5L);
    }

    private int randomX() {
        int max = plugin.getConfig().getInt("max");
        int wherex;
        wherex = rand.nextInt(max);

        // add chance of negative values
        wherex = wherex * 2;
        wherex = wherex - max;

        return wherex;
    }

    private int randomZ() {
        int max = plugin.getConfig().getInt("max");
        int wherez;
        wherez = rand.nextInt(max);

        // add chance of negative values
        wherez = wherez * 2;
        wherez = wherez - max;

        return wherez;
    }
}
