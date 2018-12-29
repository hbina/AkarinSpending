package com.akarin.hbina.akarinspending.Models;

import java.util.HashMap;
import java.util.Map;

public class AkarinItem extends AkarinValue {

    private Long itemTime;

    public AkarinItem() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public AkarinItem(String itemType, Float itemPrice, Long itemTime) {
        super(itemType, itemPrice);
        this.itemTime = itemTime;
    }

    public AkarinItem(String itemType, Float itemPrice) {
        super(itemType, itemPrice);
        this.itemTime = System.currentTimeMillis() / 1000L;
    }

    public Long getItemTime() {
        return itemTime;
    }

    public void setItemTime(Long itemTime) {
        this.itemTime = itemTime;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();

        map.put("item_type", itemType);
        map.put("item_price", itemPrice);
        map.put("item_time", itemTime);

        return map;
    }

    public String toString() {
        return "AkarinItem super:" + super.toString() + " itemTime:" + itemTime;
    }
}