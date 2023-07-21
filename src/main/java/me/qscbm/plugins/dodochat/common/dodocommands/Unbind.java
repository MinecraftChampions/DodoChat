package me.qscbm.plugins.dodochat.common.dodocommands;

import io.github.minecraftchampions.dodoopenjava.api.v2.ChannelMessageApi;
import io.github.minecraftchampions.dodoopenjava.command.CommandExecutor;
import io.github.minecraftchampions.dodoopenjava.command.CommandSender;
import me.qscbm.plugins.dodochat.common.Config;
import me.qscbm.plugins.dodochat.common.DataStorage;
import me.qscbm.plugins.dodochat.common.hook.LuckPermsHook;
import org.json.JSONArray;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class Unbind implements CommandExecutor {
    @Override
    public String getMainCommand() {
        return "unbind";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public void onCommand(CommandSender commandSender, String[] strings) {
        if (!Objects.equals(commandSender.getChannelId(), Config.getConfiguration().getString("settings.dodoCommandChannelId"))) {
            return;
        }
        if (strings.length != 1) {
            new Help().onCommand(commandSender,strings);
            return;
        }
        try {
            DataStorage.conn.prepareStatement("insert ignore into users values("+ commandSender.getSenderDodoSourceId() + ",'[]')").executeUpdate();
            ResultSet rs = DataStorage.stmt.executeQuery("select * from users where id = "+commandSender.getSenderDodoSourceId() + " limit 1");
            rs.next();
            String data = rs.getString("data");
            JSONArray jsonArray = new JSONArray(data);
            if (!jsonArray.toList().contains(LuckPermsHook.luckPerms.getUserManager().lookupUniqueId(strings[0]).get().toString())) {
                ChannelMessageApi.sendTextMessage(Config.authorization, Config.getConfiguration().getString("settings.dodoCommandChannelId"),"你没有绑定这个玩家");
                return;
            }
            int index = 0;
            for (int i = 0;i<jsonArray.length();i++) {
                if (Objects.equals(jsonArray.getString(i), LuckPermsHook.luckPerms.getUserManager().lookupUniqueId(strings[0]).get().toString())) {
                    index = i;
                    break;
                }
            }
            jsonArray.remove(index);
            DataStorage.conn.prepareStatement("replace into users(id,data) values(" + commandSender.getSenderDodoSourceId() + ",'" + jsonArray + "')").executeUpdate();
            ChannelMessageApi.sendTextMessage(Config.authorization, Config.getConfiguration().getString("settings.dodoCommandChannelId"),"解绑成功");
        } catch (SQLException | IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
