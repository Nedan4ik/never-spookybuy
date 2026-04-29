package ru.nedan.spookybuy.autobuy;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ChatUtil;
import ru.nedan.neverapi.gui.Button;
import ru.nedan.neverapi.math.FloatRectangle;
import ru.nedan.neverapi.math.TimerUtility;
import ru.nedan.neverapi.shader.Blur;
import ru.nedan.neverapi.shader.ColorUtility;
import ru.nedan.neverapi.shader.Rounds;
import ru.nedan.spookybuy.SpookyBuy;
import ru.nedan.spookybuy.autobuy.history.HistoryManager;
import ru.nedan.spookybuy.autobuy.history.SalesHistoryManager;
import ru.nedan.spookybuy.items.CollectItem;
import ru.nedan.spookybuy.util.Utils;

import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class GenericContainerScreenHook extends GenericContainerScreen {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.US);

    public GenericContainerScreenHook(GenericContainerScreen old) {
        super(old.getScreenHandler(), mc.player.inventory, old.getTitle());
    }

    private final TimerUtility afterInit = new TimerUtility();
    public Slot minPrice;
    private Button button = null;
    private Button salesClearButton = null;
    private int currentMinPrice = -1;
    private boolean slotsProcessed = false;
    private List<Slot> problematicSlots = new ArrayList<>();
    private int tickCounter = 0;
    private int initTickCounter = 0;
    private SalesHistoryManager salesHistory;

    @Override
    public void init(MinecraftClient client, int width, int height) {
        super.init(client, width, height);
        slotsProcessed = false;
        minPrice = null;
        problematicSlots.clear();
        initTickCounter = 0;
        salesHistory = SalesHistoryManager.getInstance();
    }

    public static void fill(List<Slot> slots) {
        for (Slot slot : slots) {
            ItemStack stack = slot.getStack();

            if (stack.isEmpty() || Utils.getPrice(stack) == -1) continue;

            NbtCompound compound = stack.getOrCreateTag();
            NbtCompound display = compound.getCompound("display");
            NbtList lore = display.contains("Lore", 9) ? display.getList("Lore", 8) : new NbtList();

            boolean hasPricePerUnit = false;
            int pricePerUnitIndex = -1;

            for (int i = 0; i < lore.size(); i++) {
                String line = lore.getString(i);
                if (line.contains("Цена за 1 шт.")) {
                    hasPricePerUnit = true;
                    pricePerUnitIndex = i;
                    break;
                }
            }

            int totalPrice = Utils.getPrice(stack);
            int pricePerUnit = totalPrice / stack.getCount();

            String priceText = "[\"\",{\"italic\":false,\"color\":\"green\",\"text\":\"$\"}," +
                    "{\"italic\":false,\"color\":\"white\",\"text\":\" Цена за 1 шт.: \"}," +
                    "{\"italic\":false,\"color\":\"green\",\"text\":\"$" + NUMBER_FORMAT.format(pricePerUnit) + "\"}]";

            if (hasPricePerUnit) {
                String currentPriceLine = lore.getString(pricePerUnitIndex);
                if (!currentPriceLine.contains("$" + NUMBER_FORMAT.format(pricePerUnit))) {
                    lore.set(pricePerUnitIndex, NbtString.of(priceText));
                }
                continue;
            }

            boolean inserted = false;

            for (int i = 0; i < lore.size(); i++) {
                String line = ChatUtil.stripTextFormat(lore.getString(i));
                if (line.contains("Цена:")) {
                    lore.add(i + 1, NbtString.of(priceText));
                    inserted = true;
                    break;
                }
            }

            if (!inserted) {
                for (int i = 0; i < lore.size(); i++) {
                    String line = ChatUtil.stripTextFormat(lore.getString(i));
                    if (line.contains("Продавец:") || line.contains("Seller:")) {
                        lore.add(i + 1, NbtString.of(priceText));
                        inserted = true;
                        break;
                    }
                }
            }

            if (!inserted) {
                lore.add(NbtString.of(priceText));
            }

            display.put("Lore", lore);
            compound.put("display", display);
            stack.setTag(compound);
        }
    }

    private void forceProcessSlot(Slot slot) {
        if (slot == null || slot.getStack().isEmpty()) return;

        ItemStack stack = slot.getStack();
        if (Utils.getPrice(stack) == -1) return;

        NbtCompound compound = stack.getOrCreateTag();
        NbtCompound display = compound.getCompound("display");
        NbtList lore = display.contains("Lore", 9) ? display.getList("Lore", 8) : new NbtList();

        for (int i = lore.size() - 1; i >= 0; i--) {
            String line = lore.getString(i);
            if (line.contains("Цена за 1 шт.")) {
                lore.remove(i);
            }
        }

        int totalPrice = Utils.getPrice(stack);
        int pricePerUnit = totalPrice / stack.getCount();

        String priceText = "[\"\",{\"italic\":false,\"color\":\"green\",\"text\":\"$\"}," +
                "{\"italic\":false,\"color\":\"white\",\"text\":\" Цена за 1 шт.: \"}," +
                "{\"italic\":false,\"color\":\"green\",\"text\":\"$" + NUMBER_FORMAT.format(pricePerUnit) + "\"}]";

        boolean inserted = false;

        for (int i = 0; i < lore.size(); i++) {
            String line = ChatUtil.stripTextFormat(lore.getString(i));
            if (line.contains("Цена:")) {
                lore.add(i + 1, NbtString.of(priceText));
                inserted = true;
                break;
            }
        }

        if (!inserted) {
            lore.add(NbtString.of(priceText));
        }

        display.put("Lore", lore);
        compound.put("display", display);
        stack.setTag(compound);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        HistoryManager historyManager = HistoryManager.getInstance();
        SalesHistoryManager salesHistory = SalesHistoryManager.getInstance();

        // ========== 1. Обработка слотов ==========
        if (!slotsProcessed) {
            fill(handler.slots);
            slotsProcessed = true;
        }

        for (Slot slot : handler.slots) {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty() || Utils.getPrice(stack) == -1) continue;

            NbtCompound compound = stack.getTag();
            if (compound != null && compound.contains("display")) {
                NbtCompound display = compound.getCompound("display");
                if (display.contains("Lore", 9)) {
                    NbtList lore = display.getList("Lore", 8);
                    boolean hasPricePerUnit = false;

                    for (int i = 0; i < lore.size(); i++) {
                        if (lore.getString(i).contains("Цена за 1 шт.")) {
                            hasPricePerUnit = true;
                            break;
                        }
                    }

                    if (!hasPricePerUnit && !problematicSlots.contains(slot)) {
                        problematicSlots.add(slot);
                    }
                }
            }
        }

        if (!problematicSlots.isEmpty()) {
            for (Slot slot : problematicSlots) {
                forceProcessSlot(slot);
            }
            problematicSlots.clear();
        }

        // ========== 2. Стандартный рендер (включая слоты и итемы) ==========
        super.render(matrices, mouseX, mouseY, delta);

        // ========== 3. Рендер панелей истории ПОВЕРХ слотов ==========
        float leftX = this.x - 150;
        float leftWidth = 145;
        float rightX = this.x + this.backgroundWidth + 5;
        float rightWidth = 145;

        // Логика минимальной цены
        if (this.getTitle().getString().contains("Поиск") || this.getTitle().getString().contains("П:") || this.getTitle().getString().contains("Аукционы")) {
            initTickCounter++;
            if (initTickCounter >= 50 || minPrice == null) {
                if (minPrice == null) {
                    minPrice = calculateMinPriceSlot();
                    if (minPrice != null) {
                        ItemStack stack = minPrice.getStack();
                        currentMinPrice = Utils.getPrice(stack) / stack.getCount();
                    }
                }
            }
        }

        if (button == null) {
            button = new Button(new LiteralText("Очистить"), new FloatRectangle(leftX, this.y + this.backgroundHeight + 4, leftWidth, 18), (btn) -> {
                historyManager.clear();
            }, Collections.singletonList(new LiteralText("Нажмите чтобы очистить историю покупок")));
        }

        if (salesClearButton == null) {
            salesClearButton = new Button(new LiteralText("Очистить"), new FloatRectangle(rightX, this.y + this.backgroundHeight + 4, rightWidth, 18), (btn) -> {
                salesHistory.clear();
            }, Collections.singletonList(new LiteralText("Нажмите чтобы очистить историю продаж")));
        }

        if (this.getTitle().getString().contains("Поиск") || this.getTitle().getString().contains("Аукционы")) {
            tickCounter++;
            if (tickCounter % 50 == 0 || minPrice == null) {
                Slot newMinPrice = calculateMinPriceSlot();

                if (newMinPrice != null) {
                    forceProcessSlot(newMinPrice);

                    if (minPrice == null || !minPrice.equals(newMinPrice)) {
                        minPrice = newMinPrice;
                        ItemStack stack = minPrice.getStack();
                        currentMinPrice = Utils.getPrice(stack) / stack.getCount();
                    }
                }
            }
        }

        // Подсветка минимальной цены
        if (minPrice != null) {
            int i = this.x;
            int j = this.y;

            Slot slot = minPrice;

            RenderSystem.pushMatrix();
            RenderSystem.translatef((float) i, (float) j, 0.0F);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableRescaleNormal();

            RenderSystem.glMultiTexCoord2f(33986, 240.0F, 240.0F);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

            RenderSystem.disableDepthTest();
            int j1 = slot.x;
            int k1 = slot.y;

            RenderSystem.colorMask(true, true, true, false);
            this.fillGradient(matrices, j1, k1, j1 + 16, k1 + 16, new Color(0x9907DF43, true).getRGB(), new Color(0x9907DF43, true).getRGB());
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.enableDepthTest();

            RenderSystem.popMatrix();
        }

        // Блюр и панели
        float leftPanelX = this.x - 150;
        float leftPanelY = this.y + 16;

        Blur.register(() -> {
            Rounds.drawRound(leftPanelX, this.y, 145, this.backgroundHeight, 6, Color.BLACK);
        });

        Blur.register(() -> {
            Rounds.drawRound(rightX, this.y, rightWidth, this.backgroundHeight, 6, Color.BLACK);
        });

        Blur.draw(8, ColorUtility.getColorComps(new Color(0x717171)));

        String purchaseText = "История покупок";
        String salesText = "История продаж";

        mc.textRenderer.draw(matrices, purchaseText, leftPanelX + (145 - mc.textRenderer.getWidth(purchaseText)) / 2f, this.y + 3, -1);
        mc.textRenderer.draw(matrices, salesText, rightX + (rightWidth - mc.textRenderer.getWidth(salesText)) / 2f, this.y + 3, -1);

        button.render(matrices);
        salesClearButton.render(matrices);

        historyManager.render(matrices, leftPanelX, leftPanelY, 145, this.backgroundHeight);
        salesHistory.render(matrices, rightX, this.y + 16, rightWidth, this.backgroundHeight);



        // ========== 4. Тултип рендерится ПОВЕРХ всего ==========
        if (this.focusedSlot != null && !this.focusedSlot.getStack().isEmpty()) {
            RenderSystem.disableDepthTest();
            List<net.minecraft.text.Text> tooltip = this.getTooltipFromItem(this.focusedSlot.getStack());
            this.renderTooltip(matrices, tooltip, mouseX, mouseY);
            RenderSystem.enableDepthTest();
        }
    }

    @Override
    public void renderBackground(MatrixStack matrices) {

    }

    public Slot calculateMinPriceSlot() {
        Slot minPriceSlot = null;
        int minPrice = Integer.MAX_VALUE;

        for (int i = 0; i < this.getScreenHandler().slots.size(); i++) {
            Slot slot = this.getScreenHandler().getSlot(i);

            if (!slot.getStack().isEmpty()) {
                int price = Utils.getPrice(slot.getStack());

                if (price == -1) continue;
                if (Utils.getSeller(slot.getStack()).equalsIgnoreCase(mc.getSession().getUsername())) continue;

                price = price / slot.getStack().getCount();

                CollectItem item = SpookyBuy.getInstance().getAutoBuy().getItem(slot.getStack());

                if (item == null) continue;

                if (price < minPrice) {
                    minPrice = price;
                    minPriceSlot = slot;
                }
            }
        }

        return minPriceSlot;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.button.mouseClicked(mouseX, mouseY, button);
        if (this.salesClearButton != null) {
            this.salesClearButton.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        float leftX = this.x - 150;
        float rightX = this.x + this.backgroundWidth + 5;
        float panelWidth = 145;

        if (mouseX >= leftX && mouseX <= leftX + panelWidth &&
                mouseY >= this.y && mouseY <= this.y + this.backgroundHeight) {
            HistoryManager.getInstance().scroll += (float) (amount * 16);
        }
        else if (mouseX >= rightX && mouseX <= rightX + panelWidth &&
                mouseY >= this.y && mouseY <= this.y + this.backgroundHeight) {
            SalesHistoryManager.getInstance().scroll += (float) (amount * 16);
        }

        return super.mouseScrolled(mouseX, mouseY, amount);
    }
}