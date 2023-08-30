package io.github.adainish.returngts.storage;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.github.adainish.returngts.ReturnGTS;
import io.github.adainish.returngts.obj.player.GTSPlayer;
import io.github.adainish.returngts.util.Adapters;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerStorage
{
    public static void makePlayer(UUID uuid) {
        File dir = ReturnGTS.getPlayerStorageDir();
        dir.mkdirs();


        GTSPlayer playerData = new GTSPlayer(uuid);

        File file = new File(dir, "%uuid%.json".replaceAll("%uuid%", String.valueOf(uuid)));
        if (file.exists()) {
            return;
        }

        Gson gson = Adapters.PRETTY_MAIN_GSON;
        String json = gson.toJson(playerData);

        try {
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void makePlayer(ServerPlayerEntity player) {
        File dir = ReturnGTS.getPlayerStorageDir();
        dir.mkdirs();


        GTSPlayer playerData = new GTSPlayer(player.getUniqueID());

        File file = new File(dir, "%uuid%.json".replaceAll("%uuid%", String.valueOf(player.getUniqueID())));
        if (file.exists()) {
            return;
        }

        Gson gson = Adapters.PRETTY_MAIN_GSON;
        String json = gson.toJson(playerData);

        try {
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void savePlayer(GTSPlayer player) {

        File dir = ReturnGTS.getPlayerStorageDir();
        dir.mkdirs();

        File file = new File(dir, "%uuid%.json".replaceAll("%uuid%", String.valueOf(player.uuid)));
        Gson gson = Adapters.PRETTY_MAIN_GSON;
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (reader == null) {
            ReturnGTS.log.error("Something went wrong attempting to read the Player Data");
            return;
        }


        try {
            FileWriter writer = new FileWriter(file);
            writer.write(gson.toJson(player));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        player.updateCache();
    }

    public static List<UUID> getAllPlayerUUIDS()
    {
        List<UUID> uuids = new ArrayList<>();

        File dir = ReturnGTS.getPlayerStorageDir();
        if (dir != null) {
            for (File f : dir.listFiles()) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(f.getName().replace(".json", ""));
                } catch (IllegalArgumentException e)
                {
                    continue;
                }

                uuids.add(uuid);

            }
        }

        return uuids;
    }

    public static List<GTSPlayer> getAllPlayers()
    {

        List<UUID> addedPlayers = new ArrayList<>();

        List<GTSPlayer> playerList = new ArrayList<>();

        for (GTSPlayer p:ReturnGTS.playerCache.playerCache.values()) {
            playerList.add(p);
            addedPlayers.add(p.uuid);
        }

        File dir = ReturnGTS.getPlayerStorageDir();
        if (dir != null) {
            for (File f : dir.listFiles()) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(f.getName().replace(".json", ""));
                } catch (IllegalArgumentException e)
                {
                    continue;
                }
                if (addedPlayers.contains(uuid))
                    continue;
                GTSPlayer p = getPlayer(uuid);
                if (p == null)
                    continue;
                playerList.add(p);
                addedPlayers.add(uuid);
            }
        }

        return playerList;
    }

    @Nullable
    public static GTSPlayer getPlayer(UUID uuid) {
        File dir = ReturnGTS.getPlayerStorageDir();
        dir.mkdirs();

        if (ReturnGTS.playerCache.playerCache.containsKey(uuid))
            return ReturnGTS.playerCache.playerCache.get(uuid);

        File guildFile = new File(dir, "%uuid%.json".replaceAll("%uuid%", String.valueOf(uuid)));
        Gson gson = Adapters.PRETTY_MAIN_GSON;
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(guildFile));
        } catch (FileNotFoundException e) {
            ReturnGTS.log.error("Detected non-existing player, making new player data file");
            return null;
        }

        return gson.fromJson(reader, GTSPlayer.class);
    }
}
