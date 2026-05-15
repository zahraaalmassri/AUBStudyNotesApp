package com.example.aubstudynotesapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CoursesActivity extends AppCompatActivity {

    RecyclerView recyclerCourses;

    ArrayList<Course> courseList;

    CourseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses);

        recyclerCourses = findViewById(R.id.recyclerCourses);

        courseList = new ArrayList<>();

        courseList.add(new Course("CMPS 297"));
        courseList.add(new Course("Mobile Development"));
        courseList.add(new Course("Artificial Intelligence"));
        courseList.add(new Course("Database Systems"));
        courseList.add(new Course("Operating Systems"));

        adapter = new CourseAdapter(courseList);

        recyclerCourses.setLayoutManager(
                new LinearLayoutManager(this));

        recyclerCourses.setAdapter(adapter);
    }
}