package me.qscbm.plugins.dodochat.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import io.github.minecraftchampions.dodoopenjava.api.v2.ChannelMessageApi;
import me.qscbm.plugins.dodochat.common.Config;
import me.qscbm.plugins.dodochat.common.DataStorage;
import me.qscbm.plugins.dodochat.common.Verify;
import me.qscbm.plugins.dodochat.common.hook.LuckPermsHook;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.json.JSONArray;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MinecraftCommand implements SimpleCommand {
    public void sendHelp(Invocation invocation) {
        CommandSource source = invocation.source();
        Component component1 = MiniMessage.miniMessage().deserialize("<yellow><----DodoChat帮助----></yellow>");
        Component component2 = MiniMessage.miniMessage().deserialize("<yellow>/dodochat send <channelId> <text> - 发送一条消息到Dodo频道(管理专用)</yellow>");
        Component component3 = MiniMessage.miniMessage().deserialize("<yellow>/dodochat reload                  - 重载配置文件(管理专用)</yellow>");
        Component component4 = MiniMessage.miniMessage().deserialize("<yellow>/dodochat help                    - 打开帮助界面</yellow>");
        Component component5 = MiniMessage.miniMessage().deserialize("<yellow>/dodochat confirm <code>          - 绑定账号验证</yellow>");
        source.sendMessage(component1);
        source.sendMessage(component2);
        source.sendMessage(component3);
        source.sendMessage(component4);
        source.sendMessage(component5);
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 0 || args.length > 3) {
            sendHelp(invocation);
            return;
        }
        if (args.length == 1) {
            switch (args[0]) {
                case "help" -> sendHelp(invocation);
                case "reload" -> {
                    if (invocation.source().hasPermission("dodochat.reload")) {
                        DodoChat.getINSTANCE().reload();
                        invocation.source().sendMessage(Component.text("已重载"));
                    } else {
                        sendHelp(invocation);
                    }
                }
                default -> sendHelp(invocation);
            }
        } else {
            if (Objects.equals(args[0], "send")) {
                if (invocation.source().hasPermission("dodochat.send"))
                    try {
                        ChannelMessageApi.sendTextMessage(Config.authorization, args[1], args[2]);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                invocation.source().sendMessage(Component.text("已发送"));
            } else if (Objects.equals(args[0], "confirm")) {
                if (invocation.source() instanceof Player player) {
                    if (Verify.tempMap.containsKey(player.getUsername())) {
                        if (args.length == 2) {
                            String key = args[1];
                            if (Verify.tempMap.get(player.getUsername()).values().contains(key)) {
                                try {
                                    String uuid = LuckPermsHook.luckPerms.getUserManager().lookupUniqueId(player.getUsername()).get().toString();
                                    String id = (String) Verify.tempMap.get(player.getUsername()).keySet().toArray()[0];
                                    ResultSet rs = DataStorage.stmt.executeQuery("select * from users where id = " + id + " limit 1");
                                    rs.next();
                                    String data = rs.getString("data");
                                    JSONArray jsonArray = new JSONArray(data);
                                    jsonArray.put(uuid);
                                    DataStorage.conn.prepareStatement("replace into users(id,data) values(" + id + ",'" + jsonArray + "')").executeUpdate();
                                    player.sendMessage(Component.text("绑定成功"));

                                } catch (InterruptedException | ExecutionException | SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                player.sendMessage(Component.text("你这参数不对，请在dodo重新绑定"));
                            }
                        } else {
                            player.sendMessage(Component.text("你这参数不对，请在dodo重新绑定"));
                        }
                        Verify.tempMap.remove(player.getUsername());
                    } else {
                        invocation.source().sendMessage(Component.text("没有需要验证的绑定"));
                    }
                } else {
                    invocation.source().sendMessage(Component.text("请玩家使用"));
                }
            } else {
                sendHelp(invocation);
            }
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 0|| args.length == 1) {
            return CompletableFuture.completedFuture(List.of("help","reload","send","confirm"));
        } else if (args.length <= 3) {
            if (args[0].equals("send")) {
                if (args.length == 2) {
                    return CompletableFuture.completedFuture(List.of("频道id"));
                } else {
                    return CompletableFuture.completedFuture(List.of("消息"));
                }
            } else if (args[0].equals("confirm")) {
                if (args.length == 2) {
                    return CompletableFuture.completedFuture(List.of("验证码"));
                } else {
                    return CompletableFuture.completedFuture(List.of("你确定后面还有参数?"));
                }
            } else {
                return CompletableFuture.completedFuture(List.of("你确定后面还有参数?"));
            }
        } else {
            return CompletableFuture.completedFuture(List.of("你确定后面还有参数?"));
        }
    }
}
