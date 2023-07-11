package me.qscbm.plugins.dodochat;

import io.github.minecraftchampions.dodoopenjava.api.v2.ChannelApi;
import io.github.minecraftchampions.dodoopenjava.api.v2.MemberApi;
import io.github.minecraftchampions.dodoopenjava.api.v2.RoleApi;
import io.github.minecraftchampions.dodoopenjava.configuration.ConfigurationSection;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static String parsePlaceholders(String initialString,JSONObject jsonObject) {
        replaceServerName(jsonObject);
        String tempString = initialString;
        String replacement;
        if (jsonObject.keySet().contains("server")) {
            replacement = java.util.regex.Matcher.quoteReplacement(jsonObject.getString("server"));
            tempString = tempString.replaceAll("%server%", replacement);
        }
        if (jsonObject.keySet().contains("player")) {
            replacement = java.util.regex.Matcher.quoteReplacement(jsonObject.getString("player"));
            tempString = tempString.replaceAll("%player%", replacement);
        }
        if (jsonObject.keySet().contains("sender")) {
            replacement = java.util.regex.Matcher.quoteReplacement(jsonObject.getString("sender"));
            tempString = tempString.replaceAll("%sender%", replacement);
        }
        if (jsonObject.keySet().contains("message")) {
            replacement = java.util.regex.Matcher.quoteReplacement(jsonObject.getString("message"));
            tempString = tempString.replaceAll("%message%", replacement);
        }
        if (jsonObject.keySet().contains("lastServer")) {
            replacement = java.util.regex.Matcher.quoteReplacement(jsonObject.getString("lastServer"));
            tempString = tempString.replaceAll("%lastServer%", replacement);
        }
        return tempString;
    }

    private static void replaceServerName(JSONObject jsonObject) {
        ConfigurationSection serverConfig = DodoChat.getConfiguration().getConfigurationSection("settings.replaceServerName");
        if (jsonObject.keySet().contains("lastServer")) {
            if (serverConfig.getKeys(true).contains(jsonObject.getString("lastServer"))) {
                String tempString = jsonObject.getString("lastServer");
                jsonObject.put("lastServer",serverConfig.getString(tempString));
            }
        }
        if (jsonObject.keySet().contains("server")) {
            if (serverConfig.getKeys(true).contains(jsonObject.getString("server"))) {
                String tempString = jsonObject.getString("server");
                jsonObject.put("server",serverConfig.getString(tempString));
            }
        }
    }

    public static String replaceString(String s) {
        String tempString = s;
        tempString = s.replaceAll("\\*\\*([^*]+)\\*\\*","<bold>$1</bold>");
        tempString = tempString.replaceAll("\\*([^*]+)\\*","<italic>$1</italic>");
        tempString = tempString.replaceAll("__([^_]+)__","<underlined>$1</underlined>");
        tempString = tempString.replaceAll("\n>","\n");
        tempString = tempString.replaceAll("\\|\\|([^|]+)\\|\\|","<hover:show_text:'$1'>[悬浮至此可见]</hover>");
        tempString = tempString.replaceAll("~~([^~]+)~~","$1");
        tempString = tempString.replaceAll("```([^`]+)```","$1");
        tempString = tempString.replaceAll("<#\\d+>","[跳转频道]");
        tempString = tempString.replaceAll("<@all>","@所有人");
        tempString = tempString.replaceAll("<@online>","@在线成员");
        Pattern pattern;
        if (tempString.contains("<@!")) {
            pattern = Pattern.compile("<@!\\d+>");
            Matcher matcher = pattern.matcher(tempString);
            List<String> list = new ArrayList<>();
            while(matcher.find()) {
                list.add(matcher.group());
            }
            for (String t : list) {
                String id = t.replaceAll("<@!","").replaceAll(">","");
                JSONObject jsonObject;
                try {
                    jsonObject = MemberApi.getMemberInfo(DodoChat.authorization,DodoChat.getConfiguration().getString("settings.islandId"),id);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                tempString = tempString.replaceAll(t,"@" + jsonObject.getJSONObject("data").getString("nickName"));
            }
        }
        if (tempString.contains("<@&")) {
            pattern = Pattern.compile("<@&\\d+>");
            Matcher matcher = pattern.matcher(tempString);
            List<String> list = new ArrayList<>();
            while(matcher.find()) {
                list.add(matcher.group());
            }
            for (String t : list) {
                String id = t.replaceAll("<@&","").replaceAll(">","");
                JSONObject jsonObject;
                try {
                    jsonObject = RoleApi.getRoleList(DodoChat.authorization,DodoChat.getConfiguration().getString("settings.islandId"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                for (Object object : jsonArray) {
                    if (object instanceof JSONObject json) {
                        if (Objects.equals(json.getString("roleId"), id)) {
                            tempString = tempString.replaceAll(t,"@" + json.getString("roleName"));
                        }
                    }
                }
            }
        }
        if (tempString.contains("<#")) {
            pattern = Pattern.compile("<#\\d+>");
            Matcher matcher = pattern.matcher(tempString);
            List<String> list = new ArrayList<>();
            while(matcher.find()) {
                list.add(matcher.group());
            }
            for (String t : list) {
                String id = t.replaceAll("<#","").replaceAll(">","");
                JSONObject jsonObject;
                try {
                    jsonObject = ChannelApi.getChannelInfo(DodoChat.authorization,DodoChat.getConfiguration().getString("settings.islandId"),id);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                tempString = tempString.replaceAll(t,"@" + jsonObject.getJSONObject("data").getString("channelName"));
            }
        }
        return tempString;
    }
}
