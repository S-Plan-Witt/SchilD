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
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Creates connectivity to the backend
 */
public class Api {
    private final Logger logger = LoggerConnector.getLogger();
    private final Config config;
    private final Gson gson = new Gson();
    private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     * @param config active config
     */
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
        OkHttpClient client;
        boolean isValid;
        Request request;
        Response response;

        client = new OkHttpClient();
        isValid = false;
        request = new Request.Builder()
                .url(url.concat("/user"))
                .addHeader("Authorization", "Bearer ".concat(bearer))
                .build();
        try {
            response = client.newCall(request).execute();
            //200 OK == Success code api
            if (response.code() == 200) {
                isValid = true;
                logger.info("Bearer valid");
            } else {
                logger.warn("Bearer invalid");
            }
        } catch (java.net.UnknownHostException e) {
            logger.warn("Host not found");
        } catch (Exception e) {
            logger.warn("Exception while verifying Bearer");
        }

        return isValid;
    }

    /**
     * Sucht auf dem AD Server nach dem Benutzernamen des Schülers
     *
     * @param student student object containing first and lastname
     * @return ActiveDirectory username for student
     */
    public String fetchNMUsername(@NotNull Student student) {
        OkHttpClient client;
        LdapStudent[] ldapStudents;
        RequestBody body;
        String json;
        Request request;
        Response response;

        //TODO create Payload Object
        json = "{\"lastname\":\"" + student.getLastname() + "\",\"firstname\":\"" + student.getFirstname() + "\"}";

        body = RequestBody.create(JSON, json);

        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(40, TimeUnit.SECONDS)
                .build();

        request = new Request.Builder()
                .url(this.config.getUrl().concat("/users/ldap/find"))
                .addHeader("Authorization", "Bearer ".concat(this.config.getBearer()))
                .post(body)
                .build();
        try {
            this.logger.info("Searching LDAP User for: ".concat(student.getLastname()).concat(",").concat(student.getFirstname()));
            response = client.newCall(request).execute();

            if (response.body() != null) {
                ldapStudents = gson.fromJson(Objects.requireNonNull(response.body()).string(), LdapStudent[].class);

                if (ldapStudents.length == 1) {
                    student.setNmName(ldapStudents[0].getUsername());
                    this.logger.info("found: ".concat(student.getNmName()).concat(" for: ").concat(student.getLastname()).concat(",").concat(student.getFirstname()));
                } else if (ldapStudents.length == 0) {
                    this.logger.warn("no user found for: ".concat(student.getLastname()).concat(",").concat(student.getFirstname()));
                } else {
                    this.logger.warn("multiple users found for: ".concat(student.getLastname()).concat(",").concat(student.getFirstname()));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return student.getNmName();
    }

    /**
     * Setzt die Kurse für den Schüler in der Api
     *
     * @param username username of student
     * @param courses  arraylist of courses from the student
     */
    public void uploadStudentCourses(@NotNull String username, @NotNull ArrayList<Course> courses) {
        OkHttpClient client;
        RequestBody body;
        Request request;
        Response response;

        body = RequestBody.create(JSON, gson.toJson(courses));

        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(40, TimeUnit.SECONDS)
                .build();

        request = new Request.Builder()
                .url(this.config.getUrl().concat("/users/").concat(username).concat("/courses"))
                .addHeader("Authorization", "Bearer ".concat(this.config.getBearer()))
                .post(body)
                .build();
        try {
            response = client.newCall(request).execute();
            response.close();
            this.logger.info("Setted Courses successfully for ".concat(username));
        } catch (Exception e) {
            e.printStackTrace();
            this.logger.info("Error while setting Courses for ".concat(username));
        }
    }
}
