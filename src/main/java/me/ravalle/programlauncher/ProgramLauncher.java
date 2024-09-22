package me.ravalle.programlauncher;

import com.google.common.io.Resources;
import com.sun.jna.platform.win32.Shell32;
import org.apache.logging.log4j.Level;
import xyz.duncanruns.jingle.Jingle;
import xyz.duncanruns.jingle.JingleAppLaunch;
import me.ravalle.programlauncher.gui.ProgramLauncherPanel;
import xyz.duncanruns.jingle.gui.JingleGUI;
import xyz.duncanruns.jingle.plugin.PluginManager;
import xyz.duncanruns.jingle.util.OpenUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProgramLauncher {
    public static List<String> launchProgramPaths;

    public static void main(String[] args) throws IOException {
        // This is only used to test the plugin in the dev environment
        // ProgramLauncher.main itself is never used when users run Jingle

        JingleAppLaunch.launchWithDevPlugin(args, PluginManager.JinglePluginData.fromString(
                Resources.toString(Resources.getResource(ProgramLauncher.class, "/jingle.plugin.json"), Charset.defaultCharset())
        ), ProgramLauncher::initialize);
    }

    public static void initialize() {
        // This gets run once when Jingle launches

        JingleGUI.addPluginTab("Program Launching", new ProgramLauncherPanel().mainPanel);

        launchProgramPaths = new ArrayList<>();

        // remove this when you add GUI lol
        launchProgramPaths.add("E:\\Documents\\Speedrunning\\Ninjabrain-Bot-1.4.3.jar");
        launchProgramPaths.add("C:\\Program Files\\Chatterino7\\chatterino.exe");

        launchNotOpenPrograms();
    }

    public static void launchNotOpenPrograms() {
        for (String prog : launchProgramPaths) {
            boolean isOpen = false;
            try {
                Jingle.log(Level.DEBUG, "ProgramLauncher: Searching running processes for " + prog);
                String[] cmd = { "cmd.exe", "/c", "wmic process where \"CommandLine like '%" + prog.replace("\\", "\\\\") + "%'\" get CommandLine /value" };
                Process process = Runtime.getRuntime().exec(cmd);

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(prog)) {
                        Jingle.log(Level.DEBUG, "ProgramLauncher: Found " + prog + " running, will not launch");
                        isOpen = true;
                    }
                }

                if (!isOpen) {
                    Jingle.log(Level.DEBUG, "ProgramLauncher: Could not find " + prog + " running, will launch");
                    OpenUtil.openFile(prog);
                }
            } catch (Exception e) {
                Jingle.log(Level.ERROR, Arrays.toString(e.getStackTrace()));
            }
        }
    }
}
