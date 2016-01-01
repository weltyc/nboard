package com.welty.nboard.nboard;

import com.orbanova.common.jsb.JsbFileChooser;
import com.welty.nboard.thor.DatabaseLoader;
import com.orbanova.common.misc.OperatingSystem;
import com.welty.othello.gui.ExternalEngineManager;

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
    public static void main(String[] args) {
        install();
    }

    /**
     * Install FFO database and ntest as an engine
     */
    static void install() {
        try {
            Path jarFolder = getJarFolder();
            System.out.println("Install jar folder: " + jarFolder);
            installNtest(jarFolder);
            installFfo(jarFolder);
        } catch (UnsupportedEncodingException e) {
            // error handling: don't install, but continue the rest of the program.
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
            System.out.println(engineName + " executable already installed at " + xei.wd + "/" + xei.cmd);
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
            System.out.println(engineName + " not available for operating system " + System.getProperty("os.name") + " with key " + key);
            return;
        }

        Path filePath = ntestDir.resolve(filename);
        System.out.println(filePath);
        if (!Files.exists(filePath)) {
            System.out.println(engineName + " executable can't be installed - does not exist at " + filePath);
            return;
        }

        // install it then!
        try {
            ExternalEngineManager.instance.add(engineName, ntestDir.toString(), filename);
            System.out.println("installed " + engineName + " at " + filePath);
        } catch (ExternalEngineManager.AddException e) {
            System.out.println("unable to install " + engineName + " at " + filePath + " -- " + e.getMessage());
        }
    }

    private static void installFfo(Path jarFolder) {
        JsbFileChooser chooser = new JsbFileChooser(null, DatabaseLoader.class);
        String existingDirectory = chooser.getDefaultDirectory();
        if (existingDirectory == null) {
            Path path = jarFolder.resolve("db").resolve("ffo");
            chooser.setDefaultDirectory(path.toString());
            System.out.println("installed FFO database at " + path);
        } else {
            System.out.println("database currently installed at " + existingDirectory);
        }
    }

    /**
     * Get the path of the jar file that contains this class
     * <p>
     * Behaviour is undefined if this class doesn't come from a jar file.
     *
     * @return path of the jar containing this class.
     * @throws UnsupportedEncodingException - shouldn't happen
     */
    private static Path getJarFolder() throws UnsupportedEncodingException {
        final String path = Install.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        final String decodedPath = URLDecoder.decode(path, "UTF-8");
        return Paths.get(decodedPath).getParent();
    }
}
