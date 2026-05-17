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
    ActivityResultLauncher<String> filePickerLauncher;
    FirebaseFirestore db;
    String uploadedFileUri = null;
    String courseName, lectureTitle, docId;

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

        txtLectureTitle.setText(lectureTitle);

        db = FirebaseFirestore.getInstance();

        // ✅ Load saved URI from Firestore
        loadExistingFile();

        // ✅ File picker — saves URI locally
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        // Take persistent permission so URI survives app restarts
                        getContentResolver().takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                        uploadedFileUri = uri.toString();
                        saveUriToFirestore(uploadedFileUri);
                        txtUploadStatus.setText("✅ File selected successfully!");
                        btnViewPdf.setEnabled(true);
                    }
                });

        btnUploadNotes.setOnClickListener(v -> filePickerLauncher.launch("*/*"));

        btnViewPdf.setOnClickListener(v -> {
            if (uploadedFileUri != null) {
                Intent intent = new Intent(LectureDetailsActivity.this, PdfViewerActivity.class);
                intent.putExtra("fileUri", uploadedFileUri);
                startActivity(intent);
            } else {
                Toast.makeText(this, "No file selected yet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadExistingFile() {
        if (courseName == null || docId == null) return;

        db.collection("users")
                .document(courseName)
                .collection("lectures")
                .document(docId)
                .get()
                .addOnSuccessListener(doc -> {
                    String uri = doc.getString("fileUri");
                    if (uri != null && !uri.isEmpty()) {
                        uploadedFileUri = uri;
                        txtUploadStatus.setText("✅ File already selected");
                        btnViewPdf.setEnabled(true);
                    } else {
                        txtUploadStatus.setText("No file selected yet");
                        btnViewPdf.setEnabled(false);
                    }
                });
    }

    private void saveUriToFirestore(String uri) {
        db.collection("users")
                .document(courseName)
                .collection("lectures")
                .document(docId)
                .update("fileUri", uri)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Could not save file reference", Toast.LENGTH_SHORT).show()
                );
    }
}