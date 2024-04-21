package io.github.adainish.returngts.obj;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.api.storage.PartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.api.util.Scheduling;
import com.pixelmonmod.pixelmon.api.util.helpers.SpriteItemHelper;
import io.github.adainish.returngts.ReturnGTS;
import io.github.adainish.returngts.builder.ItemUpdater;
import io.github.adainish.returngts.builder.SaleBuilder;
import io.github.adainish.returngts.builder.SaleBuilderAction;
import io.github.adainish.returngts.enumerations.SaleType;
import io.github.adainish.returngts.obj.player.GTSPlayer;
import io.github.adainish.returngts.storage.GTSStorage;
import io.github.adainish.returngts.storage.PlayerStorage;
import io.github.adainish.returngts.util.EconomyUtil;
import io.github.adainish.returngts.util.Util;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.*;

public class GTS
{
    public List<GTSItem> gtsItems = new ArrayList<>();
    public HashMap<UUID, OfflineSaleHandler> offlineSaleMap = new HashMap<>();
    public GTS()
    {

    }

    public void save()
    {
        GTSStorage.saveGTS(this);
    }

    public void wipeExpiredItems()
    {
        if (gtsItems.isEmpty())
            return;
        List<GTSItem> expired = new ArrayList<>();
        this.gtsItems.forEach(gtsItem -> {
            if (gtsItem.hasSold())
                return;
            if (gtsItem.hasExpired())
                expired.add(gtsItem);
        });
        expired.forEach(GTSItem::sendToSellerStorage);
        gtsItems.removeAll(expired);
    }

    public void updatePlayerOfflineSales(GTSPlayer player)
    {
        if (offlineSaleMap != null) {
            if (offlineSaleMap.containsKey(player.uuid)) {
                OfflineSaleHandler handler = offlineSaleMap.get(player.uuid);
                handler.handout();
                offlineSaleMap.put(player.uuid, new OfflineSaleHandler());
            }
        }
    }

    public void announce(String msg)
    {
        ReturnGTS.getServer().getPlayerList().getPlayers().forEach(serverPlayerEntity -> {
            GTSPlayer player = PlayerStorage.getPlayer(serverPlayerEntity.getUniqueID());
            if (player != null)
            {
                player.sendMessage(msg);
            }
        });
    }

    public boolean buyItem(GTSPlayer gtsPlayer, GTSItem gtsItem)
    {
        if (gtsItems.isEmpty())
            return false;
        if (gtsItem.hasExpired())
            return false;
        if (gtsItem.hasSold())
            return false;

        if (gtsItem.isSellerOffline())
        {
            if (this.offlineSaleMap == null)
                this.offlineSaleMap = new HashMap<>();
            OfflineSaleHandler offlineSaleHandler = null;
            if (this.offlineSaleMap.containsKey(gtsItem.seller))
            {
                offlineSaleHandler = this.offlineSaleMap.get(gtsItem.seller);
            } else offlineSaleHandler = new OfflineSaleHandler();
            offlineSaleHandler.add(gtsItem);
            this.offlineSaleMap.put(gtsItem.seller, offlineSaleHandler);
        } else {
            EconomyUtil.giveBalance(gtsItem.seller, gtsItem.askingPrice);
        }
        this.gtsItems.remove(gtsItem);
        gtsItem.buyer = gtsPlayer.uuid;

        gtsItem.sendToBuyer();
        return true;
    }

    public void openGTSMenu(GTSPlayer player) {
        if (player.getOptionalServerPlayer().isPresent())
            UIManager.openUIForcefully(player.getOptionalServerPlayer().get(), gtsPage(player));
        else player.sendMessage("&cSomething went wrong while loading gts...");
    }

    public List<Button> sortedGTSItems(GTSPlayer player)
    {
        List<Button> buttons = new ArrayList<>();
        if (this.gtsItems != null) {
            List<GTSItem> sortedItems = this.gtsItems;
            sortedItems.sort(Comparator.comparing(item -> item.startingTime));

            sortedItems.forEach(sortedItem -> {
                buttons.add(sortedItem.menuButton(player));
            });
        } else this.gtsItems = new ArrayList<>();
        return buttons;
    }

