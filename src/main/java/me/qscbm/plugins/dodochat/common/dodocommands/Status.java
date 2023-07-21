package me.qscbm.plugins.dodochat.common.dodocommands;

import io.github.minecraftchampions.dodoopenjava.api.v2.BotApi;
import io.github.minecraftchampions.dodoopenjava.api.v2.ChannelMessageApi;
import io.github.minecraftchampions.dodoopenjava.command.CommandExecutor;
import io.github.minecraftchampions.dodoopenjava.command.CommandSender;
import me.qscbm.plugins.dodochat.common.Config;

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
        if (!Objects.equals(commandSender.getChannelId(), Config.getConfiguration().getString("settings.dodoCommandChannelId"))) {
            return;
        }
        try {
            String dodoId = BotApi.getBotInfo(Config.authorization).getJSONObject("data").getString("dodoSourceId");
            ChannelMessageApi.sendTextMessage(Config.authorization,Config.getConfiguration().getString("settings.dodoCommandChannelId"),
                    "IMC.RE机器人\n" +
                    "状态:运行中\n"+
                    "DodoId:" + dodoId+"\n" +
                    "机器人版本:1.1\n" +
                    "Made by qscbm187531");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
