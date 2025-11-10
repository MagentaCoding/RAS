package de.magentaplugins.ras;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ras extends JavaPlugin {

    private final Set<UUID> confirmationSet = new HashSet<>();
    private final Map<String, String> messages = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("RAS geladen!");

        saveDefaultConfig();
        loadLangFile();
    }

    private void loadLangFile() {
        String lang = getConfig().getString("language", "de");
        File langFile = new File(getDataFolder() + "/lang/" + lang + ".lang");
        if (!langFile.exists()) {
            saveResource("lang/" + lang + ".lang", false);
        }

        try (FileReader reader = new FileReader(langFile)) {
            Properties props = new Properties();
            props.load(reader);
            for (String key : props.stringPropertyNames()) {
                messages.put(key, props.getProperty(key));
            }
        } catch (IOException e) {
            getLogger().severe("Fehler beim Laden der Sprachdatei: " + e.getMessage());
        }
    }

    private String msg(String key) {
        return messages.getOrDefault(key, key);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("ras")) {
            UUID playerId = sender instanceof org.bukkit.entity.Player p ? p.getUniqueId() : null;

            if (confirmationSet.contains(playerId)) {
                // Zweiter Aufruf → alles entfernen
                Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
                for (Objective obj : board.getObjectives()) {
                    obj.unregister();
                }
                sender.sendMessage(msg("success"));
                confirmationSet.remove(playerId);
            } else {
                // Erstaufruf → Warnung + Timer
                sender.sendMessage(msg("warning"));
                if (playerId != null) {
                    confirmationSet.add(playerId);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            confirmationSet.remove(playerId);
                        }
                    }.runTaskLater(this, 100L); // 5 Sekunden
                }
            }
            return true;
        }
        return false;
    }
}
