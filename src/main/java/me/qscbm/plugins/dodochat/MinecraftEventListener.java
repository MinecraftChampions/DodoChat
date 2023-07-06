package me.qscbm.plugins.dodochat;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import io.github.minecraftchampions.dodoopenjava.api.v2.ChannelMessageApi;
import org.json.JSONObject;

import java.io.IOException;

public class MinecraftEventListener {
    @Subscribe(order = PostOrder.LAST)
    public void onChangeServerEvent(ServerPostConnectEvent event) {
        if (!DodoChat.enableChanceMessage){
            return;
        }
        RegisteredServer lastSever = event.getPreviousServer();
        if(lastSever == null) {
            return;
        }
        Player player = event.getPlayer();
        RegisteredServer server = player.getCurrentServer().get().getServer();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("server",server.getServerInfo().getName());
        jsonObject.put("lastServer",lastSever.getServerInfo().getName());
        jsonObject.put("player", player.getUsername());
        String message = Utils.parsePlaceholders(DodoChat.getConfiguration().getString("settings.ChanceServer.format"),jsonObject);
        try {
            ChannelMessageApi.sendTextMessage(DodoChat.authorization,DodoChat.getConfiguration().getString("settings.ChanceServer.channelId"),message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Subscribe(order = PostOrder.LAST)
    public void onJoinEvent(PostLoginEvent event) {
        if (!DodoChat.enableJoinMessage){
            return;
        }
        Player player = event.getPlayer();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("player", player.getUsername());
        String message = Utils.parsePlaceholders(DodoChat.getConfiguration().getString("settings.JoinMessage.format"),jsonObject);
        try {
            ChannelMessageApi.sendTextMessage(DodoChat.authorization,DodoChat.getConfiguration().getString("settings.JoinMessage.channelId"),message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Subscribe(order = PostOrder.LAST)
    public void onLeaveEvent(DisconnectEvent event) {
        if (!DodoChat.enableLeaveMessage){
            return;
        }
        Player player = event.getPlayer();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("player", player.getUsername());
        String message = Utils.parsePlaceholders(DodoChat.getConfiguration().getString("settings.LeaveMessage.format"),jsonObject);
        try {
            ChannelMessageApi.sendTextMessage(DodoChat.authorization,DodoChat.getConfiguration().getString("settings.LeaveMessage.channelId"),message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Subscribe(order = PostOrder.LAST)
    public void onChatEvent(PlayerChatEvent event) {
        if (!DodoChat.enableServerMessage){
            return;
        }
        Player player = event.getPlayer();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sender", player.getUsername());
        jsonObject.put("message", event.getMessage());
        String message = Utils.parsePlaceholders(DodoChat.getConfiguration().getString("settings.SendServerMessage.format"),jsonObject);
        try {
            ChannelMessageApi.sendTextMessage(DodoChat.authorization,DodoChat.getConfiguration().getString("settings.SendServerMessage.channelId"),message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
