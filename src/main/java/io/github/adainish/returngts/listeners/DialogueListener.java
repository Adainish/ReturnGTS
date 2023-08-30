package io.github.adainish.returngts.listeners;

import com.pixelmonmod.pixelmon.api.events.dialogue.DialogueInputEvent;
import com.pixelmonmod.pixelmon.api.util.Scheduling;
import io.github.adainish.returngts.ReturnGTS;
import io.github.adainish.returngts.builder.SaleBuilderAction;
import io.github.adainish.returngts.obj.player.GTSPlayer;
import io.github.adainish.returngts.storage.PlayerStorage;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DialogueListener
{
    @SubscribeEvent
    public void onDialogueScreenEvent(DialogueInputEvent.Submitted event) {
        GTSPlayer player = PlayerStorage.getPlayer(event.getPlayer().getUniqueID());
        if (player != null)
        {
            if (player.saleBuilder != null)
            {
                switch (player.saleBuilder.saleBuilderAction)
                {
                    case asking_price_error:
                    case asking_price: {
                        int am = 0;
                        try {
                            am = Integer.parseInt(event.getInput());
                        } catch (NumberFormatException e) {
                            if (player.getOptionalServerPlayer().isPresent()) {
                                player.saleBuilder.saleBuilderAction = SaleBuilderAction.asking_price_error;
                                player.saleBuilder.dialogueInputScreenBuilder(SaleBuilderAction.asking_price_error, player).sendTo(player.getOptionalServerPlayer().get());
                            }
                            return;
                        }
                        int finalAm = am;
                        player.saleBuilder.saleBuilderAction = SaleBuilderAction.none;
                        player.saleBuilder.price = finalAm;
                        player.updateCache();

                        Scheduling.schedule(2, (task) -> {
                            player.saleBuilder.open(player);
                        }, false);
                        break;
                    }
                    case none:
                        break;
                }
            }
            if (player.itemUpdater != null)
            {
                switch (player.itemUpdater.saleBuilderAction)
                {
                    case asking_price_error:
                    case asking_price: {
                        int am = 0;
                        try {
                            am = Integer.parseInt(event.getInput());

                            if (am <= 0)
                            {
                                if (player.getOptionalServerPlayer().isPresent()) {
                                    player.itemUpdater.saleBuilderAction = SaleBuilderAction.asking_price_too_low;
                                    player.itemUpdater.dialogueInputScreenBuilder(SaleBuilderAction.asking_price_too_low, player).sendTo(player.getOptionalServerPlayer().get());
                                }
                            }
                        } catch (NumberFormatException e) {
                            if (player.getOptionalServerPlayer().isPresent()) {
                                player.itemUpdater.saleBuilderAction = SaleBuilderAction.asking_price_error;
                                player.itemUpdater.dialogueInputScreenBuilder(SaleBuilderAction.asking_price_error, player).sendTo(player.getOptionalServerPlayer().get());
                            }
                            return;
                        }
                        int finalAm = am;
                        player.itemUpdater.saleBuilderAction = SaleBuilderAction.none;
                        player.itemUpdater.newPrice = finalAm;
                        player.itemUpdater.updatePrice(player);
                        player.itemUpdater = null;
                        player.updateCache();

                        Scheduling.schedule(2, (task) -> {
                            ReturnGTS.gts.openGTSMenu(player);
                        }, false);
                        break;
                    }
                    case none:
                        break;
                }
            }
        }
    }
}
