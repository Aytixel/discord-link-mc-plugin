package fr.aytixel.discordlinkmc.server;

import com.mojang.datafixers.util.Pair;
import org.bukkit.entity.Player;

import java.util.Random;

public class ClientLink {

    public final ServerLink serverLink;

    private Boolean isRunning = true;
    private Number discordId;
    private String discordUsername;
    private String discordDiscriminator;
    private Player player;
    private String playersPositionJsonObject = "{}";
    private int linkCode = generateLinkCode();
    private boolean isLobbyOwner = false;

    public ClientLink(ServerLink serverLink) {
        this.serverLink = serverLink;

        while (serverLink.containAllowedLinkCode(getLinkCode())) regenerateLinkCode();
        serverLink.addAllowedLinkCode(getLinkCode());
        serverLink.addClientLink(this);
    }

    public synchronized void callStop() {
        isRunning = false;
    }

    public synchronized boolean isRunning() {
        return isRunning;
    }

    public synchronized void setDiscordUserInfo(Number id, String username, String discriminator) {
        if (!serverLink.isLobbyActive()) {
            isLobbyOwner = true;
            serverLink.setIsLobbyActive(true);
        }

        discordId = id;
        discordUsername = username;
        discordDiscriminator = discriminator;
    }

    public synchronized Number getDiscordId() {
        return discordId;
    }

    public synchronized String getDiscordTag() {
        return discordUsername + "#" + discordDiscriminator;
    }

    public synchronized void setPlayer(Player player) {
        this.player = player;
    }

    public synchronized Player getPlayer() {
        return player;
    }

    public synchronized void setPlayersPosition(String playersPosition) {
        playersPositionJsonObject = playersPosition;
    }

    public synchronized String getPlayersPosition() {
        return playersPositionJsonObject;
    }

    public synchronized int getLinkCode() {
        return linkCode;
    }

    public synchronized void regenerateLinkCode() {
        linkCode = generateLinkCode();
    }

    private int generateLinkCode() {
        return new Random().nextInt(1000000);
    }

    public boolean isLobbyOwner() {
        return isLobbyOwner;
    }

    public Pair<Number, String> getLobby() {
        return serverLink.getLobby();
    }

    public void setLobby(Number lobbyId, String lobbySecret) {
        serverLink.setLobby(lobbyId, lobbySecret);
    }
}
