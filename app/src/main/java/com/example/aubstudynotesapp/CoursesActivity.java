package com.example.aubstudynotesapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CoursesActivity extends AppCompatActivity {

    RecyclerView recyclerCourses;
    ArrayList<Course> courseList;
    ArrayList<Course> filteredList;
    CourseAdapter adapter;
    Button btnLogout, btnAddCourse;
    EditText searchBar;
    TextView txtWelcome;
    ProgressBar progressBar;
    FirebaseFirestore db;
    String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses);

        recyclerCourses = findViewById(R.id.recyclerCourses);
        btnLogout = findViewById(R.id.btnLogout);
        btnAddCourse = findViewById(R.id.btnAddCourse);
        searchBar = findViewById(R.id.searchBar);
        txtWelcome = findViewById(R.id.txtWelcome);
        progressBar = findViewById(R.id.progressBar);

        db = FirebaseFirestore.getInstance();

        // ✅ Dynamic username
        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        txtWelcome.setText("Welcome, " + userEmail);

        courseList = new ArrayList<>();
        filteredList = new ArrayList<>();

        adapter = new CourseAdapter(filteredList);
        recyclerCourses.setLayoutManager(new LinearLayoutManager(this));
        recyclerCourses.setAdapter(adapter);

        // ✅ Load courses from Firestore
        loadCourses();

        // ✅ Search filter
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCourses(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // ✅ Add course button
        btnAddCourse.setOnClickListener(v -> showAddCourseDialog());

        // ✅ Logout
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(CoursesActivity.this, MainActivity.class));
            finish();
        });
    }

    private void loadCourses() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("users")
                .document(userEmail)
                .collection("courses")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    courseList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        String semester = doc.getString("semester");
                        courseList.add(new Course(name, semester));
                    }
                    filteredList.clear();
                    filteredList.addAll(courseList);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void showAddCourseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Course");

        EditText inputName = new EditText(this);
        inputName.setHint("Course Name (e.g. CMPS 279)");
        inputName.setInputType(InputType.TYPE_CLASS_TEXT);

        EditText inputSemester = new EditText(this);
        inputSemester.setHint("Semester (e.g. Spring 2026)");
        inputSemester.setInputType(InputType.TYPE_CLASS_TEXT);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);
        layout.addView(inputName);
        layout.addView(inputSemester);
        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = inputName.getText().toString().trim();
            String semester = inputSemester.getText().toString().trim();
            if (!name.isEmpty() && !semester.isEmpty()) {
                saveCourseToFirestore(name, semester);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveCourseToFirestore(String name, String semester) {
        Map<String, Object> course = new HashMap<>();
        course.put("name", name);
        course.put("semester", semester);
        course.put("timestamp", System.currentTimeMillis());

        db.collection("users")
                .document(userEmail)
                .collection("courses")
                .add(course)
                .addOnSuccessListener(documentReference -> loadCourses());
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