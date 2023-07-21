package me.qscbm.plugins.dodochat.common.hook.platform;

import me.qscbm.plugins.dodochat.velocity.DodoChat;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;

import java.util.HashSet;

public class Velocity {
    public static void sendMessage(String player,String message) {
        DodoChat.getINSTANCE().getServer().getPlayer(player).get().sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    public static boolean hasPlayer(String player) {
        return DodoChat.getINSTANCE().getServer().getPlayer(player).isPresent();
    }

    public static HashSet<String> getPlayerList() {
        HashSet<String> set = new HashSet<>();
        DodoChat.getINSTANCE().getServer().getAllPlayers().forEach(player -> set.add(player.getUsername()));
        return set;
    }

    public static HashSet<String> getServerPlayerList(String server) {
        HashSet<String> set = new HashSet<>();
        DodoChat.getINSTANCE().getServer().getServer(server).get().getPlayersConnected().forEach(player -> set.add(player.getUsername()));
        return set;
    }
}
