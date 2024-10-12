package me.ravalle.programlauncher;

import com.google.common.io.Resources;
import org.apache.logging.log4j.Level;
import xyz.duncanruns.jingle.Jingle;
import xyz.duncanruns.jingle.JingleAppLaunch;
import me.ravalle.programlauncher.gui.ProgramLauncherPanel;
import xyz.duncanruns.jingle.gui.JingleGUI;
import xyz.duncanruns.jingle.plugin.PluginManager;
import xyz.duncanruns.jingle.util.OpenUtil;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.regex.Pattern;

public class ProgramLauncher {
    public static final Path PROGRAM_LAUNCHER_FOLDER_PATH = Jingle.FOLDER.resolve("program-launcher-plugin");
    public static final Path PROGRAM_LAUNCHER_SETTINGS_PATH = PROGRAM_LAUNCHER_FOLDER_PATH.resolve("settings.json");

    private static long lastInstanceLaunchAttempt;

    public static void main(String[] args) throws IOException {
        // This is only used to test the plugin in the dev environment
        // ProgramLauncher.main itself is never used when users run Jingle

        JingleAppLaunch.launchWithDevPlugin(args, PluginManager.JinglePluginData.fromString(
                Resources.toString(Resources.getResource(ProgramLauncher.class, "/jingle.plugin.json"), Charset.defaultCharset())
        ), ProgramLauncher::initialize);
    }

    public static void initialize() {
        boolean isFirstLaunch = !PROGRAM_LAUNCHER_FOLDER_PATH.toFile().exists();
        if (isFirstLaunch) {
            if (!PROGRAM_LAUNCHER_FOLDER_PATH.toFile().mkdirs()) {
                Jingle.log(Level.ERROR, "(ProgramLauncherPlugin) Unable to create plugin folder! Plugin will terminate.");
                return;
            }
            // implement julti settings import?
        }

        lastInstanceLaunchAttempt = 0;
        ProgramLauncherSettings.load();

        JingleGUI.addPluginTab("Program Launching", new ProgramLauncherPanel().mainPanel);

        if (ProgramLauncherSettings.getInstance().launchOnStart) {
            launchNotOpenPrograms();
            if (ProgramLauncherSettings.getInstance().launchMC) {
                launchInstance(ProgramLauncherSettings.getInstance().dotMinecraftPath);
            }
        }
    }

    public static synchronized void launchNotOpenPrograms() {
        for (String prog : ProgramLauncherSettings.getInstance().launchProgramPaths) {
            boolean isOpen = false;
            try {
                Jingle.log(Level.DEBUG, "(ProgramLauncherPlugin) Searching running processes for " + prog);
                String[] cmd = { "cmd.exe", "/c", "wmic process where \"CommandLine like '%" + prog.replace("\\", "\\\\") + "%'\" get CommandLine /value" };
                Process process = Runtime.getRuntime().exec(cmd);

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(prog)) {
                        Jingle.log(Level.DEBUG, "(ProgramLauncherPlugin) Found " + prog + " running, will not launch");
                        isOpen = true;
                    }
                }

                if (!isOpen) {
                    Jingle.log(Level.DEBUG, "(ProgramLauncherPlugin) Could not find " + prog + " running, will launch");
                    OpenUtil.openFile(prog);
                }
            } catch (Exception e) {
                Jingle.log(Level.ERROR, Arrays.toString(e.getStackTrace()));
            }
        }
    }

    public static synchronized void launchInstance(String dotMinecraftPath) {
        if (Jingle.getMainInstance().isPresent() && Jingle.getMainInstance().get().instancePath.toString().equals(dotMinecraftPath)) {
            Jingle.log(Level.DEBUG, "(ProgramLauncherPlugin) Instance launch requested but instance is Jingle's main instance meaning it's already open, aborting!");
            return;
        }
        if (!isValidDotMinecraftPath(dotMinecraftPath)) {
            Jingle.log(Level.WARN, "(ProgramLauncherPlugin) Instance launch requested but .minecraft path is not valid, aborting!");
            return;
        }
        if (!new File(ProgramLauncherSettings.getInstance().launcherExecutable).exists()) {
            Jingle.log(Level.WARN, "(ProgramLauncherPlugin) Instance launch requested but launcher executable is not valid, aborting!");
            return;
        }
        if (System.currentTimeMillis() - lastInstanceLaunchAttempt < 30000) {
            Jingle.log(Level.DEBUG, "(ProgramLauncherPlugin) Instance launch requested but attempted within the last 30 seconds, aborting!");
            return;
        } else {
            lastInstanceLaunchAttempt = System.currentTimeMillis();
        }

        String[] parts = dotMinecraftPath.split("\\\\");
        String instanceName = parts[parts.length - 2];

        String[] cmd = {ProgramLauncherSettings.getInstance().launcherExecutable, "-l", instanceName};
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            Jingle.log(Level.ERROR, "(ProgramLauncherPlugin) Failed to launch instance " + instanceName + " at " + dotMinecraftPath + " with executable " + ProgramLauncherSettings.getInstance().launcherExecutable);
        }
    }

    public static String tryGetLauncherExecutable(String path) {
        try {
            if (path.toUpperCase().contains("MULTIMC") || path.toUpperCase().contains("MMC")) {
                File potentialMMC = new File(path + "\\..\\..\\..\\MultiMC.exe").getCanonicalFile();
                if (potentialMMC.exists()) {
                    return potentialMMC.toString();
                }
            } else {
                File potentialPrism = new File(System.getenv("LOCALAPPDATA") + "\\Programs\\PrismLauncher\\prismlauncher.exe").getCanonicalFile();
                if (potentialPrism.exists()) {
                    return ProgramLauncherSettings.getInstance().launcherExecutable = potentialPrism.toString();
                }
            }
        } catch (IOException ignored) { }
        return "not found";
    }

    public static boolean isValidDotMinecraftPath(String path) {
        Pattern p = Pattern.compile(".*:\\\\(MultiMC|PrismLauncher)\\\\instances\\\\.*\\\\.minecraft");
        return (
                path != null
                && !path.isEmpty()
                && new File(path).exists()
                && p.matcher(path).find()
                );
    }
}
