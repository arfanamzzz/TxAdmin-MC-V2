package dev.txadmin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.*;

/**
 * "Keybind" sisi server (plugin tidak bisa menambah tombol baru di klien):
 *  F = toggle noclip, Shift+F = buka menu, Shift 2x cepat saat noclip = keluar noclip.
 */
public final class Keys implements Listener {
    private final TxAdmin p;
    private final Set<UUID> off = new HashSet<>();
    private final Map<UUID, Long> lastSneak = new HashMap<>();

    public Keys(TxAdmin p) { this.p = p; }

    public boolean enabled(Player a) {
        return p.getConfig().getBoolean("keybinds.enabled", true) && !off.contains(a.getUniqueId());
    }

    public boolean toggle(Player a) {
        if (!off.remove(a.getUniqueId())) { off.add(a.getUniqueId()); return false; }
        return true;
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent e) {
        Player a = e.getPlayer();
        if (!p.isAdmin(a) || !enabled(a)) return;
        e.setCancelled(true);
        if (a.isSneaking()) Bukkit.getScheduler().runTask(p, () -> p.gui.openMain(a));
        else p.actions.noclip(a);
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        Player a = e.getPlayer();
        if (!e.isSneaking() || !p.isAdmin(a) || !enabled(a)) return;
        if (!p.modes.noclip.containsKey(a.getUniqueId())) return;
        long now = System.currentTimeMillis();
        Long last = lastSneak.put(a.getUniqueId(), now);
        if (last != null && now - last < 400) {
            lastSneak.remove(a.getUniqueId());
            Bukkit.getScheduler().runTask(p, () -> p.actions.noclip(a));
        }
    }
}
