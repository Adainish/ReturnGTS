package io.github.adainish.returngts.util;

import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkType;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.api.util.helpers.ResourceLocationHelper;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import io.github.adainish.returngts.ReturnGTS;
import io.github.adainish.returngts.obj.player.GTSPlayer;
import io.github.adainish.returngts.storage.PlayerStorage;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.github.adainish.returngts.ReturnGTS.getServer;

public class Util
{
    public static final long DAY_IN_MILLIS = 86400000;
    public static final long HOUR_IN_MILLIS = 3600000;
    public static final long MINUTE_IN_MILLIS = 60000;
    public static final long SECOND_IN_MILLIS = 1000;
    public static MinecraftServer server = getServer();

    private static final MinecraftServer SERVER = server;


    public static boolean isPlayerOnline(ServerPlayerEntity player) {
        return isPlayerOnline(player.getUniqueID());
    }

    public static boolean isPlayerOnline(UUID uuid) {
        ServerPlayerEntity player = SERVER.getPlayerList().getPlayerByUUID(uuid);
        // IJ says it's always true ignore
        return player != null;
    }

    public static String getOfflinePlayerName(UUID uuid) {
        GTSPlayer player = PlayerStorage.getPlayer(uuid);
        if (player == null)
            return "Invalid player data";
        return player.getUsername();
    }

    public static String getPlayerName(UUID uuid) {
        ServerPlayerEntity playerEntity = getPlayer(uuid);
        if (playerEntity == null)
            return "Offline Player";
        return playerEntity.getName().getUnformattedComponentText();
    }

    public static Optional<ServerPlayerEntity> getPlayerOptional(String name) {
        return Optional.ofNullable(getServer().getPlayerList().getPlayerByUsername(name));
    }


    public static ServerPlayerEntity getPlayer(String playerName) {
        return server.getPlayerList().getPlayerByUsername(playerName);
    }

    public static String formattedPokemonNameString(Pokemon p) {

        String s = "&b%pokemon% &eLvl: %lvl%"
                .replace("%pokemon%", p.getDisplayName())
                .replace("%lvl%", String.valueOf(p.getPokemonLevel()));

        if (p.isShiny())
            s += " &6Shiny";

        return s;
    }


    public static int[] getEvsArray(Pokemon p) {
        return new int[]{p.getEVs().getStat(BattleStatsType.HP), p.getEVs().getStat(BattleStatsType.ATTACK), p.getEVs().getStat(BattleStatsType.DEFENSE), p.getEVs().getStat(BattleStatsType.SPECIAL_ATTACK), p.getEVs().getStat(BattleStatsType.SPECIAL_DEFENSE), p.getEVs().getStat(BattleStatsType.SPEED)};
    }

    public static double getEVSPercentage(int decimalPlaces, Pokemon p) {
        int total = 0;
        int[] evs = getEvsArray(p);

        for (int evStat : evs) {
            total += evStat;
        }
        double percentage = (double)total / 510.0D * 100.0D;
        return Math.floor(percentage * Math.pow(10.0D, decimalPlaces)) / Math.pow(10.0D, decimalPlaces);
    }

    public static ArrayList <String> pokemonLore(Pokemon p) {
        ArrayList<String> list = new ArrayList<>();
        list.add("&7Ball:&e " + p.getBall().getName());
        list.add("&7Ability:&e " + p.getAbility().getName());
        list.add("&7Nature:&e " + p.getNature().name());
        list.add("&7Gender:&e " + p.getGender().name());
        list.add("&7Size:&e " + p.getGrowth().name());
        if (p.getPalette().getTexture() != null) {
            if (!p.getPalette().getTexture().toString().isEmpty()) {
                list.add("&5Custom Texture: &b" + p.getPalette().getName());
            }
        }
        list.add("&7IVS: (&f%ivs%%&7)".replace("%ivs%", String.valueOf(p.getIVs().getPercentage(1))));
        list.add("&cHP: %hp% &7/ &6Atk: %atk% &7/ &eDef: %def%"
                .replace("%hp%", String.valueOf(p.getIVs().getStat(BattleStatsType.HP)))
                .replace("%atk%", String.valueOf(p.getIVs().getStat(BattleStatsType.ATTACK)))
                .replace("%def%", String.valueOf(p.getIVs().getStat(BattleStatsType.DEFENSE)))
        );
        list.add("&9SpA: %spa% &7/ &aSpD: %spd% &7/ &dSpe: %spe%"
                .replace("%spa%", String.valueOf(p.getIVs().getStat(BattleStatsType.SPECIAL_ATTACK)))
                .replace("%spd%", String.valueOf(p.getIVs().getStat(BattleStatsType.SPECIAL_DEFENSE)))
                .replace("%spe%", String.valueOf(p.getIVs().getStat(BattleStatsType.SPEED)))
        );
        list.add("&7EVS: (&f%evs%%&7)".replace("%evs%", String.valueOf(getEVSPercentage(1, p))));
        list.add("&cHP: %hp% &7/ &6Atk: %atk% &7/ &eDef: %def%"
                .replace("%hp%", String.valueOf(p.getEVs().getStat(BattleStatsType.HP)))
                .replace("%atk%", String.valueOf(p.getEVs().getStat(BattleStatsType.ATTACK)))
                .replace("%def%", String.valueOf(p.getEVs().getStat(BattleStatsType.DEFENSE)))
        );
        list.add("&9SpA: %spa% &7/ &aSpD: %spd% &7/ &dSpe: %spe%"
                .replace("%spa%", String.valueOf(p.getEVs().getStat(BattleStatsType.SPECIAL_ATTACK)))
                .replace("%spd%", String.valueOf(p.getEVs().getStat(BattleStatsType.SPECIAL_DEFENSE)))
                .replace("%spe%", String.valueOf(p.getEVs().getStat(BattleStatsType.SPEED)))
        );
        for (Attack a:p.getMoveset().attacks) {
            if (a == null)
                continue;
            list.add("&7- " + a.getActualMove().getAttackName());
        }

        return list;
    }

