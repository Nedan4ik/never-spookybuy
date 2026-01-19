package ru.nedan.spookybuy.autobuy;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import ru.nedan.spookybuy.util.Pair;
import ru.nedan.spookybuy.items.CollectItem;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class PriceMap {

    private final Map<CollectItem, Pair<BigDecimal, BigDecimal>> prices = new HashMap<>();
    private final Map<CollectItem, Boolean> autoSetupFlags = new HashMap<>();

    public void clear() {
        this.prices.clear();
        this.autoSetupFlags.clear();
    }

    public void putPrice(CollectItem collectItem, BigDecimal price, boolean sell) {
        if (prices.containsKey(collectItem)) {
            if (sell) prices.get(collectItem).setRight(price);
            else prices.get(collectItem).setLeft(price);
        } else {
            prices.put(collectItem, sell ? new Pair<>(BigDecimal.ZERO, price) : new Pair<>(price, BigDecimal.ZERO));
        }
    }

    public void putPrice(String name, BigDecimal price, boolean sell) {
        CollectItem collectItem = findItemByName(name);
        if (collectItem != null) {
            putPrice(collectItem, price, sell);
        }
    }

    public void putFlag(CollectItem collectItem, boolean flag) {
        autoSetupFlags.put(collectItem, flag);
    }

    public void putFlag(String name, boolean flag) {
        CollectItem collectItem = findItemByName(name);
        if (collectItem != null) {
            putFlag(collectItem, flag);
        }
    }

    public boolean getAutoSetupFlag(CollectItem collectItem) {
        return autoSetupFlags.get(collectItem);
    }

    public BigDecimal getPrice(CollectItem itemToFound, boolean sell) {
        Pair<BigDecimal, BigDecimal> pair = prices.get(itemToFound);
        return pair == null ? BigDecimal.ZERO : (sell ? pair.getRight() : pair.getLeft());
    }

    public void saveInConfig(JsonObject object) {
        for (Map.Entry<CollectItem, Pair<BigDecimal, BigDecimal>> entry : prices.entrySet()) {
            CollectItem item = entry.getKey();
            String itemName = item.getName();

            JsonObject itemObject = new JsonObject();
            itemObject.addProperty("buyPrice", entry.getValue().getLeft());
            itemObject.addProperty("sellPrice", entry.getValue().getRight());
            itemObject.addProperty("inAutoSetup", autoSetupFlags.get(item));

            object.add(itemName, itemObject);
        }
    }

    public void readFromConfig(JsonObject object) {
        try {
            JsonElement itemsElement = object.get("items");

            if (itemsElement == null || itemsElement instanceof JsonNull) return;

            JsonObject items = itemsElement.getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : items.entrySet()) {
                String itemName = entry.getKey();
                if (itemName.equalsIgnoreCase("inventories")) continue;

                JsonObject value = entry.getValue().getAsJsonObject();

                BigDecimal buyPrice = value.get("buyPrice").getAsBigDecimal();
                BigDecimal sellPrice = value.get("sellPrice").getAsBigDecimal();
                boolean inAutoSetup = value.get("inAutoSetup").getAsBoolean();

                putPrice(itemName, buyPrice, false);
                putPrice(itemName, sellPrice, true);
                putFlag(itemName, inAutoSetup);
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private CollectItem findItemByName(String name) {
        return prices.keySet().stream()
                .filter(item -> item.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
