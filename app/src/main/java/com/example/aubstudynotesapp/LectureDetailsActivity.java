package com.example.aubstudynotesapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class LectureDetailsActivity extends AppCompatActivity {

    Button btnUploadNotes, btnViewPdf;

    TextView txtLectureTitle, txtUploadStatus;

    ActivityResultLauncher<String[]> filePickerLauncher;

    FirebaseFirestore db;

    String uploadedFileUri = null;

    String courseName, lectureTitle, docId, userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture_details);

        btnUploadNotes = findViewById(R.id.btnUploadNotes);

        btnViewPdf = findViewById(R.id.btnViewPdf);

        txtLectureTitle = findViewById(R.id.txtLectureTitle);

        txtUploadStatus = findViewById(R.id.txtUploadStatus);

        lectureTitle = getIntent().getStringExtra("lectureTitle");

        courseName = getIntent().getStringExtra("courseName");

        docId = getIntent().getStringExtra("docId");

        userEmail = getIntent().getStringExtra("userEmail");

        txtLectureTitle.setText(lectureTitle);

        db = FirebaseFirestore.getInstance();

        loadExistingFile();

        filePickerLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.OpenDocument(),
                        uri -> {

                            if (uri != null) {

                                getContentResolver()
                                        .takePersistableUriPermission(
                                                uri,
                                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        );

                                uploadedFileUri = uri.toString();

                                saveUriToFirestore(uploadedFileUri);

                                txtUploadStatus.setText(
                                        "✅ File selected successfully!");

                                btnViewPdf.setEnabled(true);
                            }
                        });

        btnUploadNotes.setOnClickListener(v ->
                filePickerLauncher.launch(new String[]{
                        "application/pdf",
                        "application/vnd.ms-powerpoint",
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                }));
        btnViewPdf.setOnClickListener(v -> {

            if (uploadedFileUri != null) {

                Intent intent =
                        new Intent(Intent.ACTION_VIEW);

                intent.setDataAndType(
                        Uri.parse(uploadedFileUri),
                        "application/pdf");

                intent.setFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);

                startActivity(intent);

            } else {

                Toast.makeText(
                        this,
                        "No file selected yet",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void loadExistingFile() {

        if (userEmail == null ||
                courseName == null ||
                docId == null) return;

        db.collection("users")
                .document(userEmail)
                .collection("courses")
                .document(courseName)
                .collection("lectures")
                .document(docId)
                .get()
                .addOnSuccessListener(doc -> {

                    String uri =
                            doc.getString("fileUri");

                    if (uri != null && !uri.isEmpty()) {

                        uploadedFileUri = uri;

                        txtUploadStatus.setText(
                                "✅ File already selected");

                        btnViewPdf.setEnabled(true);

                    } else {

                        txtUploadStatus.setText(
                                "No file selected yet");

                        btnViewPdf.setEnabled(false);
                    }
                });
    }

    private void saveUriToFirestore(String uri) {

        db.collection("users")
                .document(userEmail)
                .collection("courses")
                .document(courseName)
                .collection("lectures")
                .document(docId)
                .update("fileUri", uri)
                .addOnFailureListener(e ->

                        Toast.makeText(
                                this,
                                "Could not save file reference",
                                Toast.LENGTH_SHORT
                        ).show());
    }
}