package com.akarin.hbina.akarinspending;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.akarin.hbina.akarinspending.Models.AkarinItem;
import com.github.mikephil.charting.charts.PieChart;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.nio.DoubleBuffer;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private static final Long ONE_MONTH_IN_SECONDS = 2592000L;
    private FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preparePieChart();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SubmitNewItem.class);
                startActivity(intent);
            }
        });

        Toolbar myToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            boolean emailVerified = user.isEmailVerified();
            String userId = user.getUid();
            populatePieChart(userId);
        } else {
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.log_out:
                FirebaseAuth.getInstance().signOut();
                Log.d(this.toString(), "Logging user out");
                goToLoginActivity();
                return true;

            default:
                Log.d(this.toString(), "Unrecognized menu item");
                return super.onOptionsItemSelected(item);
        }
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        startActivity(intent);
        finish();
    }

    private void preparePieChart() {
        PieChart chart = findViewById(R.id.chart);
    }

    private void populatePieChart(String userId) {

        if (user != null) {
            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(userId);
            ref.child("current_timestamp").setValue(ServerValue.TIMESTAMP, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(final DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Long server_timestamp = dataSnapshot.getValue(Long.class);
                            if (server_timestamp != null) {
                                Long earliestUnixTime = server_timestamp - ONE_MONTH_IN_SECONDS;
                                ref.child("items").orderByChild("itemTime").startAt(earliestUnixTime).endAt(server_timestamp).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Log.d(this.toString(), "There are " + dataSnapshot.getChildrenCount() + " items");
                                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                                            HashMap<String, Object> itemMap = (HashMap<String, Object>) child.getValue();
                                            if (itemMap != null) {
                                                AkarinItem item = new AkarinItem((String) itemMap.get("itemType"), Double.valueOf(String.valueOf(itemMap.get("itemPrice"))), (Long) itemMap.get("itemTime"));
                                                Log.d(this.toString(), "item:" + item.toString());
                                            } else {
                                                Log.e(this.toString(), "item is null");
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.e(this.toString(), "Unable to obtain AkarinItems");
                                        Log.e(this.toString(), databaseError.getMessage());
                                    }
                                });
                            } else {
                                Log.e(this.toString(), "Unable to obtain timestamp from snapshot");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(this.toString(), "Unable to obtain current timestamp");
                            Log.e(this.toString(), databaseError.getMessage());
                        }
                    });
                }
            });
        } else {
            Log.d(this.toString(), "User is null");
            goToLoginActivity();
        }
    }
}
