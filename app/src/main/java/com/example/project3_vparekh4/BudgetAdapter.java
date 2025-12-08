package com.example.project3_vparekh4;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {
    private List<Budget> budgets;
    private Context context;
    private OnBudgetListener onBudgetListener;


    public interface OnBudgetListener{
        void budgetDelete(Budget budget);
    }

    public BudgetAdapter(List<Budget> budgets, Context context){
        this.budgets = budgets;
        this.context = context;
    }

    public class BudgetViewHolder extends RecyclerView.ViewHolder{
        public TextView name;
        public TextView goal;
        public TextView progress;
        public BudgetViewHolder(View itemView){
            super(itemView);
            name = itemView.findViewById(R.id.budgetName);
            goal = itemView.findViewById(R.id.goal);
            progress = itemView.findViewById(R.id.current);
        }
    }

    @Override
    public BudgetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BudgetAdapter.BudgetViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.budget_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        holder.name.setText(budgets.get(position).getName());
        holder.goal.setText(String.format("%.2f", budgets.get(position).getGoal()));
        holder.progress.setText(String.format("%.2f", budgets.get(position).getProgress()));
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete Budget Goal?");
                builder.setMessage("Are you sure you want to delete this budget?");
                builder.setPositiveButton("Yes", (dialogInterface, i) -> {
                    onBudgetListener.budgetDelete(budgets.get(position));
                    budgets.remove(position);
                    notifyItemRemoved(position);
                });
                builder.setNegativeButton("No", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                });
                builder.setCancelable(false);
                builder.create().show();//display it
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }


}
