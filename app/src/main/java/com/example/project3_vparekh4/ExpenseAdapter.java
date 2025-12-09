package com.example.project3_vparekh4;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {
    private List<Expense> expenses;
    private Context context;
    private OnExpenseListener onExpenseListener;



    public interface OnExpenseListener{
        void expenseDelete(Expense expense);
    }

    public ExpenseAdapter(List<Expense> expenses, Context context) {
        this.expenses = expenses;
        this.context = context;

        if (context instanceof OnExpenseListener) onExpenseListener = (OnExpenseListener) context;
        else throw new RuntimeException(context.toString()+ " something messed up");
    }

    public class ExpenseViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView amount;
        public TextView category;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.nameBill);
            amount = itemView.findViewById(R.id.amountBill);
            category = itemView.findViewById(R.id.categoryBill);
        }
    }

    @Override
    public ExpenseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ExpenseViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseAdapter.ExpenseViewHolder holder, int position) {
        holder.amount.setText(String.format("$%.2f", expenses.get(position).getExpense()));
        holder.name.setText(expenses.get(position).getName());
        holder.category.setText(expenses.get(position).getCategory());
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete Expense");
                builder.setMessage("Are you sure you want to delete this expense?");
                builder.setPositiveButton("Yes", (dialogInterface, i) -> {
                    onExpenseListener.expenseDelete(expenses.get(position));
                    expenses.remove(position);
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
        return expenses.size();
    }
}