    public GooeyPage manageListingPage(GTSPlayer player, GTSItem item)
    {
        ChestTemplate.Builder builder = Util.returnBasicNonPlaceholderTemplateBuilder();

        GooeyButton updatePrice = GooeyButton.builder()
                .title(Util.formattedString("&aUpdate price"))
                .lore(Util.formattedArrayList(Arrays.asList("&7Click to update the price you're selling your item for!")))
                .onClick(b -> {
                    player.itemUpdater = new ItemUpdater(item);
                    UIManager.closeUI(b.getPlayer());
                    Scheduling.schedule(2, task -> {
                        player.itemUpdater.dialogueInputScreenBuilder(SaleBuilderAction.asking_price, player).sendTo(b.getPlayer());
                    }, false);
                })
                .display(new ItemStack(PixelmonItems.amulet_coin))
                .build();

        GooeyButton remove = GooeyButton.builder()
                .title(Util.formattedString("&cDelete Listing"))
                .display(new ItemStack(Items.MAGMA_BLOCK))
                .onClick(b -> {
                    player.sendMessage("Removed your listing!");
                    item.sendToSellerStorage();
                    ReturnGTS.gts.gtsItems.remove(item);
                    UIManager.openUIForcefully(b.getPlayer(), manageListingsPage(player));
                })
                .build();



        GooeyButton goBack = GooeyButton.builder().title(Util.formattedString("&4Go Back"))
                .display(new ItemStack(PixelmonItems.eject_button))
                .onClick(b -> {
                    this.openGTSMenu(player);
                })
                .build();


        builder.set(2, 2, updatePrice);
        builder.set(2, 6, remove);
        builder.set(2, 4, goBack);


        return GooeyPage.builder()
                .title(Util.formattedString("Confirm Purchase"))
                .template(builder.build())
                .build();
    }

    public List<Button> myListingsButtons(GTSPlayer player)
    {
        List<Button> buttons = new ArrayList<>();
        this.gtsItems.forEach(gtsItem -> {
            if (gtsItem.seller.equals(player.uuid))
            {
                buttons.add(gtsItem.manageButton(player));
            }
        });
        return buttons;
    }

    public List<Button> playerStorageButtons(GTSPlayer player)
    {
        PartyStorage storage = StorageProxy.getParty(player.uuid);
        List<Button> buttons = new ArrayList<>();

        player.retrievalStorage.pokemon.forEach(pokemon -> {
            List<String> lore = new ArrayList<>(Arrays.asList("&cClick to retrieve this pokemon"));
            lore.addAll(Util.pokemonLore(pokemon));
            GooeyButton button = GooeyButton.builder()
                    .title(Util.formattedString(Util.formattedPokemonNameString(pokemon)))
                    .lore(Util.formattedArrayList(lore))
                    .display(SpriteItemHelper.getPhoto(pokemon))
                    .onClick(b -> {
                        storage.add(pokemon);
                        player.retrievalStorage.pokemon.remove(pokemon);
                        player.updateCache();
                        UIManager.openUIForcefully(b.getPlayer(), storagePage(player));
                    })
                    .build();
            buttons.add(button);
        });

        player.retrievalStorage.itemStacks.forEach(item -> {
            List<String> lore = new ArrayList<>(Arrays.asList("&cClick to retrieve this item"));
            GooeyButton button = GooeyButton.builder()
                    .lore(Util.formattedArrayList(lore))
                    .display(item.copy())
                    .onClick(b -> {
                        if (b.getPlayer().inventory.getFirstEmptyStack() != -1) {
                            b.getPlayer().inventory.addItemStackToInventory(item);
                            player.retrievalStorage.itemStacks.remove(item);
                            player.updateCache();
                        } else player.sendMessage("&cThere's not enough space in your inventory to retrieve this item!");
                        //reopen menu
                    })
                    .build();
            buttons.add(button);
        });

        return buttons;
    }

