package com.example.aubstudynotesapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CoursesActivity extends AppCompatActivity {

    RecyclerView recyclerCourses;

    ArrayList<Course> courseList;

    CourseAdapter adapter;

    Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses);

        recyclerCourses = findViewById(R.id.recyclerCourses);

        btnLogout = findViewById(R.id.btnLogout);

        courseList = new ArrayList<>();

        courseList.add(new Course(
                "CMPS 279 - Mobile App Development",
                "Spring 2026"));

        courseList.add(new Course(
                "CMPS 285 - Artificial Intelligence",
                "Spring 2026"));

        courseList.add(new Course(
                "CMPS 231 - Data Structures",
                "Fall 2025"));

        courseList.add(new Course(
                "MATH 251 - Linear Algebra",
                "Spring 2026"));

        courseList.add(new Course(
                "CMPS 212 - Database Systems",
                "Fall 2025"));

        adapter = new CourseAdapter(courseList);

        recyclerCourses.setLayoutManager(
                new LinearLayoutManager(this));

        recyclerCourses.setAdapter(adapter);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent =
                        new Intent(CoursesActivity.this,
                                MainActivity.class);

                startActivity(intent);

                finish();
            }
        });
    }
}