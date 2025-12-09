package com.example.project3_vparekh4;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity3 extends AppCompatActivity implements BudgetAdapter.OnBudgetListener {


    private FirebaseAuth auth;
    private GoogleSignInClient mGoogleSignInClient;

    private BudgetAdapter adapter;
    private RecyclerView recyclerView;
    private ArrayList<Budget> budgets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main3);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        mGoogleSignInClient = GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build());
        budgets = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BudgetAdapter(budgets, MainActivity3.this);
        recyclerView.setAdapter(adapter);
        loadDB();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                return;
            }
        });
        Button b = findViewById(R.id.button3);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity3.this);
                builder.setTitle("Add Budget");
                View boxview = getLayoutInflater().inflate(R.layout.budget_maker, null);
                builder.setView(boxview);
                EditText name = boxview.findViewById(R.id.name);
                EditText progress = boxview.findViewById(R.id.progress);
                EditText amount = boxview.findViewById(R.id.goal);
                builder.setPositiveButton("Add", (dialogInterface, i) -> {
                    if (name.getText().toString().isEmpty() || progress.getText().toString().isEmpty() || amount.getText().toString().isEmpty()) {
                        Toast.makeText(MainActivity3.this, "Please enter all fields", Toast.LENGTH_LONG).show();
                    }
                    else{
                        addBudget(name.getText().toString(), Double.parseDouble(amount.getText().toString()), Double.parseDouble(progress.getText().toString()));
                        Toast.makeText(MainActivity3.this, "New Expense added", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                });
                builder.setCancelable(false);
                builder.create().show();//display it
            }
        });


        //navbar logic
        BottomNavigationView navbar = findViewById(R.id.bottom_nav);
        navbar.setSelectedItemId(R.id.target);
        navbar.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.user){
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity3.this);
                    builder.setTitle("Signing Out");
                    builder.setMessage("Are you sure you want to log out?");
                    builder.setPositiveButton("Yes", (dialogInterface, i) -> {
                        auth.signOut();
                        mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Intent intent = new Intent(MainActivity3.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                Toast.makeText(MainActivity3.this, "Logged out", Toast.LENGTH_LONG).show();
                                startActivity(intent);
                                finish();
                            }
                        });
                    });
                    builder.setNegativeButton("No", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        navbar.setSelectedItemId(R.id.target);
                    });
                    builder.setCancelable(false);
                    builder.create().show();//display it
                    return true;
                }
                else if(item.getItemId() == R.id.expenses){//expenses
                    Intent intent = new Intent(MainActivity3.this, MainActivity2.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                }
                else if(item.getItemId() == R.id.trends){//graph
                    Intent intent = new Intent(MainActivity3.this, MainActivity4.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                }
                else return true;
            }

        });
    }


    private void addBudget(String name, double goal, double progress){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("users").document(user.getUid()).collection("budgets").add(new Budget(name, goal, progress));
    }

    @Override
    public void budgetDelete(Budget budget) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("users").document(user.getUid()).collection("budgets").document(budget.getId()).delete();
    }

    @Override
    public void budgetEdit(Budget budget, double progress) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("users").document(user.getUid()).collection("budgets").document(budget.getId()).update("progress", progress);
    }

    public void loadDB(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("users").document(user.getUid()).collection("budgets").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){

                }
                else{
                    budgets.clear();
                    for(int i = 0; i < value.size(); ++i){
                        Budget b = value.getDocuments().get(i).toObject(Budget.class);
                        b.setId(value.getDocuments().get(i).getId());
                        budgets.add(b);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });

    }
}