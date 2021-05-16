package fr.aytixel.discordlinkmc.server;

import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import fr.aytixel.discordlinkmc.Main;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ServerRunnable implements Runnable {

    private final Main main;
    private final String host;
    private final int port;
    private final ServerLink serverLink;

    public ServerRunnable(Main main, ServerLink serverLink) {
        this.main = main;
        this.serverLink = serverLink;

        if (main.configuration.contains("host") && main.configuration.isString("host")) {
            host = main.configuration.getString("host");
        } else {
            host = "0.0.0.0";
            main.configuration.set("host", host);
            main.saveConfiguration();
        }
        if (main.configuration.contains("port") && main.configuration.isInt("port")) {
            port = main.configuration.getInt("port");
        } else {
            port = 25555;
            main.configuration.set("port", port);
            main.saveConfiguration();
        }
    }

    public void start() {
        final Thread worker = new Thread(this, "Discord Link Mc-Thread");
        worker.setDaemon(true);
        worker.start();
    }

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(host, port));

            main.logger.info("Server is listening on " + host + ":" + port);

            new Thread(() -> {
                main.logger.info("Linker thread is running");

                while (true) {
                    for (ClientLink clientLink: serverLink.clientLinkList) {
                        if (clientLink.isRunning()) {
                            final int linkCode = clientLink.getLinkCode();
                            if (serverLink.containPlayerToLink(linkCode)) {
                                final Player player = serverLink.getPlayerToLink(linkCode);

                                main.logger.info(player.getDisplayName() + " has been linked with " + clientLink.getDiscordTag());
                                player.sendMessage(ChatColor.GREEN + "You have been linked with " + clientLink.getDiscordTag());

                                clientLink.setPlayer(player);
                                serverLink.addLinkedPlayer(player);
                            }
                        } else serverLink.removeClientLink(clientLink);
                    }

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, "Discord Link Mc-Thread").start();

            new Thread(() -> {
                main.logger.info("Update thread is running");

                final GsonBuilder builder = new GsonBuilder();
                final Gson gson = builder.create();

                while (true) {
                    List<Pair<Player, Location>> playerLocationPairList = new ArrayList<>();

                    for (Player linkedPlayer : serverLink.linkedPlayerList) playerLocationPairList.add(new Pair<>(linkedPlayer, linkedPlayer.getLocation()));

                    HashMap<Player, Number> playerDiscordMap = new HashMap<>();

                    for (ClientLink clientLink : serverLink.clientLinkList) playerDiscordMap.put(clientLink.getPlayer(), clientLink.getDiscordId());

                    HashMap<Number, JsonObject> discordLocationMap = new HashMap<>();

                    for (Pair<Player, Location> playerLocationPair : playerLocationPairList) {
                        Location playerLocation = playerLocationPair.getSecond();

                        discordLocationMap.put(playerDiscordMap.get(
                                playerLocationPair.getFirst()),
                                gson.fromJson("{\"world\":\"" + Objects.requireNonNull(playerLocation.getWorld()).getName() + "\",\"x\":" + playerLocation.getX() + ",\"y\":" + playerLocation.getY() + ",\"z\":" + playerLocation.getZ() + "}", JsonElement.class).getAsJsonObject()
                        );
                    }

                    for (ClientLink clientLink : serverLink.clientLinkList) clientLink.setPlayersPosition(gson.toJson(discordLocationMap));

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, "Discord Link Mc-Thread").start();

            while (true) {
                Socket socket = serverSocket.accept();
                ClientRunnable clientRunnable = new ClientRunnable(main, socket, new ClientLink(serverLink));

                main.logger.info("New client connected");

                clientRunnable.start();
            }
        } catch (IOException e) {
            main.logger.severe("Server crash");

            e.printStackTrace();
        }
    }
}