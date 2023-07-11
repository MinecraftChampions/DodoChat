package me.qscbm.plugins.dodochat;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import io.github.minecraftchampions.dodoopenjava.command.Command;
import io.github.minecraftchampions.dodoopenjava.configuration.file.FileConfiguration;
import io.github.minecraftchampions.dodoopenjava.configuration.file.YamlConfiguration;
import io.github.minecraftchampions.dodoopenjava.configuration.util.ConfigUtil;
import io.github.minecraftchampions.dodoopenjava.event.EventManage;
import io.github.minecraftchampions.dodoopenjava.utils.BaseUtil;
import me.qscbm.plugins.dodochat.dodocommands.*;
import net.elytrium.limboauth.LimboAuth;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Plugin(
        id = "dodochat",
        name = "DodoChat",
        version = "1.0-SNAPSHOT",
        dependencies = {
                @Dependency(id = "limboauth"),
                @Dependency(id = "luckperms"),
                @Dependency(id = "libertybans")
        },
        authors = {"qscbm187531"}
)
public class DodoChat {
    public static Connection conn = null;
    private ProxyServer server;
    private final Logger logger;
    private final Path configFolder =  Paths.get("plugins/DodoChat/");

    private static FileConfiguration configuration;

    public static boolean enableDodoMessage,enableServerMessage,enableJoinMessage,enableLeaveMessage,enableChanceMessage;

    public static String authorization;

    public static Statement stmt = null;

    public static Connection banConn = null;

    public static Statement banStmt = null;
    public static LimboAuth limboAuth = null;

    public static final ChannelIdentifier MODERN_CHANNEL
            = MinecraftChannelIdentifier.create("dodochat", "main");
    public static final ChannelIdentifier LEGACY_CHANNEL
            = new LegacyChannelIdentifier("dodochat:main");

    public static LuckPerms luckPerms;
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
        logger.info("加载配置文件中");
        if (!configFolder.toFile().exists()) {
            configFolder.toFile().mkdirs();
        }
        File configFile = configFolder.resolve("config.yml").toFile();
        if (!configFile.exists()) {
            try {
                ConfigUtil.copyResourcesToFile("config.yml", configFile.getPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            logger.error("config未更改，请立刻更改");
        }
        configuration = YamlConfiguration.loadConfiguration(configFile);
        authorization = BaseUtil.Authorization(getConfiguration().getString("settings.botClientId"), getConfiguration().getString("settings.botToken"));//拼接
        enableDodoMessage = getConfiguration().getBoolean("settings.SendDodoMessage.Enable");//获取配置项
        enableServerMessage = getConfiguration().getBoolean("settings.SendServerMessage.Enable");
        enableJoinMessage = getConfiguration().getBoolean("settings.JoinMessage.Enable");
        enableLeaveMessage = getConfiguration().getBoolean("settings.LeaveMessage.Enable");
        logger.info("注册事件监听器中");
        server.getChannelRegistrar().register(MODERN_CHANNEL, LEGACY_CHANNEL);
        server.getEventManager().register(this, new MinecraftEventListener());
        EventManage.registerEvents(new DodoEventListener(),authorization); //注册DodoOpenJava事件
        logger.info("注册命令解析器中");
        server.getCommandManager().register("dodochat",new MinecraftCommand(),"dc","dodo");
        Command.registerCommand(authorization,new Help(),new Bind(),new Status(),new Verify(),new BindList(),new Unbind(),
                new ResetPassword(),new Call(),new GetBanHistory());
        logger.info("连接MySQL数据库中");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(getConfiguration().getString("settings.MySQL.url"),getConfiguration().getString("settings.MySQL.name"),getConfiguration().getString("settings.MySQL.password"));
            stmt = conn.createStatement();
            stmt.execute("create database if not exists dodochat");
            conn =  DriverManager.getConnection(getConfiguration().getString("settings.MySQL.url") + "/dodochat",getConfiguration().getString("settings.MySQL.name"),getConfiguration().getString("settings.MySQL.password"));
            banConn = DriverManager.getConnection("jdbc:mysql://" + getConfiguration().getString("settings.LibertyBansData.host") + ":" + getConfiguration().getString("settings.LibertyBansData.port") + "/" + getConfiguration().getString("settings.LibertyBansData.database"),getConfiguration().getString("settings.LibertyBansData.user"),getConfiguration().getString("settings.LibertyBansData.password"));
            String sql =
                    """
                            create table if not exists users (
                            id int primary key,
                            data text not null
                            )""";
            stmt = conn.createStatement();
            stmt.execute(sql);
            banStmt = banConn.createStatement();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("找不到MySQL驱动!");
            e.printStackTrace();
        }
        luckPerms = LuckPermsProvider.get();
        INSTANCE = this;
        limboAuth = (LimboAuth) DodoChat.getINSTANCE().getServer().getPluginManager().getPlugin("limboauth").get().getInstance().get();
        logger.info("DodoChat已启动");
    }

    public static FileConfiguration getConfiguration() {
        return configuration;
    }

    public static DodoChat INSTANCE;

    public static DodoChat getINSTANCE() {
        return INSTANCE;
    }
    public void reload() {
        if (!configFolder.toFile().exists()) {
            configFolder.toFile().mkdirs();
        }
        logger.info("重载配置文件中");
        File configFile = configFolder.resolve("config.yml").toFile();
        if (!configFile.exists()) {
            //复制config
            try {
                ConfigUtil.copyResourcesToFile("config.yml", configFile.getPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            logger.error("config未更改，请立刻更改");
        }
        configuration = YamlConfiguration.loadConfiguration(configFile);//获取config
        authorization = BaseUtil.Authorization(getConfiguration().getString("settings.botClientId"), getConfiguration().getString("settings.botToken"));//拼接
        enableDodoMessage = getConfiguration().getBoolean("settings.SendDodoMessage.Enable");//获取配置项
        enableServerMessage = getConfiguration().getBoolean("settings.SendServerMessage.Enable");
        enableChanceMessage = getConfiguration().getBoolean("settings.ChanceServer.Enable");
        enableJoinMessage = getConfiguration().getBoolean("settings.JoinMessage.Enable");
        enableLeaveMessage = getConfiguration().getBoolean("settings.LeaveMessage.Enable");
        logger.info("DodoChat已重载");
    }

    @Subscribe
    public void onShutdown(ProxyShutdownEvent event) {
        logger.info("DodoChat已关闭");
        try {
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
