package me.qscbm.plugins.dodochat.common.dodocommands;

import io.github.minecraftchampions.dodoopenjava.api.v2.PersonalApi;
import io.github.minecraftchampions.dodoopenjava.command.CommandExecutor;
import io.github.minecraftchampions.dodoopenjava.command.CommandSender;
import io.github.minecraftchampions.dodoopenjava.event.events.v2.PersonalMessageEvent;
import me.qscbm.plugins.dodochat.common.Config;
import me.qscbm.plugins.dodochat.common.DataStorage;
import me.qscbm.plugins.dodochat.common.hook.LuckPermsHook;
import org.json.JSONArray;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class Verify implements CommandExecutor {
    public static Map<String,Map<String,String>> tempMap = new HashMap<>();

    @Override
    public String getMainCommand() {
        return "verify";
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
        new Help().onCommand(commandSender,strings);
    }

    public static void run(PersonalMessageEvent e, String[] strings) {
        try {
            if (strings.length != 2) {
                PersonalApi.sendPersonalMessage(Config.authorization, Config.getConfiguration().getString("settings.islandId"), e.getDodoSourceId(),"命令格式有误");
                return;
            }
            if (!tempMap.containsKey(strings[0])) {
                PersonalApi.sendPersonalMessage(Config.authorization, Config.getConfiguration().getString("settings.islandId"), e.getDodoSourceId(),"没有待验证的账号绑定请求");
                return;
            }
            if (!Objects.equals(tempMap.get(strings[0]).get(e.getDodoSourceId()), strings[1])) {
                PersonalApi.sendPersonalMessage(Config.authorization, Config.getConfiguration().getString("settings.islandId"), e.getDodoSourceId(),"验证码错误，请重新回到群里输入/bind命令");
                tempMap.remove(strings[0]);
                return;
            }
            tempMap.remove(strings[0]);
            String id = e.getDodoSourceId();
            String player = LuckPermsHook.luckPerms.getUserManager().lookupUniqueId(strings[0]).get().toString();

            ResultSet rs = DataStorage.stmt.executeQuery("select * from users where id = " + id + " limit 1");
            rs.next();
            String data = rs.getString("data");
            JSONArray jsonArray = new JSONArray(data);
            jsonArray.put(player);
            DataStorage.conn.prepareStatement("replace into users(id,data) values(" + id + ",'" + jsonArray + "')").executeUpdate();
            PersonalApi.sendPersonalMessage(Config.authorization, Config.getConfiguration().getString("settings.islandId"), e.getDodoSourceId(),"绑定成功");
        } catch (IOException | SQLException | InterruptedException | ExecutionException e1) {
            throw new RuntimeException(e1);
        }
    }
}
