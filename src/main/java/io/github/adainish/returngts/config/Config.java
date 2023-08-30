package io.github.adainish.returngts.config;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.github.adainish.returngts.ReturnGTS;
import io.github.adainish.returngts.util.Adapters;

import java.io.*;

public class Config
{
    public int maxGTSStayMinutes = 1440;

    public Config()
    {

    }

    public static void saveConfig(Config config) {
        ReturnGTS.log.warn("Saving data...");
        File dir = ReturnGTS.getConfigDir();
        dir.mkdirs();
        File file = new File(dir, "config.json");
        Gson gson = Adapters.PRETTY_MAIN_GSON;
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (reader == null) {
            ReturnGTS.log.error("Something went wrong attempting to save");
            return;
        }


        try {
            FileWriter writer = new FileWriter(file);
            writer.write(gson.toJson(config));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ReturnGTS.log.warn("Data saved successfully!");

    }

    public static void writeConfig()
    {
        File dir = ReturnGTS.getConfigDir();
        dir.mkdirs();
        Gson gson  = Adapters.PRETTY_MAIN_GSON;
        Config config = new Config();
        try {
            File file = new File(dir, "config.json");
            if (file.exists())
                return;
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            String json = gson.toJson(config);
            writer.write(json);
            writer.close();
        } catch (IOException e)
        {
            ReturnGTS.log.warn(e);
        }
    }

    public static Config getConfig()
    {
        File dir = ReturnGTS.getConfigDir();
        dir.mkdirs();
        Gson gson  = Adapters.PRETTY_MAIN_GSON;
        File file = new File(dir, "config.json");
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            ReturnGTS.log.error("Something went wrong attempting to read the Config");
            return null;
        }

        return gson.fromJson(reader, Config.class);
    }

}
