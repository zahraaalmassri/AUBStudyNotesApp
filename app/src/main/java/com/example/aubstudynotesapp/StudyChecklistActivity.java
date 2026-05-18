package com.example.aubstudynotesapp;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class StudyChecklistActivity extends AppCompatActivity {

    EditText etTask;

    Button btnAddTask;

    LinearLayout tasksContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_checklist);

        etTask = findViewById(R.id.etTask);

        btnAddTask = findViewById(R.id.btnAddTask);

        tasksContainer = findViewById(R.id.tasksContainer);

        btnAddTask.setOnClickListener(v -> {

            String task =
                    etTask.getText().toString().trim();

            if (!task.isEmpty()) {

                addTask(task);

                etTask.setText("");
            }
        });
    }

    private void addTask(String taskText) {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.HORIZONTAL);

        card.setGravity(Gravity.CENTER_VERTICAL);

        card.setPadding(24,24,24,24);

        card.setBackgroundResource(
                android.R.drawable.dialog_holo_light_frame);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        params.setMargins(0,0,0,20);

        card.setLayoutParams(params);

        CheckBox checkBox =
                new CheckBox(this);

        TextView task =
                new TextView(this);

        task.setText(taskText);

        task.setTextSize(16);

        task.setTextColor(0xFF333333);

        task.setPadding(12,0,0,0);

        checkBox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if(isChecked) {

                        task.setPaintFlags(
                                task.getPaintFlags()
                                        | Paint.STRIKE_THRU_TEXT_FLAG);

                        task.setAlpha(0.5f);

                    } else {

                        task.setPaintFlags(0);

                        task.setAlpha(1f);
                    }
                });

        card.addView(checkBox);

        card.addView(task);

        tasksContainer.addView(card);
    }
}