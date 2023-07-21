package me.qscbm.plugins.dodochat.common.hook;

import me.qscbm.plugins.dodochat.common.hook.platform.Platform;

public class Hook {

    public static void init() {
        LuckPermsHook.init();
        if (Platform.isVelocity) {
            LibertyBansHook.init();
        }
    }
}
