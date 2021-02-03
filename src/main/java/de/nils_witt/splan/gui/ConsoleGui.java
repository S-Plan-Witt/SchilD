/*
 * Copyright (c) 2021.
 */

package de.nils_witt.splan.gui;

import de.nils_witt.splan.CliApplication;
import de.nils_witt.splan.connectors.LoggerConnector;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;

public class ConsoleGui extends Application {

    /**
     * JavaFX Starter
     */
    public static void launchGui() {
        launch();
    }

    /**
     * JavaFX stageStart
     *
     * @param primaryStage
     */
    @Override
    public void start(Stage primaryStage) {
        Logger logger = LoggerConnector.getLogger();

        Scene scene;
        TextArea area;
        TextField field;
        BorderPane border;

        try {

            field = new TextField();
            field.setEditable(false);

            area = new TextArea();
            area.setEditable(false);
            TextAreaAppender.setTextArea(area);

            border = new BorderPane();
            border.setCenter(area);
            border.setBottom(field);
            scene = new Scene(border);

            primaryStage.setScene(scene);
            primaryStage.setTitle("Console");
            primaryStage.sizeToScene();
            primaryStage.show();
            LoggerConnector.getLogger().fatal("Controller Done");
        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.info("Gui Init Done");
        CliApplication.main(new String[]{});
    }
}
