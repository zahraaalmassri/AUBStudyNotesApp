package com.example.aubstudynotesapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class LecturesActivity extends AppCompatActivity {

    Button btnUpload, btnAddLecture;

    LinearLayout lectureContainer;

    ActivityResultLauncher<String> filePickerLauncher;

    int lectureCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lectures);

        btnUpload = findViewById(R.id.btnUpload);
        btnAddLecture = findViewById(R.id.btnAddLecture);

        lectureContainer = findViewById(R.id.lectureContainer);

        filePickerLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.GetContent(),
                        uri -> {

                        });

        btnUpload.setOnClickListener(v ->
                filePickerLauncher.launch("*/*"));

        btnAddLecture.setOnClickListener(v -> {

            AlertDialog.Builder builder =
                    new AlertDialog.Builder(
                            LecturesActivity.this);

            builder.setTitle("Add Lecture");

            LinearLayout dialogLayout =
                    new LinearLayout(this);

            dialogLayout.setOrientation(
                    LinearLayout.VERTICAL);

            EditText lectureInput =
                    new EditText(this);

            lectureInput.setHint("Lecture Title");

            EditText notesInput =
                    new EditText(this);

            notesInput.setHint("Lecture Notes");

            notesInput.setMinLines(3);

            dialogLayout.addView(lectureInput);

            dialogLayout.addView(notesInput);

            builder.setView(dialogLayout);

            builder.setPositiveButton("Add",
                    (dialog, which) -> {

                        String lectureTitle =
                                lectureInput.getText().toString();

                        String lectureNotes =
                                notesInput.getText().toString();

                        addLectureCard(
                                lectureTitle,
                                lectureNotes);
                    });

            builder.setNegativeButton("Cancel",
                    (dialog, which) -> dialog.cancel());

            builder.show();
        });
    }

    private void addLectureCard(
            String lectureTitle,
            String lectureNotes) {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL);

        card.setPadding(30,30,30,30);

        card.setBackgroundColor(0xFFFFFFFF);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        params.setMargins(0,0,0,30);

        card.setLayoutParams(params);

        TextView title = new TextView(this);

        title.setText(
                "Lecture " + lectureCount +
                        " - " + lectureTitle);

        title.setTextSize(20);

        title.setTextColor(0xFF7A0019);

        TextView notes = new TextView(this);

        notes.setText(lectureNotes);

        notes.setTextSize(15);

        notes.setPadding(0,15,0,15);

        Button openBtn = new Button(this);

        openBtn.setText("Open PDF");

        openBtn.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            LecturesActivity.this,
                            PdfViewerActivity.class);

            startActivity(intent);
        });

        card.addView(title);

        card.addView(notes);

        card.addView(openBtn);

        lectureContainer.addView(card);

        lectureCount++;
    }
}