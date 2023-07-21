package me.qscbm.plugins.dodochat.spigot;

import io.github.minecraftchampions.dodoopenjava.api.v2.ChannelMessageApi;
import me.qscbm.plugins.dodochat.common.Config;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class MinecraftCommand implements TabExecutor {
    public static void sendHelp(CommandSender sender) {
        sender.sendMessage("§e<----DodoChat帮助---->");
        sender.sendMessage("§e/dodochat send <channelId> <text> 发送一条消息到Dodo频道");
        sender.sendMessage("§e/dodochat reload                  重载配置文件");
        sender.sendMessage("§e/dodochat help                    打开帮助界面");
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 0|| args.length == 1) {
            return List.of("help","reload","send");
        } else if (args.length <= 3) {
            if (!args[0].equals("send")) {
                return List.of("你确定后面还有参数?");
            } else {
                if (args.length == 2) {
                    return List.of("频道");
                } else {
                    return List.of("消息");
                }
            }
        } else {
            return List.of("你确定后面还有参数?");
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("dodochat.permission")) {
            return false;
        }
        if (args.length == 0 || args.length>3 || args.length == 2) {
            sendHelp(sender);
            return true;
        }
        if (args.length == 1) {
            switch(args[0]) {
                case "help" -> {
                    sendHelp(sender);
                }
                case "reload" -> {
                    DodoChat.getInstance().reload();
                    sender.sendMessage("已重载");
                }
                default -> sendHelp(sender);
            }
        } else {
            if (!Objects.equals(args[0], "send")) {
                sendHelp(sender);
            } else {
                try {
                    ChannelMessageApi.sendTextMessage(Config.authorization,args[1], args[2]);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                sender.sendMessage("已发送");
            }
        }
        return true;
    }
}
