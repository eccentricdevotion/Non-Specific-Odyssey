package me.eccentric_nz.nonspecificodyssey;

import org.bukkit.plugin.java.JavaPlugin;

public class NonSpecificOdyssey extends JavaPlugin {

    public static NonSpecificOdyssey plugin;
    private NonSpecificOdysseyCommands commando;
    private NonSpecificOdysseyListener listener;
    private String pluginName;

    @Override
    public void onDisable() {
    }

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
        pluginName = getConfig().getString("firstline");
    }

    public NonSpecificOdysseyCommands getCommando() {
        return commando;
    }

    public String getPluginName() {
        return pluginName;
    }

    public void debug(Object o) {
        getServer().getConsoleSender().sendMessage("[" + pluginName + "] Debug: " + o);
    }

    public NonSpecificOdysseyListener getListener() {
        return listener;
    }
}
