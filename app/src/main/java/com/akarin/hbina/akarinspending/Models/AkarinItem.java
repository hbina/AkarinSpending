package com.akarin.hbina.akarinspending.Models;

import java.util.HashMap;
import java.util.Map;

public class AkarinItem {

    private String itemType;
    private double itemPrice;
    private Long itemTime;

    public AkarinItem() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public AkarinItem(String itemType, double itemPrice, Long itemTime) {
        this.itemType = itemType;
        this.itemPrice = itemPrice;
        this.itemTime = itemTime;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public double getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(double itemPrice) {
        this.itemPrice = itemPrice;
    }

    public Long getItemTime() {
        return itemTime;
    }

    public void setItemTime(Long itemTime) {
        this.itemTime = itemTime;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();

        map.put("itemType", itemType);
        map.put("itemPrice", itemPrice);
        map.put("itemTime", itemTime);

        return map;
    }

    public String toString() {
        return "AkarinItem itemType:" + itemType + " itemPrice:" + itemPrice + " itemTime:" + itemTime;
    }
}