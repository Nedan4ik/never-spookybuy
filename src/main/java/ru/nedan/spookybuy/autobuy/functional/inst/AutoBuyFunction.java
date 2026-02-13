package ru.nedan.spookybuy.autobuy.functional.inst;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ChatUtil;
import net.minecraft.util.Formatting;
import ru.nedan.neverapi.NeverAPI;
import ru.nedan.neverapi.etc.ChatUtility;
import ru.nedan.neverapi.etc.TextBuilder;
import ru.nedan.neverapi.event.impl.EventInput;
import ru.nedan.neverapi.event.impl.EventMessage;
import ru.nedan.neverapi.event.impl.EventPlayerTick;
import ru.nedan.neverapi.math.TimerUtility;
import ru.nedan.spookybuy.SpookyBuy;
import ru.nedan.spookybuy.autobuy.AutoBuy;
import ru.nedan.spookybuy.autobuy.GenericContainerScreenHook;
import ru.nedan.spookybuy.autobuy.autoparse.AutoParser;
import ru.nedan.spookybuy.autobuy.functional.ABInputListener;
import ru.nedan.spookybuy.autobuy.functional.ABMessageListener;
import ru.nedan.spookybuy.autobuy.functional.ABTicker;
import ru.nedan.spookybuy.autobuy.history.HistoryItem;
import ru.nedan.spookybuy.autobuy.history.HistoryManager;
import ru.nedan.spookybuy.event.EventPreSell;
import ru.nedan.spookybuy.event.EventStartResell;
import ru.nedan.spookybuy.event.EventStopResell;
import ru.nedan.spookybuy.items.CollectItem;
import ru.nedan.spookybuy.items.ItemStorage;
import ru.nedan.spookybuy.util.telegram.TelegramAPI;
import ru.nedan.spookybuy.util.Utils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("UnstableApiUsage")
public class AutoBuyFunction implements ABTicker, ABMessageListener, ABInputListener {
    private final AutoBuy autoBuy;
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final ExecutorService RECONNECT_EXECUTOR = Executors.newSingleThreadExecutor(t -> new Thread(t, "Reconnect"));

    final Map<String, Boolean> flags;
    final Map<String, TimerUtility> timers;

    public AutoBuyFunction(AutoBuy autoBuy, Map<String, Boolean> flags, Map<String, TimerUtility> timers) {
        this.flags = flags;
        this.timers = timers;
        this.autoBuy = autoBuy;
    }

    // STATS
    @Getter
    private BigDecimal sell = BigDecimal.ZERO, buy = BigDecimal.ZERO;

    @Setter
    @Getter
    private boolean sendBuy = true, sendSell = true;

    ItemStack activeStack;
    CollectItem activeCollect;

    private final Pattern sellPattern = Pattern.compile("^\\[☃] У Вас купили (.+) за \\$([\\d,]+) на /ah$");
    private final Pattern buyPattern = Pattern.compile("^\\[☃] Вы успешно купили (.+) за \\$([\\d,]+)!$");

