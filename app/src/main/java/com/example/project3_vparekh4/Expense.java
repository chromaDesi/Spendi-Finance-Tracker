package com.example.project3_vparekh4;


import com.google.firebase.Timestamp;
public class Expense {

    private double expense;
    private String category;
    private Timestamp date;
    private String name;

    private boolean reoccuring;
    private String id;

    public Expense(){}

    public Expense(double expense, String category, Timestamp date, String name, boolean reoccuring) {
        this.expense = expense;
        this.category = category;
        this.date = date;
        this.name = name;
        this.reoccuring = reoccuring;
    }

    public double getExpense() {
        return expense;
    }

    public void setExpense(double expense) {
        this.expense = expense;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isReoccuring(){
        return reoccuring;
    }

    public boolean setReoccuring(boolean reoccuring){
        return this.reoccuring = reoccuring;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
}
