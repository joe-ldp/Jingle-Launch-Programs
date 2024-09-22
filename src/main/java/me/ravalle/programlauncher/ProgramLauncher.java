package me.ravalle.programlauncher;

import com.google.common.io.Resources;
import org.apache.logging.log4j.Level;
import xyz.duncanruns.jingle.Jingle;
import xyz.duncanruns.jingle.JingleAppLaunch;
import me.ravalle.programlauncher.gui.ProgramLauncherPanel;
import xyz.duncanruns.jingle.gui.JingleGUI;
import xyz.duncanruns.jingle.plugin.PluginManager;

import java.io.IOException;
import java.nio.charset.Charset;

public class ProgramLauncher {
    public static void main(String[] args) throws IOException {
        // This is only used to test the plugin in the dev environment
        // ProgramLauncher.main itself is never used when users run Jingle

        JingleAppLaunch.launchWithDevPlugin(args, PluginManager.JinglePluginData.fromString(
                Resources.toString(Resources.getResource(ProgramLauncher.class, "/jingle.plugin.json"), Charset.defaultCharset())
        ), ProgramLauncher::initialize);
    }

    public static void initialize() {
        // This gets run once when Jingle launches





    }
}
