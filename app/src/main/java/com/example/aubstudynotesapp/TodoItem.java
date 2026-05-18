package com.example.aubstudynotesapp;

public class TodoItem {
    String id;
    String task;
    boolean done;

    public TodoItem(String id, String task, boolean done) {
        this.id = id;
        this.task = task;
        this.done = done;
    }
}