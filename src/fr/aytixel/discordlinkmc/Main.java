package fr.aytixel.discordlinkmc;

import fr.aytixel.discordlinkmc.command.CommandDiscordLinkMC;
import fr.aytixel.discordlinkmc.listeners.PlayerQuitListener;
import fr.aytixel.discordlinkmc.server.ServerLink;
import fr.aytixel.discordlinkmc.server.ServerRunnable;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

public class Main extends JavaPlugin {

    private final File configurationFile = new File(this.getDataFolder(), "config.yml");
    private final PluginManager pluginManager = Bukkit.getServer().getPluginManager();

    public YamlConfiguration configuration = YamlConfiguration.loadConfiguration(configurationFile);
    public final Logger logger = Logger.getLogger("Minecraft");
    public int maxHearingDistance = 30;

    public void saveConfiguration() {
        try {
            configuration.save(configurationFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        logger.info("Discord Link MC : is starting");

        if (configuration.contains("maxHearingDistance") && configuration.isInt("maxHearingDistance")) {
            maxHearingDistance = configuration.getInt("maxHearingDistance");
        } else {
            configuration.set("maxHearingDistance", maxHearingDistance);
            saveConfiguration();
        }

        ServerLink serverLink = new ServerLink();

        //listeners
        pluginManager.registerEvents(new PlayerQuitListener(serverLink), this);

        //commands
        Objects.requireNonNull(getCommand("discordlinkmc")).setExecutor(new CommandDiscordLinkMC(serverLink));

        ServerRunnable serverRunnable = new ServerRunnable(this, serverLink);
        serverRunnable.start();

        logger.info("Discord Link MC : is started");
    }

    @Override
    public void onDisable() {
        logger.info("Discord Link MC : is stopping");
        logger.info("Discord Link MC : is stopped");
    }
}
