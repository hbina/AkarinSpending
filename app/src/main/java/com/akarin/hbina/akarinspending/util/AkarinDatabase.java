package com.akarin.hbina.akarinspending.util;

import android.util.Log;

import com.akarin.hbina.akarinspending.model.AkarinItem;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.HashMap;


public class AkarinDatabase {

  private static final Long UNIX_MONTH = 2592000L;
  private static final String TAG = "AkarinDatabase";
  private static AkarinDatabase akarinDatabase;
  private HashMap<String, AkarinItem> itemHash = new HashMap<>();
  private HashMap<String, Integer> itemIndexHash = new HashMap<>();
  private ArrayList<PieEntry> alpe = new ArrayList<>();
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
    if (!itemIndexHash.containsKey(item.getItemType())) {
      itemIndexHash.put(item.getItemType(), counter);
      alpe.add(new PieEntry(0, item.getItemType()));
      counter++;
    }
    if (!itemHash.containsKey(key)) {
      Log.d(TAG, item.toString());
      addPriceToItemIndex(item.getItemType(), item.getItemPrice());
      itemHash.put(key, item);
    }
  }

  public ArrayList<AkarinItem> getAllItem() {
    pruneDatabase(System.currentTimeMillis());
    return new ArrayList<>(itemHash.values());
  }

  private void pruneDatabase(Long currentTime) {
    for (String s : itemHash.keySet()) {
      if (itemHash.get(s).getItemTime() < (currentTime - UNIX_MONTH)) {
        addPriceToItemIndex(itemHash.get(s).getItemType(), -itemHash.get(s).getItemPrice());
        itemHash.remove(s);
      }
    }
  }

  private void addPriceToItemIndex(String itemType, Float price) {
    int indexOfItemType = itemIndexHash.get(itemType);
    Float itemTypePriceSum = price + alpe.get(indexOfItemType).getValue();
    alpe.set(indexOfItemType, new PieEntry(itemTypePriceSum, itemType));
  }

  public ArrayList<PieEntry> getAllItemAsPieEntries() {
    return alpe;
  }
}
