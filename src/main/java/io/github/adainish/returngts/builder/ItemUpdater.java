package io.github.adainish.returngts.builder;

import com.pixelmonmod.pixelmon.api.dialogue.DialogueInputScreen;
import io.github.adainish.returngts.ReturnGTS;
import io.github.adainish.returngts.obj.GTSItem;
import io.github.adainish.returngts.obj.player.GTSPlayer;
import io.github.adainish.returngts.util.Util;

public class ItemUpdater
{
    public GTSItem gtsItem;
    public SaleBuilderAction saleBuilderAction = SaleBuilderAction.none;
    public int newPrice = 100;

    public ItemUpdater(GTSItem gtsItem)
    {
        this.gtsItem = gtsItem;
    }

    public void updatePrice(GTSPlayer player)
    {
        ReturnGTS.gts.gtsItems.remove(this.gtsItem);
        this.gtsItem.askingPrice = newPrice;
        ReturnGTS.gts.gtsItems.add(this.gtsItem);
        player.sendMessage("&aThe price for your item was set to %newprice%".replace("%newprice%", String.valueOf(this.newPrice)));
    }

    public DialogueInputScreen.Builder dialogueInputScreenBuilder(SaleBuilderAction action, GTSPlayer player) {

        DialogueInputScreen.Builder builder = new DialogueInputScreen.Builder();
        builder.setShouldCloseOnEsc(false);
        switch (action) {
            case asking_price: {
                builder.setTitle(Util.formattedString("&bAsking Price"));
                builder.setText(Util.formattedString("&7How much money do you want for this listing?"));
                break;
            }

            case asking_price_error: {
                builder.setTitle(Util.formattedString("&cInvalid number"));
                builder.setText(Util.formattedString("&4Please provide a valid number!"));
                break;
            }
            case asking_price_too_low: {
                builder.setTitle(Util.formattedString("&cToo low"));
                builder.setText(Util.formattedString("&4Please provide a number higher than 0!"));
            }
            case none: {
                break;
            }
        }
        saleBuilderAction = (action);
        this.updateCache(player);
        return builder;
    }

    public void updateCache(GTSPlayer player)
    {
        player.itemUpdater = this;
        player.updateCache();
    }

}
