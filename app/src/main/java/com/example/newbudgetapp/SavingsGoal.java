package com.example.newbudgetapp;

public class SavingsGoal {
    public String goalName;
    public float goalAmount;
    public String deadline;
    public float currentAmount;

    public SavingsGoal() {

    }

    public SavingsGoal(String goalName, float goalAmount, String deadline) {
        this.goalName = goalName;
        this.goalAmount = goalAmount;
        this.deadline = deadline;
        this.currentAmount = 0;
    }

    public SavingsGoal(String goalName, float goalAmount, String deadline, float currentAmount) {
        this.goalName = goalName;
        this.goalAmount = goalAmount;
        this.deadline = deadline;
        this.currentAmount = currentAmount;
    }
}
