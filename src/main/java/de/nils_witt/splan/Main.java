/*
 * Copyright (c) 2020.
 */

package de.nils_witt.splan;

import de.nils_witt.splan.connectors.FileSystemConnector;
import de.nils_witt.splan.connectors.LoggerConnector;
import de.nils_witt.splan.gui.ConsoleGui;
import de.nils_witt.splan.connectors.ConfigConnector;
import de.nils_witt.splan.models.Config;
import de.nils_witt.splan.models.Course;
import de.nils_witt.splan.models.Student;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args){
        ConsoleGui.launchGui();
    }
}
