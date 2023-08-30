package io.github.adainish.returngts.storage;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.github.adainish.returngts.ReturnGTS;
import io.github.adainish.returngts.obj.GTS;
import io.github.adainish.returngts.util.Adapters;

import java.io.*;

public class GTSStorage
{
    public GTS gts;

    public GTSStorage()
    {
        this.gts = new GTS();
    }

    public void save()
    {
        ReturnGTS.log.warn("Now saving GTS");
        gts.save();
    }


    public static void saveGTS(GTS gts) {
        ReturnGTS.log.warn("Saving data...");
        File dir = ReturnGTS.storageDir;
        dir.mkdirs();
        File file = new File(dir, "gts.json");
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
            writer.write(gson.toJson(gts));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ReturnGTS.log.warn("Data saved successfully!");

    }

    public static void writeGTS()
    {
        File dir = ReturnGTS.storageDir;
        dir.mkdirs();
        Gson gson  = Adapters.PRETTY_MAIN_GSON;
        GTS gts = new GTS();
        try {
            File file = new File(dir, "gts.json");
            if (file.exists())
                return;
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            String json = gson.toJson(gts);
            writer.write(json);
            writer.close();
        } catch (IOException e)
        {
            ReturnGTS.log.warn(e);
        }
    }

    public static GTS getGTS()
    {
        File dir = ReturnGTS.storageDir;
        dir.mkdirs();
        Gson gson  = Adapters.PRETTY_MAIN_GSON;
        File file = new File(dir, "gts.json");
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            ReturnGTS.log.error("Something went wrong attempting to read the Config");
            return null;
        }

        return gson.fromJson(reader, GTS.class);
    }
}
