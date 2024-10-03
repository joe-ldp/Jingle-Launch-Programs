package me.ravalle.programlauncher.util;

import com.github.tuupertunut.powershelllibjava.PowerShellExecutionException;
import com.google.gson.JsonObject;
import me.ravalle.programlauncher.ProgramLauncher;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Level;
import xyz.duncanruns.jingle.Jingle;
import xyz.duncanruns.jingle.JingleAppLaunch;
import xyz.duncanruns.jingle.util.ExceptionUtil;
import xyz.duncanruns.jingle.util.GrabUtil;
import xyz.duncanruns.jingle.util.PowerShellUtil;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Majority of the code from <a href="https://github.com/marin774/Jingle-Stats-Plugin/blob/main/src/main/java/me/marin/statsplugin/util/UpdateUtil.java">Marin's Stats plugin</a>
 */
public class UpdateUtil {

    public static void checkForUpdatesAndUpdate(boolean isOnLaunch) {
        new Thread(() -> {
            UpdateInfo updateInfo = UpdateUtil.tryCheckForUpdates();
            if (updateInfo.isSuccess()) {
                int choice = JOptionPane.showConfirmDialog(null, updateInfo.getMessage(), "Update found!", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) {
                    UpdateUtil.tryUpdateAndRelaunch(updateInfo.getDownloadURL());
                }
            } else {
                if (!isOnLaunch) {
                    JOptionPane.showMessageDialog(null, updateInfo.getMessage());
                }
            }
        }, "update-checker").start();
    }

    public static UpdateInfo tryCheckForUpdates() {
        try {
            return checkForUpdates();
        } catch (Exception e) {
            return new UpdateInfo(false, "Could not check for updates. Github might be rate-limiting you, try again later.", null);
        }
    }

    public synchronized static UpdateInfo checkForUpdates() throws IOException {
        JsonObject meta = GrabUtil.grabJson("https://raw.githubusercontent.com/joe-ldp/Jingle-Launch-Programs/main/meta.json");

        Jingle.log(Level.DEBUG, "(ProgramLauncherPlugin) Grabbed Stats meta: " + meta.toString());

        VersionUtil.Version latestVersion = VersionUtil.version(meta.get("latest").getAsString());
        String downloadURL = meta.get("latest_download").getAsString();
        boolean isOutdated = VersionUtil.CURRENT_VERSION.isOlderThan(latestVersion);

        if (isOutdated) {
            return new UpdateInfo(true, "New Program Launcher Plugin version found: v" + latestVersion + "! Update now?", downloadURL);
        } else {
            return new UpdateInfo(false, "No new versions found.", null);
        }
    }

    public static class UpdateInfo {
        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getDownloadURL() {
            return downloadURL;
        }

        private final boolean success;
        private final String message;
        private final String downloadURL;

        public UpdateInfo(boolean success, String message, String downloadURL) {
            this.success = success;
            this.message = message;
            this.downloadURL = downloadURL;
        }
    }

    public static void tryUpdateAndRelaunch(String download) {
        try {
            updateAndRelaunch(download);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Unknown error while updating. Try again or update manually.");
            Jingle.log(Level.ERROR, "(ProgramLauncherPlugin) Unknown error while updating:\n" + ExceptionUtil.toDetailedString(e));
        }
    }

    private static void updateAndRelaunch(String download) throws IOException, PowerShellExecutionException, InterruptedException, InvocationTargetException {
        Path newJarPath = ProgramLauncher.PLUGINS_PATH.resolve(URLDecoder.decode(FilenameUtils.getName(download), StandardCharsets.UTF_8.name()));

        if (!Files.exists(newJarPath)) {
            Jingle.log(Level.DEBUG, "(ProgramLauncherPlugin) Downloading new jar to " + newJarPath);
            download(download, newJarPath);
            Jingle.log(Level.DEBUG, "(ProgramLauncherPlugin) Downloaded new jar " + newJarPath.getFileName());
        }

        Path javaExe = Paths.get(System.getProperty("java.home")).resolve("bin").resolve("javaw.exe");

        // Release LOCK so updating can go smoothly
        JingleAppLaunch.releaseLock();
        Jingle.options.save();

        // Use powershell's start-process to start it detached
        String powerCommand = String.format("start-process '%s' '-jar \"%s\"'", javaExe, Jingle.getSourcePath());
        Jingle.log(Level.INFO, "(ProgramLauncherPlugin) Exiting and running powershell command: " + powerCommand);
        PowerShellUtil.execute(powerCommand);

        System.exit(0);
    }

    private static void download(String download, Path newJarPath) throws IOException {
        GrabUtil.download(download, newJarPath);
    }
}