    public LinkedPage manageListingsPage(GTSPlayer player)
    {
        ChestTemplate.Builder builder = Util.returnBasicTemplateBuilder();

        GooeyButton myStorage = GooeyButton.builder()
                .title(Util.formattedString("&bMy Storage"))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), storagePage(player));
                })
                .display(new ItemStack(Items.CHEST))
                .build();


        GooeyButton goBack = GooeyButton.builder().title(Util.formattedString("&4Go Back"))
                .display(new ItemStack(PixelmonItems.eject_button))
                .onClick(b -> {
                    this.openGTSMenu(player);
                })
                .build();

        GooeyButton sellMenu = GooeyButton.builder()
                .title(Util.formattedString("&aMake a new listing"))
                .display(new ItemStack(Items.WRITABLE_BOOK))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), sellMenu(player));
                })
                .build();

        builder.set(0, 2, myStorage);
        builder.set(0, 4, goBack);
        builder.set(0, 6, sellMenu);

        return PaginationHelper.createPagesFromPlaceholders(builder.build(), myListingsButtons(player), LinkedPage.builder().title(Util.formattedString("&aGTS")).template(builder.build()));
    }

    public LinkedPage storagePage(GTSPlayer player)
    {
        ChestTemplate.Builder builder = Util.returnBasicTemplateBuilder();

        GooeyButton myListings = GooeyButton.builder()
                .title(Util.formattedString("&bMy Listings"))
                .display(new ItemStack(Items.ENCHANTED_BOOK))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), manageListingsPage(player));
                })
                .build();


        GooeyButton goBack = GooeyButton.builder().title(Util.formattedString("&4Go Back"))
                .display(new ItemStack(PixelmonItems.eject_button))
                .onClick(b -> {
                    this.openGTSMenu(player);
                })
                .build();
        GooeyButton sellMenu = GooeyButton.builder()
                .title(Util.formattedString("&aMake a new listing"))
                .display(new ItemStack(Items.WRITABLE_BOOK))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), sellMenu(player));
                })
                .build();

        builder.set(0, 2, myListings);
        builder.set(0, 4, goBack);
        builder.set(0, 6, sellMenu);

        return PaginationHelper.createPagesFromPlaceholders(builder.build(), playerStorageButtons(player), LinkedPage.builder().title(Util.formattedString("&aGTS")).template(builder.build()));
    }

    public LinkedPage gtsPage(GTSPlayer player)
    {
        ChestTemplate.Builder builder = Util.returnBasicTemplateBuilder();

        GooeyButton myListings = GooeyButton.builder()
                .title(Util.formattedString("&bMy Listings"))
                .display(new ItemStack(Items.ENCHANTED_BOOK))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), manageListingsPage(player));
                })
                .build();

        GooeyButton myStorage = GooeyButton.builder()
                .title(Util.formattedString("&bMy Storage"))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), storagePage(player));
                })
                .display(new ItemStack(Items.CHEST))
                .build();

        GooeyButton sellMenu = GooeyButton.builder()
                .title(Util.formattedString("&aMake a new listing"))
                .display(new ItemStack(Items.WRITABLE_BOOK))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), sellMenu(player));
                })
                .build();

        builder.set(0, 2, myListings);
        builder.set(0, 4, myStorage);
        builder.set(0, 6, sellMenu);

        return PaginationHelper.createPagesFromPlaceholders(builder.build(), sortedGTSItems(player), LinkedPage.builder().title(Util.formattedString("&aGTS")).template(builder.build()));
    }

    public GooeyPage sellMenu(GTSPlayer player)
    {

        ChestTemplate.Builder builder = Util.returnBasicNonPlaceholderTemplateBuilder();


        GooeyButton pokemon = GooeyButton.builder()
                .title(Util.formattedString("&aSell a Pokemon"))
                .display(new ItemStack(PixelmonItems.poke_ball))
                .onClick(b -> {

                    SaleBuilder saleBuilder = new SaleBuilder(SaleType.POKEMON);
                    saleBuilder.seller = player.uuid;
                    player.saleBuilder = saleBuilder;
                    saleBuilder.open(player);
                })
                .build();


        GooeyButton item = GooeyButton.builder()
                .title(Util.formattedString("&aSell a item"))
                .display(new ItemStack(Items.BOOK))
                .onClick(b -> {
                    SaleBuilder saleBuilder = new SaleBuilder(SaleType.ITEM);
                    saleBuilder.seller = player.uuid;
                    player.saleBuilder = saleBuilder;
                    saleBuilder.open(player);
                })
                .build();

        GooeyButton goBack = GooeyButton.builder().title(Util.formattedString("&4Go Back"))
                .display(new ItemStack(PixelmonItems.eject_button))
                .onClick(b -> {
                    this.openGTSMenu(player);
                })
                .build();

        builder.set(2, 2, item);
        builder.set(2, 4, goBack);
        builder.set(2, 6, pokemon);


        return GooeyPage.builder()
                .title(Util.formattedString("Confirm Purchase"))
                .template(builder.build())
                .build();
    }

    public GooeyPage confirmPurchaseMenu(GTSPlayer buyer, GTSItem item)
    {

        ChestTemplate.Builder builder = Util.returnBasicNonPlaceholderTemplateBuilder();


        GooeyButton purchase = GooeyButton.builder().title(Util.formattedString("&a&lClick to purchase"))
                .display(item.getDisplayItem())
                .onClick(b -> {
                    if (buyer.uuid.equals(item.seller))
                    {
                        buyer.sendMessage("&4You can't buy your own item!");
                        return;
                    }
                    if (EconomyUtil.canAfford(buyer.uuid, item.askingPrice))
                    {
                        EconomyUtil.takeBalance(buyer.uuid, item.askingPrice);
                        this.buyItem(buyer, item);
                        this.openGTSMenu(buyer);
                    } else buyer.sendMessage("&cYou can't afford this item!");
                })
                .build();

        GooeyButton goBack = GooeyButton.builder().title(Util.formattedString("&4Go Back"))
                .display(new ItemStack(PixelmonItems.eject_button))
                .onClick(b -> {
                    this.openGTSMenu(buyer);
                })
                .build();

        builder.set(2, 3, goBack);
        builder.set(2, 5, purchase);

        return GooeyPage.builder()
                .title(Util.formattedString("Confirm Purchase"))
                .template(builder.build())
                .build();
    }


}
