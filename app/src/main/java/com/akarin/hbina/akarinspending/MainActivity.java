package com.akarin.hbina.akarinspending;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.akarin.hbina.akarinspending.Models.AkarinItem;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements OnChartValueSelectedListener {
    private static final Long ONE_MONTH_IN_SECONDS = 2592000L;
    private static Integer counter = 0;
    private static HashMap<String, Integer> hash = new HashMap<>();
    private static ArrayList<NonFinalPair<String, Float>> arry = new ArrayList<>();
    private FirebaseUser user;
    private PieChart chart;

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

        chart = findViewById(R.id.chart);

        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets(5, 10, 5, 5);
        chart.setDragDecelerationFrictionCoef(0.95f);
        chart.setCenterText(generateCenterSpannableText());
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

    private SpannableString generateCenterSpannableText() {

        SpannableString s = new SpannableString("Your expenditure\nof the past 30 days");
        s.setSpan(new RelativeSizeSpan(1.7f), 0, 14, 0);
        s.setSpan(new StyleSpan(Typeface.NORMAL), 14, s.length() - 15, 0);
        s.setSpan(new ForegroundColorSpan(Color.GRAY), 14, s.length() - 15, 0);
        s.setSpan(new RelativeSizeSpan(.8f), 14, s.length() - 15, 0);
        s.setSpan(new StyleSpan(Typeface.ITALIC), s.length() - 14, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length() - 14, s.length(), 0);
        return s;
    }

    private void populatePieChart(String userId) {

        if (user != null) {
            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(userId);
            ref.child("current_timestamp").setValue(ServerValue.TIMESTAMP, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(final DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    databaseReference.addValueEventListener(new ValueEventListener() {
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
                                                AkarinItem item = new AkarinItem((String) itemMap.get("itemType"), Float.valueOf(String.valueOf(itemMap.get("itemPrice"))), (Long) itemMap.get("itemTime"));
                                                Log.d(this.toString(), "item:" + item.toString());
                                                if (hash.containsKey(item.getItemType())) {
                                                    Float currentValue = arry.get(hash.get(item.getItemType())).second;
                                                    arry.set(hash.get(item.getItemType()), new NonFinalPair<>(item.getItemType(), currentValue + item.getItemPrice()));
                                                } else {
                                                    Log.d(this.toString(), item.getItemType() + " is a new item type");
                                                    hash.put(item.getItemType(), counter++);
                                                    arry.add(new NonFinalPair<String, Float>(item.getItemType(), item.getItemPrice()));
                                                }
                                            } else {
                                                Log.e(this.toString(), "item is null");
                                            }
                                        }
                                        drawPie();
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

    private void drawPie() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (int i = 0; i < arry.size(); i++) {
            entries.add(new PieEntry(arry.get(i).second, arry.get(i).first));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expenditure");

        dataSet.setDrawIcons(false);

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
