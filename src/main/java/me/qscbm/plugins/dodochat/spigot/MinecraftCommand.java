package me.qscbm.plugins.dodochat.spigot;

import io.github.minecraftchampions.dodoopenjava.api.v2.ChannelMessageApi;
import me.qscbm.plugins.dodochat.common.Config;
import me.qscbm.plugins.dodochat.common.DataStorage;
import me.qscbm.plugins.dodochat.common.Verify;
import me.qscbm.plugins.dodochat.common.hook.LuckPermsHook;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class MinecraftCommand implements TabExecutor {
    public static void sendHelp(CommandSender sender) {
        sender.sendMessage("§e<----DodoChat帮助---->");
        sender.sendMessage("§e/dodochat send <channelId> <text> - 发送一条消息到Dodo频道(管理专用)");
        sender.sendMessage("§e/dodochat reload                  - 重载配置文件(管理专用)");
        sender.sendMessage("§e/dodochat help                    - 打开帮助界面");
        sender.sendMessage("§edodochat confirm <code>           - 绑定账号验证");
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 0|| args.length == 1) {
            return List.of("help","reload","send","confirm");
        } else if (args.length <= 3) {
            if (args[0].equals("send")) {
                if (args.length == 2) {
                    return List.of("频道id");
                } else {
                    return List.of("消息");
                }
            } else if (args[0].equals("confirm")) {
                if (args.length == 2) {
                    return List.of("验证码");
                } else {
                    return List.of("你确定后面还有参数?");
                }
            } else {
                return List.of("你确定后面还有参数?");
            }
        } else {
            return List.of("你确定后面还有参数?");
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.isOp() && !sender.hasPermission("dodochat.permission") ) {
            return false;
        }
        if (args.length == 0 || args.length>3) {
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
            if (Objects.equals(args[0], "send")) {
                if (sender.hasPermission("dodochat.send"))
                    try {
                        ChannelMessageApi.sendTextMessage(Config.authorization, args[1], args[2]);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                sender.sendMessage("已发送");
            } else if (Objects.equals(args[0], "confirm")) {
                if (sender instanceof Player player) {
                    if (Verify.tempMap.containsKey(player.getName())) {
                        if (args.length == 2) {
                            String key = args[1];
                            if (Verify.tempMap.get(player.getName()).values().contains(key)) {
                                try {
                                    String uuid = LuckPermsHook.luckPerms.getUserManager().lookupUniqueId(player.getName()).get().toString();
                                    String id = (String) Verify.tempMap.get(player.getName()).keySet().toArray()[0];
                                    ResultSet rs = DataStorage.stmt.executeQuery("select * from users where id = " + id + " limit 1");
                                    rs.next();
                                    String data = rs.getString("data");
                                    JSONArray jsonArray = new JSONArray(data);
                                    jsonArray.put(uuid);
                                    DataStorage.conn.prepareStatement("replace into users(id,data) values(" + id + ",'" + jsonArray + "')").executeUpdate();
                                    player.sendMessage("绑定成功");

                                } catch (InterruptedException | ExecutionException | SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                player.sendMessage("你这参数不对，请在dodo重新绑定");
                            }
                        } else {
                            player.sendMessage("你这参数不对，请在dodo重新绑定");
                        }
                        Verify.tempMap.remove(player.getName());
                    } else {
                        sender.sendMessage("没有需要验证的绑定");
                    }
                } else {
                    sender.sendMessage("请玩家使用");
                }
            } else {
                sendHelp(sender);
            }
            return true;
        }
        return true;
    }
}
