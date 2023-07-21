package me.qscbm.plugins.dodochat.common.hook;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

public class LuckPermsHook {
    public static LuckPerms luckPerms;

    public static void init() {
        luckPerms = LuckPermsProvider.get();
    }
}
