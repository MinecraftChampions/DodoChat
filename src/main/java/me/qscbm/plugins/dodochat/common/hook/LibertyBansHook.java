package me.qscbm.plugins.dodochat.common.hook;

import me.qscbm.plugins.dodochat.velocity.LibertybansListener;
import space.arim.libertybans.api.LibertyBans;
import space.arim.libertybans.api.event.PunishEvent;
import space.arim.omnibus.Omnibus;
import space.arim.omnibus.OmnibusProvider;
import space.arim.omnibus.events.ListenerPriorities;

public class LibertyBansHook {

    public static Omnibus omnibus = OmnibusProvider.getOmnibus();

    public static LibertyBans libertyBans = null;

    public static void init() {
        libertyBans = omnibus.getRegistry().getProvider(LibertyBans.class).get();
        omnibus.getEventBus().registerListener(PunishEvent.class, ListenerPriorities.NORMAL, new LibertybansListener());

    }
}
