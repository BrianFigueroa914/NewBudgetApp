package com.example.newbudgetapp;

public class BudgetCategory {
    public String category;
    public float limit;

    public BudgetCategory() {

    }

    public BudgetCategory(String category, float limit) {
        this.category = category;
        this.limit = limit;
    }
}
