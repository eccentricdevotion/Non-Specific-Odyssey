package me.eccentric_nz.plugins.nonspecificodyssey;

import java.io.IOException;
import java.util.HashMap;
import org.bukkit.plugin.java.JavaPlugin;

public class NonSpecificOdyssey extends JavaPlugin {

    protected static NonSpecificOdyssey plugin;
    HashMap<String, Long> rtpcooldown = new HashMap<String, Long>();
    private NonSpecificOdysseyCommands commando;

    @Override
    public void onEnable() {
        plugin = this;
        this.saveDefaultConfig();

        if (!getDataFolder().exists()) {
            if (!getDataFolder().mkdir()) {
                System.err.println("[NonSpecificOdyssey] Could not create directory!");
                System.out.println("[NonSpecificOdyssey] Requires you to manually make the NonSpecificOdyssey/ directory!");
            }
            getDataFolder().setWritable(true);
            getDataFolder().setExecutable(true);
        }
        commando = new NonSpecificOdysseyCommands(plugin);
        getCommand("randomteleport").setExecutor(commando);

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
}
