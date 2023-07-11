package me.qscbm.plugins.dodochat;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import io.github.minecraftchampions.dodoopenjava.api.v2.ChannelMessageApi;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.IOException;
import java.util.Objects;

public class MinecraftCommand implements SimpleCommand {
    public static void sendHelp(Invocation invocation) {
        CommandSource source = invocation.source();
        Component component1 = MiniMessage.miniMessage().deserialize("<yellow><----DodoChat帮助----></yellow>");
        Component component2 = MiniMessage.miniMessage().deserialize("<yellow>/dodochat send <channelId> <text> 发送一条消息到Dodo频道</yellow>");
        Component component3 = MiniMessage.miniMessage().deserialize("<yellow>/dodochat reload                  重载配置文件</yellow>");
        Component component4 = MiniMessage.miniMessage().deserialize("<yellow>/dodochat help                    打开帮助界面</yellow>");
        source.sendMessage(component1);
        source.sendMessage(component2);
        source.sendMessage(component3);
        source.sendMessage(component4);
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 0 || args.length>3 || args.length == 2) {
            sendHelp(invocation);
            return;
        }
        if (args.length == 1) {
            switch(args[0]) {
                case "help" -> {
                    sendHelp(invocation);
                }
                case "reload" -> {
                    DodoChat.getINSTANCE().reload();
                    invocation.source().sendMessage(Component.text("已重载"));
                }
                default -> sendHelp(invocation);
            }
        } else {
            if (!Objects.equals(args[0], "send")) {
                sendHelp(invocation);
            } else {
                try {
                    ChannelMessageApi.sendTextMessage(DodoChat.authorization,args[1], args[2]);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                invocation.source().sendMessage(Component.text("已发送"));
            }
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("dodochat.permission");
    }
}
