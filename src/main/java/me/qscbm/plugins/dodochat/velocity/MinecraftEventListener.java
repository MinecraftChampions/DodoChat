package me.qscbm.plugins.dodochat.velocity;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import io.github.minecraftchampions.dodoopenjava.api.v2.ChannelMessageApi;
import me.qscbm.plugins.dodochat.common.Config;
import me.qscbm.plugins.dodochat.common.Utils;
import org.json.JSONObject;

import java.io.IOException;

public class MinecraftEventListener {
    public static RegisteredServer lobbyServer = null;
    @Subscribe(order = PostOrder.LAST)
    public void onJoinServerEvent(ServerPostConnectEvent event) {
        if (!Config.enableJoinMessage){
            return;
        }
        RegisteredServer lastSever = event.getPreviousServer();
        Player player = event.getPlayer();
        RegisteredServer server = player.getCurrentServer().get().getServer();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("player", player.getUsername());
        String channelId = Config.getConfiguration().getString("settings.Servers." + server.getServerInfo().getName());
        if (channelId == null || channelId.isEmpty()) {
            return;
        }
        String message = Utils.parsePlaceholders(Config.getConfiguration().getString("settings.JoinMessage.format"),jsonObject);
        try {
            ChannelMessageApi.sendTextMessage(Config.authorization,channelId,message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (lastSever == null) {
            lobbyServer = server;
        }
        if (lastSever != null && Config.enableLeaveMessage) {
            String channelId1 = Config.getConfiguration().getString("settings.Servers." + lastSever.getServerInfo().getName());
            if (channelId1 == null || channelId1.isEmpty()) {
                return;
            }
            message = Utils.parsePlaceholders(Config.getConfiguration().getString("settings.LeaveMessage.format"),jsonObject);
            try {
                ChannelMessageApi.sendTextMessage(Config.authorization,channelId1,message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Subscribe(order = PostOrder.LAST)
    public void onLeaveEvent(DisconnectEvent event) {
        if (!Config.enableLeaveMessage) {
            return;
        }
        Player player = event.getPlayer();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("player", player.getUsername());
        String channelId1 = Config.getConfiguration().getString("settings.Servers." + player.getCurrentServer().get().getServerInfo().getName());
        if (channelId1 == null || channelId1.isEmpty()) {
            return;
        }
        String message = Utils.parsePlaceholders(Config.getConfiguration().getString("settings.LeaveMessage.format"),jsonObject);
        try {
            ChannelMessageApi.sendTextMessage(Config.authorization,channelId1,message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Subscribe(order = PostOrder.LAST)
    public void onChatEvent(PlayerChatEvent event) {
        if (!Config.enableServerMessage){
            return;
        }
        Player player = event.getPlayer();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sender", player.getUsername());
        jsonObject.put("message", event.getMessage());
        String channelId = Config.getConfiguration().getString("settings.Servers." + player.getCurrentServer().get().getServerInfo().getName());
        if (channelId == null || channelId.isEmpty()) {
            return;
        }
        String message = Utils.parsePlaceholders(Config.getConfiguration().getString("settings.SendServerMessage.format"),jsonObject);
        try {
            ChannelMessageApi.sendTextMessage(Config.authorization,channelId,message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
