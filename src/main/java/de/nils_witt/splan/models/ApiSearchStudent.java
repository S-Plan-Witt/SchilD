/*
 * Copyright (c) 2021.
 */

package de.nils_witt.splan.models;

public class ApiSearchStudent {
    private final String firstname;
    private final String lastname;
    private final String birthday;

    public ApiSearchStudent(String firstname, String lastname) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.birthday = "*";
    }

}
