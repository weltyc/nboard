package com.welty.nboard.nboard;

import com.orbanova.common.jsb.JsbFileChooser;
import com.orbanova.common.misc.Logger;
import com.orbanova.common.misc.Utils;
import com.welty.nboard.thor.DatabaseLoader;
import com.orbanova.common.misc.OperatingSystem;
import com.welty.novello.external.gui.ExternalEngineManager;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chris on 12/21/15.
 *
 * Installs the FFO database and ntest executable.
 */
public class Install {
    static final Logger log = Logger.logger(Install.class);

    public static void main(String[] args) {
        install();
    }

    /**
     * Install FFO database and ntest as an engine
     */
    static void install() {
        try {
            Path jarFolder = Utils.getJarPath(Install.class).getParent();
            log.info("Install jar folder: " + jarFolder);
            installNtest(jarFolder);
            installFfo(jarFolder);
        } catch (Exception e) {
            // error handling: don't install, but continue the rest of the program.
            log.info("Installation failed: " + e.getMessage());
            log.info("Stack trace:");
            for (StackTraceElement el : e.getStackTrace()) {
                log.info(el);
            }
        }

    }

    /**
     * Convert a String containing key-value pairs to a map
     *
     * @param string key-value pairs. The format is "key1:value1,key2:value2,...".
     *               Keys and values may not contain commas or colons. Beginning and ending whitespace
     *               is trimmed so you can add spaces around your commas.
     * @return map from keys to values
     */
    private static Map<String, String> stringToMap(String string) {
        final Map<String, String> map = new HashMap<>();

        for (String kv : string.split(",")) {
            String[] kvArray = kv.split(":");
            String key = kvArray[0].trim();
            String value = kvArray[1].trim();
            map.put(key, value);
        }
        return map;
    }

    /**
     * Add Ntest as an engine in the engine dialog.
     * <p>
     * This searches {Nboard home directory}/engines/ntest for the executable for the currently running os.
     * If it exists, and if ntest does not exist as an engine, it adds the executable.
     *
     * @param jarFolder path to the NBoard home directory.
     */
    private static void installNtest(Path jarFolder) {
        final String engineName = "NTest";
        final String engineDir = engineName.toLowerCase();
        final String ntestSource = "WINDOWS-x64:ntest.exe, WINDOWS-x86:ntest-x86.exe, LINUX:ntest, MACINTOSH:mNtest";

        ExternalEngineManager.Xei xei = ExternalEngineManager.instance.getXei(engineName);
        if (xei != null) {
            log.info(engineName + " executable already installed at " + xei.wd + "/" + xei.cmd);
            return;
        }

        Path ntestDir = jarFolder.resolve("engines").resolve(engineDir);
        String key = OperatingSystem.os.toString();
        if (OperatingSystem.os == OperatingSystem.WINDOWS) {
            final boolean is64bit = System.getenv("ProgramFiles(x86)") != null;
            key += is64bit ? "-x64" : "-x86";
        }
        String filename = stringToMap(ntestSource).get(key);
        if (filename == null) {
            log.info(engineName + " not available for operating system " + System.getProperty("os.name") + " with key " + key);
            return;
        }

        Path filePath = ntestDir.resolve(filename);
        log.info(filePath);
        if (!Files.exists(filePath)) {
            log.info(engineName + " executable can't be installed - does not exist at " + filePath);
            return;
        }

        // install it then!
        try {
            ExternalEngineManager.instance.add(engineName, ntestDir.toString(), filename);
            log.info("installed " + engineName + " at " + filePath);
        } catch (ExternalEngineManager.AddException e) {
            log.info("unable to install " + engineName + " at " + filePath + " -- " + e.getMessage());
        }
    }

    private static void installFfo(Path jarFolder) {
        JsbFileChooser chooser = new JsbFileChooser(null, DatabaseLoader.class);
        String existingDirectory = chooser.getDefaultSelection();
        if (existingDirectory == null) {
            Path path = jarFolder.resolve("db").resolve("ffo");
            chooser.setDefaultSelection(path.toString());
            log.info("installed FFO database at " + path);
        } else {
            log.info("database currently installed at " + existingDirectory);
        }
    }
}
