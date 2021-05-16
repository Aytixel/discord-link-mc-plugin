package fr.aytixel.discordlinkmc.listeners;

import fr.aytixel.discordlinkmc.server.ServerLink;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final ServerLink serverLink;

    public PlayerQuitListener(ServerLink serverLink) {
        this.serverLink = serverLink;
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        serverLink.removeClientLink(event.getPlayer());
    }
}
