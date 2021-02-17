/*
 * Copyright (c) 2021.
 */

package de.nils_witt.splan.connectors;

import com.google.gson.Gson;
import de.nils_witt.splan.models.Config;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URISyntaxException;


/**
 * Config interaction
 */
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
        String line;
        StringBuilder sb;
        String fileAsString;

        try {
            InputStream is = new FileInputStream(FileSystemConnector.getWorkingDir() + "/data/config.json");
            BufferedReader buf = new BufferedReader(new InputStreamReader(is));

            line = buf.readLine();
            sb = new StringBuilder();

            while (line != null) {
                sb.append(line).append("\n");
                line = buf.readLine();
            }

            fileAsString = sb.toString();
            try {
                config = gson.fromJson(fileAsString, Config.class);
            } catch (Exception e) {
                logger.warn("Error while reading config: ", e);
            }

        } catch (FileNotFoundException e) {
            logger.warn("Config not present");
        } catch (Exception e) {
            logger.warn("Config open failed", e);
        }
        return config;
    }

    public static void copyDefaultConfig() throws IOException {
        InputStream in;
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
