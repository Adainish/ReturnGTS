package io.github.adainish.returngts.obj.player;

import io.github.adainish.returngts.ReturnGTS;
import io.github.adainish.returngts.builder.ItemUpdater;
import io.github.adainish.returngts.builder.SaleBuilder;
import io.github.adainish.returngts.obj.GTSItem;
import io.github.adainish.returngts.storage.PlayerStorage;
import io.github.adainish.returngts.util.EconomyUtil;
import io.github.adainish.returngts.util.PermissionUtil;
import io.github.adainish.returngts.util.Util;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class GTSPlayer
{
    public UUID uuid;
    public String userName;

    public SaleHistory saleHistory;

    public RetrievalStorage retrievalStorage;
    public transient SaleBuilder saleBuilder;
    public transient ItemUpdater itemUpdater;

    public GTSPlayer()
    {
        this.saleHistory = new SaleHistory();
        this.retrievalStorage = new RetrievalStorage();
    }

    public GTSPlayer(UUID uuid)
    {
        this.uuid = uuid;
        this.saleHistory = new SaleHistory();
        this.retrievalStorage = new RetrievalStorage();
    }

    public GTSPlayer(UUID uuid, String userName)
    {
        this.uuid = uuid;
        this.userName = userName;
        this.saleHistory = new SaleHistory();
        this.retrievalStorage = new RetrievalStorage();
    }
    public void sendMessage(String msg)
    {
        if (msg == null)
            return;
        if (msg.isEmpty())
            return;
        Util.send(uuid, msg);
    }

    public boolean isOnline()
    {
        return getOptionalServerPlayer().isPresent();
    }

    public String getUsername()
    {
        if (this.userName != null)
            return userName;
        return "";
    }

    public void setUsername(String name)
    {
        this.userName = name;
    }

    public void save()
    {
        PlayerStorage.savePlayer(this);
    }

    public void updateCache()
    {
        ReturnGTS.playerCache.playerCache.put(uuid, this);
    }
    @Nullable
    public ServerPlayerEntity serverPlayer()
    {
        return Util.getPlayer(this.uuid);
    }

    public Optional<ServerPlayerEntity> getOptionalServerPlayer()
    {
        return Optional.ofNullable(serverPlayer());
    }

    public int getBalance()
    {
        return EconomyUtil.getPlayerBalance(this.uuid);
    }

    public boolean canAfford(GTSItem item)
    {
        return getBalance() >= item.askingPrice;
    }


    public boolean hasPermission(String permission)
    {
        return PermissionUtil.checkPerm(serverPlayer(), permission);
    }
}
