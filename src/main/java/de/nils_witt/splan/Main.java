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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main {

    public static void main(String[] args) {
        String path = null;
        Config configRead = null;
        ArrayList<Student> students;

        path = getJarPath();
        if (path == null) return;
        Logger logger = initLogger(path);
        if (logger == null) return;
        configRead = loadConfig(logger, path);
        if (configRead == null) return;

        final Config config = configRead;

        try {
            //Laden Schülerliste
            students = readXSLX(path.concat("/Students.xlsx"), logger);

            if (students != null) {
                students.forEach(student -> {
                    //Laden des Netman-Benutzernames für den Schüler
                    String aDUsername = fetchNMUsername(student, logger, config);
                    if (!aDUsername.equals("")) {
                        //Setzen der Kurse in der Api für den Schüler
                        uploadStudentCourses(student.getNmName(), student.getCourses(), logger, config);
                    }
                });
            } else {
                logger.info("No Students found");
            }
            logger.info("Done");

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * Ermitteln des Pfades zu "dieser" Datei und Rückgabe des Ordners in dem sich diese befindent
     *
     * @return path to jar parent folder
     */
    private static String getJarPath() {
        String path = null;

        try {
            //Ermitteln des Pfades zu dieser Klasse bzw zur Jar Datei
            File f = new File(System.getProperty("java.class.path"));
            //Ordnerpfad als String setzen
            File dir = f.getAbsoluteFile().getParentFile();
            path = dir.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return path;
    }

    /**
     * Erstellt einen Logger mit Ausgabe in eine Datei (Log.log) im Arbeitsverzeichnis
     *
     * @param path to this jar (or any working directory)
     * @return logger for this program
     */
    @Nullable
    private static Logger initLogger(@NotNull String path) {
        Logger logger = Logger.getLogger("TextLogger");
        FileHandler fh;

        try {
            //Setzen der Ausgabedatei
            fh = new FileHandler(path + "/Log.log");
            logger.addHandler(fh);
            //Einstellen der Formatierung des Logs
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return logger;
    }


    /**
     * Config.json laden
     *
     * @param logger Filelogger
     * @param path   to config.json
     * @return return config if successful loaded the file
     */
    private static Config loadConfig(@NotNull Logger logger, @NotNull String path) {
        Gson gson = new Gson();
        Config config = null;
        try {
            //Laden der Datei und lesen aller Zeilen, die in einem String gespeichert werden, da json erwartet wird.
            InputStream is = new FileInputStream(path + "/config.json");
            BufferedReader buf = new BufferedReader(new InputStreamReader(is));

            String line = buf.readLine();
            StringBuilder sb = new StringBuilder();

            while (line != null) {
                sb.append(line).append("\n");
                line = buf.readLine();
            }

            String fileAsString = sb.toString();
            try {
                //String der Datei in in Config Objekt laden.
                config = gson.fromJson(fileAsString, Config.class);
                //Überprüfen ob die config gültig ist.
                if (!verifyBearer(logger, config.getBearer(), config.getUrl())) {
                    //Falls nicht config null setzen.
                    config = null;
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error while reading config: ", e);
            }

        } catch (Exception e) {
            logger.log(Level.WARNING, "Config open failed", e);
        }
        //Wenn die Konfig erfolgreich geladen und validiert wurde, wird diese zurückgegeben, sonst wir null.
        return config;
    }

    /**
     * Überprüfen der Gültigkeit des Zugriffstoken auf die Api
     *
     * @param logger Filelogger
     * @param bearer token for api access
     * @param url    base api url
     * @return validity of bearer to given url
     */
    private static boolean verifyBearer(@NotNull Logger logger, @NotNull String bearer, @NotNull String url) {
        OkHttpClient client = new OkHttpClient();
        boolean isValid = false;
        Request request = new Request.Builder()
                .url(url.concat("/user"))
                .addHeader("Authorization", "Bearer ".concat(bearer))
                .build();
        try {
            Response response = client.newCall(request).execute();
            // Api gibt den status 200 zurück, wenn alles  gültig ist.
            if (response.code() == 200) {
                isValid = true;
                logger.info("Bearer valid");
            } else {
                logger.log(Level.WARNING, "Bearer invalid");
            }
        } catch (java.net.UnknownHostException e) {
            //URL der Api ist nicht gültig
            logger.log(Level.WARNING, "Host not found", e);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception while verifying Bearer", e);
        }

        return isValid;
    }


    /**
     * Laden der Schülerinformationen aus der XSLX in eine ArrayList
     * @param fileLocation location of the xslx file
     * @param logger Filelogger
     * @return arraylist of students
     * @throws IOException is thrown when supplied file path is not valid
     */
    @Nullable
    private static ArrayList<Student> readXSLX(@NotNull String fileLocation, @NotNull Logger logger) throws IOException {

        ArrayList<Student> students = new ArrayList<>();
        boolean isLK = false;
        Course course;
        Iterator rows;
        Iterator cells;
        Iterator secondCells;
        InputStream excelFileToRead = null;
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

        logger.info("Starting XSLX read");

        try {
            excelFileToRead = new FileInputStream(fileLocation);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error while opening File", e);
            return null;
        }

        //Öffnen der Datei
        logger.info("opening File");
        wb = new XSSFWorkbook(excelFileToRead);
        //Erstes Arbeitsblatt öffnen
        logger.info("opening Sheet");
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
                    logger.info("new Student: ".concat(lastname).concat(",".concat(firstname)));
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
                        boolean displayKlausuren = false;

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
                                                } else if (content.equals("GKS")) {
                                                    displayKlausuren = true;
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
                                        course.setKlausuren(true);
                                        logger.info("LK : ".concat(cell.getStringCellValue()));
                                        isLK = false;
                                    } else {
                                        course.setGroup(parts[1]);
                                        logger.info("Found Course: ".concat(cell.getStringCellValue()));
                                    }
                                    if (parts[0].equals("EK")) {
                                        parts[0] = "GO";
                                    }
                                    if (displayKlausuren) {
                                        course.setKlausuren(true);
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

    /**
     * Setzt die Kurse für den Schüler in der Api
     * @param username username of student
     * @param courses arraylist of courses from the student
     * @param logger Filelogger
     */
    private static void uploadStudentCourses(@NotNull String username, @NotNull ArrayList<Course> courses, @NotNull Logger logger, @NotNull Config config) {
        //Json Encoder/Decoder für die Api Kommunikation
        Gson gson = new Gson();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(40, TimeUnit.SECONDS)
                .build();
        //Erstellen der payload die an die API gesendet wird
        RequestBody body = RequestBody.create(JSON, gson.toJson(courses));

        //Erstellen der Anfrage an die API mit URL, payload und Authorisierung
        Request request = new Request.Builder()
                .url(config.getUrl().concat("/students/").concat(username).concat("/courses"))
                .addHeader("Authorization", "Bearer ".concat(config.getBearer()))
                .post(body)
                .build();
        try {
            //Anfrage an dir API senden
            Response response = client.newCall(request).execute();
            response.close();
            logger.info("Setted Courses successfully for ".concat(username));
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Error while setting Courses for ".concat(username));
        }
    }

    /**
     * Sucht auf dem AD Server nach dem Benutzernamen des Schülers
     * @param student student object containing first and lastname
     * @param logger Filelogger
     * @return ActiveDirectory username for student
     */
    private static String fetchNMUsername(@NotNull Student student, @NotNull Logger logger, @NotNull Config config) {
        //Json Encoder/Decoder für die Api Kommunikation
        Gson gson = new Gson();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(40, TimeUnit.SECONDS)
                .build();

        LdapStudent[] ldapStudents;
        RequestBody body;
        String json;
        Request request;
        Response response;

        //Erstellen der Payload
        json = "{\"lastname\":\"" + student.getLastname() + "\",\"firstname\":\"" + student.getFirstname() + "\"}";

        body = RequestBody.create(JSON, json);
        //Erstellen der Anfrage an die API
        request = new Request.Builder()
                .url(config.getUrl().concat("/students/find"))
                .addHeader("Authorization", "Bearer ".concat(config.getBearer()))
                .post(body)
                .build();
        try {
            logger.info("Searching LDAP User for: ".concat(student.getLastname()).concat(",").concat(student.getFirstname()));
            //Anfrage senden
            response = client.newCall(request).execute();
            try {
                //Antwort der API decoden
                ldapStudents = gson.fromJson(response.body().string(), LdapStudent[].class);
                //Normaler weise wird ein Benutzer oder keiner zurückgegeben
                //Länge = anzahl der Benutzer die gefunden werden
                if (ldapStudents.length == 1) {
                    //Auslesen des Benutzernames aus dem zurückgegeben Benutzer und speichern im Student
                    student.setNmName(ldapStudents[0].getsAMAccountName());
                    logger.info("found :".concat(student.getNmName()).concat(" for: ").concat(student.getLastname()).concat(",").concat(student.getFirstname()));
                } else if (ldapStudents.length == 0) {
                    logger.warning("no user found for: ".concat(student.getLastname()).concat(",").concat(student.getFirstname()));
                } else {
                    logger.warning("multiple users found for: ".concat(student.getLastname()).concat(",").concat(student.getFirstname()));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //Rückgabe des Benutzernames oder "", wenn kein Benutzer gefunden wurde
        return student.getNmName();
    }
}
