package me.qscbm.plugins.dodochat.spigot;

import io.github.minecraftchampions.dodoopenjava.api.v2.PersonalApi;
import me.qscbm.plugins.dodochat.common.Config;
import me.qscbm.plugins.dodochat.common.DodoCommandSourceException;
import me.qscbm.plugins.dodochat.common.hook.LuckPermsHook;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.permissions.PermissionRemovedExecutor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class DodoCommandSource implements CommandSender {
    public static class spigot extends Spigot {
        public spigot() {
        }

        @Override
        public void sendMessage(@NotNull BaseComponent component) {}

        @Override
        public void sendMessage(@NotNull BaseComponent... components) {}

        @Override
        public void sendMessage(@Nullable UUID sender, @NotNull BaseComponent component) {}

        @Override
        public void sendMessage(@Nullable UUID sender, @NotNull BaseComponent... components) {}
    }
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

    public spigot spigot = new spigot();

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
    public void sendMessage(@NotNull String s) {
        try {
            PersonalApi.sendPersonalMessage(Config.authorization,islandId,dodoId, s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendMessage(@NotNull String... strings) {
        Arrays.stream(strings).toList().forEach(this::sendMessage);
    }

    @Override
    public void sendMessage(@Nullable UUID uuid, @NotNull String s) {
        sendMessage(s);
    }

    @Override
    public void sendMessage(@Nullable UUID uuid, @NotNull String... strings) {
        Arrays.stream(strings).toList().forEach(this::sendMessage);
    }

    @NotNull
    @Override
    public Server getServer() {
        return DodoChat.getInstance().getServer();
    }

    @NotNull
    @Override
    public String getName() {
        return username;
    }

    @NotNull
    @Override
    public Spigot spigot() {
        return spigot;
    }

    @Override
    public boolean isPermissionSet(@NotNull String s) {
        try {
            return LuckPermsHook.luckPerms.getUserManager().loadUser(uuid)
                    .thenApplyAsync(user -> user.getCachedData().getPermissionData().checkPermission(s).asBoolean()).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isPermissionSet(@NotNull Permission permission) {
        try {
            return LuckPermsHook.luckPerms.getUserManager().loadUser(uuid)
                    .thenApplyAsync(user -> user.getCachedData().getPermissionData().checkPermission(permission.getName()).asBoolean()).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasPermission(@NotNull String s) { try {
        return LuckPermsHook.luckPerms.getUserManager().loadUser(uuid)
                .thenApplyAsync(user -> user.getCachedData().getPermissionData().checkPermission(s).asBoolean()).get();
    } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
    }
    }

    @Override
    public boolean hasPermission(@NotNull Permission permission) {
        try {
            return LuckPermsHook.luckPerms.getUserManager().loadUser(uuid)
                    .thenApplyAsync(user -> user.getCachedData().getPermissionData().checkPermission(permission.getName()).asBoolean()).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    @Override
    public PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value) {
        if (!plugin.isEnabled()) {
            throw new IllegalArgumentException("Plugin " + plugin.getDescription().getFullName() + " is disabled");
        } else {
            PermissionAttachment result = this.addAttachment(plugin);
            result.setPermission(name, value);
            this.recalculatePermissions();
            return result;
        }
    }

    private final List<PermissionAttachment> attachments = new LinkedList<>();


    @NotNull
    public PermissionAttachment addAttachment(@NotNull Plugin plugin) {
        if (!plugin.isEnabled()) {
            throw new IllegalArgumentException("Plugin " + plugin.getDescription().getFullName() + " is disabled");
        } else {
            PermissionAttachment result = new PermissionAttachment(plugin, this);
            this.attachments.add(result);
            this.recalculatePermissions();
            return result;
        }
    }

    @Nullable
    @Override
    public PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value, int ticks) {
        if (!plugin.isEnabled()) {
            throw new IllegalArgumentException("Plugin " + plugin.getDescription().getFullName() + " is disabled");
        } else {
            PermissionAttachment result = this.addAttachment(plugin, ticks);
            if (result != null) {
                result.setPermission(name, value);
            }

            return result;
        }
    }

    @Nullable
    @Override
    public PermissionAttachment addAttachment(@NotNull Plugin plugin, int i) {
        return null;
    }

    public void removeAttachment(@NotNull PermissionAttachment attachment) {
        if (this.attachments.remove(attachment)) {
            PermissionRemovedExecutor ex = attachment.getRemovalCallback();
            if (ex != null) {
                ex.attachmentRemoved(attachment);
            }

            this.recalculatePermissions();
        } else {
            throw new IllegalArgumentException("Given attachment is not part of Permissible object " + this);
        }
    }

    @Override
    public void recalculatePermissions() {

    }

    @NotNull
    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return new HashSet<>();
    }

    @Override
    public boolean isOp() {
        return Bukkit.getOfflinePlayer(uuid).isOp();
    }

    @Override
    public void setOp(boolean b) {
        Bukkit.getOfflinePlayer(uuid).setOp(b);
    }
}
