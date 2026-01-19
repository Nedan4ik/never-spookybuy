package ru.nedan.spookybuy.autobuy.history;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ru.nedan.neverapi.etc.TextBuilder;
import ru.nedan.spookybuy.util.Utils;
import ru.nedan.spookybuy.items.CollectItem;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Getter
public class HistoryItem {
    private final String seller;
    private final int price;
    private final ItemStack stack;
    private final Status status;
    private final CollectItem collectItem;
    private final String date;

    private HistoryItem(String seller, int price, ItemStack stack, CollectItem collectItem, Status status) {
        this.seller = seller;
        this.price = price;
        this.stack = stack;
        this.status = status;
        this.collectItem = collectItem;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        this.date = LocalTime.now().format(formatter);
    }

    public static HistoryItem of(ItemStack stack, CollectItem collectItem, Status status) {
        return new HistoryItem(Utils.getSeller(stack), Utils.getPrice(stack), stack, collectItem, status);
    }

    @AllArgsConstructor
    @Getter
    public enum Status {
        BUY(new TextBuilder().append("Куплено!", Formatting.GREEN).build()),
        NOTBUY(new TextBuilder().append("Не куплено!", Formatting.RED).build());

        final Text statusText;
    }
}
