package me.qscbm.plugins.dodochat.dodocommands;

import io.github.minecraftchampions.dodoopenjava.api.v2.ChannelMessageApi;
import io.github.minecraftchampions.dodoopenjava.api.v2.RoleApi;
import io.github.minecraftchampions.dodoopenjava.command.CommandExecutor;
import io.github.minecraftchampions.dodoopenjava.command.CommandSender;
import io.github.minecraftchampions.dodoopenjava.configuration.ConfigurationSection;
import me.qscbm.plugins.dodochat.DodoChat;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import org.json.JSONArray;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class Call implements CommandExecutor {
    @Override
    public String getMainCommand() {
        return "call";
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
        if (strings.length != 0) {
            new Help().onCommand(commandSender,strings);
            return;
        }
        ResultSet rs;
        List<String> groupList = new ArrayList<>();
        try {
            rs = DodoChat.stmt.executeQuery("select * from users where id = "+commandSender.getSenderDodoSourceId());
            rs.next();
            String data = rs.getString("data");
            JSONArray jsonArray = new JSONArray(data);
            jsonArray.forEach(object -> {
                if (object instanceof String str) {
                    UserManager userManager = DodoChat.luckPerms.getUserManager();
                    UUID uuid;
                    User user;
                    try {
                        user = userManager.loadUser(UUID.fromString(str)).get();
                        Collection<Group> inheritedGroups = user.getInheritedGroups(user.getQueryOptions());
                        inheritedGroups.forEach(group -> {
                            String name = group.getName();
                            if (!groupList.contains(name)) {
                                groupList.add(name.toLowerCase());
                            }
                        });
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            List<Map<?, ?>> c = DodoChat.getConfiguration().getMapList("settings.Call");
            String roleId = null;
            for (Map<?,?> config : c) {
                if (groupList.contains(((String)config.get("Group")).toLowerCase())) {
                    roleId = (String) config.get("roleId");
                    RoleApi.addRoleMember(DodoChat.authorization,DodoChat.getConfiguration().getString("settings.islandId"),commandSender.getSenderDodoSourceId(),roleId);
                }
            }
            if (roleId == null || roleId.isEmpty()) {
                 ChannelMessageApi.sendTextMessage(DodoChat.authorization, DodoChat.getConfiguration().getString("settings.dodoCommandChannelId"),"没有对应的身份组发放");
                return;
            }
            ChannelMessageApi.sendTextMessage(DodoChat.authorization, DodoChat.getConfiguration().getString("settings.dodoCommandChannelId"),"已赋予身份组");
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
