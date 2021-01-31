/*
 * Copyright (c) 2021.
 */

package de.nils_witt.splan.models;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Student {
    private String firstname = "";
    private String lastname = "";
    private Integer id = 0;
    private final ArrayList<Course> courses = new ArrayList<>();
    private String nmName = "";

    public void addCourse(Course course) {
        this.courses.add(course);
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public @NotNull ArrayList<Course> getCourses() {
        return courses;
    }

    public void getInfo() {
        System.out.println(id.toString().concat(":").concat(lastname).concat(", ").concat(firstname).concat(" Courses: " + courses.size()));
    }

    public String getNmName() {
        return nmName;
    }

    public void setNmName(String nmName) {
        this.nmName = nmName;
    }
}
