/*
 * Copyright (c) 2021.
 */

package de.nils_witt.splan.models;

public class ApiSearchStudent {
    private String firstname;
    private String lastname;
    private String birthday;

    public ApiSearchStudent() {
    }

    public ApiSearchStudent(String firstname, String lastname) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.birthday = "*";
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
}
