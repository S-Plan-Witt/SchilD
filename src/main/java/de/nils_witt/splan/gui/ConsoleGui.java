/*
 * Copyright (c) 2021.
 */

package de.nils_witt.splan.gui;

import de.nils_witt.splan.XLSXFileHandler;
import de.nils_witt.splan.connectors.Api;
import de.nils_witt.splan.connectors.ConfigConnector;
import de.nils_witt.splan.connectors.FileSystemConnector;
import de.nils_witt.splan.connectors.LoggerConnector;
import de.nils_witt.splan.models.Config;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.util.logging.Logger;

public class ConsoleGui extends Application {

    /**
     * JavaFX stageStart
     *
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        Logger logger = LoggerConnector.getLogger();
        AnchorPane rootLayout;
        try{
            System.out.println(Thread.currentThread().getContextClassLoader().getResource("config.json"));
            FXMLLoader loader = new FXMLLoader(Thread.currentThread().getContextClassLoader().getResource("fxml/mainview.fxml"));
            rootLayout = (AnchorPane) loader.load();
            Controller controller = loader.getController();

            primaryStage.setScene(new Scene(rootLayout));
            primaryStage.setTitle("Console");
            primaryStage.show();

        }catch (Exception e){
            e.printStackTrace();
        }

        logger.info("Gui Init Done");
        initApplication();
    }

    public static void launchGui() {
        launch();
    }


    private void initApplication() {
        Config config;
        XLSXFileHandler xlsxFileHandler;
        Api api;

        FileSystemConnector.createDatadirs();
        Logger logger = LoggerConnector.getLogger();
        LoggerConnector.addJsonHandler();
        if (logger == null) return;

        config = ConfigConnector.loadConfig(logger);
        if (config == null) {
            try {
                ConfigConnector.copyDefaultConfig();
                logger.info("Created default config.json");
            } catch (Exception e) {
                e.printStackTrace();
                logger.info("Failed to create default config.json");
            }
            return;
        }

        if (!Api.verifyBearer(logger, config.getBearer(), config.getUrl())) {
            //Falls nicht config null setzen.
            logger.warning("Api token invalid");

            return;
        }

        api = new Api(config);

        xlsxFileHandler = new XLSXFileHandler(FileSystemConnector.getWorkingDir().concat("/Students.xlsx"), logger, api);

        logger.info("Init Complete");
        //xlsxFileHandler.processFile();
    }
}
