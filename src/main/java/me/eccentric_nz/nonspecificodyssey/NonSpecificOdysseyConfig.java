package me.eccentric_nz.nonspecificodyssey;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class NonSpecificOdysseyConfig {

    private final NonSpecificOdyssey plugin;
    HashMap<String, String> strOptions = new HashMap<>();
    HashMap<String, Integer> intOptions = new HashMap<>();
    HashMap<String, Boolean> boolOptions = new HashMap<>();
    private FileConfiguration config = null;
    private File configFile = null;

    public NonSpecificOdysseyConfig(NonSpecificOdyssey plugin) {
        this.plugin = plugin;
        configFile = new File(plugin.getDataFolder(), "config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
        // boolean
        boolOptions.put("cooldown", true);
        boolOptions.put("no_damage", false);
        boolOptions.put("nether", false);
        boolOptions.put("end", false);
        // integer
        intOptions.put("cooldown_time", 120);
        intOptions.put("no_damage_time", 20);
        intOptions.put("max", 1024);
        intOptions.put("initial_step", 0);
        intOptions.put("step", 10);
        // string
        strOptions.put("firstline", "Random TP");
    }

    public void checkConfig() {
        int i = 0;
        // int values
        for (Map.Entry<String, Integer> entry : intOptions.entrySet()) {
            if (!config.contains(entry.getKey())) {
                plugin.getConfig().set(entry.getKey(), entry.getValue());
                i++;
            }
        }
        // string values
        for (Map.Entry<String, String> entry : strOptions.entrySet()) {
            if (!config.contains(entry.getKey())) {
                plugin.getConfig().set(entry.getKey(), entry.getValue());
                i++;
            }
        }
        // boolean values
        for (Map.Entry<String, Boolean> entry : boolOptions.entrySet()) {
            if (!config.contains(entry.getKey())) {
                plugin.getConfig().set(entry.getKey(), entry.getValue());
                i++;
            }
        }
        plugin.saveConfig();
        if (i > 0) {
            System.out.println("[NonSpecificOdyssey] Added " + i + " new items to config");
        }
    }
}
