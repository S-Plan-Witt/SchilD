package de.nils_witt.splan;

import java.util.ArrayList;

public class Student {
    private String firstname = "";
    private String lastname = "";
    private Integer id = 0;
    private ArrayList<Course> courses = new ArrayList<>();
    private String nmName = "";

    public void setId(Integer id) {
        this.id = id;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void addCourse(Course course){
        this.courses.add(course);
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public Integer getId() {
        return id;
    }

    public ArrayList<Course> getCourses() {
        return courses;
    }

    public void getInfo(){
        System.out.println(id.toString().concat(":").concat(lastname).concat(", ").concat(firstname).concat(" Courses: " + courses.size()));
    }

    public String getNmName() {
        return nmName;
    }

    public void setNmName(String nmName) {
        this.nmName = nmName;
    }
}
