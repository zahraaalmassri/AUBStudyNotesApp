package com.example.aubstudynotesapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button btnLogin;

    EditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        btnLogin = findViewById(R.id.btnLogin);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email =
                        etEmail.getText().toString().trim();

                String password =
                        etPassword.getText().toString().trim();

                if(email.isEmpty() || password.isEmpty()) {

                    Toast.makeText(
                            MainActivity.this,
                            "Please fill all fields",
                            Toast.LENGTH_SHORT
                    ).show();

                } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

                    Toast.makeText(
                            MainActivity.this,
                            "Enter valid email",
                            Toast.LENGTH_SHORT
                    ).show();

                } else if(password.length() < 6) {

                    Toast.makeText(
                            MainActivity.this,
                            "Password must be at least 6 characters",
                            Toast.LENGTH_SHORT
                    ).show();

                } else {

                    Intent intent =
                            new Intent(MainActivity.this,
                                    CoursesActivity.class);

                    startActivity(intent);
                }
            }
        });
    }
}