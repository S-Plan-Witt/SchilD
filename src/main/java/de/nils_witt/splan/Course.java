
/*
 * Copyright (c) 2019.
 */

package de.nils_witt.splan;

public class Course {
    private String grade = "";
    private String subject = "";
    private String group = "";
    private boolean klausuren = false;


    public void setGrade(String grade) {
        this.grade = grade;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setKlausuren(boolean klausuren) {
        this.klausuren = klausuren;
    }
}
