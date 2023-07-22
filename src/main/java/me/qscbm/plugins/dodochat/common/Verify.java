package me.qscbm.plugins.dodochat.common;

import io.github.minecraftchampions.dodoopenjava.api.v2.PersonalApi;
import io.github.minecraftchampions.dodoopenjava.command.CommandExecutor;
import io.github.minecraftchampions.dodoopenjava.command.CommandSender;
import io.github.minecraftchampions.dodoopenjava.event.events.v2.PersonalMessageEvent;
import me.qscbm.plugins.dodochat.common.Config;
import me.qscbm.plugins.dodochat.common.DataStorage;
import me.qscbm.plugins.dodochat.common.dodocommands.Help;
import me.qscbm.plugins.dodochat.common.hook.LuckPermsHook;
import org.json.JSONArray;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class Verify {
    public static Map<String,Map<String,String>> tempMap = new HashMap<>();
}
