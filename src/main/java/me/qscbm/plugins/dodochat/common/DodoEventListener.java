package me.qscbm.plugins.dodochat.common;

import io.github.minecraftchampions.dodoopenjava.api.v2.MemberApi;
import io.github.minecraftchampions.dodoopenjava.configuration.ConfigurationSection;
import io.github.minecraftchampions.dodoopenjava.event.EventHandler;
import io.github.minecraftchampions.dodoopenjava.event.Listener;
import io.github.minecraftchampions.dodoopenjava.event.events.v2.MessageEvent;
import io.github.minecraftchampions.dodoopenjava.event.events.v2.PersonalMessageEvent;
import me.qscbm.plugins.dodochat.common.dodocommands.Verify;
import me.qscbm.plugins.dodochat.common.hook.platform.Platform;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

public class DodoEventListener implements Listener {
    @EventHandler
    public void onMessageEvent(MessageEvent event) {
        if (!Config.enableDodoMessage) {
            return;
        }
        ConfigurationSection section = Config.getConfiguration().getConfigurationSection("settings.Servers");
        if (section == null) {
            return;
        }
        section.getKeys(false).forEach(server -> {
            if (section.get(server) instanceof String channelId) {
                if (channelId.equals(event.getChannelId())) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("sender", event.getMemberNickName());
                    Set<String> players = Platform.getServerPlayerList(server);
                    if (event.getMessageIntType() == 1) {
                        String text = event.getMessageBody().getString("content");
                        if (text.indexOf(">") == 0) {
                            text = text.replaceFirst(">", "");
                        }
                        jsonObject.put("message", text);
                    } else {
                        jsonObject.put("message", "[" + event.getMessageType() + "]请在Dodo频道内查看");
                    }
                    String message;
                    if (Objects.equals(server, "spigot-server")) {
                        message = Utils.replaceString(Utils.parsePlaceholders(Config.getConfiguration().getString("settings.SendDodoMessage.spigot-format"), jsonObject));
                    } else {
                        message = Utils.replaceString(Utils.parsePlaceholders(Config.getConfiguration().getString("settings.SendDodoMessage.format"), jsonObject));
                    }
                    if (Platform.isVelocity) {
                        message = message.replaceAll("§.","");
                    } else {
                        if (message.contains("§")) {
                            List<String> list = Config.getConfiguration().getStringList("settings.SendDodoMessage.useColor");
                            if (list == null || list.isEmpty()) {
                                message = message.replaceAll("§.", "");
                            } else {
                                boolean can = false;
                                for (String str : list) {
                                    try {
                                        HashSet<String> set = new HashSet<>();
                                        JSONArray arr = MemberApi.getMemberRoleList(Config.authorization, Config.getConfiguration().getString("settings.islandId"), event.getDodoSourceId()).getJSONArray("data");
                                        for (Object o : arr) {
                                            if (o instanceof JSONObject j) {
                                                set.add(j.getString("roleId"));
                                            }
                                        }
                                        if (set.contains(str)) {
                                            can = true;
                                            break;
                                        }
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                ;
                                if (!can) {
                                    message = message.replaceAll("§.", "");
                                }
                            }
                        }
                    }
                    for (String player : players) {
                        Platform.sendMessage(player, message);
                    }
                }
            }
        });
    }

    @EventHandler
    public void onPersonalMessageEvent(PersonalMessageEvent e) {
        if (Objects.equals(e.getMessageIntType(), 1)) {
            if (e.getMessageBody().getString("content").indexOf("/") == 0) {
                String command = e.getMessageBody().getString("content").replaceFirst("/", "");
                List<String> Command = new ArrayList(List.of(command.split(" ")));
                String MainCommand = Command.get(0);
                Command.remove(0);
                String[] args = Command.toArray(new String[Command.size()]);
                if  (Objects.equals(MainCommand, "verify")) {
                    Verify.run(e,args);
                }
            }
        }
    }
}
