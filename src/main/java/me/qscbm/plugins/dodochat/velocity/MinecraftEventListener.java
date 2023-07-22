package me.qscbm.plugins.dodochat.velocity;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import io.github.minecraftchampions.dodoopenjava.api.v2.ChannelMessageApi;
import io.github.minecraftchampions.dodoopenjava.api.v2.PersonalApi;
import me.qscbm.plugins.dodochat.common.Config;
import me.qscbm.plugins.dodochat.common.DataStorage;
import me.qscbm.plugins.dodochat.common.Utils;
import me.qscbm.plugins.dodochat.common.Verify;
import me.qscbm.plugins.dodochat.common.hook.LuckPermsHook;
import net.kyori.adventure.text.Component;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

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
        if (player.getCurrentServer().isEmpty()) {
            return;
        }
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
        if (Verify.tempMap.containsKey(event.getPlayer().getUsername())) {
            if (event.getMessage().startsWith("验证 ")) {
                String[] args = event.getMessage().split("\\s+");
                if (args.length == 2) {
                    String key = args[1];
                    if (Verify.tempMap.get(event.getPlayer().getUsername()).values().contains(key)) {
                        try {
                            String player = LuckPermsHook.luckPerms.getUserManager().lookupUniqueId(event.getPlayer().getUsername()).get().toString();
                            String id = (String)Verify.tempMap.get(event.getPlayer().getUsername()).keySet().toArray()[0];
                            ResultSet rs = DataStorage.stmt.executeQuery("select * from users where id = " + id + " limit 1");
                            rs.next();
                            String data = rs.getString("data");
                            JSONArray jsonArray = new JSONArray(data);
                            jsonArray.put(player);
                            DataStorage.conn.prepareStatement("replace into users(id,data) values(" + id + ",'" + jsonArray + "')").executeUpdate();
                            event.getPlayer().sendMessage(Component.text("绑定成功"));

                        } catch (InterruptedException | ExecutionException | SQLException  e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        event.getPlayer().sendMessage(Component.text("你这参数不对，dodo重新绑定"));
                    }
                } else {
                    event.getPlayer().sendMessage(Component.text("你这参数不对，dodo重新绑定"));
                }
                Verify.tempMap.remove(event.getPlayer().getUsername());
                event.setResult(PlayerChatEvent.ChatResult.denied());
                return;
            }
        }
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
