package com.example.aubstudynotesapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LecturesActivity extends AppCompatActivity {

    FloatingActionButton btnAddLecture;
    LinearLayout lectureContainer;
    FirebaseFirestore db;
    String courseName, semester, userEmail;
    ProgressBar progressBar;
    TextView txtCourseName, txtSemesterLabel, txtEmpty, btnBack;
    EditText searchLectureBar;
    ArrayList<String> lectureTitles = new ArrayList<>();
    ArrayList<String> lectureDocIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lectures);

        btnAddLecture = findViewById(R.id.btnAddLecture);
        lectureContainer = findViewById(R.id.lectureContainer);
        progressBar = findViewById(R.id.progressBar);
        txtCourseName = findViewById(R.id.txtCourseName);
        txtSemesterLabel = findViewById(R.id.txtSemesterLabel);
        txtEmpty = findViewById(R.id.txtEmpty);
        btnBack = findViewById(R.id.btnBack);
        searchLectureBar = findViewById(R.id.searchLectureBar);

        courseName = getIntent().getStringExtra("courseName");
        semester = getIntent().getStringExtra("semester");
        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        if (courseName != null) txtCourseName.setText(courseName);
        if (semester != null) txtSemesterLabel.setText(semester);

        db = FirebaseFirestore.getInstance();
        loadLectures();

        btnBack.setOnClickListener(v -> finish());

        searchLectureBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterLectures(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnAddLecture.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Add Lecture");

            EditText input = new EditText(this);
            input.setHint("e.g. Lecture 1 - Introduction");
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            LinearLayout layout = new LinearLayout(this);
            layout.setPadding(48, 24, 48, 0);
            layout.addView(input);
            builder.setView(layout);

            builder.setPositiveButton("Add", (dialog, which) -> {
                String title = input.getText().toString().trim();
                if (!title.isEmpty()) {
                    saveLectureToFirestore(title);
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        });
    }

    private void loadLectures() {
        progressBar.setVisibility(View.VISIBLE);
        txtEmpty.setVisibility(View.GONE);

        db.collection("users")
                .document(userEmail)
                .collection("courses")
                .document(courseName)
                .collection("lectures")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    lectureContainer.removeAllViews();
                    lectureTitles.clear();
                    lectureDocIds.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        txtEmpty.setVisibility(View.VISIBLE);
                        return;
                    }

                    int count = 1;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String title = doc.getString("title");
                        String docId = doc.getId();
                        lectureTitles.add(title);
                        lectureDocIds.add(docId);
                        addLectureCard(title, count, docId);
                        count++;
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load lectures", Toast.LENGTH_SHORT).show();
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
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Lecture added!", Toast.LENGTH_SHORT).show();
                    loadLectures();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to add lecture", Toast.LENGTH_SHORT).show()
                );
    }

    private void addLectureCard(String lectureTitle, int lectureNumber, String docId) {
        // Outer card
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setPadding(20, 20, 20, 20);
        card.setBackgroundResource(R.drawable.card_white_rounded);
        card.setElevation(4f);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        card.setLayoutParams(params);

        // Number badge
        TextView numBadge = new TextView(this);
        numBadge.setText(String.valueOf(lectureNumber));
        numBadge.setTextSize(14);
        numBadge.setTextColor(0xFFFFFFFF);
        numBadge.setGravity(android.view.Gravity.CENTER);
        numBadge.setBackgroundResource(R.drawable.circle_red_bg);
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(40, 40);
        badgeParams.setMargins(0, 0, 16, 0);
        numBadge.setLayoutParams(badgeParams);
        card.addView(numBadge);

        // Title + hint
        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        textLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView title = new TextView(this);
        title.setText(lectureTitle);
        title.setTextSize(15);
        title.setTextColor(0xFF1A1A1A);

        TextView hint = new TextView(this);
        hint.setText("Tap to view notes & files");
        hint.setTextSize(12);
        hint.setTextColor(0xFF999999);

        textLayout.addView(title);
        textLayout.addView(hint);
        card.addView(textLayout);

        // Arrow
        TextView arrow = new TextView(this);
        arrow.setText("›");
        arrow.setTextSize(22);
        arrow.setTextColor(0xFF7A0019);
        arrow.setGravity(android.view.Gravity.CENTER);
        card.addView(arrow);

        card.setOnClickListener(v -> {
            Intent intent = new Intent(LecturesActivity.this, LectureDetailsActivity.class);
            intent.putExtra("lectureTitle", lectureTitle);
            intent.putExtra("courseName", courseName);
            intent.putExtra("userEmail", userEmail);
            intent.putExtra("docId", docId);
            startActivity(intent);
        });

        // Long press to delete
        card.setOnLongClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Lecture")
                    .setMessage("Delete \"" + lectureTitle + "\"?")
                    .setPositiveButton("Delete", (dialog, which) ->
                            deleteLecture(docId))
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });

        lectureContainer.addView(card);
    }

    private void deleteLecture(String docId) {
        db.collection("users")
                .document(userEmail)
                .collection("courses")
                .document(courseName)
                .collection("lectures")
                .document(docId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Lecture deleted", Toast.LENGTH_SHORT).show();
                    loadLectures();
                });
    }

    private void filterLectures(String query) {
        lectureContainer.removeAllViews();
        int count = 1;
        for (int i = 0; i < lectureTitles.size(); i++) {
            String title = lectureTitles.get(i);
            String docId = lectureDocIds.get(i);
            if (title.toLowerCase().contains(query.toLowerCase())) {
                addLectureCard(title, count, docId);
                count++;
            }
        }
        txtEmpty.setVisibility(lectureContainer.getChildCount() == 0 ? View.VISIBLE : View.GONE);
    }
}