package me.eccentric_nz.plugins.nonspecificodyssey;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NonSpecificOdysseyCommands implements CommandExecutor {

    private final NonSpecificOdyssey plugin;
    private final Random rand = new Random();
    private final String plugin_name = ChatColor.GOLD + "[Non-Specific Odyssey] " + ChatColor.RESET;

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
                            break;
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
                            break;
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
        if (cmd.getName().equalsIgnoreCase("biome")) {
            if (args.length < 1) {
                return false;
            }
            String upper = args[0].toUpperCase(Locale.ENGLISH);
            if (upper.equals("LIST")) {
                String b = "";
                for (Biome bi : Biome.values()) {
                    if (!bi.equals(Biome.HELL) && !bi.equals(Biome.SKY)) {
                        b += bi.toString() + ", ";
                    }
                }
                b = b.substring(0, b.length() - 2);
                sender.sendMessage("Biomes: " + b);
                return true;
            } else {
                if (!sender.hasPermission("nonspecificodyssey.biome." + upper)) {
                    sender.sendMessage(plugin_name + "You do not have permission to use biome teleports!");
                    return true;
                }
                if (player == null) {
                    sender.sendMessage(plugin_name + "This command can only be run by a player!");
                    return true;
                }
                try {
                    Biome biome = Biome.valueOf(upper);
                    sender.sendMessage("Searching for biome, this may take some time!");
                    Location nsob = searchBiome(player, biome);
                    if (nsob == null) {
                        sender.sendMessage("Could not find biome!");
                        return true;
                    } else {
                        movePlayer(player, nsob, player.getLocation().getWorld());
                    }
                } catch (IllegalArgumentException iae) {
                    sender.sendMessage("Biome type not valid!");
                }
                return true;
            }
        }
        if (cmd.getName().equalsIgnoreCase("nsoadmin")) {
            if (!sender.hasPermission("nonspecificodyssey.admin")) {
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

    public Location randomOverworldLocation(World w) {
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
            x -= 120;
            z -= 120;
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

    public void movePlayer(Player p, Location l, World from) {

        final Player thePlayer = p;
        plugin.listener.travellers.add(p.getName());
        l.setY(l.getY() + 0.2);
        final Location theLocation = l;

        final World to = theLocation.getWorld();
        final boolean allowFlight = thePlayer.getAllowFlight();
        final boolean crossWorlds = from != to;

        // try loading chunk
        World world = l.getWorld();
        Chunk chunk = world.getChunkAt(l);
        while (!world.isChunkLoaded(chunk)) {
            world.loadChunk(chunk);
        }

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                thePlayer.teleport(theLocation);
                thePlayer.getWorld().playSound(theLocation, Sound.ENDERMAN_TELEPORT, 1.0F, 1.0F);
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
        }, 15L);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (plugin.listener.travellers.contains(thePlayer.getName())) {
                    plugin.listener.travellers.remove(thePlayer.getName());
                }
            }
        }, 100L);
    }

    private int randomX() {
        int max = plugin.getConfig().getInt("max");
        int wherex;
        wherex = rand.nextInt(max);

        // add chance of negative values
        wherex *= 2;
        wherex -= max;

        return wherex;
    }

    private int randomZ() {
        int max = plugin.getConfig().getInt("max");
        int wherez;
        wherez = rand.nextInt(max);

        // add chance of negative values
        wherez *= 2;
        wherez -= max;

        return wherez;
    }

    private Location searchBiome(Player p, Biome b) {
        Location l = null;
        int startx = p.getLocation().getBlockX();
        int startz = p.getLocation().getBlockZ();
        World w = p.getLocation().getWorld();
        int limit = 30000;
        int step = plugin.getConfig().getInt("step");
        //int diagonal = (int) Math.round(Math.sqrt(Math.pow(step, 2) / 2D));
        // search in a random direction
        Integer[] directions = new Integer[]{0, 1, 2, 3};
        Collections.shuffle(Arrays.asList(directions));
        for (int i = 0; i < 4; i++) {
            switch (directions[i]) {
                case 0:
                    // east
                    for (int east = startx; east < limit; east += step) {
                        Biome chkb = w.getBiome(east, startz);
                        if (chkb.equals(b)) {
                            p.sendMessage(plugin_name + b.toString() + " biome found in an easterly direction!");
                            return new Location(w, east, w.getHighestBlockYAt(east, startz), startz);
                        }
                    }
                    break;
                case 1:
                    // south
                    for (int south = startz; south < limit; south += step) {
                        Biome chkb = w.getBiome(startx, south);
                        if (chkb.equals(b)) {
                            p.sendMessage(plugin_name + b.toString() + " biome found in a southerly direction!");
                            return new Location(w, startx, w.getHighestBlockYAt(startx, south), south);
                        }
                    }
                    break;
                case 2:
                    // west
                    for (int west = startx; west > -limit; west -= step) {
                        Biome chkb = w.getBiome(west, startz);
                        if (chkb.equals(b)) {
                            p.sendMessage(plugin_name + b.toString() + " biome found in a westerly direction!");
                            return new Location(w, west, w.getHighestBlockYAt(west, startz), startz);
                        }
                    }
                    break;
                case 3:
                    // north
                    for (int north = startz; north > -limit; north -= step) {
                        Biome chkb = w.getBiome(startx, north);
                        if (chkb.equals(b)) {
                            p.sendMessage(plugin_name + b.toString() + " biome found in a northerly direction!");
                            return new Location(w, startx, w.getHighestBlockYAt(startx, north), north);
                        }
                    }
                    break;
            }
        }
        return l;
    }
}
