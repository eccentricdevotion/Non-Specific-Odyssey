/*
 *  Copyright 2014 eccentric_nz.
 */
package me.eccentric_nz.nonspecificodyssey;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

/**
 *
 * @author eccentric_nz
 */
public class NonSpecificOdysseyTabComplete implements TabCompleter {

    private final List<String> BIOME_SUBS = new ArrayList<String>();

    public NonSpecificOdysseyTabComplete() {
        for (Biome bi : org.bukkit.block.Biome.values()) {
            if (!bi.equals(Biome.HELL) && !bi.equals(Biome.SKY)) {
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
        return StringUtil.copyPartialMatches(token, from, new ArrayList<String>(from.size()));
    }
}
