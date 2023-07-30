package me.qscbm.plugins.dodochat.velocity;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.crypto.IdentifiedKey;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.player.PlayerSettings;
import com.velocitypowered.api.proxy.player.ResourcePackInfo;
import com.velocitypowered.api.proxy.player.TabList;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.api.util.ModInfo;
import io.github.minecraftchampions.dodoopenjava.api.v2.MemberApi;
import io.github.minecraftchampions.dodoopenjava.api.v2.PersonalApi;
import me.qscbm.plugins.dodochat.common.Config;
import me.qscbm.plugins.dodochat.common.DodoCommandSourceException;
import me.qscbm.plugins.dodochat.common.hook.LuckPermsHook;
import me.qscbm.plugins.dodochat.common.hook.platform.Velocity;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.UnaryOperator;

public class DodoCommandSource implements Player {
    private UUID uuid;

    private String username;

    private String channelId;

    private String islandId;

    private String dodoId;

    public DodoCommandSource(String uuid,String channelId,String islandId,String dodoId) {
        this(UUID.fromString(uuid),channelId,islandId,dodoId);
    }

    public DodoCommandSource(UUID uuid,String channelId,String islandId,String dodoId) {
        this.uuid = uuid;
        try {
            this.username = LuckPermsHook.luckPerms.getUserManager().lookupUsername(uuid).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new DodoCommandSourceException("DodoChat引起报错,初始化命令源时无法查找Username,请检查玩家是否有进入过游戏.本报错理论上不会触发,因为如果要触发,绑定用户时就不会成功,也有可能是其他原因造成,请注意日志");
        }
        this.channelId = channelId;
        this.dodoId = dodoId;
        this.islandId = islandId;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getIslandId() {
        return islandId;
    }

    public void setIslandId(String islandId) {
        this.islandId = islandId;
    }

    public String getDodoId() {
        return dodoId;
    }

    public void setDodoId(String dodoId) {
        this.dodoId = dodoId;
    }

    @Override
    public boolean hasPermission(String permission) {
        try {
            return LuckPermsHook.luckPerms.getUserManager().loadUser(uuid)
                    .thenApplyAsync(user -> user.getCachedData().getPermissionData().checkPermission(permission).asBoolean()).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public @Nullable Locale getEffectiveLocale() {
        if (Velocity.hasPlayer(username)) {
            return DodoChat.getINSTANCE().getServer().getPlayer(username).get().getEffectiveLocale();
        }
        return Locale.CHINA;
    }

    @Override
    public void setEffectiveLocale(Locale locale) {
        if (Velocity.hasPlayer(username)) {
            DodoChat.getINSTANCE().getServer().getPlayer(username).get().setEffectiveLocale(locale);
        }
    }

    @Override
    public UUID getUniqueId() {
        return this.uuid;
    }

    @Override
    public Optional<ServerConnection> getCurrentServer() {
        if (Velocity.hasPlayer(username)) {
            return DodoChat.getINSTANCE().getServer().getPlayer(username).get().getCurrentServer();
        }
        DodoChat.getINSTANCE().getLogger().warn("检测到命令映射的命令调用了Player#getCurrentServer(获取在线服务器)方法,可能导致命令无法使用乃至报错");
        return Optional.empty();
    }

    @Override
    public PlayerSettings getPlayerSettings() {
        if (Velocity.hasPlayer(username)) {
            return DodoChat.getINSTANCE().getServer().getPlayer(username).get().getPlayerSettings();
        }
        DodoChat.getINSTANCE().getLogger().warn("检测到命令映射的命令调用了Player#hasSentPlayerSettings(获取玩家设置)方法,可能导致命令无法使用乃至报错");
        return null;
    }

    @Override
    public boolean hasSentPlayerSettings() {
        if (Velocity.hasPlayer(username)) {
            return DodoChat.getINSTANCE().getServer().getPlayer(username).get().hasSentPlayerSettings();
        }
        DodoChat.getINSTANCE().getLogger().warn("检测到命令映射的命令调用了Player#hasSentPlayerSettings(玩家是否已发送设置)方法,可能导致命令无法使用乃至报错");
        return false;
    }

    @Override
    public Optional<ModInfo> getModInfo() {
        if (Velocity.hasPlayer(username)) {
            return DodoChat.getINSTANCE().getServer().getPlayer(username).get().getModInfo();
        }
        return Optional.empty();
    }

    @Override
    public long getPing() {
        if (Velocity.hasPlayer(username)) {
            return DodoChat.getINSTANCE().getServer().getPlayer(username).get().getPing();
        }
        DodoChat.getINSTANCE().getLogger().warn("检测到命令映射的命令调用了Player#getPing(获取玩家ping值)方法,可能导致命令无法使用乃至报错");
        return 1;
    }

    @Override
    public boolean isOnlineMode() {
        if (Velocity.hasPlayer(username)) {
            return DodoChat.getINSTANCE().getServer().getPlayer(username).get().isOnlineMode();
        }
        try {
            Set<String> roleIds = new HashSet<>();
            JSONArray jsonArray =  MemberApi.getMemberRoleList(Config.authorization,islandId,dodoId).getJSONArray("data");
            jsonArray.forEach(object -> {
                if (object instanceof JSONObject jsonObject) {
                    roleIds.add(jsonObject.getString("roleId"));
                }
            });
            return roleIds.contains(Config.getConfiguration().getString("settings.CommandMapping.Settings.onlineModeRoleId"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ConnectionRequestBuilder createConnectionRequest(RegisteredServer registeredServer) {
        if (Velocity.hasPlayer(username)) {
            return DodoChat.getINSTANCE().getServer().getPlayer(username).get().createConnectionRequest(registeredServer);
        }
        DodoChat.getINSTANCE().getLogger().warn("检测到命令映射的命令调用了Player#createConnectionRequest(创建新链接)方法,可能导致命令无法使用乃至报错");
        return null;
    }

    @Override
    public List<GameProfile.Property> getGameProfileProperties() {
        if (Velocity.hasPlayer(username)) {
            return DodoChat.getINSTANCE().getServer().getPlayer(username).get().getGameProfileProperties();
        }
        DodoChat.getINSTANCE().getLogger().warn("检测到命令映射的命令调用了Player#getGameProfileProperties(获取玩家配置文件)方法,可能导致命令无法使用乃至报错");
        return new ArrayList<>();
    }

    @Override
    public void setGameProfileProperties(List<GameProfile.Property> list) {
        if (Velocity.hasPlayer(username)) {
            DodoChat.getINSTANCE().getServer().getPlayer(username).get().setGameProfileProperties(list);
        }
    }

    @Override
    public GameProfile getGameProfile() {
        if (Velocity.hasPlayer(username)) {
            return DodoChat.getINSTANCE().getServer().getPlayer(username).get().getGameProfile();
        }
        return new GameProfile(this.uuid,this.username,new ArrayList<>());
    }

    @Override
    public void clearHeaderAndFooter() {}

    @Override
    public Component getPlayerListHeader() {
        if (Velocity.hasPlayer(username)) {
            return DodoChat.getINSTANCE().getServer().getPlayer(username).get().getPlayerListHeader();
        }
        DodoChat.getINSTANCE().getLogger().warn("检测到命令映射的命令调用了Player#getPlayerListHeader(获取玩家Tab列表页头)方法,可能导致命令无法使用乃至报错");
        if (Velocity.getPlayerList().size() != 0) {
            return DodoChat.getINSTANCE().getServer().getPlayer(Velocity.getPlayerList().stream().toList().get(0)).get().getPlayerListHeader();
        }
        return Component.text("");
    }

    @Override
    public Component getPlayerListFooter() {
        if (Velocity.hasPlayer(username)) {
            return DodoChat.getINSTANCE().getServer().getPlayer(username).get().getPlayerListFooter();
        }
        DodoChat.getINSTANCE().getLogger().warn("检测到命令映射的命令调用了Player#getPlayerListFooter(获取玩家Tab列表页脚)方法,可能导致命令无法使用乃至报错");
        if (Velocity.getPlayerList().size() != 0) {
            return DodoChat.getINSTANCE().getServer().getPlayer(Velocity.getPlayerList().stream().toList().get(0)).get().getPlayerListFooter();
        }
        return Component.text("");
    }

    @Override
    public TabList getTabList() {
        if (Velocity.hasPlayer(username)) {
            return DodoChat.getINSTANCE().getServer().getPlayer(username).get().getTabList();
        }
        DodoChat.getINSTANCE().getLogger().warn("检测到命令映射的命令调用了Player#getTabList(获取玩家Tab列表)方法,可能导致命令无法使用乃至报错");
        if (Velocity.getPlayerList().size() != 0) {
            return DodoChat.getINSTANCE().getServer().getPlayer(Velocity.getPlayerList().stream().toList().get(0)).get().getTabList();
        }
        return null;
    }

    @Override
    public void disconnect(Component component) {
        if (Velocity.hasPlayer(username)) {
            DodoChat.getINSTANCE().getServer().getPlayer(username).get().disconnect(component);
        }
    }

    @Override
    public void spoofChatInput(String s) {
        if (Velocity.hasPlayer(username)) {
            DodoChat.getINSTANCE().getServer().getPlayer(username).get().spoofChatInput(s);
        }
    }

    @Override
    @Deprecated
    public void sendResourcePack(String s) {
        if (Velocity.hasPlayer(username)) {
            DodoChat.getINSTANCE().getServer().getPlayer(username).get().sendResourcePack(s);
        }
    }

    @Override
    @Deprecated
    public void sendResourcePack(String s, byte[] bytes) {
        if (Velocity.hasPlayer(username)) {
            DodoChat.getINSTANCE().getServer().getPlayer(username).get().sendResourcePack(s,bytes);
        }
    }

    @Override
    public void sendResourcePackOffer(ResourcePackInfo resourcePackInfo) {
        if (Velocity.hasPlayer(username)) {
            DodoChat.getINSTANCE().getServer().getPlayer(username).get().sendResourcePackOffer(resourcePackInfo);
        }
    }

    @Override
    public @Nullable ResourcePackInfo getAppliedResourcePack() {
        if (Velocity.hasPlayer(username)) {
            return DodoChat.getINSTANCE().getServer().getPlayer(username).get().getAppliedResourcePack();
        }
        DodoChat.getINSTANCE().getLogger().warn("检测到命令映射的命令调用了Player#getAppliedResourcePack(获取玩家资源包信息)方法,可能导致命令无法使用乃至报错");
        return null;
    }

    @Override
    public @Nullable ResourcePackInfo getPendingResourcePack() {
        if (Velocity.hasPlayer(username)) {
            return DodoChat.getINSTANCE().getServer().getPlayer(username).get().getPendingResourcePack();
        }
        DodoChat.getINSTANCE().getLogger().warn("检测到命令映射的命令调用了Player#getPendingResourcePack(获取玩家正在下载的资源包信息)方法,可能导致命令无法使用乃至报错");
        return null;
    }

    @Override
    public boolean sendPluginMessage(ChannelIdentifier channelIdentifier, byte[] bytes) {
        if (Velocity.hasPlayer(username)) {
            return DodoChat.getINSTANCE().getServer().getPlayer(username).get().sendPluginMessage(channelIdentifier,bytes);
        }
        DodoChat.getINSTANCE().getLogger().warn("检测到命令映射的命令调用了Player#sendPluginMessage(发送插件消息)方法,可能导致命令无法使用乃至报错");
        return false;
    }

    @Override
    public @Nullable String getClientBrand() {
        return "Dodo";
    }

    @Override
    public Tristate getPermissionValue(String s) {
        try {
            return Tristate.fromNullableBoolean(LuckPermsHook.luckPerms.getUserManager().loadUser(uuid)
                    .thenApplyAsync(user -> user.getCachedData().getPermissionData().checkPermission(s)).get().asBoolean());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        if (Velocity.hasPlayer(username)) {
            return DodoChat.getINSTANCE().getServer().getPlayer(username).get().getRemoteAddress();
        }
        return new InetSocketAddress(25575);
    }

    @Override
    public Optional<InetSocketAddress> getVirtualHost() {
        DodoChat.getINSTANCE().getLogger().warn("检测到命令映射的命令调用了InboundConnection#getVirtualHost(获取用户输入到客户端中的主机名)方法,可能导致命令无法使用乃至报错");
        if (Velocity.hasPlayer(username)) {
            return DodoChat.getINSTANCE().getServer().getPlayer(username).get().getVirtualHost();
        }
        return Optional.empty();
    }

    @Override
    public boolean isActive() {
        DodoChat.getINSTANCE().getLogger().warn("检测到命令映射的命令调用了InboundConnection#isActive(在线状态)方法,可能导致命令无法使用乃至报错");
        return true;
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        DodoChat.getINSTANCE().getLogger().warn("检测到命令映射的命令调用了InboundConnection#getProtocolVersion(获取协议版本)方法,可能导致命令无法使用乃至报错");
        if (Velocity.hasPlayer(username)) {
            return DodoChat.getINSTANCE().getServer().getPlayer(username).get().getProtocolVersion();
        }
        return ProtocolVersion.MINECRAFT_1_13;
    }

    @Override
    public @Nullable IdentifiedKey getIdentifiedKey() {
        DodoChat.getINSTANCE().getLogger().warn("检测到命令映射的命令调用了KeyIdentifiable#getProtocolVersion(获取公钥)方法,可能导致命令无法使用乃至报错");
        if (Velocity.hasPlayer(username)) {
            return DodoChat.getINSTANCE().getServer().getPlayer(username).get().getIdentifiedKey();
        }
        return null;
    }

    @Override
    public @NotNull Identity identity() {
        DodoChat.getINSTANCE().getLogger().warn("检测到命令映射的命令调用了Identified#identity(获取社交功能相关信息)方法,可能导致命令无法使用乃至报错");
        if (Velocity.hasPlayer(username)) {
            return DodoChat.getINSTANCE().getServer().getPlayer(username).get().identity();
        }
        return Identity.nil();
    }

    @Override
    public void sendMessage(@NotNull ComponentLike message) {
        this.sendMessage(message.asComponent());
    }

    @Override
    public void sendMessage(@NotNull Component message) {
        try {
            PersonalApi.sendPersonalMessage(Config.authorization,islandId,dodoId, PlainTextComponentSerializer.plainText().serialize(message));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Deprecated
    public void sendMessage(@NotNull Identified source, @NotNull ComponentLike message) {
        this.sendMessage(message.asComponent());
    }

    @Override
    @Deprecated
    public void sendMessage(@NotNull Identity source, @NotNull ComponentLike message) {
        this.sendMessage(message.asComponent());
    }

    @Override
    @Deprecated
    public void sendMessage(@NotNull Identified source, @NotNull Component message) {
        this.sendMessage(message);
    }

    @Override
    @Deprecated
    public void sendMessage(@NotNull Identity source, @NotNull Component message) {
        this.sendMessage(message);
    }

    @Override
    @Deprecated
    public void sendMessage(@NotNull Component message, ChatType.@NotNull Bound boundChatType) {
        this.sendMessage(message);
    }

    @Override
    public void sendMessage(@NotNull ComponentLike message, ChatType.@NotNull Bound boundChatType) {
        this.sendMessage(message.asComponent());
    }

    @Override
    public void sendMessage(@NotNull SignedMessage signedMessage, ChatType.@NotNull Bound boundChatType) {
        if (signedMessage.unsignedContent() != null) {
            this.sendMessage(signedMessage.unsignedContent());
        }
    }

    @Override
    public void deleteMessage(@NotNull SignedMessage signedMessage) {}

    @Override
    public void deleteMessage(SignedMessage.@NotNull Signature signature) {}

    @Override
    public void sendActionBar(@NotNull ComponentLike message) {
        this.sendActionBar(message.asComponent());
    }

    @Override
    public void sendActionBar(@NotNull Component message) {
        this.sendMessage(Component.text("插件发送ActionBar:").append(message));
    }

    @Override
    public void sendPlayerListHeader(@NotNull ComponentLike header) {}

    @Override
    public void sendPlayerListHeader(@NotNull Component header) {}

    @Override
    public void sendPlayerListFooter(@NotNull ComponentLike footer) {}

    @Override
    public @NotNull HoverEvent<HoverEvent.ShowEntity> asHoverEvent(@NotNull UnaryOperator<HoverEvent.ShowEntity> op) {
        return HoverEvent.showEntity(key().key(),this.uuid);
    }

    @Override
    public @NotNull HoverEvent<HoverEvent.ShowEntity> asHoverEvent() {
        return HoverEvent.showEntity(key().key(),this.uuid);
    }

    @Override
    public void sendPlayerListFooter(@NotNull Component footer) {}

    @Override
    public void sendPlayerListHeaderAndFooter(@NotNull ComponentLike header, @NotNull ComponentLike footer) {}

    @Override
    public void sendPlayerListHeaderAndFooter(@NotNull Component header, @NotNull Component footer) {}

    @Override
    public void showTitle(@NotNull Title title) {
        this.sendMessage(Component.text("插件发送Title:").append(title.title()));
        this.sendMessage(Component.text("插件发送SubTitle:").append(title.subtitle()));
    }

    @Override
    public <T> void sendTitlePart(@NotNull TitlePart<T> part, @NotNull T value) {}

    @Override
    public void clearTitle() {}

    @Override
    public void resetTitle() {}

    @Override
    public void showBossBar(@NotNull BossBar bar) {
        this.sendMessage(Component.text("插件发送BoosBar:").append(bar.name()));
    }

    @Override
    public void hideBossBar(@NotNull BossBar bar) {}

    @Override
    public void playSound(@NotNull Sound sound) {}

    @Override
    public void playSound(@NotNull Sound sound, double x, double y, double z) {}

    @Override
    public void playSound(@NotNull Sound sound, Sound.@NotNull Emitter emitter) {}

    @Override
    public void stopSound(@NotNull Sound sound) {}

    @Override
    public void stopSound(@NotNull SoundStop stop) { }

    @Override
    public void openBook(Book.@NotNull Builder book) {}

    @Override
    public void openBook(@NotNull Book book) {}
}
