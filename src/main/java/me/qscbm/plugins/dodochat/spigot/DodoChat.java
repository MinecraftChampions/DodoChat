package me.qscbm.plugins.dodochat.spigot;

import io.github.minecraftchampions.dodoopenjava.command.Command;
import io.github.minecraftchampions.dodoopenjava.event.EventManage;
import me.qscbm.plugins.dodochat.common.Config;
import me.qscbm.plugins.dodochat.common.DataStorage;
import me.qscbm.plugins.dodochat.common.DodoEventListener;
import me.qscbm.plugins.dodochat.common.dodocommands.*;
import me.qscbm.plugins.dodochat.common.hook.Hook;
import me.qscbm.plugins.dodochat.common.hook.platform.Platform;
import me.qscbm.plugins.dodochat.spigot.cmdmapping.Trigger;
import me.qscbm.plugins.dodochat.common.dodocommands.McCmdHelp;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class DodoChat extends JavaPlugin {
    @Override
    public void onEnable() {
        INSTANCE = this;
        Platform.isVelocity = false;
        Hook.init();
        Config.init();
        DataStorage.init(Config.getConfiguration().getString("settings.MySQL.url"),Config.getConfiguration().getString("settings.MySQL.name"),Config.getConfiguration().getString("settings.MySQL.password"),Config.getConfiguration().getString("settings.MySQL.database"));
        Bukkit.getPluginManager().registerEvents(new MinecraftEventListener(),this);
        Bukkit.getPluginCommand("dodochat").setExecutor(new MinecraftCommand());
        Bukkit.getPluginCommand("dodochat").setTabCompleter(new MinecraftCommand());
        EventManage.registerEvents(new Trigger(),Config.authorization); //注册DodoOpenJava事件
        EventManage.registerEvents(new DodoEventListener(),Config.authorization);
        saveDefaultConfig();
        if (Config.getConfiguration().getBoolean("settings.EnableCommands")) {
            Command.registerCommand(Config.authorization, new Help(), new Bind(), new Status(), new BindList(), new Unbind(),
                    new Call(), new MInfo(), new McCmdHelp());
        }
        saveResource("database.json",false);
        getLogger().info("DodoChat已加载");

    }

    public static DodoChat getInstance() {
        return INSTANCE;
    }

    public static DodoChat INSTANCE;

    @Override
    public void onDisable() {
        getLogger().info("DodoChat已卸载");
    }

    public void reload() {
        getLogger().info("重载配置文件中");
        Config.init();
        DataStorage.init(Config.getConfiguration().getString("settings.MySQL.url"),Config.getConfiguration().getString("settings.MySQL.name"),Config.getConfiguration().getString("settings.MySQL.password"),Config.getConfiguration().getString("settings.MySQL.database"));
        getLogger().info("DodoChat已重载");
    }
}
