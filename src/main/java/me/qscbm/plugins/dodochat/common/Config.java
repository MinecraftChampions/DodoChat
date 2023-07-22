package me.qscbm.plugins.dodochat.common;

import io.github.minecraftchampions.dodoopenjava.configuration.file.FileConfiguration;
import io.github.minecraftchampions.dodoopenjava.configuration.file.YamlConfiguration;
import io.github.minecraftchampions.dodoopenjava.configuration.util.ConfigUtil;
import io.github.minecraftchampions.dodoopenjava.utils.BaseUtil;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {
    private static FileConfiguration configuration;
    private static final Path configFolder =  Paths.get("plugins/DodoChat/");

    public static String authorization;
    public static void init() {
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
            System.out.println("[DodoChat]检测到未更改配置文件，请及时更改配置文件");
        }
        File dataFile = configFolder.resolve("database.json").toFile();
        if (!configFile.exists()) {
            try {
                ConfigUtil.copyResourcesToFile("database.json", dataFile.getPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
       Config.dataFile = dataFile;
        configuration = YamlConfiguration.loadConfiguration(configFile);
        authorization = BaseUtil.Authorization(getConfiguration().getString("settings.botClientId"), getConfiguration().getString("settings.botToken"));//拼接
        enableDodoMessage = getConfiguration().getBoolean("settings.SendDodoMessage.Enable");//获取配置项
        enableServerMessage = getConfiguration().getBoolean("settings.SendServerMessage.Enable");
        enableJoinMessage = getConfiguration().getBoolean("settings.JoinMessage.Enable");
        enableLeaveMessage = getConfiguration().getBoolean("settings.LeaveMessage.Enable");
    }

    public static boolean enableDodoMessage,enableServerMessage,enableJoinMessage,enableLeaveMessage;

    public static FileConfiguration getConfiguration() {
        return configuration;
    }

    public static File dataFile = null;
    public static JSONObject getData() {
        return new JSONObject(ConfigUtil.readFile(dataFile));
    }
}
