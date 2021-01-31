/*
 * Copyright (c) 2021.
 */

package de.nils_witt.splan.connectors;

import com.google.gson.Gson;
import de.nils_witt.splan.models.Config;
import de.nils_witt.splan.models.Course;
import de.nils_witt.splan.models.LdapStudent;
import de.nils_witt.splan.models.Student;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Api {
    private Config config;
    private final Logger logger = LoggerConnector.getLogger();

    public Api(Config config) {
        this.config = config;
    }

    /**
     * Überprüfen der Gültigkeit des Zugriffstoken auf die Api
     *
     * @param logger Filelogger
     * @param bearer token for api access
     * @param url    base api url
     * @return validity of bearer to given url
     */
    public static boolean verifyBearer(@NotNull Logger logger, @NotNull String bearer, @NotNull String url) {
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
            logger.log(Level.WARNING, "Host not found");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception while verifying Bearer");
        }

        return isValid;
    }

    /**
     * Sucht auf dem AD Server nach dem Benutzernamen des Schülers
     * @param student student object containing first and lastname
     * @return ActiveDirectory username for student
     */
    public String fetchNMUsername(@NotNull Student student) {
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
                .url(this.config.getUrl().concat("/users/ldap/find"))
                .addHeader("Authorization", "Bearer ".concat(this.config.getBearer()))
                .post(body)
                .build();
        try {
            this.logger.info("Searching LDAP User for: ".concat(student.getLastname()).concat(",").concat(student.getFirstname()));
            //Anfrage senden
            response = client.newCall(request).execute();


            if(response.body() != null){
                //Rückgabe der Api auswerten
                ldapStudents = gson.fromJson(response.body().string(), LdapStudent[].class);
                //Normaler weise wird ein Benutzer oder keiner zurückgegeben
                //Länge = Anzahl der Benutzer die gefunden werden
                if (ldapStudents.length == 1) {
                    //Auslesen des Benutzernames aus dem zurückgegeben Benutzer und speichern im Student
                    student.setNmName(ldapStudents[0].getUsername());
                    this.logger.info("found: ".concat(student.getNmName()).concat(" for: ").concat(student.getLastname()).concat(",").concat(student.getFirstname()));
                } else if (ldapStudents.length == 0) {
                    this.logger.warning("no user found for: ".concat(student.getLastname()).concat(",").concat(student.getFirstname()));
                } else {
                    this.logger.warning("multiple users found for: ".concat(student.getLastname()).concat(",").concat(student.getFirstname()));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //Rückgabe des Benutzernames oder "", wenn kein Benutzer gefunden wurde
        return student.getNmName();
    }

    /**
     * Setzt die Kurse für den Schüler in der Api
     * @param username username of student
     * @param courses arraylist of courses from the student
     */
    public void uploadStudentCourses(@NotNull String username, @NotNull ArrayList<Course> courses) {
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
                .url(this.config.getUrl().concat("/users/").concat(username).concat("/courses"))
                .addHeader("Authorization", "Bearer ".concat(this.config.getBearer()))
                .post(body)
                .build();
        try {
            //Anfrage an dir API senden
            Response response = client.newCall(request).execute();
            response.close();
            this.logger.info("Setted Courses successfully for ".concat(username));
        } catch (Exception e) {
            e.printStackTrace();
            this.logger.info("Error while setting Courses for ".concat(username));
        }
    }
}
