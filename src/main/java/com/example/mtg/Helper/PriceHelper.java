package com.example.mtg.Helper;

import com.example.mtg.Magic.Price;

public class PriceHelper {
    Price today;
    Price yesterday;
    Price weekAgo;

    public PriceHelper(Price today, Price yesterday, Price weekAgo) {
        this.today = today;
        this.yesterday = yesterday;
        this.weekAgo = weekAgo;
    }
}
