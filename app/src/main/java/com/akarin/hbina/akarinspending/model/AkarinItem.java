package com.akarin.hbina.akarinspending.model;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class AkarinItem extends AkarinValue implements Comparable<Long> {

  private Long itemTime;

  public AkarinItem() {
    // Required for Firebase
  }

  public AkarinItem(String itemType, Float itemPrice, Long itemTime) {
    super(itemType, itemPrice);
    this.itemTime = itemTime;
  }

  public AkarinItem(String itemType, Float itemPrice) {
    super(itemType, itemPrice);
    this.itemTime = System.currentTimeMillis();
  }

  public Long getItemTime() {
    return itemTime;
  }

  public void setItemTime(Long itemTime) {
    this.itemTime = itemTime;
  }

  @Override
  public Map<String, Object> toMap() {
    HashMap<String, Object> map = new HashMap<>();

    map.put("itemType", itemType);
    map.put("itemPrice", itemPrice);
    map.put("itemTime", itemTime);

    return map;
  }

  public String toString() {
    return "AkarinItem super:" + super.toString() + " itemTime:" + itemTime;
  }

  @Override
  public int compareTo(@NonNull Long aLong) {
    return Long.compare(itemTime, aLong);
  }
}