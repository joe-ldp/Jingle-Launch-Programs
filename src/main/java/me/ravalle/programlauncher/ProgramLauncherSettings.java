package me.ravalle.programlauncher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import org.apache.logging.log4j.Level;
import xyz.duncanruns.jingle.Jingle;
import xyz.duncanruns.jingle.JingleOptions;
import xyz.duncanruns.jingle.util.ExceptionUtil;
import xyz.duncanruns.jingle.util.FileUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Majority of the code from <a href="https://github.com/marin774/Jingle-Stats-Plugin/blob/main/src/main/java/me/marin/statsplugin/io/StatsPluginSettings.java">Marin's Stats plugin</a>
 */
public class ProgramLauncherSettings {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Path SETTINGS_PATH = ProgramLauncher.PROGRAM_LAUNCHER_FOLDER_PATH.resolve("settings.json");
    public static final Path SETTINGS_BACKUP_PATH = ProgramLauncher.PROGRAM_LAUNCHER_FOLDER_PATH.resolve("settings.json.backup");


    private static ProgramLauncherSettings instance = null;

    @SerializedName("launch program paths")
    public List<String> launchProgramPaths = new ArrayList<>();

    @SerializedName("launch minecraft instance")
    public boolean launchMC = false;

    @SerializedName("minecraft instance path")
    public String dotMinecraftPath;

    @SerializedName("launcher executable")
    public String launcherExecutable;

    @SerializedName("launch on start")
    public boolean launchOnStart = true;

    public static ProgramLauncherSettings getInstance() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        instance = loadOrDefault();
    }

    private static ProgramLauncherSettings loadOrDefault(){
        return loadFrom(SETTINGS_PATH).orElseGet(() -> loadFrom(SETTINGS_BACKUP_PATH).orElseGet(ProgramLauncherSettings::new));
    }

    private static Optional<ProgramLauncherSettings> loadFrom(Path path){
        if (Files.exists(path)) {
            try {
                ProgramLauncherSettings options = FileUtil.readJson(path, ProgramLauncherSettings.class, GSON);
                return Optional.of(options);
            } catch (Exception e) {
                Jingle.logError("Failed to load program-launcher-plugin/" + path.getFileName().toString(), e);
            }
        }
        return Optional.empty();

    }

    public static void save() {
        try {
            //getInstance().launchMC &= ProgramLauncher.isValidDotMinecraftPath(getInstance().dotMinecraftPath) && new File(getInstance().launcherExecutable).exists();
            FileUtil.writeString(SETTINGS_PATH, GSON.toJson(instance));
            FileUtil.writeString(SETTINGS_BACKUP_PATH, GSON.toJson(instance));
        } catch (IOException e) {
            Jingle.log(Level.ERROR, "(ProgramLauncherPlugin) Failed to save Program Launcher Settings: " + ExceptionUtil.toDetailedString(e));
        }
    }
}
