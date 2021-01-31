/*
 * Copyright (c) 2021.
 */

package de.nils_witt.splan.gui;

import javafx.scene.control.TextArea;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class GuiLoggingHandler extends Handler {

    private final TextArea textArea;
    private final SimpleFormatter simpleFormatter = new SimpleFormatter();

    public GuiLoggingHandler(TextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void publish(LogRecord record) {
        if (isLoggable(record)) {
            synchronized (this.textArea) {
                //this.textArea.appendText(getFormatter().format(record));
                this.textArea.appendText(simpleFormatter.format(record));
            }
        }
    }

    @Override
    public void flush() {
        synchronized (this.textArea) {
            this.textArea.clear();
        }
    }

    @Override
    public void close() throws SecurityException {

    }
}
