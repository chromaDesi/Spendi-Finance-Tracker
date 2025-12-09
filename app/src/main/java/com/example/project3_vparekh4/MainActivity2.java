package com.example.project3_vparekh4;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity2 extends AppCompatActivity implements ExpenseAdapter.OnExpenseListener {

    private FirebaseAuth auth;
    private GoogleSignInClient mGoogleSignInClient;
    final Calendar calendar = Calendar.getInstance();
    private ExpenseAdapter adapter;
    private RecyclerView recyclerView;
    private ArrayList<Expense> expenses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        auth = FirebaseAuth.getInstance();
        mGoogleSignInClient = GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build());
        expenses = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExpenseAdapter(expenses, MainActivity2.this);
        recyclerView.setAdapter(adapter);
        loadDB(calendar.getTime());


        Button b = findViewById(R.id.button3);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity2.this);
                builder.setTitle("Add Expense");
                View boxview = getLayoutInflater().inflate(R.layout.expense_maker, null);
                builder.setView(boxview);
                EditText name = boxview.findViewById(R.id.editTextText);
                EditText date = boxview.findViewById(R.id.editTextDate2);
                EditText category = boxview.findViewById(R.id.editTextText2);
                EditText amount = boxview.findViewById(R.id.editTextNumberDecimal);
                CheckBox reoccuring = boxview.findViewById(R.id.checkBox);
                date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DatePickerDialog datePicker = new DatePickerDialog(MainActivity2.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int yy, int mm, int dd) {
                                date.setText(String.format("%02d/%02d/%04d", mm + 1, dd, yy));
                                calendar.set(yy, mm, dd);
                                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
                                calendar.set(java.util.Calendar.MINUTE, 0);
                                calendar.set(java.util.Calendar.SECOND, 0);
                                calendar.set(Calendar.MILLISECOND, 0);
                            }
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                        datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
                        datePicker.show();
                    }
                });
                builder.setPositiveButton("Yes", (dialogInterface, i) -> {
                    if (name.getText().toString().isEmpty() || date.getText().toString().isEmpty() || category.getText().toString().isEmpty() || amount.getText().toString().isEmpty()) {
                        Toast.makeText(MainActivity2.this, "Please enter all fields", Toast.LENGTH_LONG).show();

                    }
                    else{
                        addExpense(name.getText().toString(), category.getText().toString(), calendar.getTime(), Double.parseDouble(amount.getText().toString()), reoccuring.isChecked());
                        Toast.makeText(MainActivity2.this, "New Expense added", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                });
                builder.setCancelable(false);
                builder.create().show();//display it
            }
        });
        //check a diff day
        Button changeDay = findViewById(R.id.button4);
        changeDay.setText(String.format("%02d/%02d/%04d",calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.YEAR)));
        changeDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePicker = new DatePickerDialog(MainActivity2.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int yy, int mm, int dd) {
                        changeDay.setText(String.format("%02d/%02d/%04d", mm + 1, dd, yy));
                        calendar.set(yy, mm, dd);
                        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
                        calendar.set(java.util.Calendar.MINUTE, 0);
                        calendar.set(java.util.Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        loadDB(calendar.getTime());

                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
                datePicker.show();
            }
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                return;
            }
        });

        //navbar transition
        BottomNavigationView navbar = findViewById(R.id.bottom_nav);
        navbar.setSelectedItemId(R.id.expenses);
        navbar.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.user){
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity2.this);
                    builder.setTitle("Signing Out");
                    builder.setMessage("Are you sure you want to log out?");
                    builder.setPositiveButton("Yes", (dialogInterface, i) -> {
                        auth.signOut();
                        mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Intent intent = new Intent(MainActivity2.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                Toast.makeText(MainActivity2.this, "Logged out", Toast.LENGTH_LONG).show();
                                startActivity(intent);
                                finish();
                            }
                        });
                    });
                    builder.setNegativeButton("No", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        navbar.setSelectedItemId(R.id.expenses);
                    });
                    builder.setCancelable(false);
                    builder.create().show();//display it
                    return true;
                }
                else if(item.getItemId() == R.id.target){//graph
                    Intent intent = new Intent(MainActivity2.this, MainActivity3.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                }
                else if(item.getItemId() == R.id.trends){//goals
                    Intent intent = new Intent(MainActivity2.this, MainActivity4.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                }
                else return true;
            }

        });
    }


    public void addExpense(String name, String category, Date date, double amount, boolean reoccuring){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("users").document(user.getUid()).collection("expenses").add(new Expense(amount, category, new Timestamp(date), name, reoccuring));
    }

    @Override
    public void expenseDelete(Expense expense) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("users").document(user.getUid()).collection("expenses").document(expense.getId()).delete();
    }


    public void loadDB(Date selectedDate){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        Calendar dstart = Calendar.getInstance();
        dstart.setTime(selectedDate);
        Calendar dend = Calendar.getInstance();
        dend.setTime(selectedDate);
        dstart.set(java.util.Calendar.HOUR_OF_DAY, 0);
        dstart.set(java.util.Calendar.MINUTE, 0);
        dstart.set(java.util.Calendar.SECOND, 0);
        dstart.set(Calendar.MILLISECOND, 0);
        dend.set(java.util.Calendar.HOUR_OF_DAY, 23);
        dend.set(java.util.Calendar.MINUTE, 59);
        dend.set(java.util.Calendar.SECOND, 59);
        dend.set(Calendar.MILLISECOND, 999);
        database.collection("users").document(user.getUid()).collection("expenses").whereGreaterThanOrEqualTo(
                "date",
                new Timestamp(dstart.getTime())).whereLessThanOrEqualTo("date", new Timestamp(dend.getTime())).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                }
                else{
                    expenses.clear();
                    for(int i = 0; i < value.size(); ++i){
                        Expense e = value.getDocuments().get(i).toObject(Expense.class);
                        e.setId(value.getDocuments().get(i).getId());
                        expenses.add(e);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });

    }

}