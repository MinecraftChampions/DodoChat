package me.qscbm.plugins.dodochat.spigot;

import io.github.minecraftchampions.dodoopenjava.api.v2.ChannelMessageApi;
import me.qscbm.plugins.dodochat.common.Config;
import me.qscbm.plugins.dodochat.common.Utils;
import me.qscbm.plugins.dodochat.velocity.DodoChat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.json.JSONObject;

import java.io.IOException;

public class MinecraftEventListener implements Listener {
    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
        if (!Config.enableJoinMessage){
            return;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("player", e.getPlayer().getName());
        String channelId = Config.getConfiguration().getString("settings.Servers.spigot-server");
        if (channelId == null || channelId.isEmpty()) {
            return;
        }
        String message = Utils.parsePlaceholders(Config.getConfiguration().getString("settings.JoinMessage.format"),jsonObject);
        try {
            ChannelMessageApi.sendTextMessage(Config.authorization,channelId,message);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @EventHandler
    public void onPlayerLeaveEvent(PlayerQuitEvent e) {
        if (!Config.enableLeaveMessage){
            return;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("player", e.getPlayer().getName());
        String channelId = Config.getConfiguration().getString("settings.Servers.spigot-server");
        if (channelId == null || channelId.isEmpty()) {
            return;
        }
        String message = Utils.parsePlaceholders(Config.getConfiguration().getString("settings.LeaveMessage.format"),jsonObject);
        try {
            ChannelMessageApi.sendTextMessage(Config.authorization,channelId,message);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChatEvent(AsyncPlayerChatEvent e) {
        if (!Config.enableLeaveMessage){
            return;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sender", e.getPlayer().getName());
        jsonObject.put("message", e.getMessage());
        String channelId = Config.getConfiguration().getString("settings.Servers.spigot-server");
        if (channelId == null || channelId.isEmpty()) {
            return;
        }
        String message = Utils.parsePlaceholders(Config.getConfiguration().getString("settings.SendServerMessage.format"),jsonObject);
        try {
            ChannelMessageApi.sendTextMessage(Config.authorization,channelId,message);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
