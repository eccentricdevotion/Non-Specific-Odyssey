package me.eccentric_nz.plugins.nonspecificodyssey;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class NonSpecificOdysseyConfig {

    private final NonSpecificOdyssey plugin;
    private FileConfiguration config = null;
    private File configFile = null;
    HashMap<String, String> strOptions = new HashMap<String, String>();
    HashMap<String, Integer> intOptions = new HashMap<String, Integer>();
    HashMap<String, Boolean> boolOptions = new HashMap<String, Boolean>();

    public NonSpecificOdysseyConfig(NonSpecificOdyssey plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        this.config = YamlConfiguration.loadConfiguration(configFile);
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
