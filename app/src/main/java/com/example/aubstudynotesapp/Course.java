package com.example.aubstudynotesapp;

public class Course {

    String name;
    String semester;

    public Course(String name, String semester) {
        this.name = name;
        this.semester = semester;
    }

    public String getName() {
        return name;
    }

    public String getSemester() {
        return semester;
    }
}