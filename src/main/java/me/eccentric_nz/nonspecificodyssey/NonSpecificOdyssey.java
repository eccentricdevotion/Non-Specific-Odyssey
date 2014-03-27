package me.eccentric_nz.nonspecificodyssey;

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
        saveDefaultConfig();
        new NonSpecificOdysseyConfig(this).checkConfig();
        listener = new NonSpecificOdysseyListener(this);
        getServer().getPluginManager().registerEvents(listener, this);
        commando = new NonSpecificOdysseyCommands(this);
        getCommand("randomteleport").setExecutor(commando);
        getCommand("nsoadmin").setExecutor(commando);
        getCommand("biome").setExecutor(commando);
        getCommand("biome").setTabCompleter(new NonSpecificOdysseyTabComplete());

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
