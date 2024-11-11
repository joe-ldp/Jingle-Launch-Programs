package me.ravalle.programlauncher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import org.apache.logging.log4j.Level;
import xyz.duncanruns.jingle.Jingle;
import xyz.duncanruns.jingle.util.ExceptionUtil;
import xyz.duncanruns.jingle.util.FileUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static me.ravalle.programlauncher.ProgramLauncher.PROGRAM_LAUNCHER_SETTINGS_PATH;

/**
 * Majority of the code from <a href="https://github.com/marin774/Jingle-Stats-Plugin/blob/main/src/main/java/me/marin/statsplugin/io/StatsPluginSettings.java">Marin's Stats plugin</a>
 */
public class ProgramLauncherSettings {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

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

    @SerializedName("version")
    public String version;

    public static ProgramLauncherSettings getInstance() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        if (!Files.exists(PROGRAM_LAUNCHER_SETTINGS_PATH)) {
            instance = new ProgramLauncherSettings();
            instance.version = ProgramLauncher.VERSION;
            save();
        } else {
            String s;
            try {
                s = FileUtil.readString(PROGRAM_LAUNCHER_SETTINGS_PATH);
            } catch (IOException e) {
                instance = new ProgramLauncherSettings();
                return;
            }
            instance = GSON.fromJson(s, ProgramLauncherSettings.class);
        }
    }

    public static void save() {
        try {
            //getInstance().launchMC &= ProgramLauncher.isValidDotMinecraftPath(getInstance().dotMinecraftPath) && new File(getInstance().launcherExecutable).exists();
            FileUtil.writeString(PROGRAM_LAUNCHER_SETTINGS_PATH, GSON.toJson(instance));
        } catch (IOException e) {
            Jingle.log(Level.ERROR, "(ProgramLauncherPlugin) Failed to save Program Launcher Settings: " + ExceptionUtil.toDetailedString(e));
        }
    }
}
