package io.github.adainish.returngts.builder;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.pixelmonmod.pixelmon.api.dialogue.DialogueInputScreen;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.api.util.Scheduling;
import com.pixelmonmod.pixelmon.api.util.helpers.SpriteItemHelper;
import io.github.adainish.returngts.ReturnGTS;
import io.github.adainish.returngts.enumerations.SaleType;
import io.github.adainish.returngts.obj.GTSItem;
import io.github.adainish.returngts.obj.player.GTSPlayer;
import io.github.adainish.returngts.util.Util;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.*;
import java.util.stream.Collectors;

public class SaleBuilder
{
    public UUID seller;
    public SaleType saleType;
    public SaleBuilderAction saleBuilderAction = SaleBuilderAction.none;
    public ItemStack stack;
    public Pokemon pokemon;
    public int price = 100;

    public SaleBuilder()
    {

    }

    public SaleBuilder(SaleType saleType)
    {
        this.saleType = saleType;
    }

    public SaleBuilder(Pokemon pokemon)
    {
        this.pokemon = pokemon;
        this.saleType = SaleType.POKEMON;
        this.saleBuilderAction = SaleBuilderAction.asking_price;
    }
    public SaleBuilder(ItemStack stack)
    {
        this.stack = stack;
        this.saleType = SaleType.ITEM;
        this.saleBuilderAction = SaleBuilderAction.asking_price;
    }

    public void updateCache(GTSPlayer player)
    {
        player.saleBuilder = this;
        player.updateCache();
    }



    public List<Button> pokemonPartyButtons(GTSPlayer player)
    {
        List<Button> gooeyButtons;
        PlayerPartyStorage pps = StorageProxy.getParty(player.uuid);

        gooeyButtons = Arrays.stream(pps.getAll()).filter(Objects::nonNull).map(p -> GooeyButton.builder()
                .title(Util.formattedString(Util.formattedPokemonNameString(p)))
                .lore(Util.formattedArrayList(Util.pokemonLore(p)))
                .display(SpriteItemHelper.getPhoto(p))
                .onClick(b -> {
                    //open main ui
                    this.pokemon = p;
                    player.saleBuilder = this;
                    UIManager.openUIForcefully(b.getPlayer(), sellMenu(player));
                })
                .build()).collect(Collectors.toList());

        return gooeyButtons;
    }

    public List<Button> pokemonPCButtons(GTSPlayer player)
    {
        List<Button> gooeyButtons;
        PCStorage pcs = StorageProxy.getPCForPlayer(player.uuid);

        gooeyButtons = Arrays.stream(pcs.getAll()).filter(Objects::nonNull).map(p -> GooeyButton.builder()
                .title(Util.formattedString(Util.formattedPokemonNameString(p)))
                .lore(Util.formattedArrayList(Util.pokemonLore(p)))
                .display(SpriteItemHelper.getPhoto(p))
                .onClick(b -> {
                    //open main ui
                    this.pokemon = p;
                    player.saleBuilder = this;
                    UIManager.openUIForcefully(b.getPlayer(), sellMenu(player));
                })
                .build()).collect(Collectors.toList());

        return gooeyButtons;
    }

    public List<Button> itemStackButtons(GTSPlayer player)
    {
        List<Button> buttons = new ArrayList<>();
        if (player.getOptionalServerPlayer().isPresent()) {
            ServerPlayerEntity serverPlayer = player.getOptionalServerPlayer().get();
            serverPlayer.inventory.mainInventory.forEach(itemstack -> {
                if (itemstack.isEmpty())
                    return;
                GooeyButton button = GooeyButton.builder()
                        .display(itemstack.copy())
                        .onClick(b -> {
                            this.stack = itemstack;
                            player.saleBuilder = this;
                            UIManager.openUIForcefully(b.getPlayer(), sellMenu(player));
                        })
                        .build();
                buttons.add(button);
            });
        }
        return buttons;
    }

