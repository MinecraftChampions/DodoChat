package me.qscbm.plugins.dodochat.dodocommands;

import io.github.minecraftchampions.dodoopenjava.api.v2.ChannelMessageApi;
import io.github.minecraftchampions.dodoopenjava.command.CommandExecutor;
import io.github.minecraftchampions.dodoopenjava.command.CommandSender;
import io.github.minecraftchampions.dodoopenjava.utils.DateUtil;
import me.qscbm.plugins.dodochat.DodoChat;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    @Override
    public void onCommand(CommandSender commandSender, String[] strings) {
        if (!Objects.equals(commandSender.getChannelId(), DodoChat.getConfiguration().getString("settings.dodoCommandChannelId"))) {
            return;
        }
        if (strings.length != 1) {
            new Help().onCommand(commandSender,strings);
            return;
        }
        CompletableFuture<UUID> uuidCF = DodoChat.luckPerms.getUserManager().lookupUniqueId(strings[0]);
        try {
            UUID uuid = uuidCF.get();
            if (uuid == null) {
                ChannelMessageApi.sendTextMessage(DodoChat.authorization, DodoChat.getConfiguration().getString("settings.dodoCommandChannelId"),"查无此人");
                return;
            }
            StringBuilder uuidSB = new StringBuilder(uuid.toString().replaceAll("-","")).insert(0,"0x");
            ResultSet rs = DodoChat.banStmt.executeQuery("select * from libertybans_simple_history where victim_uuid=" + uuidSB);
            System.out.println(uuidSB);
            StringBuilder message = new StringBuilder("该玩家封禁记录:");
            while(rs.next()) {
                long end = rs.getLong("end");
                long start = rs.getLong("start");
                String time;
                if (end == 0) {
                    time = "永不解封";
                } else {
                    time = "封禁" + (end-start)/24/60/60 + "天";
                }
                message.append("\n").append(DateUtil.format(new Date(rs.getLong("start") * 1000), DateUtil.Format_Two)).append(" -> 因").append(rs.getString("reason")).append("被判").append(time);
                ChannelMessageApi.sendTextMessage(DodoChat.authorization, DodoChat.getConfiguration().getString("settings.dodoCommandChannelId"),message.toString());
            }
        } catch (InterruptedException | ExecutionException | IOException | SQLException e) {
            try {
                ChannelMessageApi.sendTextMessage(DodoChat.authorization, DodoChat.getConfiguration().getString("settings.dodoCommandChannelId"),"获取数据库失败");
                e.printStackTrace();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

}
