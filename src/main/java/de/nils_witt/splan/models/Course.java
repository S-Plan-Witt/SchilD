/*
 * Copyright (c) 2021.
 */

package de.nils_witt.splan.models;

public class Course {
    private String grade = "";
    private String subject = "";
    private String group = "";
    private boolean exams = false;

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean isExams() {
        return exams;
    }

    public void setExams(boolean exams) {
        this.exams = exams;
    }
}
