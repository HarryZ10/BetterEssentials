package com.harryzhu.koolkidztools.service;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PinService {
    private final Map<UUID, Map<String, Location>> pinLocations = new HashMap<>();
    private final FileConfiguration pinConfig;
    private final File pinFile;

    public PinService(JavaPlugin plugin) {
        pinFile = new File(plugin.getDataFolder(), "pins.yml");
        pinConfig = YamlConfiguration.loadConfiguration(pinFile);
    }

    public void loadPins() {
        if (pinConfig.contains("pins")) {
            for (String uuidKey : Objects.requireNonNull(pinConfig.getConfigurationSection("pins")).getKeys(false)) {
                UUID playerUUID = UUID.fromString(uuidKey);
                Map<String, Location> playerPins = new HashMap<>();

                for (String pinName : Objects.requireNonNull(pinConfig.getConfigurationSection("pins." + uuidKey)).getKeys(false)) {
                    Location location = new Location(
                            Bukkit.getWorld(Objects.requireNonNull(pinConfig.getString("pins." + uuidKey + "." + pinName + ".world"))),
                            pinConfig.getDouble("pins." + uuidKey + "." + pinName + ".x"),
                            pinConfig.getDouble("pins." + uuidKey + "." + pinName + ".y"),
                            pinConfig.getDouble("pins." + uuidKey + "." + pinName + ".z")
                    );
                    playerPins.put(pinName, location);
                }

                pinLocations.put(playerUUID, playerPins);
            }
        }
    }

    public void savePins() {
        pinConfig.set("pins", null); // Clear existing pins

        for (UUID playerUUID : pinLocations.keySet()) {
            Map<String, Location> playerPins = pinLocations.get(playerUUID);

            for (String pinName : playerPins.keySet()) {
                Location location = playerPins.get(pinName);
                String key = playerUUID.toString() + "." + pinName;
                pinConfig.set("pins." + key + ".world", Objects.requireNonNull(location.getWorld()).getName());
                pinConfig.set("pins." + key + ".x", location.getX());
                pinConfig.set("pins." + key + ".y", location.getY());
                pinConfig.set("pins." + key + ".z", location.getZ());
            }
        }

        try {
            pinConfig.save(pinFile);
        } catch (IOException e) {
            Bukkit.getLogger().info("Something went wrong!");
        }
    }

    public void setPinLocation(UUID uuid, String pinName, Location location) {
        pinLocations.computeIfAbsent(uuid, k -> new HashMap<>()).put(pinName, location);
        Bukkit.getLogger().info("Set pin '" + pinName + "' for player " + uuid); // Debug message
    }

    public Map<String, Location> getPinLocations(UUID uniqueId) {
        return pinLocations.getOrDefault(uniqueId, new HashMap<>());
    }

    public HashMap<String, Location> getAllPinsForPlayer(UUID uniqueId) {
        HashMap<String, Location> playerPins = new HashMap<>();
        for (String pinName : getPinLocations(uniqueId).keySet()) {
            playerPins.put(pinName, getPinLocations(uniqueId).get(pinName));
        }
        return playerPins;
    }
}
