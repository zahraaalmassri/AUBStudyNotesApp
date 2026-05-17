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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class LectureDetailsActivity extends AppCompatActivity {

    Button btnUploadNotes, btnViewPdf;
    TextView txtLectureTitle, txtUploadStatus;
    ActivityResultLauncher<String> filePickerLauncher;
    StorageReference storageRef;
    String uploadedFileUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture_details);

        btnUploadNotes = findViewById(R.id.btnUploadNotes);
        btnViewPdf = findViewById(R.id.btnViewPdf);
        txtLectureTitle = findViewById(R.id.txtLectureTitle);
        txtUploadStatus = findViewById(R.id.txtUploadStatus);

        String lectureTitle = getIntent().getStringExtra("lectureTitle");
        txtLectureTitle.setText(lectureTitle);

        storageRef = FirebaseStorage.getInstance().getReference();

        // ✅ FIXED - actually uploads the file and stores the URL
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uploadFile(uri, lectureTitle);
                    }
                });

        btnUploadNotes.setOnClickListener(v -> filePickerLauncher.launch("*/*"));

        btnViewPdf.setOnClickListener(v -> {
            if (uploadedFileUrl != null) {
                Intent intent = new Intent(LectureDetailsActivity.this, PdfViewerActivity.class);
                intent.putExtra("fileUrl", uploadedFileUrl); // ✅ FIXED - passes URL
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please upload a file first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadFile(Uri uri, String lectureTitle) {
        txtUploadStatus.setText("Uploading...");
        btnUploadNotes.setEnabled(false);

        String fileName = "lectures/" + lectureTitle + "_" + System.currentTimeMillis();
        StorageReference fileRef = storageRef.child(fileName);

        fileRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot ->
                        fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            uploadedFileUrl = downloadUri.toString();
                            txtUploadStatus.setText("✅ Uploaded successfully!");
                            btnUploadNotes.setEnabled(true);
                            Toast.makeText(this, "Upload complete!", Toast.LENGTH_SHORT).show();
                        })
                )
                .addOnFailureListener(e -> {
                    txtUploadStatus.setText("❌ Upload failed");
                    btnUploadNotes.setEnabled(true);
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }

}