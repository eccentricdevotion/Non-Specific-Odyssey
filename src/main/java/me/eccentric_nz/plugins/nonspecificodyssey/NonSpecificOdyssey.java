package me.eccentric_nz.plugins.nonspecificodyssey;

import java.io.IOException;
import java.util.HashMap;
import org.bukkit.plugin.java.JavaPlugin;

public class NonSpecificOdyssey extends JavaPlugin {

    protected static NonSpecificOdyssey plugin;
    HashMap<String, Long> rtpcooldown = new HashMap<String, Long>();
    private NonSpecificOdysseyCommands commando;
    public NonSpecificOdysseyListener listener;

    @Override
    public void onEnable() {
        plugin = this;
        this.saveDefaultConfig();

        if (!getDataFolder().exists()) {
            if (!getDataFolder().mkdir()) {
                System.err.println("[Non-Specific Odyssey] Could not create directory!");
                System.out.println("[Non-Specific Odyssey] Requires you to manually make the NonSpecificOdyssey/ directory!");
            }
            getDataFolder().setWritable(true);
            getDataFolder().setExecutable(true);
        }
        listener = new NonSpecificOdysseyListener(this);
        this.getServer().getPluginManager().registerEvents(listener, this);
        commando = new NonSpecificOdysseyCommands(this);
        getCommand("randomteleport").setExecutor(commando);
        getCommand("nsoadmin").setExecutor(commando);
        getCommand("biome").setExecutor(commando);
        getCommand("biome").setTabCompleter(new NonSpecificOdysseyTabComplete());
        if (!this.getConfig().contains("step")) {
            this.getConfig().set("step", 10);
        }
        if (!this.getConfig().contains("firstline")) {
            this.getConfig().set("firstline", "Random TP");
        }

        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }
    }

    @Override
    public void onDisable() {
        this.saveConfig();
    }

    public NonSpecificOdysseyCommands getCommando() {
        return commando;
    }
}
