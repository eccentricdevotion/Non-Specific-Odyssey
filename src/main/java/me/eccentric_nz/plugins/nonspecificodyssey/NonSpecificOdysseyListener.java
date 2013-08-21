/*
 *  Copyright 2013 eccentric_nz.
 */
package me.eccentric_nz.plugins.nonspecificodyssey;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

/**
 *
 * @author eccentric_nz
 */
public class NonSpecificOdysseyListener implements Listener {

    List<String> travellers = new ArrayList<String>();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerSuffocate(EntityDamageByBlockEvent event) {
        Entity e = event.getEntity();
        if (e instanceof Player && event.getCause().equals(DamageCause.SUFFOCATION)) {
            Player p = (Player) e;
            String name = p.getName();
            if (travellers.contains(name)) {
                Location l = p.getLocation();
                double y = l.getWorld().getHighestBlockYAt(l);
                l.setY(y);
                p.teleport(l);
                travellers.remove(name);
            }
        }
    }
}
