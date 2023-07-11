package me.qscbm.plugins.dodochat.dodocommands;

import io.github.minecraftchampions.dodoopenjava.api.v2.ChannelMessageApi;
import io.github.minecraftchampions.dodoopenjava.command.CommandExecutor;
import io.github.minecraftchampions.dodoopenjava.command.CommandSender;
import me.qscbm.plugins.dodochat.DodoChat;
import org.json.JSONArray;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class BindList implements CommandExecutor {
    @Override
    public String getMainCommand() {
        return "blist";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public void onCommand(CommandSender commandSender, String[] strings) {
        if (!Objects.equals(commandSender.getChannelId(), DodoChat.getConfiguration().getString("settings.dodoCommandChannelId"))) {
            return;
        }
        if (strings.length != 0) {
            new Help().onCommand(commandSender,strings);
            return;
        }
        try {
            DodoChat.conn.prepareStatement("insert ignore into users values("+ commandSender.getSenderDodoSourceId() + ",'[]')").executeUpdate();
            ResultSet rs = DodoChat.stmt.executeQuery("select * from users where id = "+commandSender.getSenderDodoSourceId() + " limit 1");
            rs.next();
            String data = rs.getString("data");
            JSONArray jsonArray = new JSONArray(data);
            if (jsonArray.isEmpty()) {
                ChannelMessageApi.sendTextMessage(DodoChat.authorization, DodoChat.getConfiguration().getString("settings.dodoCommandChannelId"),"你还没有绑定玩家");
                return;
            }
            StringBuilder stringBuilder = new StringBuilder("已经绑定的玩家：");
            jsonArray.forEach(str -> {
                try {
                    stringBuilder.append("\n").append(DodoChat.luckPerms.getUserManager().lookupUsername(UUID.fromString((String) str)).get());
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });
            ChannelMessageApi.sendTextMessage(DodoChat.authorization, DodoChat.getConfiguration().getString("settings.dodoCommandChannelId"),stringBuilder.toString());
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