    @Override
    public void tick(EventPlayerTick e) {
        if (AutoParser.getInstance().tick()) return;

        if (!SpookyBuy.getInstance().isState()) return;

        assert mc.interactionManager != null;
        assert mc.player != null;

        if (mc.currentScreen instanceof GenericContainerScreen screen) {
            GenericContainerScreenHandler screenHandler = screen.getScreenHandler();
            int sId = screenHandler.syncId;

            if (flags.get("resell")) {
                if (timers.get("ab.resellItem").hasPasses(400)) {
                    if (!ChatUtil.stripTextFormat(screen.getTitle().getString()).contains("Хранилище")) {
                        clickSilent(sId, 46);
                    } else {
                        ItemStack stack = screenHandler.getSlot(0).getStack();

                        if (stack.isEmpty()) {
                            flags.replace("resell", false);
                            flags.replace("autoSell", true);
                            timers.get("autosell.open").updateLast();
                            timers.get("autosell.sell").updateLast();
                            mc.player.closeHandledScreen();

                            NeverAPI.getApi().getEventBus().post(new EventStopResell());
                        } else {
                            clickSilent(sId, 0);
                        }
                    }

                    timers.get("ab.resellItem").updateLast();
                }

                return;
            }

            if (timers.get("ab.resell").hasPasses(90000)) {
                EventStartResell eventStartResell = new EventStartResell();
                NeverAPI.getApi().getEventBus().post(eventStartResell);

                if (!eventStartResell.isCanceled()) {
                    flags.replace("resell", true);
                    timers.get("ab.resell").updateLast();
                }
            }

            if (timers.get("ab.update").hasPasses(270)) {
                clickSilent(sId, 49);
                timers.get("ab.update").updateLast();
            }

            long time = SpookyBuy.getInstance().getAutoSetupTime();

            if (time >= 300000) {
                if (timers.get("autosetup.enable").hasPasses(time)) {
                    SpookyBuy.getInstance().setAutoSetupState(true);
                    mc.player.closeHandledScreen();
                    onAutoSetupToggle(true);
                    AutoParser.getInstance().hasAutoBuyState = true;
                    timers.get("autosetup.enable").updateLast();
                }
            }

            TimerUtility buyTimer = timers.get("ab.buy");
            if (!buyTimer.hasPasses(450)) return;

            for (Slot slot : screenHandler.slots) {
                ItemStack stack = slot.getStack();

                if (stack.isEmpty()) continue;

                int totalPrice = Utils.getPrice(stack);
                if (totalPrice <= 0) continue;

                if (totalPrice > autoBuy.balance.intValue()) continue;

                int count = stack.getCount();
                int stackPrice = totalPrice / count;

                CollectItem collectItem = null;
                for (CollectItem item : ItemStorage.ALL) {
                    if (CollectItem.CHECKER.test(stack, item)) {
                        collectItem = item;
                        break;
                    }
                }

                if (collectItem == null) continue;

                BigDecimal limitPrice = autoBuy.getPriceMap().getPrice(collectItem, false);
                if (limitPrice == null) continue;

                if (limitPrice.compareTo(BigDecimal.valueOf(stackPrice)) < 0) continue;

                buyTimer.updateLast();
                timers.get("ab.update").updateLast();

                activeStack = stack.copy();
                activeCollect = collectItem;

                mc.interactionManager.clickSlot(
                        sId,
                        slot.id,
                        0,
                        SlotActionType.QUICK_MOVE,
                        mc.player
                );

                break;
            }
        } else {
            if (timers.get("autosell.open").hasPasses(3000)) {
                mc.player.sendChatMessage("/ah");
                timers.get("autosell.open").updateLast();
            }

            if (!flags.get("autoSell")) return;

            if (timers.get("autosell.sell").hasPasses(1500)) {
                for (int i = 0; i <= 36; ++i) {
                    if (i == 36) {
                        flags.replace("autoSell", false);
                        return;
                    }

                    ItemStack stack = mc.player.inventory.getStack(i);
                    if (stack.isEmpty()) continue;
                    CollectItem collectItem = autoBuy.getItem(stack);
                    if (collectItem == null) continue;
                    boolean isPhantom = Utils.getPrice(stack) != -1;
                    if (isPhantom) continue;

                    timers.get("autosell.sell").updateLast();
                    timers.get("autosell.open").updateLast();

                    BigDecimal sellPrice = autoBuy.getPriceMap().getPrice(collectItem, true);
                    int price = sellPrice.intValue() * stack.getCount();

                    int maxSellPrice = collectItem.getMaxSellPrice();

                    if (price > maxSellPrice) continue;

                    if (i < 9) {
                        mc.player.inventory.selectedSlot = i;
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(i));

                        EventPreSell eventPreSell = new EventPreSell(collectItem);
                        NeverAPI.getApi().getEventBus().post(eventPreSell);
                        if (eventPreSell.isCanceled()) continue;

                        mc.player.sendChatMessage("/ah sell " + price);
                    } else {
                        mc.interactionManager.pickFromInventory(i);

                        EventPreSell eventPreSell = new EventPreSell(collectItem);
                        NeverAPI.getApi().getEventBus().post(eventPreSell);
                        if (eventPreSell.isCanceled()) continue;

                        mc.player.sendChatMessage("/ah sell " + price);
                    }

                    break;
                }
            }
        }
    }

    boolean rejoining;

    @Override
    public void message(EventMessage e) {
        String mes = e.getMessage();
        assert mc.player != null;

        if (!e.isSend()) {
            if (ChatUtil.stripTextFormat(mes).equalsIgnoreCase("[☃] Освободите хранилище или уберите предметы с продажи")) {
                flags.replace("autoSell", false);
                return;
            }

            if (mes.equalsIgnoreCase("Данная команда недоступна в режиме AFK") || mes.equalsIgnoreCase("Недопустимо нажимать в режиме AFK") && !rejoining) {
                RECONNECT_EXECUTOR.execute(() -> {
                    rejoining = true;
                    String anarchy = Utils.getCurrentAnarchy();
                    mc.player.sendChatMessage("/hub");

                    try {
                        Thread.sleep(1200);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace(System.err);
                    }

                    mc.player.sendChatMessage("/an" + anarchy);
                    rejoining = false;
                });

                return;
            }

            Matcher sellMatcher = sellPattern.matcher(mes);
            if (sellMatcher.matches()) {
                String item = sellMatcher.group(1);
                String price = sellMatcher.group(2);

                if (sendSell)
                    TelegramAPI.sendMessage(String.format("У вас купили %s за $%s", item, price), null);

                sell = sell.add(new BigDecimal(price.replaceAll("[^\\d.]", "")));
                return;
            }

            if (activeStack == null || activeCollect == null) return;

            if (mes.equalsIgnoreCase("[☃] Этот товар уже купили!")) {
                if (sendBuy)
                    TelegramAPI.sendMessage(
                            String.format("Не удалось купить %s (%s) у игрока %s. Причина: Товар уже купили.",
                                    activeStack.getName().getString(),
                                    activeCollect.getName(),
                                    Utils.getSeller(activeStack)
                            ),
                            null
                    );

                addHistoryItem(HistoryItem.Status.NOTBUY);
                activeStack = null;
                return;
            }

            if (mes.equalsIgnoreCase("[☃] У Вас не хватает денег!")) {
                if (sendBuy)
                    TelegramAPI.sendMessage(
                            String.format("Не удалось купить %s (%s) у игрока %s. Причина: У вас не хватает денег.",
                                    activeStack.getName().getString(),
                                    activeCollect.getName(),
                                    Utils.getSeller(activeStack)
                            ),
                            null
                    );

                addHistoryItem(HistoryItem.Status.NOTBUY);

                activeStack = null;
                return;
            }

            Matcher buyMatcher = buyPattern.matcher(mes);
            if (buyMatcher.matches()) {
                String item = buyMatcher.group(1);
                String price = buyMatcher.group(2);

                String seller = Utils.getSeller(activeStack);
                String displayName = activeCollect.getName();

                if (sendBuy)
                    TelegramAPI.sendMessage(
                            String.format("Вы успешно купили %s (%s x%s) у %s за $%s", item, displayName, activeStack.getCount(), seller, price),
                            null
                    );

                buy = buy.add(new BigDecimal(price.replaceAll("[^\\d.]", "")));

                addHistoryItem(HistoryItem.Status.BUY);
                activeStack = null;
            }
        }
    }

    // ANTI AFK
    private final TimerUtility notifyTimer = new TimerUtility();

    @Override
    public void input(EventInput e) {
        if (SpookyBuy.getInstance().isState()) {
            if (e.getMovementForward() != 0 || e.getMovementSideways() != 0 || e.isJumping() || e.isSneaking()) {
                if (notifyTimer.hasPasses(3000)) {
                    TextBuilder textBuilder = SpookyBuy.getSpookyBuyAppender().copy()
                            .append("Автобай активен - движение заблокировано");

                    ChatUtility.sendMessage(textBuilder.build());

                    notifyTimer.updateLast();
                }
            }

            e.setMovementForward(0.0F);
            e.setMovementSideways(0.0F);
            e.setJumping(false);
            e.setSneaking(false);
        }
    }

    private void clickSilent(int containerId, int slot) {
        assert mc.player != null;
        short short1 = mc.player.currentScreenHandler.getNextActionId(mc.player.inventory);
        mc.player.networkHandler.sendPacket(new ClickSlotC2SPacket(containerId, slot, 0, SlotActionType.PICKUP, ItemStack.EMPTY, short1));
    }

    public static void onAutoSetupToggle(boolean autoBuy) {
        ChatUtility.sendMessage(new TextBuilder()
                .append("АвтоСетап успешно ")
                .append(SpookyBuy.getInstance().isAutoSetupState() ? "запущен!" : "выключен!", SpookyBuy.getInstance().isAutoSetupState() ? Formatting.GREEN : Formatting.RED)
                .build());

        if (autoBuy) SpookyBuy.getInstance().setState(false);

        AutoParser.getInstance().added.clear();
    }

    private void addHistoryItem(HistoryItem.Status status) {
        HistoryItem historyItem = HistoryItem.of(activeStack, activeCollect, status);
        HistoryManager.getInstance().add(historyItem);

        if (mc.currentScreen instanceof GenericContainerScreenHook) {
            HistoryManager.getInstance().scroll -= 38;
        }
    }
}
