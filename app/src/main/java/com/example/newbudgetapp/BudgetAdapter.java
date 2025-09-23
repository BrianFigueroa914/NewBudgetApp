package com.example.newbudgetapp;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private final List<BudgetCategory> categoryList;
    private final Map<String, Float> spendingMap;
    private final String userID;

    public BudgetAdapter(List<BudgetCategory> categoryList, Map<String, Float> spendingMap, String userID) {
        this.categoryList = categoryList;
        this.spendingMap = spendingMap;
        this.userID = userID;
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_budget_category, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        BudgetCategory category = categoryList.get(position);
        float spent = spendingMap.getOrDefault(category.category, 0f);
        float limit = category.limit;

        holder.categoryName.setText(category.category);
        holder.limit.setText("Limit: $" + limit);
        holder.spentLabel.setText("Spent: $" + spent);
        int progress = (int) ((spent / limit) * 100);
        holder.progress.setProgress(Math.min(progress, 100));

        holder.optionsMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), holder.optionsMenu);
            popup.getMenuInflater().inflate(R.menu.budget_item_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_delete_budget) {
                    confirmDeleteBudget(category, position, v);
                    return true;
                }
                return false;
            });

            popup.show();
        });
    }

    private void confirmDeleteBudget(BudgetCategory category, int position, View contextView) {
        new AlertDialog.Builder(contextView.getContext())
                .setTitle("Delete Budget")
                .setMessage("Delete budget for \"" + category.category + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    FirebaseFirestore.getInstance()
                            .collection("Users")
                            .document(userID)
                            .get()
                            .addOnSuccessListener(doc -> {
                                List<Map<String, Object>> updatedList = (List<Map<String, Object>>) doc.get("budgets");
                                if (updatedList != null) {
                                    updatedList.removeIf(b -> category.category.equals(b.get("category")));
                                    FirebaseFirestore.getInstance()
                                            .collection("Users")
                                            .document(userID)
                                            .update("budgets", updatedList)
                                            .addOnSuccessListener(aVoid -> {
                                                categoryList.remove(position);
                                                notifyItemRemoved(position);
                                            });
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName, limit, spentLabel;
        ProgressBar progress;
        ImageView optionsMenu;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.itemCategoryName);
            limit = itemView.findViewById(R.id.itemLimit);
            spentLabel = itemView.findViewById(R.id.itemSpentLabel);
            progress = itemView.findViewById(R.id.budgetProgress);
            optionsMenu = itemView.findViewById(R.id.itemBudgetOptionsMenu);
        }
    }
}
