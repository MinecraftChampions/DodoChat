package me.qscbm.plugins.dodochat.dodocommands;

import io.github.minecraftchampions.dodoopenjava.api.v2.ChannelMessageApi;
import io.github.minecraftchampions.dodoopenjava.command.CommandExecutor;
import io.github.minecraftchampions.dodoopenjava.command.CommandSender;
import me.qscbm.plugins.dodochat.DodoChat;
import net.elytrium.limboauth.event.AuthUnregisterEvent;
import org.json.JSONArray;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ResetPassword implements CommandExecutor {
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
        if (!Objects.equals(commandSender.getChannelId(), DodoChat.getConfiguration().getString("settings.dodoCommandChannelId"))) {
            return;
        }
        if (strings.length != 1 ) {
            return;
        }
        try {
            DodoChat.conn.prepareStatement("insert ignore into users values(" + commandSender.getSenderDodoSourceId() + ",'[]')").executeUpdate();
            ResultSet rs = DodoChat.stmt.executeQuery("select * from users where id = " + commandSender.getSenderDodoSourceId() + " limit 1");
            rs.next();
            String data = rs.getString("data");
            JSONArray jsonArray = new JSONArray(data);
            String uuid = DodoChat.luckPerms.getUserManager().lookupUniqueId(strings[0]).get().toString();
            if (!jsonArray.toList().contains(uuid)) {
                ChannelMessageApi.sendTextMessage(DodoChat.authorization, DodoChat.getConfiguration().getString("settings.dodoCommandChannelId"),"你没有绑定这个账号");
                return;
            }
            DodoChat.limboAuth.removePlayerFromCache(DodoChat.luckPerms.getUserManager().lookupUsername(UUID.fromString(uuid)).get());
            DodoChat.limboAuth.getPlayerDao().deleteById(DodoChat.luckPerms.getUserManager().lookupUsername(UUID.fromString(uuid)).get().toLowerCase(Locale.ROOT));
            DodoChat.getINSTANCE().getServer().getEventManager().fireAndForget(new AuthUnregisterEvent(strings[0]));
            ChannelMessageApi.sendTextMessage(DodoChat.authorization, DodoChat.getConfiguration().getString("settings.dodoCommandChannelId"),"已重置密码，请及时登录");
        } catch (SQLException | IOException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
