/*
 * Copyright (c) 2021.
 */

package de.nils_witt.splan.connectors;

import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerConnector {

    /**
     * Erstellt einen Logger mit Ausgabe in eine Datei (Log.log) im Arbeitsverzeichnis
     *
     * @return logger for this program
     */
    public static Logger getLogger(){
        return Logger.getLogger("Logger");
    }

    public static void addHandler(Handler handler){
        getLogger().addHandler(handler);
    }

    public static void addJsonHandler(){
        FileHandler jsonHandler;

        try {
            jsonHandler = new FileHandler(FileSystemConnector.getWorkingDir() + "/data/logs/Log.log");
            addHandler(jsonHandler);
            SimpleFormatter formatter = new SimpleFormatter();
            jsonHandler.setFormatter(formatter);
            getLogger().info("Json logger attached");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
