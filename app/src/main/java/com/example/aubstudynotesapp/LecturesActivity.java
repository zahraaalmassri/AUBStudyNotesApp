package com.example.aubstudynotesapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class LecturesActivity extends AppCompatActivity {

    Button btnPdf, btnPdf2, btnPdf3, btnUpload;

    ActivityResultLauncher<String> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lectures);

        btnPdf = findViewById(R.id.btnPdf);
        btnPdf2 = findViewById(R.id.btnPdf2);
        btnPdf3 = findViewById(R.id.btnPdf3);

        btnUpload = findViewById(R.id.btnUpload);

        filePickerLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.GetContent(),
                        uri -> {

                            if (uri != null) {

                                Toast.makeText(
                                        LecturesActivity.this,
                                        "File Uploaded Successfully",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });

        View.OnClickListener pdfListener =
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent browserIntent =
                                new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://www.aub.edu.lb"));

                        startActivity(browserIntent);
                    }
                };

        btnPdf.setOnClickListener(pdfListener);
        btnPdf2.setOnClickListener(pdfListener);
        btnPdf3.setOnClickListener(pdfListener);

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                filePickerLauncher.launch("*/*");
            }
        });
    }
}