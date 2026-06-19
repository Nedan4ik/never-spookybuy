package ru.nedan.spookybuy.autobuy.history;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Getter
@RequiredArgsConstructor
public class SalesHistoryItem {
    private final String itemName;
    private final double price;
    private final String date;

    public SalesHistoryItem(String itemName, double price) {
        this.itemName = itemName;
        this.price = price;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        this.date = LocalTime.now().format(formatter);
    }
}