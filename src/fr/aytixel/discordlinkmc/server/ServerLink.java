package fr.aytixel.discordlinkmc.server;

import com.mojang.datafixers.util.Pair;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerLink {

    public final CopyOnWriteArrayList<ClientLink> clientLinkList = new CopyOnWriteArrayList<>();
    public final CopyOnWriteArrayList<Player> linkedPlayerList = new CopyOnWriteArrayList<>();

    private final HashMap<Integer, Player> playerToLinkMap = new HashMap<>();
    private final List<Integer> allowedLinkCodeList = new ArrayList<>();
    private Pair lobby;
    private boolean isLobbyActive = false;

    public synchronized void addClientLink(ClientLink clientLink) {
        clientLinkList.add(clientLink);
    }

    public synchronized void removeClientLink(ClientLink clientLink) {
        clientLink.callStop();
        linkedPlayerList.remove(clientLink.getPlayer());
        clientLinkList.remove(clientLink);

        if (linkedPlayerList.isEmpty()) isLobbyActive = false;
    }

    public synchronized void removeClientLink(Player player) {
        for (ClientLink clientLink : clientLinkList) {
            if (clientLink.getPlayer() == player) {
                clientLink.callStop();
                linkedPlayerList.remove(player);
                clientLinkList.remove(clientLink);

                if (linkedPlayerList.isEmpty()) isLobbyActive = false;

                break;
            }
        }
    }

    public synchronized void addLinkedPlayer(Player player) {
        linkedPlayerList.add(player);
    }

    public synchronized boolean containLinkedPlayer(Player player) {
        return linkedPlayerList.contains(player);
    }

    public synchronized void addPlayerToLink(int code, Player player) {
        playerToLinkMap.put(code, player);
        allowedLinkCodeList.remove((Integer) code);
    }

    public synchronized Boolean containPlayerToLink(int code) {
        return playerToLinkMap.containsKey(code);
    }

    public synchronized Player getPlayerToLink(int code) {
        Player player = playerToLinkMap.get(code);
        playerToLinkMap.remove(code);

        return player;
    }

    public synchronized void addAllowedLinkCode(int code) {
        allowedLinkCodeList.add(code);
    }

    public synchronized boolean containAllowedLinkCode(int code) {
        return allowedLinkCodeList.contains(code);
    }

    public Pair<Number, String> getLobby() {
        return lobby;
    }

    public void setLobby(Number lobbyId, String lobbySecret) {
        this.lobby = new Pair(lobbyId, lobbySecret);
    }

    public boolean isLobbyActive() {
        return isLobbyActive;
    }

    public void setIsLobbyActive(boolean state) {
        isLobbyActive = state;
    }
}