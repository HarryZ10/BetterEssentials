package com.harryzhu.koolkidztools;

import com.harryzhu.koolkidztools.service.PinService;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Set;

public final class KoolKidzTools extends JavaPlugin {
    private PinService pinService;

    @Override
    public void onEnable() {
        getLogger().info("KoolKidzTools has been enabled!");
        pinService = new PinService(this);
        pinService.loadPins();
    }

    @Override
    public void onDisable() {
        getLogger().info("KoolKidzTools has been disabled!");
        pinService.savePins();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (command.getName().equalsIgnoreCase("place")) {
                return setPinCommand(player, args);
            } else if (command.getName().equalsIgnoreCase("places")) {
                return listPinsCommand(player);
            }
        }
        return false;
    }

    private boolean setPinCommand(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage("You must specify a name for the pin.");
            return false;
        }

        String pinName = args[0];
        pinService.setPinLocation(player.getUniqueId(), pinName, player.getLocation());
        player.sendMessage("Pin '" + pinName + "' set at your current location!");
        pinService.savePins();
        return true;
    }

    private boolean listPinsCommand(Player player) {
        HashMap<String, Location> playerPins = pinService.getAllPinsForPlayer(player.getUniqueId());
        if (!playerPins.isEmpty()) {
            player.sendMessage("");
            player.sendMessage("");

            // Iterate through the player's pins
            for (HashMap.Entry<String, Location> entry : playerPins.entrySet()) {
                String pinName = entry.getKey();
                Location location = entry.getValue();

                // Get the player's location and round to nearest thousandth
                double x = Math.round(location.getX() * 1000) / 1000.0;
                double y = Math.round(location.getY() * 1000) / 1000.0;
                double z = Math.round(location.getZ() * 1000) / 1000.0;

                // Create a new TextComponent for the pinned location and add extra flair
                TextComponent message = new TextComponent("- ");
                message.setColor(ChatColor.YELLOW);

                // Adds the pin name to the message and extra flair
                TextComponent pinNameComponent = new TextComponent(pinName);
                pinNameComponent.setColor(ChatColor.AQUA);
                pinNameComponent.setBold(true);

                // Extra flair
                message.addExtra(pinNameComponent);
                message.addExtra(ChatColor.WHITE + ": ");

                // Adds hover event to show coordinates when hovering over the pin name
                TextComponent hoverText = new TextComponent("X=" + x + ", Y=" + y + ", Z=" + z);
                hoverText.setColor(ChatColor.GREEN);

                hoverText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(pinName + ": X=" + x + ", Y=" + y + ", Z=" + z).create()));
                message.addExtra(hoverText);

                // Sends the message to the player
                player.spigot().sendMessage(message);

                // Adds a line break between each pinned location
                player.sendMessage("");
            }
        } else {
            player.sendMessage("You have not set any pins yet.");
        }
        return true;
    }
}
