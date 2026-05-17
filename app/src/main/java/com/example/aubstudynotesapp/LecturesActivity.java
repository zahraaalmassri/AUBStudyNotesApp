package com.example.aubstudynotesapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.HashMap;
import java.util.Map;

public class LecturesActivity extends AppCompatActivity {

    Button btnAddLecture;
    LinearLayout lectureContainer;
    FirebaseFirestore db;
    String courseName;
    String userEmail;
    ProgressBar progressBar;
    TextView txtCourseName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lectures);

        btnAddLecture = findViewById(R.id.btnAddLecture);
        lectureContainer = findViewById(R.id.lectureContainer);
        progressBar = findViewById(R.id.progressBar);
        txtCourseName = findViewById(R.id.txtCourseName);

        // ✅ Get course name from intent
        courseName = getIntent().getStringExtra("courseName");
        if (courseName != null) {
            txtCourseName.setText(courseName);
        }

        // ✅ Get logged in user email
        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        db = FirebaseFirestore.getInstance();

        // ✅ Load lectures from Firestore
        loadLectures();

        btnAddLecture.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(LecturesActivity.this);
            builder.setTitle("Add Lecture");

            EditText input = new EditText(this);
            input.setHint("Lecture Title");
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("Add", (dialog, which) -> {
                String lectureTitle = input.getText().toString().trim();
                if (!lectureTitle.isEmpty()) {
                    saveLectureToFirestore(lectureTitle);
                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        });
    }

    private void loadLectures() {
        progressBar.setVisibility(android.view.View.VISIBLE);

        db.collection("users")
                .document(userEmail)
                .collection("courses")
                .document(courseName)
                .collection("lectures")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    lectureContainer.removeAllViews();
                    int count = 1;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String title = doc.getString("title");
                        String docId = doc.getId();
                        addLectureCard(title, count, docId);
                        count++;
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(android.view.View.GONE);
                });
    }

    private void saveLectureToFirestore(String lectureTitle) {
        Map<String, Object> lecture = new HashMap<>();
        lecture.put("title", lectureTitle);
        lecture.put("timestamp", System.currentTimeMillis());

        db.collection("users")
                .document(userEmail)
                .collection("courses")
                .document(courseName)
                .collection("lectures")
                .add(lecture)
                .addOnSuccessListener(documentReference -> loadLectures());
    }

    private void addLectureCard(String lectureTitle, int lectureNumber, String docId) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(30, 30, 30, 30);
        card.setBackgroundColor(0xFFFFFFFF);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 30);
        card.setLayoutParams(params);

        TextView title = new TextView(this);
        title.setText("Lecture " + lectureNumber + " - " + lectureTitle);
        title.setTextSize(20);
        title.setTextColor(0xFF7A0019);
        title.setPadding(10, 10, 10, 10);
        card.addView(title);

        card.setOnClickListener(v -> {
            Intent intent = new Intent(LecturesActivity.this, LectureDetailsActivity.class);
            intent.putExtra("lectureTitle", lectureTitle);
            intent.putExtra("courseName", courseName);
            intent.putExtra("userEmail", userEmail);
            intent.putExtra("docId", docId);
            startActivity(intent);
        });

        lectureContainer.addView(card);
    }
}