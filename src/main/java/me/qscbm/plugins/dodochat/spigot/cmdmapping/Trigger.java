package me.qscbm.plugins.dodochat.spigot.cmdmapping;

import io.github.minecraftchampions.dodoopenjava.api.v2.ChannelMessageApi;
import io.github.minecraftchampions.dodoopenjava.command.CommandSender;
import io.github.minecraftchampions.dodoopenjava.event.EventHandler;
import io.github.minecraftchampions.dodoopenjava.event.Listener;
import io.github.minecraftchampions.dodoopenjava.event.events.v2.MessageEvent;
import me.qscbm.plugins.dodochat.common.Config;
import me.qscbm.plugins.dodochat.common.DataStorage;
import me.qscbm.plugins.dodochat.common.hook.LuckPermsHook;
import me.qscbm.plugins.dodochat.spigot.DodoChat;
import me.qscbm.plugins.dodochat.spigot.DodoCommandSource;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.json.JSONArray;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Trigger implements Listener {
    @EventHandler
    public void onMessageEvent(MessageEvent e) {
        String content = e.getMessageBody().getString("content");
        if (content.indexOf("/") != 0) {
            return;
        }
        String command = content.replaceFirst("/", "");
        List<Object> o = new ArrayList<>(Arrays.asList(Config.getConfiguration().getList("settings.CommandMapping.MappingList").toArray()));
        List<Map<String,String>> configList = new ArrayList<>();
        for (Object ob : o) {
            if (ob instanceof Map c) {
                configList.add(c);
            }
        }
        if(configList.isEmpty()) {
            return;
        }
        String[] args = command.split(" ");
        if (args.length == 1) {
            CommandSender s = new CommandSender();
            s.InitSender(e.jsonObject);
            return;
        }
        String dodoId = e.getDodoSourceId();
        String islandId = e.getIslandSourceId();
        String channelId = e.getChannelId();
        for (Map<String,String> config : configList) {
            String doCommand = config.get("dodoCommand");
            String mcCommand = config.get("mcCommand");
            if (Objects.equals(doCommand, args[0])) {
                String player = args[1];
                List<String> list = new ArrayList<>(List.of(args));
                list.remove(1);
                list.remove(0);
                list.add(0,mcCommand);
                String cmd = StringUtils.join(list," ");
                try {
                    DataStorage.conn.prepareStatement("insert ignore into users values("+ dodoId + ",'[]')").executeUpdate();
                    ResultSet rs = DataStorage.stmt.executeQuery("select * from users where id = "+ dodoId + " limit 1");
                    rs.next();
                    String data = rs.getString("data");
                    JSONArray jsonArray = new JSONArray(data);
                    List<String> userList = new ArrayList<>();
                    jsonArray.forEach(object->{
                        if (object instanceof String str) {
                            try {
                                userList.add(LuckPermsHook.luckPerms.getUserManager().lookupUsername(UUID.fromString(str)).get());
                            } catch (InterruptedException | ExecutionException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    });
                    if (!userList.contains(player)) {
                        ChannelMessageApi.sendTextMessage(Config.authorization, Config.getConfiguration().getString("settings.dodoCommandChannelId"),"错误的玩家名字");
                        return;
                    }
                    UUID uuid = LuckPermsHook.luckPerms.getUserManager().lookupUniqueId(player).get();
                    DodoCommandSource source = new DodoCommandSource(uuid,channelId,islandId,dodoId);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DodoChat.getInstance(),()-> {
                        boolean s;
                        s = (Bukkit.dispatchCommand(source, cmd));
                        if (!s) {
                            try {
                                ChannelMessageApi.sendTextMessage(Config.authorization, Config.getConfiguration().getString("settings.dodoCommandChannelId"),"命令执行失败");
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        } else {
                            try {
                                ChannelMessageApi.sendTextMessage(Config.authorization, Config.getConfiguration().getString("settings.dodoCommandChannelId"),"命令执行成功");
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    });
                } catch (SQLException ex) {
                    try {
                        ChannelMessageApi.sendTextMessage(Config.authorization, Config.getConfiguration().getString("settings.dodoCommandChannelId"),"命令执行错误");
                        ex.printStackTrace();
                    } catch (IOException exc) {
                        throw new RuntimeException(exc);
                    }
                    throw new RuntimeException(ex);
                } catch (IOException | ExecutionException | InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
}
