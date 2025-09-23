package com.example.newbudgetapp;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class SavingsGoalAdapter extends RecyclerView.Adapter<SavingsGoalAdapter.GoalViewHolder> {

    private final List<SavingsGoal> goalList;
    private final String userID;

    public SavingsGoalAdapter(List<SavingsGoal> goalList, String userID) {
        this.goalList = goalList;
        this.userID = userID;
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_savings_goal, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        SavingsGoal goal = goalList.get(position);

        holder.goalName.setText(goal.goalName);
        holder.deadline.setText("Deadline: " + goal.deadline);
        holder.targetAmount.setText("Target: $" + goal.goalAmount);
        holder.remaining.setText("Remaining: $" + (goal.goalAmount - goal.currentAmount));
        float progressPercent = (goal.currentAmount / goal.goalAmount) * 100f;
        holder.progress.setProgress((int) progressPercent);

        holder.optionsMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), holder.optionsMenu);
            popup.getMenuInflater().inflate(R.menu.goal_item_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_add) {
                    showAddDialog(holder, goal, position);
                    return true;
                } else if (id == R.id.menu_delete) {
                    confirmDelete(goal, position, v);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    private void showAddDialog(GoalViewHolder holder, SavingsGoal goal, int position) {
        EditText input = new EditText(holder.itemView.getContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter amount to add");
        input.setPadding(40, 20, 40, 20);

        new AlertDialog.Builder(holder.itemView.getContext())
                .setTitle("Add to Savings")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String value = input.getText().toString().trim();
                    if (value.isEmpty()) {
                        Toast.makeText(holder.itemView.getContext(), "Amount required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    float amount = Float.parseFloat(value);
                    if (goal.currentAmount + amount > goal.goalAmount) {
                        Toast.makeText(holder.itemView.getContext(), "Exceeds goal amount!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    updateFirestoreGoal(goal.goalName, goal.currentAmount + amount, position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(SavingsGoal goal, int position, View contextView) {
        new AlertDialog.Builder(contextView.getContext())
                .setTitle("Delete Goal")
                .setMessage("Are you sure you want to delete \"" + goal.goalName + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteGoal(goal, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateFirestoreGoal(String goalName, float newAmount, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDoc = db.collection("Users").document(userID);

        userDoc.get().addOnSuccessListener(doc -> {
            List<Map<String, Object>> goalArray = (List<Map<String, Object>>) doc.get("savingsGoals");
            if (goalArray != null) {
                float previousAmount = goalList.get(position).currentAmount;
                float amountAdded = newAmount - previousAmount;

                for (Map<String, Object> map : goalArray) {
                    if (goalName.equals(map.get("goalName"))) {
                        map.put("currentAmount", newAmount);
                    }
                }



                userDoc.update("savingsGoals", goalArray)
                        .addOnSuccessListener(aVoid -> {
                            goalList.get(position).currentAmount = newAmount;
                            notifyItemChanged(position);

                            Map<String, Object> expenseEntry = new HashMap<>();
                            expenseEntry.put("amount", amountAdded);
                            expenseEntry.put("category", "Saved to Goal: " + goalName);
                            expenseEntry.put("timestamp", com.google.firebase.Timestamp.now());

                            List<Map<String, Object>> expenseList = (List<Map<String, Object>>) doc.get("expenseEntries");
                            if (expenseList == null) expenseList = new ArrayList<>();
                            expenseList.add(expenseEntry);

                            userDoc.update("expenseEntries", expenseList);
                        });
            }
        });
    }


    private void deleteGoal(SavingsGoal goal, int position) {
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(userID)
                .get()
                .addOnSuccessListener(doc -> {
                    List<Map<String, Object>> goalArray = (List<Map<String, Object>>) doc.get("savingsGoals");
                    if (goalArray != null) {
                        goalArray.removeIf(g -> goal.goalName.equals(g.get("goalName")));
                        FirebaseFirestore.getInstance()
                                .collection("Users")
                                .document(userID)
                                .update("savingsGoals", goalArray)
                                .addOnSuccessListener(aVoid -> {
                                    goalList.remove(position);
                                    notifyItemRemoved(position);
                                });
                    }
                });
    }

    @Override
    public int getItemCount() {
        return goalList.size();
    }

    public static class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView goalName, deadline, targetAmount, remaining;
        ProgressBar progress;
        ImageView optionsMenu;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            goalName = itemView.findViewById(R.id.itemGoalName);
            deadline = itemView.findViewById(R.id.itemDeadline);
            targetAmount = itemView.findViewById(R.id.itemTargetAmount);
            remaining = itemView.findViewById(R.id.itemRemainingAmount);
            progress = itemView.findViewById(R.id.itemProgressBar);
            optionsMenu = itemView.findViewById(R.id.itemOptionsMenu);
        }
    }
}
