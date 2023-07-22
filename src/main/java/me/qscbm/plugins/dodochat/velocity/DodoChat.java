package me.qscbm.plugins.dodochat.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.minecraftchampions.dodoopenjava.command.Command;
import io.github.minecraftchampions.dodoopenjava.event.EventManage;
import io.github.minecraftchampions.dodoopenjava.event.websocket.EventTrigger;
import me.qscbm.plugins.dodochat.common.Config;
import me.qscbm.plugins.dodochat.common.DataStorage;
import me.qscbm.plugins.dodochat.common.DodoEventListener;
import me.qscbm.plugins.dodochat.common.hook.Hook;
import me.qscbm.plugins.dodochat.common.dodocommands.*;
import me.qscbm.plugins.dodochat.common.hook.platform.Platform;
import net.elytrium.limboauth.LimboAuth;
import org.slf4j.Logger;

@Plugin(
        id = "dodochat",
        name = "DodoChat",
        version = "1.1",
        dependencies = {
                @Dependency(id = "limboauth"),
                @Dependency(id = "luckperms"),
                @Dependency(id = "libertybans")
        },
        authors = {"qscbm187531"}
)
public class DodoChat {
    private ProxyServer server;
    private final Logger logger;
    @Inject
    public DodoChat(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    public Logger getLogger() {
        return logger;
    }
    public ProxyServer getServer() {
        return server;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        INSTANCE = this;
        Platform.isVelocity = true;
        Hook.init();
        logger.info("加载配置文件中");
        Config.init();
        logger.info("注册事件监听器中");
        server.getEventManager().register(this, new MinecraftEventListener());
        EventManage.registerEvents(new DodoEventListener(),Config.authorization); //注册DodoOpenJava事件
        logger.info("注册命令解析器中");
        server.getCommandManager().register("dodochat",new MinecraftCommand(),"dc","dodo");
        Command.registerCommand(Config.authorization,new Help(),new Bind(),new Status(),new Verify(),new BindList(),new Unbind(),
                new ResetPassword(),new Call(),new GetBanHistory(),new MInfo());
        /*
        非IMC.RE服务器使用
        Command.registerCommand(authorization,new Help(),new Bind(),new Status(),new Verify(),new BindList(),new Unbind(),
                new ResetPassword(),new Call(),new GetBanHistory());
        */
        logger.info("连接MySQL数据库中");
        DataStorage.init(Config.getConfiguration().getString("settings.MySQL.url"),Config.getConfiguration().getString("settings.MySQL.name"),Config.getConfiguration().getString("settings.MySQL.password"),Config.getConfiguration().getString("settings.MySQL.database"));
        logger.info("DodoChat已启动");
    }


    public static DodoChat INSTANCE;

    public static DodoChat getINSTANCE() {
        return INSTANCE;
    }
    public void reload() {
        logger.info("重载配置文件中");
        Config.init();
        logger.info("DodoChat已重载");
        DataStorage.init(Config.getConfiguration().getString("settings.MySQL.url"),Config.getConfiguration().getString("settings.MySQL.name"),Config.getConfiguration().getString("settings.MySQL.password"),Config.getConfiguration().getString("settings.MySQL.database"));
        EventManage.registerEvents(new DodoEventListener(),Config.authorization); //注册DodoOpenJava事件
        EventTrigger.main(Config.authorization);
    }

    @Subscribe
    public void onShutdown(ProxyShutdownEvent event) {
        logger.info("DodoChat已关闭");
    }

}
