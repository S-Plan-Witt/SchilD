/*
 * Copyright (c) 2020.
 */

package de.nils_witt.splan;

import de.nils_witt.splan.gui.ConsoleGui;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private final static Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        boolean gui = true;

        try {
            if(System.getenv("GUI").equals("false")){
                gui = false;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        if(gui){
            ConsoleGui.launchGui();
        }else {
            CliApplication.main(args);
        }

    }
}
