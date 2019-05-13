package com.example.barcodescanner;

import android.text.Editable;

public class ProductItem {

    private String code, name, party, count;

    public ProductItem(String code, String name, String party,  String count) {
        this.name = name;
        this.party = party;
        this.code = code;
        this.count = count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public String getParty() {
        return party;
    }

    public String getCode() {
        return code;
    }

    public String getCount() {
        return count;
    }
}
