package com.example.project3_vparekh4;

import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
        void budgetEdit(Budget budget, double progress);
    }

    public BudgetAdapter(List<Budget> budgets, Context context){
        this.budgets = budgets;
        this.context = context;
        if (context instanceof BudgetAdapter.OnBudgetListener) onBudgetListener = (BudgetAdapter.OnBudgetListener) context;
        else throw new RuntimeException(context.toString()+ " something messed up");
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
        holder.goal.setText(String.format("Goal: $%.2f", budgets.get(position).getGoal()));
        holder.progress.setText(String.format("So far: $%.2f", budgets.get(position).getProgress()));
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
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Edit Budget Goal");
                final EditText newProgress = new EditText(context);
                newProgress.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                newProgress.setHint("New Progress");
                builder.setView(newProgress);
                builder.setPositiveButton("Confirm", (dialogInterface, i) -> {
                    if (newProgress.getText().toString().isEmpty()) {
                        Toast.makeText(context, "No Empty Fields", Toast.LENGTH_LONG).show();
                    }
                    else{
                        if(Double.parseDouble(newProgress.getText().toString()) > budgets.get(position).getGoal()){
                            Toast.makeText(context, "Progress cannot be greater than goal", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if(Double.parseDouble(newProgress.getText().toString()) == budgets.get(position).getGoal()){
                            Toast.makeText(context, "YAYY!! Goal Reached", Toast.LENGTH_SHORT).show();
                        }
                        onBudgetListener.budgetEdit(budgets.get(position), Double.parseDouble(newProgress.getText().toString()));
                    }

                });
                builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                });
                builder.setCancelable(false);
                builder.create().show();//display it
            }
        });
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }


}
