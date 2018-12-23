package com.akarin.hbina.akarinspending;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.akarin.hbina.akarinspending.Models.Item;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SubmitNewItem extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private DatabaseReference mDatabase;
    private EditText itemPriceText;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_new_item);
        user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Button cancelBtn = findViewById(R.id.btn_cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button submitBtn = findViewById(R.id.btn_submit);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItem(user.getUid(), "dummy", 123.45);
                finish();
            }
        });

        Spinner spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
    }

    private void addItem(String userId, String itemName, double itemPrice) {
        Item user = new Item(itemName, itemPrice);

        String key = mDatabase.child("users").child(userId).push().getKey();

        Map<String, Object> newItem = user.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/users/" + userId + "/" + key, newItem);

        mDatabase.updateChildren(childUpdates);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d(this.toString(), (String) adapterView.getItemAtPosition(i));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
