package me.qscbm.plugins.dodochat.common.hook.platform;

import org.bukkit.Bukkit;

import java.util.HashSet;

public class Spigot {
    public static void sendMessage(String player,String message) {
        Bukkit.getPlayer(player).sendMessage(message);
    }

    public static boolean hasPlayer(String player) {
        return Bukkit.getPlayer(player) != null;
    }

    public static HashSet<String> getPlayerList() {
        HashSet<String> set = new HashSet<>();
        Bukkit.getOnlinePlayers().forEach(player -> set.add(player.getName()));
        return set;
    }
}
