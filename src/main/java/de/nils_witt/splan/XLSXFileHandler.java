/*
 * Copyright (c) 2021.
 */

package de.nils_witt.splan;

import de.nils_witt.splan.connectors.Api;
import de.nils_witt.splan.models.Course;
import de.nils_witt.splan.models.Student;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class XLSXFileHandler {
    private final ArrayList<Student> students;
    private final String filePath;
    private final Logger logger;
    private final Api api;

    public XLSXFileHandler(String filePath, Logger logger, Api api) {
        this.filePath = filePath;
        this.logger = logger;
        this.api = api;
        this.students = new ArrayList<>();
    }

    public void processFile() {

        try {
            //Laden Schülerliste
            read();

            this.students.forEach(student -> {
                //Laden des Netman-Benutzernames für den Schüler
                String aDUsername = api.fetchNMUsername(student);
                if (!aDUsername.equals("")) {
                    //Setzen der Kurse in der Api für den Schüler
                    api.uploadStudentCourses(student.getNmName(), student.getCourses());
                }
            });
            this.logger.info("Done");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Laden der Schülerinformationen aus der XSLX in eine ArrayList
     *
     * @throws IOException is thrown when supplied file path is not valid
     */
    private void read() throws IOException {

        boolean isLK = false;
        Course course;
        Iterator rows;
        Iterator cells;
        Iterator secondCells;
        InputStream excelFileToRead;
        String lastname;
        String firstname;
        String grade = "";
        String[] parts;
        Student student;
        XSSFWorkbook wb;
        XSSFSheet sheet;
        XSSFRow row;
        XSSFRow secondRow;
        XSSFCell cell;
        XSSFCell nameCell;
        XSSFCell secondCell;

        this.logger.info("Starting XSLX read");

        try {
            excelFileToRead = new FileInputStream(this.filePath);
        } catch (Exception e) {
            this.logger.warn("Error while opening File", e);
            return;
        }

        //Öffnen der Datei
        this.logger.info("opening File");
        wb = new XSSFWorkbook(excelFileToRead);
        //Erstes Arbeitsblatt öffnen
        this.logger.info("opening Sheet");
        sheet = wb.getSheetAt(0);
        //Zeilen-Zeiger öffnen
        rows = sheet.rowIterator();

        //Öffnen der dritten Zeile und der B Spalte zum auslesen der Stufe
        if (sheet.getRow(2) != null) {
            cell = sheet.getRow(2).getCell(1);
            if (cell != null) {
                if (cell.getCellType() == CellType.STRING) {
                    grade = cell.getStringCellValue();
                }
            }
        }
        logger.info("Found Grade: ".concat(grade));

        //Solage durch die Zeilen gehen bis es keine definiten mehr gibt
        while (rows.hasNext()) {
            //Öffnen der Zeile
            row = (XSSFRow) rows.next();
            //A und B Zelle öffnen zum überprüfen, ob es eine Schülerzeile ist
            cell = row.getCell(0);
            nameCell = row.getCell(1);
            if (cell != null && nameCell != null && rows.hasNext()) {

                if (cell.getCellType() == CellType.NUMERIC && nameCell.getCellType() == CellType.STRING) {
                    //Laden der nächsten Reihe, da diese LK, schriftlich oder mündlich enthält
                    secondRow = (XSSFRow) rows.next();

                    student = new Student();
                    //Nachnamen auslesen und Komma am Ende abtrennen
                    lastname = nameCell.getStringCellValue().substring(0, nameCell.getStringCellValue().length() - 1);
                    //Leerzeichen am Ende des Nachnames entfernen
                    while (lastname.substring((lastname.length() - 1)).equals(" ")) {
                        lastname = lastname.substring(0, (lastname.length() - 1));
                    }
                    student.setLastname(lastname);

                    //Vornamen auslesen
                    firstname = secondRow.getCell(0).getStringCellValue();
                    //Leerzeichen am Ende des Vornames entfernen
                    while (firstname.substring((firstname.length() - 1)).equals(" ")) {
                        firstname = firstname.substring(0, (firstname.length() - 1));
                    }

                    student.setFirstname(firstname);
                    this.logger.info("new Student: ".concat(lastname).concat(",".concat(firstname)));
                    //Öffnen des Zellen-Zeigers in der Aktuellen Spalte
                    cells = row.cellIterator();
                    secondCells = secondRow.cellIterator();
                    //Springen zur jeweils ersten Spalte mit einem Kurs
                    cells.next();
                    cells.next();
                    secondCells.next();

                    while (cells.hasNext()) {
                        cell = (XSSFCell) cells.next();
                        secondCell = secondRow.getCell(cell.getColumnIndex());

                        boolean displayExams = false;

                        if (cell.getCellType() == CellType.STRING) {
                            //Falls Zelle leer ist überspringen/ nur leerzeichen enthält
                            if (!cell.getStringCellValue().equals(" ")) {
                                //Wenn die unterhalb liegende Zelle einen Wert enthält überprüfen, ob es sich um einen LK handelt
                                if (secondCell != null) {
                                    if (secondCell.getCellType() == CellType.STRING) {
                                        try {
                                            String content = secondCell.getStringCellValue();
                                            if (!content.equals("")) {
                                                if (content.startsWith("LK")) {
                                                    isLK = true;
                                                } else if (content.equals("GKS")) {
                                                    displayExams = true;
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                course = new Course();
                                course.setGrade(grade);
                                parts = cell.getStringCellValue().split(" ");
                                //Überprüfen, ob der Kursname aus Fach und Nummer besteht
                                if (parts.length == 2) {
                                    if (isLK) {
                                        course.setGroup("L" + parts[1]);
                                        course.setExams(true);
                                        this.logger.info("LK : ".concat(cell.getStringCellValue()));
                                        isLK = false;
                                    } else {
                                        course.setGroup(parts[1]);
                                        this.logger.info("Found Course: ".concat(cell.getStringCellValue()));
                                    }
                                    switch (parts[0]) {
                                        case "IV" -> parts[0] = "VOKU";
                                        case "E-PK" -> {
                                            parts[0] = "E";
                                            course.setGroup("PK");
                                        }
                                    }
                                    if (displayExams) {
                                        course.setExams(true);
                                    }
                                    course.setSubject(parts[0]);
                                    student.addCourse(course);
                                }
                            }
                        }
                    }
                    this.students.add(student);
                }
            }
        }
    }
}
