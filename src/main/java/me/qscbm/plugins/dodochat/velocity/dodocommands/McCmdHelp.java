package me.qscbm.plugins.dodochat.velocity.dodocommands;

import io.github.minecraftchampions.dodoopenjava.api.v2.ChannelMessageApi;
import io.github.minecraftchampions.dodoopenjava.command.CommandExecutor;
import io.github.minecraftchampions.dodoopenjava.command.CommandSender;
import io.github.minecraftchampions.dodoopenjava.configuration.ConfigurationSection;
import me.qscbm.plugins.dodochat.common.Config;
import me.qscbm.plugins.dodochat.common.dodocommands.Help;
import me.qscbm.plugins.dodochat.common.hook.platform.Platform;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;

public class McCmdHelp implements CommandExecutor {
    @Override
    public String getMainCommand() {
        return Config.getConfiguration().getString("settings.CommandMapping.Settings.help.command");
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
        if (!Platform.isVelocity) {
            return;
        }
        if (strings.length != 0) {
            new Help().onCommand(commandSender,strings);
            return;
        }
        String content = Config.getConfiguration().getString("settings.CommandMapping.Settings.help.content");
        List<String> stringList = new ArrayList<>(Arrays.stream(content.split("\n")).toList());
        int line = 0;
        for (int i = 0;i<stringList.size();i++) {
            if (stringList.get(i).contains("%dodoCommand%") && stringList.get(i).contains("%vcCommand%")) {
                line = i;
                break;
            }
        }
        List<Object> o = new ArrayList<>(Arrays.asList(Config.getConfiguration().getList("settings.CommandMapping.MappingList").toArray()));
        List<Map<String,String>> configList = new ArrayList<>();
        for (Object ob : o) {
            if (ob instanceof Map c) {
                configList.add(c);
            }
        }
        int length = configList.size();
        for (int i = 0;i<length-1;i++) {
            stringList.add(line+1+i,stringList.get(line));
        }
        String text = StringUtils.join(stringList,"\n");
        for (Map<String,String> config : configList) {
            text = text.replaceFirst("%dodoCommand%",config.get("dodoCommand"));
            text = text.replaceFirst("%vcCommand%",config.get("vcCommand"));
        }
        try {
            ChannelMessageApi.sendTextMessage(Config.authorization,commandSender.getChannelId(),text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
