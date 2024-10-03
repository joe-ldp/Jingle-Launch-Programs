package me.ravalle.programlauncher.util;

import com.github.tuupertunut.powershelllibjava.PowerShellExecutionException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import xyz.duncanruns.jingle.Jingle;
import xyz.duncanruns.jingle.JingleAppLaunch;
import xyz.duncanruns.jingle.plugin.PluginManager;
import xyz.duncanruns.jingle.util.ExceptionUtil;
import xyz.duncanruns.jingle.util.PowerShellUtil;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Majority of the code from <a href="https://github.com/marin774/Jingle-Stats-Plugin/blob/main/src/main/java/me/marin/statsplugin/util/VersionUtil.java">Marin's Stats plugin</a>
 */
public class VersionUtil {

    public static final Version CURRENT_VERSION = new Version(1, 0, 0);

    public static Version version(String version) {
        String[] parts = version.split("\\.");
        if (parts.length == 2) {
            return new Version(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        } else {
            return new Version(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        }
    }

    public static void deleteOldVersionJars() {
        List<Pair<Path, PluginManager.JinglePluginData>> plugins = Collections.emptyList();
        try {
            plugins = PluginManager.getFolderPlugins();
        } catch (IOException e) {
            Jingle.log(Level.ERROR, "(StatsPlugin) Failed to load plugins from folder:\n" + ExceptionUtil.toDetailedString(e));
        }

        // Mod ID -> path and data
        Map<String, Pair<Path, PluginManager.JinglePluginData>> bestPluginVersions = new HashMap<>();

        plugins.forEach(pair -> {
            PluginManager.JinglePluginData data = pair.getRight();
            if (!data.id.equals("jingle_program_launcher_plugin")) {
                return;
            }

            if (bestPluginVersions.containsKey(data.id)) {
                if (xyz.duncanruns.jingle.util.VersionUtil.tryCompare(data.version.split("\\+")[0], bestPluginVersions.get(data.id).getRight().version.split("\\+")[0], 0) > 0) {
                    Pair<Path, PluginManager.JinglePluginData> oldPlugin = bestPluginVersions.get(data.id);
                    deletePluginJar(oldPlugin.getLeft());
                    bestPluginVersions.put(data.id, pair);
                } else {
                    deletePluginJar(pair.getLeft());
                }
            } else {
                bestPluginVersions.put(data.id, pair);
            }
        });
    }

    private static void temp_forceDeleteBrokenJar(Path path) {
        Path javaExe = Paths.get(System.getProperty("java.home")).resolve("bin").resolve("javaw.exe");

        JingleAppLaunch.releaseLock();
        Jingle.options.save();

        // Use powershell's start-process to start it detached
        String command = String.format(
                "Start-Process powershell -ArgumentList \"Start-Sleep -Seconds 1; Remove-Item -Path '\"\"%s\"\"' -Force; Start-Process '\"\"%s\"\"' '-jar \"\"%s\"\"'\" -NoNewWindow",
                path,                          // Path to the file to delete
                javaExe,                       // Path to javaw.exe
                Jingle.getSourcePath()        // Path to the JAR file
        );

        Jingle.log(Level.INFO, "(StatsPlugin) Force deleting broken jar: " + command);

        try {
            PowerShellUtil.execute(command);
        } catch (PowerShellExecutionException | IOException e) {
            Jingle.log(Level.ERROR, ExceptionUtil.toDetailedString(e));
        }

        System.exit(0);
    }

    private static void deletePluginJar(Path path) {
        try {
            Files.delete(path);
        } catch (Exception e) {
            Jingle.log(Level.ERROR, "(StatsPlugin) Failed to delete " + path.getFileName() + " plugin:\n" + ExceptionUtil.toDetailedString(e));
        }
    }

    public static class Version implements Comparable<Version> {
        public final int major;
        public final int minor;
        public final int patch;

        public Version(int major, int minor) {
            this(major, minor, 0);
        }
        public Version(int major, int minor, Integer patch) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
        }

        @Override
        public String toString() {
            return major + "." + minor + "." + patch;
        }

        public boolean isOlderThan(Version version) {
            return this.compareTo(version) < 0;
        }

        @Override
        public int compareTo(@Nullable Version o) {
            if (o == null) return 1;
            if (o == this) return 0;
            if (this.major - o.major != 0) return this.major - o.major;
            if (this.minor - o.minor != 0) return this.minor - o.minor;

            return this.patch - o.patch;
        }
    }

}