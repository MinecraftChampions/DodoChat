package me.qscbm.plugins.dodochat.velocity.dodocommands;

import io.github.minecraftchampions.dodoopenjava.api.v2.ChannelMessageApi;
import io.github.minecraftchampions.dodoopenjava.api.v2.PersonalApi;
import io.github.minecraftchampions.dodoopenjava.command.CommandExecutor;
import io.github.minecraftchampions.dodoopenjava.command.CommandSender;
import me.qscbm.plugins.dodochat.common.Config;
import me.qscbm.plugins.dodochat.common.DataStorage;
import me.qscbm.plugins.dodochat.common.dodocommands.Help;
import me.qscbm.plugins.dodochat.common.hook.LuckPermsHook;
import me.qscbm.plugins.dodochat.common.hook.platform.Platform;
import me.qscbm.plugins.dodochat.velocity.DodoChat;
import org.json.JSONArray;

import java.io.IOException;
import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class ResetPassword implements CommandExecutor {
    private static final String str = "abcdefghijklmnopqrstuvwxyz";
    private static final String numStr = "0123456789";

    private static char getRandomChar(String str) {
        SecureRandom random = new SecureRandom();
        return str.charAt(random.nextInt(str.length()));
    }

    private static char getLowChar() {
        return getRandomChar(str);
    }

    private static char getUpperChar() {
        return Character.toUpperCase(getLowChar());
    }

    private static char getNumChar() {
        return getRandomChar(numStr);
    }
    private static char getRandomChar(int funNum) {
        switch (funNum) {
            case 1 -> {
                return getUpperChar();
            }
            case 2 -> {
                return getNumChar();
            }
            default -> {
                return getLowChar();
            }
        }
    }


    @Override
    public String getMainCommand() {
        return "resetpassword";
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
        if (strings.length != 1 ) {
            new Help().onCommand(commandSender,strings);
            return;
        }
        try {
            DataStorage.conn.prepareStatement("insert ignore into users values(" + commandSender.getSenderDodoSourceId() + ",'[]')").executeUpdate();
            ResultSet rs = DataStorage.stmt.executeQuery("select * from users where id = " + commandSender.getSenderDodoSourceId() + " limit 1");
            rs.next();
            String data = rs.getString("data");
            JSONArray jsonArray = new JSONArray(data);
            String uuid = LuckPermsHook.luckPerms.getUserManager().lookupUniqueId(strings[0]).get().toString();
            if (!jsonArray.toList().contains(uuid)) {
                ChannelMessageApi.sendTextMessage(Config.authorization, Config.getConfiguration().getString("settings.dodoCommandChannelId"),"你没有绑定这个账号");
                return;
            }
            List<Character> list = new ArrayList<>(12);
            list.add(getLowChar());
            list.add(getUpperChar());
            list.add(getNumChar());
            for (int i = 3; i < 12; i++) {
                SecureRandom random = new SecureRandom();
                int funNum = random.nextInt(3);
                list.add(getRandomChar(funNum));
            }
            Collections.shuffle(list);
            StringBuilder stringBuilder = new StringBuilder(list.size());
            for (Character c : list) {
                stringBuilder.append(c);
            }
            String newPassword = stringBuilder.toString();
            DodoChat.getINSTANCE().getServer().getCommandManager().executeAsync(DodoChat.getINSTANCE().getServer().getConsoleCommandSource(),"forcechangepassword " + strings[0] + " " + newPassword);
            PersonalApi.sendPersonalMessage(Config.authorization, commandSender.getIslandSourceId(),commandSender.getSenderDodoSourceId(),"你的密码已经重置，重置后密码为: `" + newPassword + "`,请上线后及时更改密码");
            ChannelMessageApi.sendTextMessage(Config.authorization, commandSender.getChannelId(),"已重置密码，请查看私信");
        } catch (SQLException | IOException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
