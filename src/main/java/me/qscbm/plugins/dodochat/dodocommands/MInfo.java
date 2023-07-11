package me.qscbm.plugins.dodochat.dodocommands;

import io.github.minecraftchampions.dodoopenjava.api.v2.ChannelMessageApi;
import io.github.minecraftchampions.dodoopenjava.command.CommandExecutor;
import io.github.minecraftchampions.dodoopenjava.command.CommandSender;
import me.qscbm.plugins.dodochat.DodoChat;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

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
        if (!Objects.equals(commandSender.getChannelId(), DodoChat.getConfiguration().getString("settings.dodoCommandChannelId"))) {
            return;
        }
        if (strings.length != 1) {
            new Help().onCommand(commandSender,strings);
            return;
        }
        try {
            String uuid = DodoChat.luckPerms.getUserManager().lookupUniqueId(strings[0]).get().toString();
            ChannelMessageApi.sendTextMessage(DodoChat.authorization, DodoChat.getConfiguration().getString("settings.dodoCommandChannelId"),"[打开此链接查看玩家信息](https://plan.imc.re/player/" + uuid.toLowerCase() + ")");
        } catch (InterruptedException | ExecutionException | IOException e) {
            try {
                ChannelMessageApi.sendTextMessage(DodoChat.authorization, DodoChat.getConfiguration().getString("settings.dodoCommandChannelId"),"查询数据库失败，请联系管理员");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
