package fr.aytixel.discordlinkmc.command;

import fr.aytixel.discordlinkmc.server.ServerLink;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandDiscordLinkMC implements CommandExecutor {

    private final ServerLink serverLink;

    public CommandDiscordLinkMC(ServerLink serverLink) {
        this.serverLink = serverLink;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (label.equals("discordlinkmc")) {
            if (args.length == 2) {
                if (args[0].equals("link")) {
                    if (commandSender instanceof Player) {
                        final Player player = (Player) commandSender;
                        final int linkCode = Integer.parseInt(args[1]);

                        if (serverLink.containLinkedPlayer(player)) commandSender.sendMessage(ChatColor.YELLOW + "You are already linked");
                        else if (serverLink.containPlayerToLink(linkCode)) commandSender.sendMessage(ChatColor.YELLOW + "This code has already been used");
                        else if (serverLink.containAllowedLinkCode(linkCode)) {
                            serverLink.addPlayerToLink(linkCode, player);

                            commandSender.sendMessage(ChatColor.GREEN + "Success, you will be linked soon");
                        } else commandSender.sendMessage(ChatColor.YELLOW + "Your code is no longer valid");
                    } else commandSender.sendMessage(ChatColor.RED + "Only the players can run this command");
                } else commandSender.sendMessage("This command doesn't exist");
            } else if (args.length < 2) commandSender.sendMessage(ChatColor.RED + "Your command has not enough argument");
            else commandSender.sendMessage(ChatColor.RED + "Your command has too many argument");

            return true;
        }

        return false;
    }
}
