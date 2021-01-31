/*
 * Copyright (c) 2021.
 */

package de.nils_witt.splan.gui;

import de.nils_witt.splan.connectors.LoggerConnector;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class Controller {
    @FXML
    private TextArea textArea;


    @FXML
    private void initialize(){
        textArea.setEditable(false);

        GuiLoggingHandler guiLoggingHandler = new GuiLoggingHandler(textArea);
        LoggerConnector.addHandler(guiLoggingHandler);
        LoggerConnector.getLogger().info("Controller DOne");
    }

}
