package me.eccentric_nz.nonspecificodyssey;

import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class NonSpecificOdysseyCommands implements CommandExecutor {

    private final NonSpecificOdyssey plugin;
    private final Random rand = new Random();
    private final HashMap<String, Long> rtpcooldown = new HashMap<>();
    private final List<Biome> NOT_NORMAL = Arrays.asList(Biome.NETHER_WASTES, Biome.CRIMSON_FOREST, Biome.WARPED_FOREST, Biome.THE_VOID, Biome.BASALT_DELTAS, Biome.SOUL_SAND_VALLEY, Biome.END_BARRENS, Biome.END_HIGHLANDS, Biome.END_MIDLANDS, Biome.THE_END, Biome.SMALL_END_ISLANDS);

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
                sender.sendMessage("[" + plugin.getPluginName() + "] " + "This command can only be run by a player!");
                return true;
            }
            if (!player.hasPermission("nonspecificodyssey.use")) {
                sender.sendMessage("[" + plugin.getPluginName() + "] " + "You do not have permission to run this command!");
                return true;
            }
            // get system time
            long cooldownPeriod = (long) plugin.getConfig().getInt("cooldown_time") * 1000;
            long systime = System.currentTimeMillis();
            long playerTime;
            if (rtpcooldown.containsKey(player.getName())) {
                playerTime = rtpcooldown.get(player.getName());
            } else {
                playerTime = systime - (cooldownPeriod + 1);
            }
            if (player.hasPermission("nonspecificodyssey.bypass") || (systime - playerTime) >= cooldownPeriod) {
                World pworld = player.getWorld();
                if (!player.hasPermission("nonspecificodyssey.use." + pworld.getName())) {
                    sender.sendMessage("[" + plugin.getPluginName() + "] " + "You do not have permission to random teleport in this world!");
                    return true;
                }
                Location random;
                if (args.length == 0) {
                    switch (pworld.getEnvironment()) {
                        case NETHER:
                            if (plugin.getConfig().getBoolean("nether") && player.hasPermission("nonspecificodyssey.nether")) {
                                random = randomNetherLocation(pworld);
                            } else {
                                player.sendMessage("[" + plugin.getPluginName() + "] " + "You cannot random teleport in the Nether");
                                return true;
                            }
                            break;
                        case THE_END:
                            if (plugin.getConfig().getBoolean("end") && player.hasPermission("nonspecificodyssey.end")) {
                                random = randomTheEndLocation(pworld);
                            } else {
                                player.sendMessage("[" + plugin.getPluginName() + "] " + "You cannot random teleport in The End");
                                return true;
                            }
                            break;
                        default:
                            random = randomOverworldLocation(pworld);
                            break;
                    }
                    // teleport within this world only
                    sender.sendMessage("[" + plugin.getPluginName() + "] " + "Teleporting...");
                    movePlayer(player, random, pworld);
                    if (plugin.getConfig().getBoolean("cooldown")) {
                        rtpcooldown.put(player.getName(), systime);
                    }
                    return true;
                }
                if (args.length > 0) {
                    // teleport to the specified world
                    World world = plugin.getServer().getWorld(args[0]);
                    if (world == null) {
                        sender.sendMessage("[" + plugin.getPluginName() + "] " + "Could not find the world '" + args[0] + "'. Are you sure you typed it correctly?");
                        return true;
                    }
                    if (!player.hasPermission("nonspecificodyssey.use." + args[0])) {
                        sender.sendMessage("[" + plugin.getPluginName() + "] " + "You do not have permission to random teleport to this world!");
                        return true;
                    }
                    switch (world.getEnvironment()) {
                        case NETHER:
                            if (plugin.getConfig().getBoolean("nether") && player.hasPermission("nonspecificodyssey.nether")) {
                                random = randomNetherLocation(world);
                            } else {
                                player.sendMessage("[" + plugin.getPluginName() + "] " + "You cannot random teleport to the Nether");
                                return true;
                            }
                            break;
                        case THE_END:
                            if (plugin.getConfig().getBoolean("end") && player.hasPermission("nonspecificodyssey.end")) {
                                random = randomTheEndLocation(world);
                            } else {
                                player.sendMessage("[" + plugin.getPluginName() + "] " + "You cannot random teleport to The End");
                                return true;
                            }
                            break;
                        default:
                            random = randomOverworldLocation(world);
                            break;
                    }
                    sender.sendMessage("[" + plugin.getPluginName() + "] " + "Teleporting to " + world.getName() + "...");
                    movePlayer(player, random, world);
                    rtpcooldown.put(player.getName(), systime);
                    return true;
                }
            } else {
                long secs = Math.round((cooldownPeriod - (systime - playerTime)) / 1000);
                sender.sendMessage("[" + plugin.getPluginName() + "] " + "Your random teleport cooldown period still has " + secs + " seconds to go.");
                return true;
            }
        }
        if (cmd.getName().equalsIgnoreCase("biome")) {
            if (args.length < 1) {
                return false;
            }
            String upper = args[0].toUpperCase(Locale.ENGLISH);
            if (upper.equals("LIST")) {
                StringBuilder sb = new StringBuilder();
                for (Biome bi : Biome.values()) {
                    if (!NOT_NORMAL.contains(bi)) {
                        sb.append(bi.toString()).append(", ");
                    }
                }
                String b = sb.toString().substring(0, sb.length() - 2);
                sender.sendMessage("Biomes: " + b);
                return true;
            } else {
                if (!sender.hasPermission("nonspecificodyssey.biome." + upper)) {
                    sender.sendMessage("[" + plugin.getPluginName() + "] " + "You do not have permission to use biome teleports!");
                    return true;
                }
                if (player == null) {
                    sender.sendMessage("[" + plugin.getPluginName() + "] " + "This command can only be run by a player!");
                    return true;
                }
                World w = null;
                if (args.length > 1) {
                    plugin.getServer().getWorld(args[1]);
                    if (w == null) {
                        sender.sendMessage("[" + plugin.getPluginName() + "] " + "Could not find the world '" + args[1] + "'. Are you sure you typed it correctly?");
                        return true;
                    }
                }
                try {
                    Biome biome = Biome.valueOf(upper);
                    sender.sendMessage("Searching for biome, this may take some time!");
                    Location nsob = searchBiome(player, biome, w);
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
                sender.sendMessage("[" + plugin.getPluginName() + "] " + "You do not have permission to change the config!");
                return true;
            }
            if (args.length < 1) {
                sender.sendMessage("[" + plugin.getPluginName() + "] " + "Not enough command arguments!");
                return false;
            }
            if (args[0].equalsIgnoreCase("cooldown") || args[0].equalsIgnoreCase("no_damage") || args[0].equalsIgnoreCase("nether") || args[0].equalsIgnoreCase("end")) {
                String option = args[0].toLowerCase();
                boolean bool = !plugin.getConfig().getBoolean(option);
                plugin.getConfig().set(option, bool);
                plugin.saveConfig();
                sender.sendMessage("[" + plugin.getPluginName() + "] " + option + " was set to: " + bool);
                return true;
            }
            if (args[0].equalsIgnoreCase("cooldown_time") || args[0].equalsIgnoreCase("no_damage_time") || args[0].equalsIgnoreCase("max") || args[0].equalsIgnoreCase("step") || args[0].equalsIgnoreCase("initial_step")) {
                if (args.length < 2) {
                    sender.sendMessage("[" + plugin.getPluginName() + "] " + "Not enough command arguments!");
                    return false;
                }
                String option = args[0].toLowerCase();
                int amount = Integer.parseInt(args[1]);
                plugin.getConfig().set(option, amount);
                plugin.saveConfig();
                sender.sendMessage("[" + plugin.getPluginName() + "] " + option + " was set to: " + amount);
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
                Material chkBlock = w.getBlockAt(x, highest, z).getRelative(BlockFace.DOWN).getType();
                if (!chkBlock.equals(Material.WATER) && !chkBlock.equals(Material.LAVA) && !chkBlock.equals(Material.FIRE)) {
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
            while (!startBlock.getType().equals(Material.AIR)) {
                startBlock = startBlock.getRelative(BlockFace.DOWN);
            }
            int air = 0;
            while (startBlock.getType().equals(Material.AIR) && startBlock.getLocation().getBlockY() > 30) {
                startBlock = startBlock.getRelative(BlockFace.DOWN);
                air++;
            }
            Material id = startBlock.getType();
            if ((id.equals(Material.NETHERRACK) || id.equals(Material.SOUL_SAND) || id.equals(Material.GLOWSTONE) || id.equals(Material.NETHER_BRICK) || id.equals(Material.NETHER_BRICK_FENCE) || id.equals(Material.NETHER_BRICK_STAIRS)) && air >= 4) {
                random = startBlock.getLocation();
                int randomLocY = random.getBlockY();
                random.setY(randomLocY + 1);
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
                break;
            }
        }
        return random;
    }

    public void movePlayer(Player p, Location l, World from) {

        UUID uuid = p.getUniqueId();
        plugin.getListener().getTravellers().add(uuid);
        l.setY(l.getY() + 0.2);
        Location theLocation = l;

        World to = theLocation.getWorld();
        boolean allowFlight = p.getAllowFlight();
        boolean crossWorlds = from != to;

        // try loading chunk
        World world = l.getWorld();
        Chunk chunk = world.getChunkAt(l);
        while (!world.isChunkLoaded(chunk)) {
            world.loadChunk(chunk);
        }

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            p.teleport(theLocation);
            p.getWorld().playSound(theLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
        }, 10L);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            p.teleport(theLocation);
            if (plugin.getConfig().getBoolean("no_damage")) {
                p.setNoDamageTicks(plugin.getConfig().getInt("no_damage_time") * 20);
            }
            if (p.getGameMode() == GameMode.CREATIVE || (allowFlight && crossWorlds)) {
                p.setAllowFlight(true);
            }
        }, 15L);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (plugin.getListener().getTravellers().contains(uuid)) {
                plugin.getListener().getTravellers().remove(uuid);
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

    public Location searchBiome(Player p, Biome b, World w) {
        Location l = null;
        int startx = p.getLocation().getBlockX();
        int startz = p.getLocation().getBlockZ();
        if (w == null) {
            w = p.getLocation().getWorld();
        }
        int limit = 30000;
        int step = plugin.getConfig().getInt("step");
        // search in a random direction
        Integer[] directions = new Integer[]{0, 1, 2, 3};
        Collections.shuffle(Arrays.asList(directions));
        for (int i = 0; i < 4; i++) {
            switch (directions[i]) {
                case 0:
                    // east
                    startx += plugin.getConfig().getInt("initial_step");
                    for (int east = startx; east < limit; east += step) {
                        Biome chkb = w.getBiome(east, startz);
                        if (chkb.equals(b)) {
                            p.sendMessage("[" + plugin.getPluginName() + "] " + b.toString() + " biome found in an easterly direction!");
                            return new Location(w, east, w.getHighestBlockYAt(east, startz), startz);
                        }
                    }
                    break;
                case 1:
                    startz += plugin.getConfig().getInt("initial_step");
                    // south
                    for (int south = startz; south < limit; south += step) {
                        Biome chkb = w.getBiome(startx, south);
                        if (chkb.equals(b)) {
                            p.sendMessage("[" + plugin.getPluginName() + "] " + b.toString() + " biome found in a southerly direction!");
                            return new Location(w, startx, w.getHighestBlockYAt(startx, south), south);
                        }
                    }
                    break;
                case 2:
                    // west
                    startx -= plugin.getConfig().getInt("initial_step");
                    for (int west = startx; west > -limit; west -= step) {
                        Biome chkb = w.getBiome(west, startz);
                        if (chkb.equals(b)) {
                            p.sendMessage("[" + plugin.getPluginName() + "] " + b.toString() + " biome found in a westerly direction!");
                            return new Location(w, west, w.getHighestBlockYAt(west, startz), startz);
                        }
                    }
                    break;
                case 3:
                    startz -= plugin.getConfig().getInt("initial_step");
                    // north
                    for (int north = startz; north > -limit; north -= step) {
                        Biome chkb = w.getBiome(startx, north);
                        if (chkb.equals(b)) {
                            p.sendMessage("[" + plugin.getPluginName() + "] " + b.toString() + " biome found in a northerly direction!");
                            return new Location(w, startx, w.getHighestBlockYAt(startx, north), north);
                        }
                    }
                    break;
            }
        }
        return l;
    }
}
