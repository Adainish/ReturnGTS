package io.github.adainish.returngts.listeners;

import io.github.adainish.returngts.ReturnGTS;
import io.github.adainish.returngts.obj.player.GTSPlayer;
import io.github.adainish.returngts.storage.PlayerStorage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerListener
{
    @SubscribeEvent
    public void onPlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.getPlayer() != null) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) event.getPlayer();
            GTSPlayer player = PlayerStorage.getPlayer(serverPlayer.getUniqueID());
            if (player == null) {
                PlayerStorage.makePlayer(serverPlayer.getUniqueID());
                player = PlayerStorage.getPlayer(serverPlayer.getUniqueID());

            }

            if (player != null) {
                player.setUsername(serverPlayer.getName().getString());
                player.updateCache();
                if (ReturnGTS.gts != null)
                {
                    ReturnGTS.gts.updatePlayerOfflineSales(player);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event)
    {
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) event.getPlayer();
        if (serverPlayer != null) {
            GTSPlayer player = PlayerStorage.getPlayer(serverPlayer.getUniqueID());
            if (player != null) {
                player.save();
            }
        }
    }
}
