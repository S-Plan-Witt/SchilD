/*
 * Copyright (c) 2021.
 */

package de.nils_witt.splan.connectors;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Logger interaction
 */
public class LoggerConnector {

    /**
     *
     * @return application Logger
     */
    public static Logger getLogger() {
        return LogManager.getRootLogger();
    }

}
