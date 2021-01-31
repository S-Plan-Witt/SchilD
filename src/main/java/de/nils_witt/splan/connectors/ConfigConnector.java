/*
 * Copyright (c) 2021.
 */

package de.nils_witt.splan.connectors;

import com.google.gson.Gson;
import de.nils_witt.splan.Main;
import de.nils_witt.splan.models.Config;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigConnector {

    /**
     * Config.json laden
     *
     * @param logger Filelogger
     * @return return config if successful loaded the file
     */
    public static @Nullable Config loadConfig(@NotNull Logger logger) {
        Gson gson = new Gson();
        Config config = null;
        try {
            //Laden der Datei und lesen aller Zeilen, die in einem String gespeichert werden, da json erwartet wird.
            InputStream is = new FileInputStream(FileSystemConnector.getWorkingDir() + "/data/config.json");
            BufferedReader buf = new BufferedReader(new InputStreamReader(is));

            String line = buf.readLine();
            StringBuilder sb = new StringBuilder();

            while (line != null) {
                sb.append(line).append("\n");
                line = buf.readLine();
            }

            String fileAsString = sb.toString();
            try {
                //String der Datei in in Config Objekt laden.
                config = gson.fromJson(fileAsString, Config.class);
                //Überprüfen ob die config gültig ist.
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error while reading config: ", e);
            }

        } catch (FileNotFoundException e) {
            logger.log(Level.WARNING, "Config not present");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Config open failed", e);
        }
        //Wenn die Konfig erfolgreich geladen und validiert wurde, wird diese zurückgegeben, sonst wir null.
        return config;
    }

    public static void copyDefaultConfig() throws IOException, URISyntaxException {
        InputStream in;
        JarURLConnection conn;
        JarFile jarfile;
        URL url;
        BufferedReader inputFileReader;
        File outputFileLocation;
        BufferedWriter outStream;
        String line;

        outputFileLocation = new File(FileSystemConnector.getWorkingDir() + "/data/config.json");


        in = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.json");
        inputFileReader = new BufferedReader(new InputStreamReader(in));

        outStream = new BufferedWriter(new FileWriter(outputFileLocation));
        while ((line = inputFileReader.readLine()) != null) {
            outStream.write(line);
            outStream.newLine();
        }
        outStream.close();
        in.close();
    }
}
