package com.example.aubstudynotesapp;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    ArrayList<Course> courses;

    public CourseAdapter(ArrayList<Course> courses) {
        this.courses = courses;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.course_item, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.txtCourseName.setText(course.getName());
        holder.txtSemester.setText(course.getSemester());

        // ✅ Semester badge — short version e.g. "S26"
        String semester = course.getSemester();
        String badge = semester.length() > 3
                ? semester.substring(0, 1) + semester.substring(semester.length() - 2)
                : semester;
        holder.txtSemesterBadge.setText(badge);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(
                    holder.itemView.getContext(),
                    LecturesActivity.class);
            intent.putExtra("courseName", course.getName());
            intent.putExtra("semester", course.getSemester());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView txtCourseName, txtSemester, txtSemesterBadge;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCourseName = itemView.findViewById(R.id.txtCourseName);
            txtSemester = itemView.findViewById(R.id.txtSemester);
            txtSemesterBadge = itemView.findViewById(R.id.txtSemesterBadge);
        }
    }
}