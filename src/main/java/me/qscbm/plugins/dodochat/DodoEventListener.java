package me.qscbm.plugins.dodochat;

import com.velocitypowered.api.proxy.Player;
import io.github.minecraftchampions.dodoopenjava.configuration.ConfigurationSection;
import io.github.minecraftchampions.dodoopenjava.event.EventHandler;
import io.github.minecraftchampions.dodoopenjava.event.Listener;
import io.github.minecraftchampions.dodoopenjava.event.events.v2.MessageEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Objects;

public class DodoEventListener implements Listener {
    @EventHandler
    public void onMessageEvent(MessageEvent event) {
        if (!DodoChat.enableDodoMessage) {
            return;
        }
        ConfigurationSection section = DodoChat.getConfiguration().getConfigurationSection("settings.SendDodoMessage.channelId");
        section.getKeys(true).forEach(server -> {
            if (section.get(server) instanceof String channelId) {
                if (channelId.equals(event.getChannelId())) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("sender",event.getMemberNickName());
                    Collection<Player> players = DodoChat.getINSTANCE().getServer().getServer(server).get().getPlayersConnected();
                    if (event.getMessageIntType() == 1) {
                        String text = event.getMessageBody().getString("content");
                        if (text.indexOf(">")==0) {
                            text = text.replaceFirst(">","");
                        }
                        jsonObject.put("message",text);
                    } else {
                        jsonObject.put("message","[" + event.getMessageType() + "]请在Dodo频道内查看");
                    }
                    String message = Utils.replaceString(Utils.parsePlaceholders(DodoChat.getConfiguration().getString("settings.SendDodoMessage.format"),jsonObject));

                    Component component = MiniMessage.miniMessage().deserialize(message);
                    players.forEach(player -> player.sendMessage(component));
                }
            }
        });
    }
}
