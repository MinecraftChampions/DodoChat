package me.qscbm.plugins.dodochat.common.hook;

import space.arim.libertybans.api.LibertyBans;
import space.arim.omnibus.Omnibus;
import space.arim.omnibus.OmnibusProvider;

public class LibertyBansHook {

    public static Omnibus omnibus = OmnibusProvider.getOmnibus();

    public static LibertyBans libertyBans = null;

    public static void init() {
        libertyBans = omnibus.getRegistry().getProvider(LibertyBans.class).get();
    }
}
