package com.akarin.hbina.akarinspending.model;

import java.util.HashMap;
import java.util.Map;

public class AkarinValue {

  String itemType;
  Float itemPrice;

  AkarinValue() {
    // Required for Firebase
  }

  AkarinValue(String itemType, Float itemPrice) {
    this.itemType = itemType;
    this.itemPrice = itemPrice;
  }

  public String getItemType() {
    return this.itemType;
  }

  public void setItemType(String itemType) {
    this.itemType = itemType;
  }

  public Float getItemPrice() {
    return this.itemPrice;
  }

  public void setItemPrice(Float itemPrice) {
    this.itemPrice = itemPrice;
  }

  public void addItemPrice(Float f) {
    this.itemPrice += f;
  }

  public Map<String, Object> toMap() {
    HashMap<String, Object> map = new HashMap<>();

    map.put("itemType", itemType);
    map.put("itemPrice", itemPrice);

    return map;
  }

  public String toString() {
    return "AkarinValue itemType:" + itemType + " itemPrice:" + itemPrice;
  }
}
