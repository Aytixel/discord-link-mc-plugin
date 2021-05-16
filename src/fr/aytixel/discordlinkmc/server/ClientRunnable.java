package fr.aytixel.discordlinkmc.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.aytixel.discordlinkmc.Main;

import java.io.*;
import java.net.Socket;

public class ClientRunnable implements Runnable {

    private final Main main;
    private final Socket socket;
    private final ClientLink clientLink;

    public ClientRunnable(Main main, Socket socket, ClientLink clientLink) {
        this.main = main;
        this.socket = socket;
        this.clientLink = clientLink;
    }

    public void start() {
        final Thread worker = new Thread(this, "Discord Link Mc-Thread");
        worker.setDaemon(true);
        worker.start();
    }

    public void run() {
        try {
            final InputStream input = socket.getInputStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            final OutputStream output = socket.getOutputStream();
            final PrintWriter writer = new PrintWriter(output, true);

            final Gson gson = new Gson();
            JsonObject jsonObject;

            writer.println(String.format("{\"state\":\"serverConfig\",\"maxHearingDistance\":%d}", main.maxHearingDistance));

            do {
                String text = reader.readLine();
                jsonObject = gson.fromJson(text, JsonElement.class).getAsJsonObject();

                String state = jsonObject.get("state").getAsString();

                switch (state) {
                    case "discordUserInfo" -> {
                        clientLink.setDiscordUserInfo(jsonObject.get("id").getAsNumber(), jsonObject.get("username").getAsString(), jsonObject.get("discriminator").getAsString());

                        if (clientLink.isLobbyOwner()) {
                            writer.println("{\"state\":\"createLobby\"}");

                            main.logger.info(clientLink.getDiscordTag() + " is the owner of the new lobby");
                        } else if (clientLink.getLobby() != null) writer.println(String.format("{\"state\":\"connectLobby\",\"id\":%d,\"secret\":\"%s\"}", clientLink.getLobby().getFirst().longValue(), clientLink.getLobby().getSecond()));

                        writer.println(String.format("{\"state\":\"linkCode\",\"code\":%d}", clientLink.getLinkCode()));

                        main.logger.info("Link code send to the new client " + clientLink.getDiscordTag());

                        new Thread(() -> {
                            main.logger.info(clientLink.getDiscordTag() + " client position thread is running");

                            while (clientLink.isRunning()) {
                                writer.println(String.format("{\"state\":\"sendPlayersPosition\",\"positions\":%s}", clientLink.getPlayersPosition()));

                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            writer.println("{\"state\":\"end\"}");
                        }, "Discord Link Mc-Thread").start();
                    }
                    case "createLobby" -> {
                        clientLink.setLobby(jsonObject.get("id").getAsNumber(), jsonObject.get("secret").getAsString());

                        main.logger.info("The new lobby is : id : " + jsonObject.get("id").getAsNumber() + ", secret : " + jsonObject.get("secret").getAsString());
                    }
                }
            } while (clientLink.isRunning() && !jsonObject.get("state").getAsString().equals("end"));

            socket.close();

            main.logger.info(clientLink.getDiscordTag() + " client has disconnected");
        } catch (IOException ex) {
            main.logger.warning("Server exception : " + ex.getMessage());
        }

        clientLink.callStop();
    }
}
