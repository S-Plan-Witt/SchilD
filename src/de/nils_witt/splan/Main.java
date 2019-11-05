

/*
 * Copyright (c) 2019.
 */

package de.nils_witt.splan;

import com.google.gson.Gson;
import okhttp3.*;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class Main {

    private static final String bearer = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6IndpdHRuaWwxNjExIiwic2Vzc2lvbiI6IjA1OWk4dDBseGk5c3RjMGpreHVidmUiLCJpYXQiOjE1NzI1OTQyOTB9.dEZ9HmEEWEVrWhi2hhh05X5sei5YSnCpuWCDvWX1TxtJrTTPEr1sSYup4djv89cRqX9bZ-tCC1yUP1iq-1ySjP6Aml8EjgnveR8zwXngPC3v85q6mLXf0jR9qVYr4xSGO16h71RzrYa6rAu4IZEqpVEvlOfw6G1BMm6lxpEaB23ZL--LqwOwDha5BjcPXK2OyrNsgADSmRMn-cobIyLh6ab7O5DrdJpxCsPKKGrPEQLtPv6CNiSImM7_dN7VIYqTdPVfFcC0vxxkL3ge2rbN8AmCXn3q7ZgYpYZdV5YTDPrLZE7WJyT07m1UWUPR1XX9RMtgADrlPSKf_KWLdyZZkcTcjpholeaUWT9KSt6x3VdQ3qQPZkBQd3zcLK9VskcFaxB4sCqFxPq-TGOEIpybbca2ioOm8GG6207b2EyQW__B201VxDFQ5X0Xj0_4W6dKg6fwbaG-qehZZIv-zeZ1C-DfY7XqPqd7sooWsfepOo6lj5I1Z_RnCb3txZVxtPC6Ye3TssvOKKML2luUJmIdN5MXAby-IkwMrdCVfESMMlPM3uGuo07o61M89GWWn_GRVkzBQiqEhildRvRk6jDLEjkcq7PflQSIvJyHM0MDiZCd4V2eOah6gqhbHonuLvdFZzzxYJTiIkTWK8WirIXLnZTmihcb3fXWn8iS21M41D4";
    private static final String url = "https://api.nils-witt.codes";

    public static void main(String[] args) {
        ArrayList<Student> students;
        try {
            //Getting List of Students from Xslx
            students = readXSLX();


            students.forEach(student -> {
                //Getting AD Username
                String aDUsername = fetchNMUsername(student);
                if (!aDUsername.equals("")) {
                    uploadStudentCourses(student.getNmName(), student.getCourses());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static ArrayList<Student> readXSLX() throws Exception {

        ArrayList<Student> students = new ArrayList<>();
        Boolean isLK = false;
        Course course;
        Gson gson = new Gson();
        Iterator rows;
        Iterator cells;
        Iterator secondCells;
        InputStream ExcelFileToRead = new FileInputStream("/Users/nilswitt/Downloads/Students.xlsx");
        String lastname;
        String firstname;
        String grade = "";
        String[] parts;
        Student student;
        //Excel Datei/ Workbook
        XSSFWorkbook wb;
        //Arbeitsblatt
        XSSFSheet sheet;
        //Zeilen auf dem sheet
        XSSFRow row;
        XSSFRow secondRow;
        //Zellen in einer Row
        XSSFCell cell;
        XSSFCell nameCell;
        XSSFCell secondCell = null;

        //Öffnen der Datei
        wb = new XSSFWorkbook(ExcelFileToRead);
        //Erstes Arbeitsblatt öffnen
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

                    //Öffnen des Zellen-Zeigers in der Aktuellen Spalte
                    cells = row.cellIterator();
                    secondCells = secondRow.cellIterator();
                    //Springen zur jeweils ersten Spalte mit einem Kurs
                    cells.next();
                    cells.next();
                    secondCells.next();

                    while (cells.hasNext()) {
                        cell = (XSSFCell) cells.next();
                        if (secondCells.hasNext()) {
                            secondCell = (XSSFCell) secondCells.next();
                        }

                        if (cell.getCellType() == CellType.STRING) {
                            //Falls Zelle leer ist überspringen/ nur leerzeichen enthält
                            if (!cell.getStringCellValue().equals(" ")) {
                                //Wenn die unterhalb liegende Zelle einen Wert enthält überprüfen, ob es sich um einen LK handelt
                                if (secondCell != null) {
                                    if (secondCell.getCellType() == CellType.STRING) {
                                        try {
                                            String content = secondCell.getStringCellValue();
                                            if (!content.equals("")) {
                                                if (content.substring(0, 2).equals("LK")) {
                                                    isLK = true;
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
                                        course.setCourseNumber("L" + parts[1]);
                                        isLK = false;
                                    } else {
                                        course.setCourseNumber(parts[1]);
                                    }

                                    course.setSubject(parts[0]);
                                    //Kurs dem Schüler hinzufügen
                                    student.addCourse(course);
                                }

                            }
                        }
                    }
                    //Schuüler der Schülerliste hinzufügen
                    students.add(student);
                }
            }
        }

        return students;
    }

    private static void uploadStudentCourses(String username, ArrayList<Course> courses) {
        Gson gson = new Gson();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(JSON, gson.toJson(courses));
        Request request = new Request.Builder()
                .url(url.concat("/users/students/").concat(username).concat("/setCourses"))
                .addHeader("Authorization", "Bearer ".concat(bearer))
                .post(body)
                .build();
        try {
            Response response = client.newCall(request).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String fetchNMUsername(Student student) {
        Gson gson = new Gson();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();

        LdapStudent[] ldapStudents;
        RequestBody body;
        String json;
        Request request;
        Response response;

        json = "{\"lastname\":\"" + student.getLastname() + "\",\"firstname\":\"" + student.getFirstname() + "\"}";

        body = RequestBody.create(JSON, json);
        request = new Request.Builder()
                .url(url.concat("/users/find"))
                .addHeader("Authorization", "Bearer ".concat(bearer))
                .post(body)
                .build();
        try {
            response = client.newCall(request).execute();
            try {
                ldapStudents = gson.fromJson(response.body().string(), LdapStudent[].class);
                if (ldapStudents.length == 1) {
                    student.setNmName(ldapStudents[0].getsAMAccountName());
                    System.out.println("Found: ".concat(student.getLastname()).concat(": ").concat(student.getNmName()));
                } else if (ldapStudents.length == 0) {
                    System.out.println("no user found for:" + student.getLastname());
                } else {
                    System.out.println("Multiple Students found :" + student.getLastname());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return student.getNmName();
    }
}
