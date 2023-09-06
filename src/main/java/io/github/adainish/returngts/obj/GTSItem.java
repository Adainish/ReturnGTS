package io.github.adainish.returngts.obj;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.api.util.helpers.SpriteItemHelper;
import io.github.adainish.returngts.ReturnGTS;
import io.github.adainish.returngts.enumerations.SaleType;
import io.github.adainish.returngts.obj.player.GTSPlayer;
import io.github.adainish.returngts.storage.PlayerStorage;
import io.github.adainish.returngts.util.Util;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class GTSItem {
    public UUID seller;
    public SaleType saleType;
    public Pokemon pokemon;
    public ItemStack itemStack;
    public int askingPrice;
    public long startingTime;
    public UUID buyer;

    public GTSItem(UUID seller, SaleType saleType, Pokemon pokemon, ItemStack itemStack, int askingPrice) {
        this.seller = seller;
        this.saleType = saleType;
        this.pokemon = pokemon;
        this.itemStack = itemStack;
        this.askingPrice = askingPrice;
        this.startingTime = System.currentTimeMillis();
    }

    public List<String> lore() {
        List<String> lore = new ArrayList<>();
        if (this.pokemon != null) {
            lore.add("&7Expires in: &c%expirytime%".replace("%expirytime%", this.timeLeftString()));
            lore.addAll(Util.pokemonLore(this.pokemon));
        } else if (this.itemStack != null && !this.itemStack.isEmpty()) {
            lore.add("&7Expires in: &c%expirytime%".replace("%expirytime%", this.timeLeftString()));
        }
        return lore;
    }

    public long timeLeft() {
        return this.startingTime + (TimeUnit.MINUTES.toMillis(ReturnGTS.config.maxGTSStayMinutes)) - System.currentTimeMillis();
    }

    public String timeLeftString() {
        long timeLeft = this.timeLeft();
        long days = timeLeft / Util.DAY_IN_MILLIS;
        timeLeft = timeLeft % Util.DAY_IN_MILLIS;
        long hours = timeLeft / Util.HOUR_IN_MILLIS;
        timeLeft = timeLeft % Util.HOUR_IN_MILLIS;
        long minutes = timeLeft / Util.MINUTE_IN_MILLIS;
        timeLeft = timeLeft % Util.MINUTE_IN_MILLIS;
        long seconds = timeLeft / Util.SECOND_IN_MILLIS;
        return days + " " + (days == 1 ? "day" : "days") + ", " + hours + " " + (hours == 1 ? "hour" : "hours") + ", " + minutes + " " + (minutes == 1 ? "minute" : "minutes") + ", " + seconds + " " + (seconds == 1 ? "second" : "seconds");
    }

    public String displayTitle() {
        String s = "&b%entry% &a$%price%".replace("%price%", String.valueOf(this.askingPrice));
        if (this.itemStack == null)
            s = s.replace("%entry%", Util.formattedPokemonNameString(this.pokemon));
        else s = s.replace("%entry%", Util.getItemStackName(this.itemStack.copy()));
        return s;
    }

    public boolean isSellerOffline()
    {
        return ReturnGTS.getServer().getPlayerList().getPlayerByUUID(this.seller) == null;
    }

    public GooeyButton menuButton(GTSPlayer player) {
        return GooeyButton.builder()
                .title(Util.formattedString(displayTitle()))
                .lore(Util.formattedArrayList(lore()))
                .display(getDisplayItem())
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), ReturnGTS.gts.confirmPurchaseMenu(player, this));
                })
                .build();
    }

    public GooeyButton manageButton(GTSPlayer player) {
        List<String> lore = new ArrayList<>(lore());
        lore.add("&cClick to manage this item");
        return GooeyButton.builder()
                .title(Util.formattedString(displayTitle()))
                .lore(Util.formattedArrayList(lore))
                .display(getDisplayItem())
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), ReturnGTS.gts.manageListingPage(player, this));
                })
                .build();
    }

    public ItemStack getDisplayItem() {
        ItemStack stack;
        if (this.pokemon != null)
            stack = SpriteItemHelper.getPhoto(pokemon);
        else stack = this.itemStack.copy();
        return stack;
    }

    public boolean hasSold() {
        return buyer != null;
    }

    public boolean hasExpired() {
        return System.currentTimeMillis() >= (startingTime + TimeUnit.MINUTES.toMillis(ReturnGTS.config.maxGTSStayMinutes));
    }

    public void sendToSellerStorage() {
        if (this.seller != null) {
            GTSPlayer gtsPlayer = PlayerStorage.getPlayer(this.seller);
            if (gtsPlayer != null) {
                if (this.itemStack != null && !this.itemStack.isEmpty())
                    gtsPlayer.retrievalStorage.itemStacks.add(this.itemStack.copy());
                if (gtsPlayer.isOnline())
                    gtsPlayer.sendMessage("&4One of your Item Sales expired and was sent to your storage.");
                if (this.pokemon != null) {
                    gtsPlayer.retrievalStorage.pokemon.add(this.pokemon);
                    if (gtsPlayer.isOnline())
                        gtsPlayer.sendMessage("&4One of your Pokemon Sales expired and was sent to your storage.");
                }
                gtsPlayer.updateCache();
            }
        }
    }

    public String itemString() {
        String itemString = "";
        if (this.pokemon != null)
            itemString = this.pokemon.getSpecies().getName();
        else itemString = Util.getItemStackName(this.itemStack);
        return itemString;
    }
    public void sendToBuyer() {
        GTSPlayer seller = PlayerStorage.getPlayer(this.seller);
        GTSPlayer buyer = PlayerStorage.getPlayer(this.buyer);
        String itemString = itemString();

        if (buyer != null) {
            if (buyer.getOptionalServerPlayer().isPresent()) {
                ServerPlayerEntity serverPlayer = buyer.getOptionalServerPlayer().get();
                if (this.itemStack != null) {
                    if (serverPlayer.inventory.getFirstEmptyStack() == -1) {
                        buyer.retrievalStorage.itemStacks.add(this.itemStack.copy());
                        buyer.sendMessage("&aYour inventory was full, as such your purchased item was sent to your storage.");
                    } else serverPlayer.inventory.addItemStackToInventory(this.itemStack.copy());
                }
                if (this.pokemon != null) {
                    PlayerPartyStorage pps = StorageProxy.getParty(buyer.uuid);
                    if (pps.getFirstEmptyPosition() == null) {
                        buyer.retrievalStorage.pokemon.add(this.pokemon);
                        buyer.sendMessage("&aYour party was full, as such your purchased pokemon was sent to your gts storage.");
                    } else {
                        pps.add(this.pokemon);
                    }
                }
                buyer.sendMessage("&aYou bought the GTS listing of %item% for $%amount%!"
                        .replace("%item%", itemString)
                        .replace("%amount%", String.valueOf(this.askingPrice))
                        .replace("%buyer%", buyer.getUsername())
                );
                buyer.updateCache();
                if (seller != null) {
                    if (seller.isOnline()) {
                        //send sale message
                        seller.sendMessage("&aYour GTS listing of %item% sold for %amount% to %buyer%"
                                .replace("%item%", itemString)
                                .replace("%amount%", String.valueOf(this.askingPrice))
                                .replace("%buyer%", buyer.getUsername())
                        );
                    }
                }
            }
        }

    }
}
