package me.qscbm.plugins.dodochat.dodocommands;

import io.github.minecraftchampions.dodoopenjava.api.v2.BotApi;
import io.github.minecraftchampions.dodoopenjava.api.v2.ChannelMessageApi;
import io.github.minecraftchampions.dodoopenjava.command.CommandExecutor;
import io.github.minecraftchampions.dodoopenjava.command.CommandSender;
import me.qscbm.plugins.dodochat.DodoChat;

import java.io.IOException;
import java.util.Objects;

public class Status implements CommandExecutor {
    @Override
    public String getMainCommand() {
        return "status";
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
        try {
            String dodoId = BotApi.getBotInfo(DodoChat.authorization).getJSONObject("data").getString("dodoSourceId");
            ChannelMessageApi.sendTextMessage(DodoChat.authorization,DodoChat.getConfiguration().getString("settings.dodoCommandChannelId"),
                    "IMC.RE机器人\n" +
                    "状态:运行中\n"+
                    "DodoId:" + dodoId+"\n" +
                    "机器人版本:1.0-快照版\n" +
                    "Made by qscbm187531 and DongShaoNB");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