    public static ServerPlayerEntity getPlayer(UUID uuid) {
        return server.getPlayerList().getPlayerByUUID(uuid);
    }

    public static RegistryKey<World> getDimension(String dimension) {
        return dimension.isEmpty() ? null : getDimension(ResourceLocationHelper.of(dimension));
    }

    public static RegistryKey<World> getDimension(ResourceLocation key) {
        return RegistryKey.getOrCreateKey(Registry.WORLD_KEY, key);
    }

    public static Optional<ServerWorld> getWorld(RegistryKey<World> key) {
        return Optional.ofNullable(ServerLifecycleHooks.getCurrentServer().getWorld(key));
    }

    public static Optional<ServerWorld> getWorld(String key) {
        return getWorld(getDimension(key));
    }

    public static GooeyButton filler = GooeyButton.builder()
            .display(new ItemStack(Blocks.GRAY_STAINED_GLASS_PANE, 1))
            .build();

    public boolean isPokeBall() {

        return false;
    }

    public boolean isPokeBall(ItemStack stack) {
        return stack.getItem().getRegistryName().equals(PixelmonItems.poke_ball.getRegistryName());
    }

    public static String getItemStackName(CompoundNBT compoundNBT) {
        ItemStack stack = ItemStack.read(compoundNBT);

        return getItemStackName(stack);
    }
    // TODO: 21/12/2022 Add capitalisation reformatted for lower cased word translations

    public static String getItemStackName(ItemStack stack) {
        String formattedName = null;

        if (stack.hasDisplayName()) {
            formattedName = stack.getDisplayName().getUnformattedComponentText();
        } else {
            formattedName = stack.getItem().getRegistryName().getPath().replace("_", " ");
        }
        if (formattedName.isEmpty())
            formattedName = "Name Not Stored";
        return formattedName;
    }

    public static List<String> getItemStackLore(ItemStack itemStack) {
        List<String> lore = new ArrayList<>();

        if (itemStack.hasTag()) {
            ListNBT displayTag = itemStack.getOrCreateChildTag("display").getList("Lore", 8);
            for (int i = 0; i < displayTag.size(); i++) {
                lore.add(displayTag.getString(i));
            }
        }

        return lore;
    }
    public static GooeyButton filler() {
        return GooeyButton.builder()
                .display(new ItemStack(Items.GRAY_STAINED_GLASS_PANE))
                .build();
    }

    public static ChestTemplate.Builder returnBasicNonPlaceholderTemplateBuilder() {
        ChestTemplate.Builder builder = ChestTemplate.builder(5);
        builder.fill(filler());
        return builder;
    }

    public static ChestTemplate.Builder returnSizeableBasicTemplateBuilder(int rows) {
        ChestTemplate.Builder builder = ChestTemplate.builder(rows);
        builder.fill(filler());

        PlaceholderButton placeHolderButton = new PlaceholderButton();
        LinkedPageButton previous = LinkedPageButton.builder()
                .display(new ItemStack(Items.SPECTRAL_ARROW))
                .title(Util.formattedString("Previous Page"))
                .linkType(LinkType.Previous)
                .build();

        LinkedPageButton next = LinkedPageButton.builder()
                .display(new ItemStack(Items.SPECTRAL_ARROW))
                .title(Util.formattedString("Next Page"))
                .linkType(LinkType.Next)
                .build();

        builder.set(0, 3, previous)
                .set(0, 5, next)
                .rectangle(1, 1, 3, 7, placeHolderButton);
        return builder;
    }

