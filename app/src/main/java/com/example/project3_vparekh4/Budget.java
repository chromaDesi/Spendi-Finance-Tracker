package com.example.project3_vparekh4;

public class Budget {
    private String name;
    private double goal;
    private double progress;
    private String id;

    public Budget() {}

    public Budget(String name, double goal, double progress) {
        this.name = name;
        this.goal = goal;
        this.progress = progress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getGoal() {
        return goal;
    }

    public void setGoal(double goal) {
        this.goal = goal;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
