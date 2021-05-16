/*
 * Copyright (c) 2021.
 */

package de.nils_witt.splan.models;

public class LdapStudent {
    private String lastName;
    private String firstName;
    private String username;

    public String getUsername() {
        return username;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
