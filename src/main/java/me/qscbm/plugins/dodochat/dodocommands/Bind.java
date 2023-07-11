package me.qscbm.plugins.dodochat.dodocommands;

import io.github.minecraftchampions.dodoopenjava.api.v2.ChannelMessageApi;
import io.github.minecraftchampions.dodoopenjava.command.CommandExecutor;
import io.github.minecraftchampions.dodoopenjava.command.CommandSender;
import me.qscbm.plugins.dodochat.DodoChat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.json.JSONArray;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class Bind implements CommandExecutor {
    @Override
    public String getMainCommand() {
        return "bind";
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
        if (strings.length != 1) {
            new Help().onCommand(commandSender,strings);
            return;
        }
        try {
            DodoChat.conn.prepareStatement("insert ignore into users values("+ commandSender.getSenderDodoSourceId() + ",'[]')").executeUpdate();
            ResultSet rs = DodoChat.stmt.executeQuery("select * from users where id = "+commandSender.getSenderDodoSourceId() + " limit 1");
            rs.next();
            String data = rs.getString("data");
            JSONArray jsonArray = new JSONArray(data);
            if (DodoChat.getINSTANCE().getServer().getPlayer(strings[0]).isEmpty()) {
                ChannelMessageApi.sendTextMessage(DodoChat.authorization, DodoChat.getConfiguration().getString("settings.dodoCommandChannelId"),"玩家不在线，请登录后再试");
                return;
            }
            if (jsonArray.toList().contains(DodoChat.luckPerms.getUserManager().lookupUniqueId(strings[0]).get().toString())) {
                ChannelMessageApi.sendTextMessage(DodoChat.authorization, DodoChat.getConfiguration().getString("settings.dodoCommandChannelId"),"你已经绑定了这个玩家");
                return;
            }
            rs = DodoChat.stmt.executeQuery("select * from users");
            while (rs.next()) {
                String  temp = rs.getString("data");
                JSONArray json = new JSONArray(temp);
                if (json.toList().contains(strings[0])) {
                    ChannelMessageApi.sendTextMessage(DodoChat.authorization, DodoChat.getConfiguration().getString("settings.dodoCommandChannelId"),"已经有人绑定了这个玩家");
                    return;
                }
            }
            StringBuilder stringBuilder = new StringBuilder(String.valueOf(new Random().nextInt(9999)));
            int length = stringBuilder.length();
            if (length < 4) {
                for (int i = 0;i<4-length;i++) {
                    stringBuilder.insert(0,"0");
                }
            }
            ChannelMessageApi.sendTextMessage(DodoChat.authorization, DodoChat.getConfiguration().getString("settings.dodoCommandChannelId"),"已将验证码发送到账户，请注意查收，私信机器人”/verify <游戏名> <验证码>“验证");
            String code = stringBuilder.toString();
            Component component = MiniMessage.miniMessage().deserialize("<gray>服务器悄悄的对你说:你收到这条消息的原因是Dodo频道中有人要绑定你的账号，如果非本人操作，请不要理会。验证码:"+code+"</gray>");
            DodoChat.getINSTANCE().getServer().getPlayer(strings[0]).get().sendMessage(component);
            Verify.tempMap.put(strings[0], Map.of(commandSender.getSenderDodoSourceId(),code));
        } catch (SQLException | IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
