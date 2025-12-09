package com.example.project3_vparekh4;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.MenuItem;
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

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
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
import java.util.HashMap;

public class MainActivity4 extends AppCompatActivity {


    private FirebaseAuth auth;
    private GoogleSignInClient mGoogleSignInClient;
    private PieChart piechart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main4);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        mGoogleSignInClient = GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                return;
            }
        });
        piechart = findViewById(R.id.chart);
        piechart.setUsePercentValues(true);
        piechart.getDescription().setEnabled(false);
        piechart.setBackgroundColor(Color.BLACK);
        piechart.setDrawCenterText(true);
        piechart.setCenterText("Weekly\nHabits");
        piechart.setCenterTextColor(Color.BLACK);
        piechart.setCenterTextSize(20f);
        piechart.setCenterTextTypeface(Typeface.DEFAULT_BOLD);
        Legend legends = piechart.getLegend();
        legends.setTextSize(20f);
        legends.setTextColor(Color.WHITE);
        legends.setFormSize(20f);
        legends.setWordWrapEnabled(true);
        legends.setForm(Legend.LegendForm.CIRCLE);
        displayPiechart();
        //navbar logic
        BottomNavigationView navbar = findViewById(R.id.bottom_nav);
        navbar.setSelectedItemId(R.id.trends);
        navbar.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.user){
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity4.this);
                    builder.setTitle("Signing Out");
                    builder.setMessage("Are you sure you want to log out?");
                    builder.setPositiveButton("Yes", (dialogInterface, i) -> {
                        auth.signOut();
                        mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Intent intent = new Intent(MainActivity4.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                Toast.makeText(MainActivity4.this, "Logged out", Toast.LENGTH_LONG).show();
                                startActivity(intent);
                                finish();
                            }
                        });
                    });
                    builder.setNegativeButton("No", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        navbar.setSelectedItemId(R.id.trends);
                    });
                    builder.setCancelable(false);
                    builder.create().show();//display it
                    return true;
                }
                else if(item.getItemId() == R.id.expenses){//expenses
                    Intent intent = new Intent(MainActivity4.this, MainActivity2.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                }
                else if(item.getItemId() == R.id.target){//graph
                    Intent intent = new Intent(MainActivity4.this, MainActivity3.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                }
                else return true;
            }

        });
    }



    public void displayPiechart(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        Calendar dstart = Calendar.getInstance();
        dstart.add(java.util.Calendar.DAY_OF_YEAR, -7);
        dstart.set(java.util.Calendar.HOUR_OF_DAY, 0);
        dstart.set(java.util.Calendar.MINUTE, 0);
        dstart.set(java.util.Calendar.SECOND, 0);
        dstart.set(Calendar.MILLISECOND, 0);
        database.collection("users").document(user.getUid()).collection("expenses").whereGreaterThanOrEqualTo("date", new Timestamp(dstart.getTime())).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value == null || error != null){
                    Toast.makeText(MainActivity4.this, "Error loading Firebase/Empty Read", Toast.LENGTH_LONG).show();
                }
                else{
                    HashMap<String, Double> data = new HashMap<>();
                    for(int i = 0; i < value.size(); ++i) {
                        Expense e = value.getDocuments().get(i).toObject(Expense.class);
                        if (data.containsKey(e.getCategory())) {
                            data.put(e.getCategory(), data.get(e.getCategory()) + e.getExpense());
                        } else {
                            data.put(e.getCategory(), e.getExpense());
                        }
                    }
                    ArrayList<PieEntry> entries = new ArrayList<>();
                    for (String key : data.keySet()) entries.add(new PieEntry(data.get(key).floatValue(), key));
                    PieDataSet ds = new PieDataSet(entries, "");
                    ArrayList<Integer> colors = new ArrayList<>();
                    for(int i = 0; i < entries.size(); ++i){
                        colors.add(Color.rgb((int)(Math.random() * 256), (int)(Math.random() * 256), (int)(Math.random() * 256)));
                    }
                    ds.setColors(colors);
                    ds.setSliceSpace(2f);
                    ds.setSelectionShift(4f);
                    PieData pd = new PieData(ds);
                    pd.setValueFormatter(new PercentFormatter(piechart));
                    pd.setValueTextSize(16f);
                    pd.setValueTextColor(Color.BLACK);
                    pd.setValueTypeface(Typeface.DEFAULT_BOLD);
                    piechart.setData(pd);
                    piechart.animateY(5000, Easing.EaseOutCirc);
                    piechart.invalidate();
                }
            }
        });
    }
}