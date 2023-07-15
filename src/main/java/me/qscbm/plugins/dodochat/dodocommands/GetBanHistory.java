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

    /**
     * 使用的是LibertyBans插件
     */
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
            /*
             * 这个插件封禁记录数据库格式就是这样:
             * type: 0封1禁2警3踢
             +----+------+-------------+------------------------------------+--------------------------------+------------------------------------+-------------------+-------+------------+------------+
             | id | type | victim_type | victim_uuid                        | victim_address                 | operator                           | reason            | scope | start      | end        |
             +----+------+-------------+------------------------------------+--------------------------------+------------------------------------+-------------------+-------+------------+------------+
             |  1 |    0 |           0 | 0x28C57FB9BFEC4A2F86B097531272F21E | 0x00000000                     | 0x00000000000000000000000000000000 | No reason stated. |       | 1689044585 |          0 |
             |  2 |    0 |           0 | 0xBD556687AC7632AEB02917573B6F9261 | 0x00000000                     | 0x00000000000000000000000000000000 | No reason stated. |       | 1689061086 | 1689147486 |
             |  3 |    1 |           0 | 0xCD566987BNC7632EGB0467573B6C8264 | 0x00000000                     | 0x00000000000000000000000000000000 | No reason stated. |       | 1689385449 |          0 |
             |  4 |    2 |           0 | 0xBE5478878C7632EDB04564573B6H5298 | 0x00000000                     | 0x00000000000000000000000000000000 | No reason stated. |       | 1689385459 |          0 |
             |  5 |    3 |           0 | 0xAB5E66776C7632EBBA456dE73B623245 | 0x00000000                     | 0x00000000000000000000000000000000 | No reason stated. |       | 1689385626 |          0 |
             +----+------+-------------+------------------------------------+--------------------------------+------------------------------------+-------------------+-------+------------+------------+
             */
            ResultSet rs = DodoChat.banStmt.executeQuery("select * from libertybans_simple_history where victim_uuid=" + uuidSB);
            StringBuilder message = new StringBuilder("该玩家处罚记录:");
            while(rs.next()) {
                long end = rs.getLong("end");
                long start = rs.getLong("start");
                String time;
                // 如果想要指定类型的话这边是可以加个判断的
                switch (rs.getInt("type")) {
                    case 0 -> {
                        if (end == 0) {
                            time = "永久封禁";
                        } else {
                            time = "封禁" + (end-start)/24/60/60 + "天";
                        }
                    }
                    case 1 -> {
                        if (end == 0) {
                            time = "永久禁言";
                        } else {
                            time = "禁言" + (end-start)/24/60/60 + "天";
                        }
                    }
                    case 2 -> time = "警告一次";
                    case 3 -> time = "踢出服务器";
                    default ->  {
                        ChannelMessageApi.sendTextMessage(DodoChat.authorization, DodoChat.getConfiguration().getString("settings.dodoCommandChannelId"),"获取数据库失败");
                        return;
                    }
                }
                message.append("\n").append(DateUtil.format(new Date(rs.getLong("start") * 1000), DateUtil.Format_Two)).append(" -> 因").append(rs.getString("reason")).append("被判").append(time);
            }
            ChannelMessageApi.sendTextMessage(DodoChat.authorization, DodoChat.getConfiguration().getString("settings.dodoCommandChannelId"),message.toString());

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
