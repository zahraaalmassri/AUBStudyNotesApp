package com.example.aubstudynotesapp;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class LectureDetailsActivity extends AppCompatActivity {

    Button btnUploadNotes, btnViewPdf, btnGenerateSummary, btnSaveNotes, btnAddTodo;
    TextView txtLectureTitle, txtUploadStatus, txtFileName, txtSummary,
            txtSummaryStatus, txtNotesSaved, btnBack;
    EditText etQuickNotes, etTodoInput;
    LinearLayout layoutFileChip, todoContainer;
    ActivityResultLauncher<String[]> filePickerLauncher;
    FirebaseFirestore db;
    String uploadedFileUri = "";
    String uploadedLectureText = "";
    String courseName, lectureTitle, userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture_details);

        PDFBoxResourceLoader.init(getApplicationContext());
        db = FirebaseFirestore.getInstance();

        btnUploadNotes = findViewById(R.id.btnUploadNotes);
        btnViewPdf = findViewById(R.id.btnViewPdf);
        btnGenerateSummary = findViewById(R.id.btnGenerateSummary);
        btnSaveNotes = findViewById(R.id.btnSaveNotes);
        btnAddTodo = findViewById(R.id.btnAddTodo);
        btnBack = findViewById(R.id.btnBack);
        txtLectureTitle = findViewById(R.id.txtLectureTitle);
        txtUploadStatus = findViewById(R.id.txtUploadStatus);
        txtFileName = findViewById(R.id.txtFileName);
        txtSummary = findViewById(R.id.txtSummary);
        txtSummaryStatus = findViewById(R.id.txtSummaryStatus);
        txtNotesSaved = findViewById(R.id.txtNotesSaved);
        etQuickNotes = findViewById(R.id.etQuickNotes);
        etTodoInput = findViewById(R.id.etTodoInput);
        todoContainer = findViewById(R.id.todoContainer);
        layoutFileChip = findViewById(R.id.layoutFileChip);

        lectureTitle = getIntent().getStringExtra("lectureTitle");
        courseName = getIntent().getStringExtra("courseName");
        userEmail = getIntent().getStringExtra("userEmail");

        txtLectureTitle.setText(lectureTitle);
        btnBack.setOnClickListener(v -> finish());

        loadSavedLecture();
        loadTodos();

        // File picker
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        try {
                            getContentResolver().takePersistableUriPermission(
                                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            uploadedFileUri = uri.toString();
                            extractPdfText(uri);
                            saveLectureToFirestore();
                            showFileChip(getFileName(uri));
                            txtUploadStatus.setText("PDF uploaded successfully");
                            btnViewPdf.setEnabled(true);
                        } catch (Exception e) {
                            Toast.makeText(this, "Could not read PDF",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        btnUploadNotes.setOnClickListener(v ->
                filePickerLauncher.launch(new String[]{"application/pdf"}));

        btnViewPdf.setOnClickListener(v -> {
            if (!uploadedFileUri.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(uploadedFileUri), "application/pdf");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "No PDF viewer found",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No PDF uploaded", Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ Save notes to Firestore (was only showing text before, now actually saves)
        btnSaveNotes.setOnClickListener(v -> {
            String notes = etQuickNotes.getText().toString().trim();
            if (!notes.isEmpty()) {
                saveNotesToFirestore(notes);
            } else {
                txtNotesSaved.setText("Please type notes first");
            }
        });

        // ✅ Add todo
        btnAddTodo.setOnClickListener(v -> {
            String task = etTodoInput.getText().toString().trim();
            if (!task.isEmpty()) {
                addTodoToFirestore(task);
                etTodoInput.setText("");
            }
        });

        // ✅ AI summary from PDF text
        btnGenerateSummary.setOnClickListener(v -> {
            if (uploadedLectureText.isEmpty()) {
                txtSummary.setText("Please upload a lecture PDF first to generate a summary.");
                return;
            }
            generateAISummary(uploadedLectureText);
        });
    }

    // ===== PDF =====

    private void extractPdfText(Uri uri) {
        try {
            InputStream inputStream =
                    getContentResolver().openInputStream(uri);

            PDDocument document =
                    PDDocument.load(inputStream);

            PDFTextStripper stripper =
                    new PDFTextStripper();

            uploadedLectureText =
                    stripper.getText(document);

            document.close();

            android.util.Log.d("PDF",
                    "Extracted: " + uploadedLectureText.length() + " chars");

            if (uploadedLectureText.length() > 1200) {
                uploadedLectureText =
                        uploadedLectureText.substring(0, 1200);
            }

            if (uploadedLectureText.isEmpty()) {
                Toast.makeText(this,
                        "Could not extract text — PDF may be image-based",
                        Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            uploadedLectureText = "";
            android.util.Log.e("PDF", "Extraction failed: " + e.getMessage());
            Toast.makeText(this,
                    "PDF extraction error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void loadSavedLecture() {
        // ✅ Disable generate until data loads
        btnGenerateSummary.setEnabled(false);
        btnGenerateSummary.setText("Loading...");

        db.collection("users")
                .document(userEmail)
                .collection("courses")
                .document(courseName)
                .collection("lectures")
                .document(lectureTitle)
                .get()
                .addOnSuccessListener(doc -> {
                    btnGenerateSummary.setEnabled(true);
                    btnGenerateSummary.setText("Generate");

                    if (doc.exists()) {
                        String savedUri = doc.getString("fileUri");
                        String savedText = doc.getString("lectureText");
                        String savedNotes = doc.getString("quickNotes");

                        if (savedUri != null && !savedUri.isEmpty()) {
                            uploadedFileUri = savedUri;
                            txtUploadStatus.setText("PDF uploaded");
                            showFileChip("Saved PDF");
                            btnViewPdf.setEnabled(true);
                        }
                        if (savedText != null && !savedText.isEmpty()) {
                            uploadedLectureText = savedText;
                            txtSummary.setText(
                                    "PDF ready! Tap Generate for AI summary.");
                            txtSummary.setTextColor(0xFF4CAF50);
                        } else if (savedUri != null && !savedUri.isEmpty()) {
                            // PDF exists but no text — warn user
                            txtSummary.setText(
                                    "PDF uploaded but text could not be extracted. " +
                                            "Try re-uploading a text-based PDF.");
                            txtSummary.setTextColor(0xFFFF6B6B);
                        }
                        if (savedNotes != null && !savedNotes.isEmpty()) {
                            etQuickNotes.setText(savedNotes);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    btnGenerateSummary.setEnabled(true);
                    btnGenerateSummary.setText("Generate");
                    android.util.Log.e("Firestore",
                            "Load failed: " + e.getMessage());
                });
    }

    private void saveLectureToFirestore() {
        if (userEmail == null || courseName == null || lectureTitle == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("fileUri", uploadedFileUri);
        data.put("lectureText", uploadedLectureText);
        data.put("lectureTitle", lectureTitle);
        data.put("lastUpdated", System.currentTimeMillis());

        db.collection("users")
                .document(userEmail)
                .collection("courses")
                .document(courseName)
                .collection("lectures")
                .document(lectureTitle)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Lecture saved", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show());
    }

    // ===== NOTES =====

    private void saveNotesToFirestore(String notes) {
        db.collection("users")
                .document(userEmail)
                .collection("courses")
                .document(courseName)
                .collection("lectures")
                .document(lectureTitle)
                .update("quickNotes", notes)
                .addOnSuccessListener(aVoid ->
                        txtNotesSaved.setText("✅ Notes saved!"))
                .addOnFailureListener(e -> {
                    // If document doesn't exist yet use set
                    Map<String, Object> data = new HashMap<>();
                    data.put("quickNotes", notes);
                    db.collection("users")
                            .document(userEmail)
                            .collection("courses")
                            .document(courseName)
                            .collection("lectures")
                            .document(lectureTitle)
                            .set(data, SetOptions.merge())
                            .addOnSuccessListener(aVoid2 ->
                                    txtNotesSaved.setText("✅ Notes saved!"));
                });
    }

    // ===== TODOS =====

    private void loadTodos() {
        db.collection("users")
                .document(userEmail)
                .collection("courses")
                .document(courseName)
                .collection("lectures")
                .document(lectureTitle)
                .collection("todos")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    todoContainer.removeAllViews();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String task = doc.getString("task");
                        Boolean done = doc.getBoolean("done");
                        String todoId = doc.getId();
                        addTodoRow(todoId, task, done != null && done);
                    }
                });
    }

    private void addTodoToFirestore(String task) {
        Map<String, Object> todo = new HashMap<>();
        todo.put("task", task);
        todo.put("done", false);
        todo.put("timestamp", System.currentTimeMillis());

        db.collection("users")
                .document(userEmail)
                .collection("courses")
                .document(courseName)
                .collection("lectures")
                .document(lectureTitle)
                .collection("todos")
                .add(todo)
                .addOnSuccessListener(ref -> loadTodos());
    }

    private void addTodoRow(String todoId, String task, boolean done) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 0, 0, 12);
        row.setLayoutParams(rowParams);

        CheckBox checkBox = new CheckBox(this);
        checkBox.setChecked(done);

        TextView taskText = new TextView(this);
        taskText.setText(task);
        taskText.setTextSize(14);
        taskText.setTextColor(done ? 0xFFAAAAAA : 0xFF333333);
        taskText.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        if (done) {
            taskText.setPaintFlags(
                    taskText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        TextView deleteBtn = new TextView(this);
        deleteBtn.setText("✕");
        deleteBtn.setTextSize(14);
        deleteBtn.setTextColor(0xFFCCCCCC);
        deleteBtn.setPadding(16, 0, 0, 0);

        checkBox.setOnCheckedChangeListener((btn, isChecked) ->
                db.collection("users")
                        .document(userEmail)
                        .collection("courses")
                        .document(courseName)
                        .collection("lectures")
                        .document(lectureTitle)
                        .collection("todos")
                        .document(todoId)
                        .update("done", isChecked)
                        .addOnSuccessListener(aVoid -> loadTodos()));

        deleteBtn.setOnClickListener(v ->
                db.collection("users")
                        .document(userEmail)
                        .collection("courses")
                        .document(courseName)
                        .collection("lectures")
                        .document(lectureTitle)
                        .collection("todos")
                        .document(todoId)
                        .delete()
                        .addOnSuccessListener(aVoid -> loadTodos()));

        row.addView(checkBox);
        row.addView(taskText);
        row.addView(deleteBtn);
        todoContainer.addView(row);
    }

    // ===== AI =====

    private void generateAISummary(String pdfText) {
        if (pdfText == null || pdfText.trim().isEmpty()) {
            txtSummary.setText(
                    "No PDF text available. Please re-upload your PDF.");
            return;
        }

        btnGenerateSummary.setEnabled(false);
        btnGenerateSummary.setText("Generating...");
        txtSummaryStatus.setText("Generating AI summary from PDF...");
        txtSummary.setText("");
        txtSummary.setTextColor(0xFF333333);

        GeminiHelper.summarize(pdfText, lectureTitle,
                new GeminiHelper.GeminiCallback() {
                    @Override
                    public void onSuccess(String result) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            txtSummary.setText(result);
                            txtSummary.setTextColor(0xFF1A1A1A);
                            txtSummaryStatus.setText("✅ Summary generated");
                            btnGenerateSummary.setEnabled(true);
                            btnGenerateSummary.setText("Regenerate");
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            android.util.Log.e("Gemini", "Failed: " + error);
                            if (error.contains("429")) {
                                txtSummary.setText(
                                        "Too many requests. Please wait a minute and try again.");
                            } else if (error.toLowerCase().contains("network")) {
                                txtSummary.setText(
                                        "Network error. Check your internet connection.");
                            } else if (error.contains("API key")) {
                                txtSummary.setText(
                                        "API key error. Check your Gemini API key.");
                            } else {
                                txtSummary.setText(
                                        "Failed to generate summary.\nError: " + error);
                            }
                            txtSummaryStatus.setText("Generation failed");
                            btnGenerateSummary.setEnabled(true);
                            btnGenerateSummary.setText("Try Again");
                        });
                    }
                });
    }

    // ===== HELPERS =====

    private void showFileChip(String fileName) {
        layoutFileChip.setVisibility(View.VISIBLE);
        txtFileName.setText(fileName);
    }

    private String getFileName(Uri uri) {
        String path = uri.getPath();
        if (path != null && path.contains("/")) {
            return path.substring(path.lastIndexOf("/") + 1);
        }
        return "Lecture.pdf";
    }
}