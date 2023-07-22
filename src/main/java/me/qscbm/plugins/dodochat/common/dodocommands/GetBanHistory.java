package me.qscbm.plugins.dodochat.common.dodocommands;

import io.github.minecraftchampions.dodoopenjava.api.v2.ChannelMessageApi;
import io.github.minecraftchampions.dodoopenjava.command.CommandExecutor;
import io.github.minecraftchampions.dodoopenjava.command.CommandSender;
import io.github.minecraftchampions.dodoopenjava.utils.DateUtil;
import me.qscbm.plugins.dodochat.common.Config;
import me.qscbm.plugins.dodochat.common.hook.LibertyBansHook;
import me.qscbm.plugins.dodochat.common.hook.LuckPermsHook;
import me.qscbm.plugins.dodochat.common.hook.platform.Platform;
import space.arim.libertybans.api.PlayerVictim;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class GetBanHistory implements CommandExecutor {
    @Override
    public String getMainCommand() {
        return "getbanhistory";
    }

    @Override
    public String getPermission() {
        return null;
    }

    /**
     * 使用的是LibertyBans插件
     */
    @Override
    public void onCommand(CommandSender commandSender, String[] strings) {
        if (!Objects.equals(commandSender.getChannelId(), Config.getConfiguration().getString("settings.dodoCommandChannelId"))) {
            return;
        }
        if (!Platform.isVelocity) {
            return;
        }
        if (strings.length != 1) {
            new Help().onCommand(commandSender,strings);
            return;
        }
        CompletableFuture<UUID> uuidCF = LuckPermsHook.luckPerms.getUserManager().lookupUniqueId(strings[0]);

        try {
            UUID uuid = uuidCF.get();
            if (uuid == null) {
                ChannelMessageApi.sendTextMessage(Config.authorization, Config.getConfiguration().getString("settings.dodoCommandChannelId"),"查无此人");
                return;
            }
            StringBuilder message = new StringBuilder("该玩家历史封禁记录:");
            LibertyBansHook.libertyBans.getSelector().selectionBuilder().victim(PlayerVictim.of(LuckPermsHook.luckPerms.getUserManager().lookupUniqueId(strings[0]).get())).selectAll().build().getAllSpecificPunishments().thenAcceptAsync(list -> {
                list.forEach(p -> {
                    long end = p.getEndDateSeconds();
                    long start = p.getStartDateSeconds();
                    String time;
                    // 如果想要指定类型的话这边是可以加个判断的
                    switch (p.getType()) {
                        case BAN -> {
                            if (end == 0) {
                                time = "永久封禁";
                            } else {
                                if (end-start/24/60/60 >= 1) {
                                    time = "封禁" + (end - start)/1000 / 24d / 60d / 60d + "天";
                                } else {
                                    time = "封禁" + (end - start)/1000 / 60d / 60d + "小时";
                                }
                            }
                        }
                        case MUTE -> {
                            if (end == 0) {
                                time = "永久禁言";
                            } else {
                                if (end-start/24/60/60 >= 1) {
                                    time = "禁言" + (end - start)/1000 / 24d / 60d / 60d + "天";
                                } else {
                                    time = "禁言" + (end - start)/1000 / 60d / 60d + "小时";
                                }
                            }
                        }
                        case WARN -> time = "警告一次";
                        case KICK -> time = "踢出服务器";
                        default ->  {
                            try {
                                ChannelMessageApi.sendTextMessage(Config.authorization, Config.getConfiguration().getString("settings.dodoCommandChannelId"),"获取数据库失败");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            return;
                        }
                    }
                    message.append("\n").append(DateUtil.format(new Date(start * 1000), DateUtil.Format_Two)).append(" -> 因").append(p.getReason()).append("被判").append(time);
                });
                try {
                    ChannelMessageApi.sendTextMessage(Config.authorization, Config.getConfiguration().getString("settings.dodoCommandChannelId"),message.toString());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (InterruptedException | ExecutionException | IOException e) {
            try {
                ChannelMessageApi.sendTextMessage(Config.authorization, Config.getConfiguration().getString("settings.dodoCommandChannelId"),"获取数据库失败");
                e.printStackTrace();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

}