    public static ChestTemplate.Builder returnBasicTemplateBuilder() {
        ChestTemplate.Builder builder = ChestTemplate.builder(5);
        builder.fill(filler());

        PlaceholderButton placeHolderButton = new PlaceholderButton();
        LinkedPageButton previous = LinkedPageButton.builder()
                .display(new ItemStack(Items.SPECTRAL_ARROW))
                .title(Util.formattedString("Previous Page"))
                .linkType(LinkType.Previous)
                .build();

        LinkedPageButton next = LinkedPageButton.builder()
                .display(new ItemStack(Items.SPECTRAL_ARROW))
                .title(Util.formattedString("Next Page"))
                .linkType(LinkType.Next)
                .build();

        builder.set(0, 3, previous)
                .set(0, 5, next)
                .rectangle(1, 1, 3, 7, placeHolderButton);
        return builder;
    }

    public static void sendSuccessFullMessage(ServerPlayerEntity playerEntity, String message) {
        StringTextComponent textComponent = new StringTextComponent(formattedString(TextUtil.getMessagePrefix().getString() + message));
        Style componentStyle = Style.EMPTY;
        componentStyle = componentStyle.applyFormatting(TextFormatting.GREEN);
        textComponent.setStyle(componentStyle);
        playerEntity.sendMessage(textComponent, playerEntity.getUniqueID());
    }

    public static void sendFailMessage(ServerPlayerEntity playerEntity, String message) {
        StringTextComponent textComponent = new StringTextComponent(formattedString(TextUtil.getMessagePrefix().getString() + message));
        Style componentStyle = Style.EMPTY;
        componentStyle = componentStyle.applyFormatting(TextFormatting.RED);
        textComponent.setStyle(componentStyle);
        playerEntity.sendMessage(textComponent, playerEntity.getUniqueID());
    }

    public static MinecraftServer getInstance() {
        return SERVER;
    }

    public static TextComponent commandTextComponent(String message, String command) {
        TextComponent component = new StringTextComponent(Util.formattedString(message));
        Style componentStyle = Style.EMPTY;
        componentStyle = componentStyle.applyFormatting(TextFormatting.YELLOW);
        componentStyle = componentStyle.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command));
        component.setStyle(componentStyle);
        return component;
    }


    public static void sendArray(UUID uuid, List<String> message) {
        for (String s : message) {
            getPlayer(uuid).sendMessage(new StringTextComponent(((TextUtil.getMessagePrefix()).getString() + s).replaceAll("&([0-9a-fk-or])", "\u00a7$1")), uuid);
        }
    }

    public static void sendArrayUnformatted(UUID uuid, List<String> message) {
        for (String s : message) {
            getPlayer(uuid).sendMessage(new StringTextComponent((s).replaceAll("&([0-9a-fk-or])", "\u00a7$1")), uuid);
        }
    }

    public static void send(UUID uuid, String message) {
        if (uuid == null)
            return;
        getPlayer(uuid).sendMessage(new StringTextComponent(((TextUtil.getMessagePrefix()).getString() + message).replaceAll("&([0-9a-fk-or])", "\u00a7$1")), uuid);
    }

    public static String getResourceLocationStringFromItemStack(ItemStack stack)
    {
        return stack.getItem().getRegistryName().toString();
    }

    public static void send(ServerPlayerEntity player, String message) {
        if (player == null)
            return;
        player.sendMessage(new StringTextComponent(((TextUtil.getMessagePrefix()).getString() + message).replaceAll("&([0-9a-fk-or])", "\u00a7$1")), player.getUniqueID());
    }

    public static void sendNoFormat(UUID uuid, String message) {
        getPlayer(uuid).sendMessage(new StringTextComponent((message).replaceAll("&([0-9a-fk-or])", "\u00a7$1")), uuid);
    }

    public static void sendNoFormat(ServerPlayerEntity player, String message) {
        if (player == null)
            return;
        player.sendMessage(new StringTextComponent((message).replaceAll("&([0-9a-fk-or])", "\u00a7$1")), player.getUniqueID());
    }


    public static void send(CommandSource sender, String message) {
        sender.sendFeedback(new StringTextComponent(((TextUtil.getMessagePrefix()).getString() + message).replaceAll("&([0-9a-fk-or])", "\u00a7$1")), false);
    }


    public static void sendNoFormat(CommandSource sender, String message) {
        sender.sendFeedback(new StringTextComponent((message).replaceAll("&([0-9a-fk-or])", "\u00a7$1")), false);
    }


    public static String formattedString(String s) {
        return s.replaceAll("&", "§");
    }

    public static List<String> formattedArrayList(List<String> list) {

        List<String> formattedList = new ArrayList<>();
        for (String s : list) {
            formattedList.add(formattedString(s));
        }

        return formattedList;
    }

    public static void runCommand(String cmd)
    {
        try {
            getServer().getCommandManager().getDispatcher().execute(cmd, getServer().getCommandSource());
        } catch (CommandSyntaxException e) {
            ReturnGTS.log.error(e);
        }
    }
}
