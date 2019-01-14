package com.akarin.hbina.akarinspending.activity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

import com.akarin.hbina.akarinspending.R;
import com.akarin.hbina.akarinspending.model.AkarinItem;
import com.akarin.hbina.akarinspending.util.AkarinDatabase;
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

import java.util.ArrayList;

public class Main extends AppCompatActivity implements OnChartValueSelectedListener {
  public static final String[] ARRAY_ITEM_TYPES = new String[]{ "Others", "Food", "Grocery", "Rent" };
  private static final Long ONE_MONTH_IN_SECONDS = 2592000L;
  private static final String TAG = "Main";
  private static FirebaseUser user;
  private static PieChart chart;

  private AkarinDatabase akarinDatabase = AkarinDatabase.getDatabase();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    preparePieChart();

    FloatingActionButton fab = findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(Main.this, SubmitItem.class);
        startActivity(intent);
      }
    });

    user = FirebaseAuth.getInstance().getCurrentUser();
    if (user != null) {
      String userId = user.getUid();
      setupFirebaseConnection(userId);
    } else {
      goToLoginActivity();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
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
    Log.v(TAG, "goToLoginActivity()");

    FirebaseAuth.getInstance().signOut();
    Intent intent = new Intent(getApplicationContext(), Login.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
    startActivity(intent);
  }

  private void preparePieChart() {
    Log.v(TAG, "preparePieChart()");

    // Set up chart
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

    // Set up legend
    Legend legend = chart.getLegend();
    legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
    legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
    legend.setOrientation(Legend.LegendOrientation.VERTICAL);
    legend.setDrawInside(false);
    legend.setXEntrySpace(7f);
    legend.setYEntrySpace(0f);
    legend.setYOffset(0f);

    // entry label styling
    chart.setEntryLabelColor(Color.WHITE);
    chart.setEntryLabelTextSize(12f);
  }

  private void setupFirebaseConnection(String userId) {
    Log.v(TAG, "setupFirebaseConnection( userId:" + userId + ")");
    if (user != null) {
      Long latestUnixTime = System.currentTimeMillis() / 1000L;
      Long earliestUnixTime = latestUnixTime - ONE_MONTH_IN_SECONDS;

      final DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users")
          .child(userId);
      userReference.child("items").orderByChild("itemTime").startAt(earliestUnixTime)
          .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
              for (DataSnapshot child : dataSnapshot.getChildren()) {
                AkarinItem akarinItem = child.getValue(AkarinItem.class);
                if (akarinItem != null) {
                  Log.d(TAG, akarinItem.toString());
                  akarinDatabase.addItem(child.getKey(), akarinItem);
                }
              }
              refreshPie();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
              Log.e(TAG, databaseError.getMessage());
            }
          });
    } else {
      goToLoginActivity();
    }
  }

  private void refreshPie() {
    Log.v(TAG, "refreshPie()");
    ArrayList<PieEntry> entries = akarinDatabase.getAllItemAsPieEntries();
    if (entries.size() > 0) {

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
  }

  @Override
  public void onValueSelected(Entry entry, Highlight highlight) {
    Log.v(TAG, "onValueSelected(entry:" + entry + " hihglight:" + highlight + ")");
    if (entry != null) {
      /*
      TODO: Start another activity to show the list of items
       */
    }
  }

  @Override
  public void onNothingSelected() {
    Log.v(TAG, "onNothingSelected()");
  }
}
