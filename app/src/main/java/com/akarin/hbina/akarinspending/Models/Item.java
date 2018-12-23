package com.akarin.hbina.akarinspending.Models;

import java.util.HashMap;
import java.util.Map;

public class Item {

    public String itemName;
    public double itemPrice;

    public Item() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Item(String itemName, double itemPrice) {
        this.itemName = itemName;
        this.itemPrice = itemPrice;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();

        map.put("itemName", itemName);
        map.put("itemPrice", itemPrice);

        return map;
    }
}