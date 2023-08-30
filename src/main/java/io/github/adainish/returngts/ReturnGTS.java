package io.github.adainish.returngts;

import ca.landonjw.gooeylibs2.implementation.tasks.Task;
import com.pixelmonmod.pixelmon.Pixelmon;
import io.github.adainish.returngts.cache.PlayerCache;
import io.github.adainish.returngts.commands.Command;
import io.github.adainish.returngts.config.Config;
import io.github.adainish.returngts.config.LanguageConfig;
import io.github.adainish.returngts.listeners.DialogueListener;
import io.github.adainish.returngts.listeners.PlayerListener;
import io.github.adainish.returngts.obj.GTS;
import io.github.adainish.returngts.storage.GTSStorage;
import io.github.adainish.returngts.tasks.UpdateGTSTask;
import io.github.adainish.returngts.wrapper.PermissionWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("returngts")
public class ReturnGTS {


    public static ReturnGTS instance;

    public static final String MOD_NAME = "ReturnGTS";
    public static final String VERSION = "1.0.0";
    public static final String AUTHORS = "Winglet";
    public static final String YEAR = "2023";
    public static final Logger log = LogManager.getLogger(MOD_NAME);
    private static MinecraftServer server;
    public static File configDir;
    public static File storageDir;
    public static File playerStorageDir;

    public static PermissionWrapper permissionWrapper;

    public static PlayerCache playerCache;

    public static LanguageConfig languageConfig;

    public static Config config;


    public ReturnGTS() {
        instance = this;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static File getConfigDir() {
        return configDir;
    }

    public static void setConfigDir(File configDir) {
        ReturnGTS.configDir = configDir;
    }

    public static File getPlayerStorageDir() {
        return playerStorageDir;
    }

    public static void setPlayerStorageDir(File playerStorageDir) {
        ReturnGTS.playerStorageDir = playerStorageDir;
    }

    public static MinecraftServer getServer() {
        return server;
    }

    public static GTS gts;

    public List<Task> activeTasks = new ArrayList<>();



    private void setup(final FMLCommonSetupEvent event) {
        log.info("Booting up %n by %authors %v %y"
                .replace("%n", MOD_NAME)
                .replace("%authors", AUTHORS)
                .replace("%v", VERSION)
                .replace("%y", YEAR)
        );
        initDirs();
    }

    @SubscribeEvent
    public void onCommandRegistry(RegisterCommandsEvent event)
    {
        permissionWrapper = new PermissionWrapper();
        event.getDispatcher().register(Command.getCommand());
    }


    @SubscribeEvent
    public void onServerStarted(FMLServerStartedEvent event) {
        log.warn("Finalising set up");
        server = ServerLifecycleHooks.getCurrentServer();
        initConfig();
        registerListeners();
        playerCache = new PlayerCache();
        log.warn("Finalised set up");
        this.initTasks();
    }

    @SubscribeEvent
    public void onServerShutdown(FMLServerStoppingEvent event)
    {
        this.shutdownTasks();
        saveAll();
    }

    public void registerListeners()
    {
        log.warn("Registering listeners");
        MinecraftForge.EVENT_BUS.register(new PlayerListener());
        Pixelmon.EVENT_BUS.register(new DialogueListener());
    }

    public void initDirs() {
        log.log(Level.WARN, "Setting up Storage Paths and Directories for %name%".replace("%name%", MOD_NAME));
        setConfigDir(new File(FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath()) + "/%name%/".replace("%name%", MOD_NAME)));
        getConfigDir().mkdir();
        storageDir = new File(configDir + "/Storage/");
        storageDir.mkdirs();
        playerStorageDir = new File(storageDir + "/PlayerData/");
        playerStorageDir.mkdirs();
    }

    public void loadConfigs() {
        log.log(Level.WARN, "Loading and Reading Config Data for %name%".replace("%name%", MOD_NAME));
        LanguageConfig.writeConfig();
        languageConfig = LanguageConfig.getConfig();
        save();
        Config.writeConfig();
        config = Config.getConfig();
        GTSStorage.writeGTS();
        gts = GTSStorage.getGTS();
    }

    public void initConfig() {
        loadConfigs();
    }

    public void handleTasks()
    {
        this.shutdownTasks();
        this.initTasks();
    }
    public void shutdownTasks()
    {
        if (this.activeTasks.isEmpty())
        {
            log.warn("There are no tasks to shut down! skipping...");
            return;
        }
        log.warn("Expiring all active tasks...");
        this.activeTasks.forEach(Task::setExpired);
        log.warn("Expired all tasks!");
        log.warn("Clearing task cache!");
        this.activeTasks.clear();
        log.warn("Cleared task cache!");
    }
    public void initTasks()
    {
        if (this.activeTasks.isEmpty())
        {
            log.warn("Creating GTS tasks...");
            activeTasks.add(Task.builder().execute(new UpdateGTSTask()).interval(20 * 60).infinite().build());
        } else log.warn("Tasks already exist for gts, please expire these first!");
    }

    public void saveAll()
    {
        log.warn("Saving all data!");
        this.save();
        if (playerCache != null)
        {
            log.warn("Saving player data...");
            playerCache.playerCache.forEach((uuid, gtsPlayer) -> {
                gtsPlayer.save();
            });
        } else log.warn("Failed to save player data..");
    }
    public void save()
    {
        if (config != null) {
            log.warn("Saving config data");
            Config.saveConfig(config);
        } else log.error("Failed to save the regular config as it was null.. This is normal when the server boots up.");
        if (gts != null)
        {
            log.warn("Saving GTS data");
            gts.save();
        } else log.error("Failed to save gts data as it was null.. This is normal when the server boots up.");

    }


    public void reload()
    {
        log.warn("Reload Requested... Just a moment");
        this.shutdownTasks();
        initDirs();
        initConfig();
        this.initTasks();
    }
}
