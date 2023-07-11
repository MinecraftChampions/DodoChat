package me.qscbm.plugins.dodochat.dodocommands;

import io.github.minecraftchampions.dodoopenjava.api.v2.ChannelMessageApi;
import io.github.minecraftchampions.dodoopenjava.command.CommandExecutor;
import io.github.minecraftchampions.dodoopenjava.command.CommandSender;
import me.qscbm.plugins.dodochat.DodoChat;

import java.io.IOException;
import java.util.Objects;

public class Help implements CommandExecutor {

    @Override
    public String getMainCommand() {
        return "help";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public void onCommand(CommandSender commandSender, String[] strings) {
        if (Objects.equals(commandSender.getChannelId(), DodoChat.getConfiguration().getString("settings.dodoCommandChannelId"))) {
            try {
                ChannelMessageApi.sendTextMessage(DodoChat.authorization,commandSender.getChannelId(), """
                        指令列表:
                        /help                    - 获取指令列表
                        /status                  - 获取机器人状态
                        /bind <游戏名>            - 绑定账号
                        /verify <游戏名> <验证码>  - 验证(私信发送机器人)
                        /blist                   - 获得绑定的账号列表
                        /unbind <游戏名>          - 解除绑定账号
                        /resetpassword <游戏名>   - 重置账号密码
                        /getbanhistory <游戏名>    - 获取封禁记录
                        /call                    - 获取身份组
                        """);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
