package io.github.adainish.returngts.config;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.github.adainish.returngts.ReturnGTS;
import io.github.adainish.returngts.util.Adapters;

import java.io.*;

public class LanguageConfig
{
    public String prefix;
    public String splitter;

    public LanguageConfig()
    {
        this.prefix = "&6[&dGTS&6]";
        this.splitter = " &e» ";
    }

    public static void writeConfig()
    {
        File dir = ReturnGTS.getConfigDir();
        dir.mkdirs();
        Gson gson  = Adapters.PRETTY_MAIN_GSON;
        LanguageConfig config = new LanguageConfig();
        try {
            File file = new File(dir, "language.json");
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

    public static LanguageConfig getConfig()
    {
        File dir = ReturnGTS.getConfigDir();
        dir.mkdirs();
        Gson gson  = Adapters.PRETTY_MAIN_GSON;
        File file = new File(dir, "language.json");
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            ReturnGTS.log.error("Something went wrong attempting to read the Config");
            return null;
        }

        return gson.fromJson(reader, LanguageConfig.class);
    }
}
