package com.akarin.hbina.akarinspending.Models;

public class AkarinValue {

    String itemType;
    Float itemPrice;

    AkarinValue() {
        this.itemType = "Others";
        this.itemPrice = 0f;
    }

    public AkarinValue(String itemType, Float itemPrice) {
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

    public String toString() {
        return "AkarinValue itemType:" + itemType + " itemPrice:" + itemPrice;
    }
}
