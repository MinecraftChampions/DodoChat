package me.qscbm.plugins.dodochat.common.hook.platform;

import java.util.HashSet;
import java.util.Objects;

public class Platform {
    public static boolean isVelocity = false;

    public static void sendMessage(String player,String message) {
        if (isVelocity) {
            Velocity.sendMessage(player,message);
        } else {
            Spigot.sendMessage(player, message);
        }
    }

    public static boolean hasPlayer(String player) {
        if (isVelocity) {
            return Velocity.hasPlayer(player);
        } else {
            return Spigot.hasPlayer(player);
        }
    }

    public static HashSet<String> getPlayerList() {
        if (isVelocity) {
            return Velocity.getPlayerList();
        } else {
            return Spigot.getPlayerList();
        }
    }

    public static HashSet<String> getServerPlayerList(String server) {
        if (Objects.equals(server, "spigot-server") && !Platform.isVelocity) {
            return Spigot.getPlayerList();
        } else {
            return Velocity.getServerPlayerList(server);
        }
    }
}
