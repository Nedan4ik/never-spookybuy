package ru.nedan.spookybuy.autobuy.autoparse;

import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Formatting;
import ru.nedan.neverapi.NeverAPI;
import ru.nedan.neverapi.etc.ChatUtility;
import ru.nedan.neverapi.etc.TextBuilder;
import ru.nedan.spookybuy.items.ItemStorage;
import ru.nedan.spookybuy.util.Pair;
import ru.nedan.spookybuy.SpookyBuy;
import ru.nedan.spookybuy.util.Utils;
import ru.nedan.spookybuy.autobuy.AutoBuy;
import ru.nedan.spookybuy.autobuy.GenericContainerScreenHook;
import ru.nedan.spookybuy.autobuy.autoparse.coefficient.Coefficient;
import ru.nedan.spookybuy.event.EventAutoSetupStop;
import ru.nedan.spookybuy.items.CollectItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AutoParser {
    @Getter
    private static AutoParser instance;

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    public final List<CollectItem> added = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    public boolean hasAutoBuyState;

    public AutoParser() {
        instance = this;
    }

    public boolean tick() {
        if (SpookyBuy.getInstance().isAutoSetupState()) {
            AutoBuy autoBuy = SpookyBuy.getInstance().getAutoBuy();
            CollectItem collectItem = ItemStorage.ALL
                    .stream()
                    .filter(item -> !added.contains(item) && autoBuy.getPriceMap().getAutoSetupFlag(item))
                    .findFirst()
                    .orElse(null);

            if (collectItem != null) {
                if (autoBuy.getTimers().get("autosetup.startItem").hasPasses(1000)) {
                    autoBuy.getTimers().get("autosetup.startItem").updateLast();
                    executor.submit(() -> {
                        try {
                            mc.player.sendChatMessage("/ah search " + collectItem.getName());

                            Thread.sleep(600);

                            if (mc.currentScreen instanceof GenericContainerScreenHook screenHook) {
                                Slot slot = screenHook.minPrice;

                                if (slot == null) {
                                    added.add(collectItem);
                                    return;
                                }

                                ItemStack stack = slot.getStack();
                                int price = Utils.getPrice(stack);

                                if (price != -1) {
                                    CollectItem item = autoBuy.getItem(stack);

                                    if (item == null) return;

                                    BigDecimal rawPrice = BigDecimal.valueOf(price)
                                            .divide(BigDecimal.valueOf(stack.getCount()), 1, RoundingMode.HALF_UP);

                                    BigDecimal difference = BigDecimal.valueOf(price)
                                            .divide(BigDecimal.valueOf(stack.getCount()), 1, RoundingMode.HALF_UP)
                                            .subtract(autoBuy.getPriceMap().getPrice(collectItem, false))
                                            .abs();

                                    Coefficient coefficient = Coefficient.findForPrice(difference);

                                    ChatUtility.sendMessage(item.getName() + ":");

                                    if (coefficient == null) {
                                        ChatUtility.sendMessage("Нет коэффициент для различия в " + difference.toPlainString());
                                        added.add(collectItem);
                                        return;
                                    }

                                    Pair<Double, Double> decimalPair = coefficient.getDecimalPair();

                                    ChatUtility.sendMessage("Применяю коэффициенты: " + decimalPair.getLeft() + " " + decimalPair.getRight());

                                    BigDecimal buyPrice = rawPrice.multiply(BigDecimal.valueOf(decimalPair.getLeft()));
                                    BigDecimal sellPrice = rawPrice.multiply(BigDecimal.valueOf(decimalPair.getRight()));

                                    buyPrice = buyPrice.setScale(0, RoundingMode.HALF_UP);
                                    sellPrice = sellPrice.setScale(0, RoundingMode.HALF_UP);

                                    ChatUtility.sendMessage("Цена покупки: " + buyPrice.toPlainString());
                                    ChatUtility.sendMessage("Цена продажи: " + sellPrice.toPlainString());

                                    autoBuy.getPriceMap().putPrice(collectItem, buyPrice, false);
                                    autoBuy.getPriceMap().putPrice(collectItem, sellPrice, true);
                                }
                            }

                            added.add(collectItem);
                        } catch (InterruptedException e) {
                            e.printStackTrace(System.err);
                            Thread.currentThread().interrupt();
                        }
                    });
                }
            } else {
                SpookyBuy.getInstance().setAutoSetupState(false);

                ChatUtility.sendMessage(new TextBuilder()
                        .append("АвтоСетап успешно ")
                        .append(SpookyBuy.getInstance().isAutoSetupState() ? "запущен!" : "выключен!", SpookyBuy.getInstance().isAutoSetupState() ? Formatting.GREEN : Formatting.RED)
                        .build());

                if (hasAutoBuyState) {
                    hasAutoBuyState = false;
                    SpookyBuy.getInstance().setState(true);
                }

                autoBuy.getTimers().get("autosetup.enable").updateLast();
                mc.player.closeHandledScreen();

                NeverAPI.getApi().getEventBus().post(new EventAutoSetupStop());
            }

            return true;
        }

        return false;
    }
}
