package com.example.aubstudynotesapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

public class LectureDetailsActivity extends AppCompatActivity {

    Button btnUploadNotes, btnViewPdf, btnSaveNotes;
    TextView txtLectureTitle, txtUploadStatus, txtFileName, txtNotesSaved, txtSummary, btnBack;
    EditText etQuickNotes;
    ActivityResultLauncher<String[]> filePickerLauncher;
    FirebaseFirestore db;
    String uploadedFileUri = null;
    String courseName, lectureTitle, docId, userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture_details);

        // Views
        btnUploadNotes = findViewById(R.id.btnUploadNotes);
        btnViewPdf = findViewById(R.id.btnViewPdf);
        btnSaveNotes = findViewById(R.id.btnSaveNotes);
        btnBack = findViewById(R.id.btnBack);
        txtLectureTitle = findViewById(R.id.txtLectureTitle);
        txtUploadStatus = findViewById(R.id.txtUploadStatus);
        txtFileName = findViewById(R.id.txtFileName);
        txtNotesSaved = findViewById(R.id.txtNotesSaved);
        txtSummary = findViewById(R.id.txtSummary);
        etQuickNotes = findViewById(R.id.etQuickNotes);

        // Extras
        lectureTitle = getIntent().getStringExtra("lectureTitle");
        courseName = getIntent().getStringExtra("courseName");
        docId = getIntent().getStringExtra("docId");
        userEmail = getIntent().getStringExtra("userEmail");

        txtLectureTitle.setText(lectureTitle);
        db = FirebaseFirestore.getInstance();

        // Load existing data
        loadExistingFile();
        loadNotes();

        // Back
        btnBack.setOnClickListener(v -> finish());

        // File picker
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        getContentResolver().takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        uploadedFileUri = uri.toString();
                        saveUriToFirestore(uploadedFileUri);

                        // Show filename
                        String fileName = getFileName(uri);
                        txtFileName.setText("📄 " + fileName);
                        txtFileName.setVisibility(View.VISIBLE);
                        txtUploadStatus.setText("✅ File uploaded successfully!");
                        btnViewPdf.setEnabled(true);

                        // Update AI summary placeholder
                        txtSummary.setText("File \"" + fileName + "\" uploaded. AI summary coming soon.");
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
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(uploadedFileUri), "application/pdf");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    // Try generic view if PDF viewer not found
                    Intent generic = new Intent(Intent.ACTION_VIEW);
                    generic.setData(Uri.parse(uploadedFileUri));
                    generic.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(generic);
                }
            } else {
                Toast.makeText(this, "No file uploaded yet", Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ Save quick notes to Firestore
        btnSaveNotes.setOnClickListener(v -> {
            String notes = etQuickNotes.getText().toString().trim();
            saveNotesToFirestore(notes);
        });
    }

    private void loadExistingFile() {
        if (userEmail == null || courseName == null || docId == null) return;

        db.collection("users")
                .document(userEmail)
                .collection("courses")
                .document(courseName)
                .collection("lectures")
                .document(docId)
                .get()
                .addOnSuccessListener(doc -> {
                    String uri = doc.getString("fileUri");
                    if (uri != null && !uri.isEmpty()) {
                        uploadedFileUri = uri;
                        txtUploadStatus.setText("✅ File already uploaded");
                        btnViewPdf.setEnabled(true);

                        // Try to show filename
                        try {
                            String fileName = getFileName(Uri.parse(uri));
                            txtFileName.setText("📄 " + fileName);
                            txtFileName.setVisibility(View.VISIBLE);
                        } catch (Exception ignored) {}
                    } else {
                        txtUploadStatus.setText("No file uploaded yet");
                        btnViewPdf.setEnabled(false);
                    }
                });
    }

    private void loadNotes() {
        if (userEmail == null || courseName == null || docId == null) return;

        db.collection("users")
                .document(userEmail)
                .collection("courses")
                .document(courseName)
                .collection("lectures")
                .document(docId)
                .get()
                .addOnSuccessListener(doc -> {
                    String notes = doc.getString("quickNotes");
                    if (notes != null && !notes.isEmpty()) {
                        etQuickNotes.setText(notes);
                        txtNotesSaved.setText("Last saved notes loaded ✓");
                    }
                });
    }

    private void saveNotesToFirestore(String notes) {
        db.collection("users")
                .document(userEmail)
                .collection("courses")
                .document(courseName)
                .collection("lectures")
                .document(docId)
                .update("quickNotes", notes)
                .addOnSuccessListener(aVoid -> {
                    txtNotesSaved.setText("✅ Notes saved!");
                    // Update AI summary with notes preview
                    if (!notes.isEmpty()) {
                        String preview = notes.length() > 80
                                ? notes.substring(0, 80) + "..."
                                : notes;
                        txtSummary.setText("Based on your notes:\n\n\"" + preview + "\"\n\nAI summary coming soon.");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save notes", Toast.LENGTH_SHORT).show()
                );
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
                        Toast.makeText(this, "Could not save file reference", Toast.LENGTH_SHORT).show()
                );
    }

    private String getFileName(Uri uri) {
        String path = uri.getPath();
        if (path != null && path.contains("/")) {
            return path.substring(path.lastIndexOf("/") + 1);
        }
        return "Uploaded File";
    }
}