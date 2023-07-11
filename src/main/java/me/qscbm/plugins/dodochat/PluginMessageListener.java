package me.qscbm.plugins.dodochat;

import com.velocitypowered.api.event.AwaitingEventExecutor;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import org.checkerframework.checker.nullness.qual.Nullable;

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
        });
    }
}