    public LinkedPage selectPCPokemonPage(GTSPlayer player)
    {
        ChestTemplate.Builder builder = Util.returnBasicTemplateBuilder();


        GooeyButton goBack = GooeyButton.builder().title(Util.formattedString("&4Go Back"))
                .display(new ItemStack(PixelmonItems.eject_button))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), sellMenu(player));
                })
                .build();

        builder.set(0, 5, goBack);

        return PaginationHelper.createPagesFromPlaceholders(builder.build(), pokemonPCButtons(player), LinkedPage.builder().title(Util.formattedString("&aGTS")).template(builder.build()));
    }

    public LinkedPage selectPartyPokemonPage(GTSPlayer player)
    {
        ChestTemplate.Builder builder = Util.returnBasicTemplateBuilder();


        GooeyButton goBack = GooeyButton.builder().title(Util.formattedString("&4Go Back"))
                .display(new ItemStack(PixelmonItems.eject_button))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), sellMenu(player));
                })
                .build();

        builder.set(0, 5, goBack);

        return PaginationHelper.createPagesFromPlaceholders(builder.build(), pokemonPartyButtons(player), LinkedPage.builder().title(Util.formattedString("&aGTS")).template(builder.build()));
    }

    public LinkedPage selectItemPage(GTSPlayer player)
    {
        ChestTemplate.Builder builder = Util.returnSizeableBasicTemplateBuilder(6);


        GooeyButton goBack = GooeyButton.builder().title(Util.formattedString("&4Go Back"))
                .display(new ItemStack(PixelmonItems.eject_button))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), sellMenu(player));
                })
                .build();

        builder.set(0, 5, goBack);

        return PaginationHelper.createPagesFromPlaceholders(builder.build(), itemStackButtons(player), LinkedPage.builder().title(Util.formattedString("&aGTS")).template(builder.build()));
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
            case none: {
                break;
            }
        }
        saleBuilderAction = (action);
        this.updateCache(player);
        return builder;
    }

    public GTSItem buildGTSItem()
    {
        if (this.stack != null)
        return new GTSItem(this.seller, this.saleType, this.pokemon, this.stack.copy(), this.price);
        else return new GTSItem(this.seller, this.saleType, this.pokemon, null, this.price);
    }

    public GooeyPage sellMenu(GTSPlayer player)
    {

        ChestTemplate.Builder builder = Util.returnBasicNonPlaceholderTemplateBuilder();


        GooeyButton price = GooeyButton.builder()
                .title(Util.formattedString("&aAsking Price"))
                .lore(Util.formattedArrayList(Arrays.asList("&7Click to set an asking price!", "&7The current asking price is &a$%amount%".replace("%amount%", String.valueOf(this.price)))))
                .display(new ItemStack(PixelmonItems.amulet_coin))
                .onClick(b -> {
                    //open price selection
                    this.saleBuilderAction = SaleBuilderAction.asking_price;
                    UIManager.closeUI(b.getPlayer());
                    player.saleBuilder = this;
                    player.updateCache();
                    Scheduling.schedule(2, task -> {
                        this.dialogueInputScreenBuilder(this.saleBuilderAction, player).sendTo(b.getPlayer());
                    }, false);
                })
                .build();


        GooeyButton goBack = GooeyButton.builder().title(Util.formattedString("&4Cancel sale"))
                .display(new ItemStack(PixelmonItems.eject_button))
                .onClick(b -> {
                    player.saleBuilder = null;
                    player.updateCache();
                    ReturnGTS.gts.openGTSMenu(player);
                })
                .build();

        GooeyButton itemButton = null;
        if (this.saleType.equals(SaleType.POKEMON))
        {
            if (this.pokemon != null) {
                List<String> lore = new ArrayList<>();
                lore.add("&7Click to select a different pokemon");
                lore.addAll(Util.pokemonLore(this.pokemon));
                itemButton = GooeyButton.builder()
                        .title(Util.formattedString(Util.formattedPokemonNameString(this.pokemon)))
                        .lore(Util.formattedArrayList(lore))
                        .display(SpriteItemHelper.getPhoto(this.pokemon))
                        .onClick(b -> {
                            UIManager.openUIForcefully(b.getPlayer(), selectPartyPokemonPage(player));
                        })
                        .build();
            } else {
                itemButton = GooeyButton.builder()
                        .title(Util.formattedString("&aNo Pokemon set yet!"))
                        .lore(Util.formattedArrayList(Arrays.asList("&7Click to select a pokemon!")))
                        .display(new ItemStack(PixelmonItems.poke_ball))
                        .onClick(b -> {
                            UIManager.openUIForcefully(b.getPlayer(), selectPartyPokemonPage(player));
                        })
                        .build();
            }
        } else {
            if (this.stack != null)
            {
                itemButton = GooeyButton.builder()
                        .lore(Util.formattedArrayList(new ArrayList<>(Arrays.asList("&7Click to select a different item"))))
                        .display(this.stack.copy())
                        .onClick(b -> {
                            UIManager.openUIForcefully(b.getPlayer(), selectItemPage(player));
                        })
                        .build();
            } else {
                itemButton = GooeyButton.builder()
                        .title(Util.formattedString("&aNo Item Yet!"))
                        .lore(Util.formattedArrayList(Arrays.asList("&7Click to select an item!")))
                        .display(new ItemStack(Items.WRITABLE_BOOK))
                        .onClick(b -> {
                            UIManager.openUIForcefully(b.getPlayer(), selectItemPage(player));
                        })
                        .build();
            }
        }

        GooeyButton confirm = GooeyButton.builder()
                .title(Util.formattedString("&aSell Listing"))
                .display(new ItemStack(Items.GREEN_WOOL))
                .lore(Util.formattedArrayList(Arrays.asList("&aClick to confirm your sale")))
                .onClick(b -> {
                    if (this.pokemon != null)
                    {
                        PlayerPartyStorage pps = StorageProxy.getParty(player.uuid);
                        if (pps.countAll() <= 1)
                        {
                            player.sendMessage("&4&lYou can't sell your last pokemon!");
                            return;
                        }
                    }
                    this.seller = b.getPlayer().getUniqueID();
                    GTSItem item = buildGTSItem();
                    ReturnGTS.gts.gtsItems.add(item);
                    //remove from inventory/party/pc
                    if (this.stack != null)
                    {
                        b.getPlayer().inventory.deleteStack(this.stack);
                    } else {
                        PlayerPartyStorage pps = StorageProxy.getParty(player.uuid);

                        pps.set(this.pokemon.getPosition(), null);
                    }
                    player.saleBuilder = null;
                    player.updateCache();
                    player.sendMessage("&aWe've added your listing to the GTS!");
                    ReturnGTS.gts.announce("&a%player% has listed a %item% for %price%!"
                            .replace("%player%", player.getUsername())
                            .replace("%item%", item.displayTitle())
                            .replace("%price%", String.valueOf(item.askingPrice)));
                    ReturnGTS.gts.openGTSMenu(player);
                })
                .build();

        if (!doneBuilding())
        {
            confirm = GooeyButton.builder()
                    .title(Util.formattedString("&cNot done building"))
                    .lore(Util.formattedArrayList(Arrays.asList("&7You still need to finish your sale!", "Don't forget to pick what you're selling and what you're asking price wise!")))
                    .display(new ItemStack(Items.BARRIER))
                    .build();
        }

        builder.set(2, 1, goBack);
        builder.set(2, 3, price);
        if (itemButton != null) {
            builder.set(2, 5, itemButton);
        }
        builder.set(2, 7, confirm);

        return GooeyPage.builder()
                .title(Util.formattedString("Sale Builder"))
                .template(builder.build())
                .build();
    }

    public void open(GTSPlayer player)
    {
        if (player.getOptionalServerPlayer().isPresent())
            UIManager.openUIForcefully(player.getOptionalServerPlayer().get(), sellMenu(player));
        else player.sendMessage("&cSomething went wrong while loading the sale builder...");
    }

    public boolean doneBuilding()
    {
        if (this.price <= 0)
            return false;
        if (this.saleType.equals(SaleType.POKEMON))
        {
            return this.pokemon != null;
        }
        else if (this.saleType.equals(SaleType.ITEM)){
            return this.stack != null;
        }
        else return false;
    }
}
