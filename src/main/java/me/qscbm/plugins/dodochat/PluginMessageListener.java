package me.qscbm.plugins.dodochat;

import com.velocitypowered.api.event.AwaitingEventExecutor;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import io.github.minecraftchampions.dodoopenjava.api.v2.ChannelMessageApi;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class PluginMessageListener implements AwaitingEventExecutor<PluginMessageEvent> {

    @Override
    public @Nullable EventTask executeAsync(PluginMessageEvent event) {
        return EventTask.async(() -> {
            final boolean cancelled = !event.getResult().isAllowed()
                    || !(event.getSource() instanceof ServerConnection)
                    || !(event.getIdentifier().equals(DodoChat.MODERN_CHANNEL)
                    || event.getIdentifier().equals(DodoChat.LEGACY_CHANNEL));
            if (cancelled) {
                DodoChat.getINSTANCE().getLogger().debug("PluginMessageEvent | Not allowed");
                return;
            }
            event.setResult(PluginMessageEvent.ForwardResult.handled());

            final byte[] input = event.getData();
            String message = new String(input, StandardCharsets.UTF_8);
            try {
                ChannelMessageApi.sendTextMessage(DodoChat.authorization,DodoChat.getConfiguration().getString("settings.SendServerMessage.channelId"),message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
