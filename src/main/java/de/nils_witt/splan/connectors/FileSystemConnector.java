/*
 * Copyright (c) 2021.
 */

package de.nils_witt.splan.connectors;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

/**
 * Filesystem Interaction
 */
public class FileSystemConnector {

    /**
     * Copy a file from source to destination.
     *
     * @param source      the source
     * @param destination the destination
     * @return True if succeeded , False if not
     */
    public static boolean copy(InputStream source, String destination) {
        boolean succeess = true;

        System.out.println("Copying ->" + source + "\n\tto ->" + destination);

        try {
            Files.copy(source, Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            //LoggerConnector.getLogger().log(Level.WARNING, "", ex);
            succeess = false;
        }

        return succeess;
    }

    /**
     * @return application run path
     */
    public static Path getWorkingDir() {
        return Paths.get("").toAbsolutePath();
    }

    /**
     * Creates all dirs for the application runtime
     */
    public static void createDataDirs() {
        File dataDir = new File(getWorkingDir() + "/data/");
        dataDir.mkdir();
        File logDir = new File(getWorkingDir() + "/data/logs");
        logDir.mkdir();
    }
}
