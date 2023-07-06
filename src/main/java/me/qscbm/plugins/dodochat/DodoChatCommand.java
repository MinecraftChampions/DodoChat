package me.qscbm.plugins.dodochat;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.VelocityBrigadierMessage;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.minecraftchampions.dodoopenjava.api.v2.ChannelMessageApi;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.aopalliance.intercept.Invocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DodoChatCommand {
    public static List<String> subCommands = List.of(
            "help",
            "send",
            "reload"
    );

    public static void sendHelp(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();
        Component component1 = MiniMessage.miniMessage().deserialize("<yellow><----DodoChat帮助----></yellow>");
        Component component2 = MiniMessage.miniMessage().deserialize("<yellow>/dodochat send <text> 发送一条消息到Dodo频道</yellow>");
        Component component3 = MiniMessage.miniMessage().deserialize("<yellow>/dodochat reload      重载配置文件</yellow>");
        Component component4 = MiniMessage.miniMessage().deserialize("<yellow>/dodochat help        打开帮助界面</yellow>");
        source.sendMessage(component1);
        source.sendMessage(component2);
        source.sendMessage(component3);
        source.sendMessage(component4);
    }

    public static BrigadierCommand createBrigadierCommand() {
        LiteralCommandNode<CommandSource> helloNode = LiteralArgumentBuilder
                .<CommandSource>literal("dodochat")
                .requires(source -> source.hasPermission("dodochat.permission"))
                .executes(context -> {
                    sendHelp(context);
                    return Command.SINGLE_SUCCESS;
                })
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("arg1", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            subCommands.forEach(text -> builder.suggest(
                                    text,
                                    VelocityBrigadierMessage.tooltip(
                                            MiniMessage.miniMessage().deserialize("<bold>" + text + "</bold>")
                                    )
                            ));
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            String argumentProvided = context.getArgument("arg1", String.class);
                            switch(argumentProvided) {
                                case "help" -> sendHelp(context);
                                case "reload" -> {
                                    DodoChat.getINSTANCE().reload();
                                    context.getSource().sendMessage(Component.text("已重载"));
                                }
                                default -> {
                                    sendHelp(context);
                                }
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(RequiredArgumentBuilder.<CommandSource,String>argument("arg2",StringArgumentType.greedyString())
                                .executes(commandContext -> {
                                    if (Objects.equals(commandContext.getArgument("arg1", String.class), "send")) {
                                        String message = commandContext.getArgument("arg2",String.class);
                                        try {
                                            ChannelMessageApi.sendTextMessage(DodoChat.authorization,DodoChat.getConfiguration().getString("settings.SendServerMessage.channelId"),message);
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                        commandContext.getSource().sendMessage(Component.text("已发送"));
                                    } else {
                                        sendHelp(commandContext);
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .build();

        return new BrigadierCommand(helloNode);
    }

}
