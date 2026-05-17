package com.example.aubstudynotesapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;

public class CoursesActivity extends AppCompatActivity {

    RecyclerView recyclerCourses;
    ArrayList<Course> courseList;
    ArrayList<Course> filteredList;
    CourseAdapter adapter;
    Button btnLogout;
    EditText searchBar;
    TextView txtWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses);

        recyclerCourses = findViewById(R.id.recyclerCourses);
        btnLogout = findViewById(R.id.btnLogout);
        searchBar = findViewById(R.id.searchBar);
        txtWelcome = findViewById(R.id.txtWelcome);

        // ✅ Fix 7 - dynamic username from Firebase
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        txtWelcome.setText("Welcome, " + email);

        courseList = new ArrayList<>();
        courseList.add(new Course("CMPS 279 - Mobile App Development", "Spring 2026"));
        courseList.add(new Course("CMPS 285 - Artificial Intelligence", "Spring 2026"));
        courseList.add(new Course("CMPS 231 - Data Structures", "Fall 2025"));
        courseList.add(new Course("MATH 251 - Linear Algebra", "Spring 2026"));
        courseList.add(new Course("CMPS 212 - Database Systems", "Fall 2025"));

        filteredList = new ArrayList<>(courseList);

        adapter = new CourseAdapter(filteredList);
        recyclerCourses.setLayoutManager(new LinearLayoutManager(this));
        recyclerCourses.setAdapter(adapter);

        // ✅ Fix 6 - search filters the list
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCourses(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // ✅ Fix 1 - proper Firebase signOut
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(CoursesActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    private void filterCourses(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(courseList);
        } else {
            for (Course course : courseList) {
                if (course.getName().toLowerCase().contains(query.toLowerCase()) ||
                        course.getSemester().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(course);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}