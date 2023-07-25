package me.qscbm.plugins.dodochat.common.dodocommands;

import io.github.minecraftchampions.dodoopenjava.api.v2.ChannelMessageApi;
import io.github.minecraftchampions.dodoopenjava.command.CommandExecutor;
import io.github.minecraftchampions.dodoopenjava.command.CommandSender;
import me.qscbm.plugins.dodochat.common.Config;
import me.qscbm.plugins.dodochat.common.DataStorage;
import me.qscbm.plugins.dodochat.common.hook.LuckPermsHook;
import net.luckperms.api.model.user.User;
import org.json.JSONArray;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * IMC.RE服务器玩家信息
 */
public class MInfo implements CommandExecutor {
    @Override
    public String getMainCommand() {
        return "minfo";
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
            ResultSet rs = DataStorage.stmt.executeQuery("select * from users where id = "+ commandSender.getSenderDodoSourceId() + " limit 1");
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
            if (!userList.contains(strings[0])) {
                ChannelMessageApi.sendTextMessage(Config.authorization, Config.getConfiguration().getString("settings.dodoCommandChannelId"),"只能查询绑定的玩家信息");
                return;
            }
            String uuid = LuckPermsHook.luckPerms.getUserManager().lookupUniqueId(strings[0]).get().toString();
            ChannelMessageApi.sendTextMessage(Config.authorization, Config.getConfiguration().getString("settings.dodoCommandChannelId"), "[打开此链接查看玩家信息](https://plan.imc.re/player/" + uuid.toLowerCase() + ")");

        } catch (InterruptedException | ExecutionException | IOException e) {
            try {
                ChannelMessageApi.sendTextMessage(Config.authorization, Config.getConfiguration().getString("settings.dodoCommandChannelId"),"查询数据库失败，请联系管理员");
                e.printStackTrace();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
