/*
 * Copyright (c) 2020.
 */

package de.nils_witt.splan;

import de.nils_witt.splan.gui.ConsoleGui;

public class Main {

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
