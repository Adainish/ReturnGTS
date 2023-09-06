package io.github.adainish.returngts.obj;

import io.github.adainish.returngts.obj.player.GTSPlayer;
import io.github.adainish.returngts.storage.PlayerStorage;
import io.github.adainish.returngts.util.EconomyUtil;

import java.util.UUID;

public class OfflineSale
{
    public GTSItem gtsItem;

    public OfflineSale(GTSItem gtsItem)
    {
        this.gtsItem = gtsItem;
    }

    public void updateSeller()
    {
        GTSPlayer seller = PlayerStorage.getPlayer(gtsItem.seller);
        if (seller != null)
        {
            seller.sendMessage("&6Your GTS listing of %item% sold for %amount% while you were offline"
                    .replace("%item%", gtsItem.itemString())
                    .replace("%amount%", String.valueOf(gtsItem.askingPrice)));
            seller.sendMessage("&aWe've updated your balance due to your sale while you were offline!");
        }
        EconomyUtil.giveBalance(gtsItem.seller, gtsItem.askingPrice);
    }
}
