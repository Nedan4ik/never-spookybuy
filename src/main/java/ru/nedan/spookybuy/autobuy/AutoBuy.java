package ru.nedan.spookybuy.autobuy;

import lombok.Getter;
import net.minecraft.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import ru.nedan.neverapi.NeverAPI;
import ru.nedan.neverapi.etc.ChatUtility;
import ru.nedan.neverapi.event.api.Event;
import ru.nedan.neverapi.event.impl.EventInput;
import ru.nedan.neverapi.event.impl.EventMessage;
import ru.nedan.neverapi.event.impl.EventPlayerTick;
import ru.nedan.neverapi.event.impl.EventPress;
import ru.nedan.neverapi.math.TimerUtility;
import ru.nedan.spookybuy.autobuy.functional.inst.AutoBuyFunction;
import ru.nedan.spookybuy.items.CollectItem;
import ru.nedan.spookybuy.items.ItemStorage;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class AutoBuy {
    private final PriceMap priceMap = new PriceMap();

    private final Map<String, TimerUtility> timers = new HashMap<>();
    private final Map<String, Boolean> flags = new HashMap<>();

    public BigDecimal balance = BigDecimal.ZERO;

    AutoBuyFunction ab;

    public AutoBuy() {
        for (CollectItem collectItem : ItemStorage.ALL) {
            priceMap.putPrice(collectItem, new BigDecimal(100), true);
            priceMap.putFlag(collectItem, true);
        }

        timers.put("ab.update", new TimerUtility());
        timers.put("ab.buy", new TimerUtility());
        timers.put("ab.resell", new TimerUtility());
        timers.put("ab.resellItem", new TimerUtility());
        timers.put("autosell.open", new TimerUtility());
        timers.put("autosell.sell", new TimerUtility());
        timers.put("autosetup.enable", new TimerUtility());

        timers.put("autosetup.startItem", new TimerUtility());

        flags.put("autoSell", false);
        flags.put("resell", false);

        ab = new AutoBuyFunction(this, flags, timers);

        Event.addListener(EventPress.class, e -> {
            if (e.getAction() == 1 && e.getKey() == GLFW.GLFW_KEY_KP_9 && NeverAPI.isInit()) {
                ItemStorage.reload();
                priceMap.clear();

                ItemStorage.reload();

                for (CollectItem collectItem : ItemStorage.ALL) {
                    priceMap.putPrice(collectItem, new BigDecimal(100), true);
                    priceMap.putFlag(collectItem, true);
                }

                ChatUtility.sendMessage("Обновлено!");
            }
        });

        Event.addListener(EventPlayerTick.class, ab::tick);
        Event.addListener(EventMessage.class, ab::message);
        Event.addListener(EventInput.class, ab::input);
    }

    public void onSBLine(String string) {
        Pattern pattern = Pattern.compile("╠ \\$ Монет: ([\\d,]+)");
        Matcher matcher = pattern.matcher(string);

        if (matcher.find()) {
            String coinsStr = matcher.group(1).replace(",", "");
            int coins = Integer.parseInt(coinsStr);
            balance = new BigDecimal(coins);
        }
    }

    public CollectItem getItem(ItemStack stack) {
        return ItemStorage.ALL.stream()
                .filter(item -> CollectItem.CHECKER.test(stack, item))
                .findFirst()
                .orElse(null);
    }
}
