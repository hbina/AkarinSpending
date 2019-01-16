package com.akarin.hbina.akarinspending.util;

import android.util.Log;

import com.akarin.hbina.akarinspending.model.AkarinItem;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Container to valid instances of AkarinItem.
 */
public class AkarinDatabase {

    private static final Long UNIX_MONTH = 2592000L;
    private static final String TAG = "AkarinDatabase";
    private static AkarinDatabase akarinDatabase;
    private HashMap<String, AkarinItem> hashOfStringToAkarinItem = new HashMap<>();
    private HashMap<String, Integer> hashOfStringToInteger = new HashMap<>();
    private ArrayList<PieEntry> arrayOfPieEntries = new ArrayList<>();
    private int counter = 0;

    private AkarinDatabase() {
    }

    public static AkarinDatabase getDatabase() {
        if (akarinDatabase == null) {
            akarinDatabase = new AkarinDatabase();
        }
        return akarinDatabase;
    }

    public void addItem(String key, AkarinItem item) {
        addItemType(item.getItemType());
        if (!hashOfStringToAkarinItem.containsKey(key)) {
            Log.d(TAG, item.toString());
            addPriceToItemIndex(item.getItemType(), item.getItemPrice());
            hashOfStringToAkarinItem.put(key, item);
        }
    }

    private void addItemType(String itemType) {
        if (!hashOfStringToInteger.containsKey(itemType)) {
            hashOfStringToInteger.put(itemType, counter);
            arrayOfPieEntries.add(new PieEntry(0, itemType));
            counter++;
        }
    }

    private void pruneDatabase(Long currentTime) {
        for (String s : hashOfStringToAkarinItem.keySet()) {
            if (hashOfStringToAkarinItem.get(s).getItemTime() < (currentTime - UNIX_MONTH)) {
                addPriceToItemIndex(hashOfStringToAkarinItem.get(s).getItemType(), -hashOfStringToAkarinItem.get(s).getItemPrice());
                hashOfStringToAkarinItem.remove(s);
            }
        }
    }

    private void addPriceToItemIndex(String itemType, Float price) {
        int indexOfItemType = hashOfStringToInteger.get(itemType);
        float itemTypePriceSum = price + arrayOfPieEntries.get(indexOfItemType).getValue();
        arrayOfPieEntries.set(indexOfItemType, new PieEntry(itemTypePriceSum, itemType));
    }

    public ArrayList<PieEntry> getAllItemAsPieEntries() {
        pruneDatabase(System.currentTimeMillis());
        return arrayOfPieEntries;
    }
}
