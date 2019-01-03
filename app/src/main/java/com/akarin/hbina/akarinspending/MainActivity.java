package com.akarin.hbina.akarinspending;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.akarin.hbina.akarinspending.Models.AkarinItem;
import com.akarin.hbina.akarinspending.Models.AkarinValue;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements OnChartValueSelectedListener {
    public static final String[] ARRAY_ITEM_TYPES = new String[]{"Others", "Food", "Grocery", "Rent"};
    private static final Long ONE_MONTH_IN_SECONDS = 2592000L;
    private static Integer counter = 0;
    private FirebaseUser user;
    private PieChart chart;

    private static void initializeHash(HashMap<String, AkarinValue> hash) {
        if (hash != null) {
            for (String a : ARRAY_ITEM_TYPES) {
                hash.put(a, new AkarinValue(a, 0f));
            }
        }
    }

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
            String userId = user.getUid();
            queryFirebase(userId);
        } else {
            onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.log_out:
                goToLoginActivity();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void goToLoginActivity() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        startActivity(intent);
        finish();
    }

    private void preparePieChart() {

        chart = findViewById(R.id.chart);

        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets(5, 10, 5, 5);
        chart.setDragDecelerationFrictionCoef(0.95f);
        chart.setCenterText(new SpannableString("Your expenditure\nof the past 30 days"));
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);
        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(110);
        chart.setHoleRadius(58f);
        chart.setTransparentCircleRadius(61f);
        chart.setDrawCenterText(true);
        chart.setRotationAngle(0);
        chart.setRotationEnabled(true);
        chart.setHighlightPerTapEnabled(true);
        chart.setOnChartValueSelectedListener(this);

        chart.animateY(1400, Easing.EaseInOutQuad);
        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        // entry label styling
        chart.setEntryLabelColor(Color.WHITE);
        chart.setEntryLabelTextSize(12f);
    }

    private void queryFirebase(String userId) {
        if (user != null) {
            Long latestUnixTime = System.currentTimeMillis() / 1000L;
            Long earliestUnixTime = latestUnixTime - ONE_MONTH_IN_SECONDS;

            final DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(userId);
            userReference.child("items").orderByChild("item_time").startAt(earliestUnixTime).endAt(latestUnixTime).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    HashMap<String, AkarinValue> hash = new HashMap<>();
                    initializeHash(hash);
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        HashMap<String, Object> itemMap = (HashMap<String, Object>) child.getValue();
                        if (itemMap != null) {
                            if (itemMap.get("item_type") != null && itemMap.get("item_price") != null && itemMap.get("item_time") != null) {
                                AkarinItem item = new AkarinItem((String) itemMap.get("item_type"), Float.valueOf(String.valueOf(itemMap.get("item_price"))), (Long) itemMap.get("item_time"));
                                hash.get(item.getItemType()).addItemPrice(item.getItemPrice());
                            } else {
                                Log.e(this.toString(), "Data received is corrupted");
                            }
                        }
                    }
                    drawPie(hash);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(this.toString(), "Unable to obtain AkarinItems");
                    Log.e(this.toString(), databaseError.getMessage());
                }
            });
        } else {
            goToLoginActivity();
        }
    }

    private void drawPie(HashMap<String, AkarinValue> hash) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (String a : ARRAY_ITEM_TYPES) {
            if (hash.get(a).getItemPrice() > 0f) {
                entries.add(new PieEntry(hash.get(a).getItemPrice(), hash.get(a).getItemType()));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expenditure");
        dataSet.setDrawIcons(false);

        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.MATERIAL_COLORS) {
            colors.add(c);
        }
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        chart.setData(data);
        chart.highlightValues(null);
        chart.invalidate();
    }

    @Override
    public void onValueSelected(Entry entry, Highlight highlight) {
        if (entry == null)
            return;
        Toast.makeText(
                getApplicationContext(),
                "Value: " + entry.getY() + ", index: " + highlight.getX() + ", DataSet index: " + highlight.getDataSetIndex(),
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onNothingSelected() {
        Log.d(this.toString(), "Nothing is selected...");
    }
}
