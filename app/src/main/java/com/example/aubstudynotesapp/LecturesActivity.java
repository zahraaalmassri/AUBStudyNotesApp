package com.example.aubstudynotesapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LecturesActivity extends AppCompatActivity {

    Button btnPdf, btnUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lectures);

        btnPdf = findViewById(R.id.btnPdf);
        btnUpload = findViewById(R.id.btnUpload);

        btnPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent browserIntent =
                        new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://www.aub.edu.lb"));

                startActivity(browserIntent);
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(
                        LecturesActivity.this,
                        "Upload feature coming soon",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}