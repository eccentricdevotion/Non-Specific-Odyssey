/*
 *  Copyright 2014 eccentric_nz.
 */
package me.eccentric_nz.nonspecificodyssey;

import com.google.common.collect.ImmutableList;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author eccentric_nz
 */
public class NonSpecificOdysseyTabComplete implements TabCompleter {

    private final List<String> BIOME_SUBS = new ArrayList<>();
    private final List<Biome> NOT_NORMAL = Arrays.asList(Biome.NETHER, Biome.END_BARRENS, Biome.END_HIGHLANDS, Biome.END_MIDLANDS, Biome.THE_END, Biome.SMALL_END_ISLANDS);

    public NonSpecificOdysseyTabComplete() {
        for (Biome bi : org.bukkit.block.Biome.values()) {
            if (!NOT_NORMAL.contains(bi)) {
                BIOME_SUBS.add(bi.toString());
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        // Remember that we can return null to default to online player name matching
        if (args.length == 1) {
            return partial(args[args.length - 1], BIOME_SUBS);
        }
        return ImmutableList.of();
    }

    private List<String> partial(String token, Collection<String> from) {
        return StringUtil.copyPartialMatches(token, from, new ArrayList<>(from.size()));
    }
}
