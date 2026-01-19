package ru.nedan.spookybuy.autobuy;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ChatUtil;
import ru.nedan.neverapi.NeverAPI;
import ru.nedan.neverapi.gui.Button;
import ru.nedan.neverapi.math.FloatRectangle;
import ru.nedan.neverapi.math.TimerUtility;
import ru.nedan.neverapi.shader.Blur;
import ru.nedan.neverapi.shader.ColorUtility;
import ru.nedan.neverapi.shader.Rounds;
import ru.nedan.spookybuy.SpookyBuy;
import ru.nedan.spookybuy.util.Utils;
import ru.nedan.spookybuy.autobuy.history.HistoryManager;
import ru.nedan.spookybuy.items.CollectItem;

import java.awt.*;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class GenericContainerScreenHook extends GenericContainerScreen {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public GenericContainerScreenHook(GenericContainerScreen old) {
        super(old.getScreenHandler(), mc.player.inventory, old.getTitle());
    }

    private final TimerUtility afterInit = new TimerUtility();
    public Slot minPrice;
    private Button button = null;

    @Override
    public void init(MinecraftClient client, int width, int height) {
        super.init(client, width, height);

    }

    public static void fill(List<Slot> slots) {
        for (Slot slot : slots) {
            ItemStack stack = slot.getStack();

            if (stack.isEmpty() || stack.getCount() == 1 || Utils.getPrice(stack) == -1) continue;

            NbtCompound compound = stack.getOrCreateTag();
            NbtCompound display = compound.getCompound("display");
            NbtList lore = display.contains("Lore", 9) ? display.getList("Lore", 8) : new NbtList();

            boolean next = true;

            for (int i = 0; i < lore.size(); i++) {
                String line = lore.getString(i);

                if (line.contains("Цена за 1 шт.")) {
                    next = false;
                }
            }

            if (!next) continue;

            NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);

            String priceText = "[\"\",{\"italic\":false,\"color\":\"green\",\"text\":\"$\"}," +
                    "{\"italic\":false,\"color\":\"white\",\"text\":\" Цена за 1 шт.: \"}," +
                    "{\"italic\":false,\"color\":\"green\",\"text\":\"$" + numberFormat.format(Utils.getPrice(stack) / stack.getCount()) + "\"}]";

            boolean inserted = false;
            for (int i = 0; i < lore.size(); i++) {
                String line = ChatUtil.stripTextFormat(lore.getString(i));
                if (line.contains("Цена: ")) {
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
    }

    @Override
    @SuppressWarnings("deprecation")
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        HistoryManager historyManager = HistoryManager.getInstance();
        super.render(matrices, mouseX, mouseY, delta);

        fill(handler.slots);

        if (button == null) {
            button = new Button(new LiteralText("Очистить"), new FloatRectangle(this.x - 150, this.y + this.backgroundHeight + 4, 145, 18), (btn) -> {
                historyManager.clear();
            }, Collections.singletonList(new LiteralText("Нажмите чтобы очистить историю")));
        }

        if (this.title.getString().contains("Поиск") || this.title.getString().contains("Аукционы")) {
            if (afterInit.hasPasses(50)) {
                if (minPrice == null) minPrice = calculateMinPriceSlot();
            }
        }

        if (NeverAPI.isDevBuild()) {
            if (focusedSlot != null) {
                ItemStack stack = focusedSlot.getStack();
                CollectItem collectItem = SpookyBuy.getInstance().getAutoBuy().getItem(stack);

                if (collectItem != null) {
                    System.out.println(collectItem.getName());
                } else {
                    System.out.println(stack.getAttributeModifiers(EquipmentSlot.OFFHAND).entries());
                }
            }
        }

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

        float offset = this.y + 16;
        float x = this.x - 148;

        Blur.register(() -> {
            Rounds.drawRound(this.x - 150, this.y, 145, this.backgroundHeight, 6, Color.BLACK);
        });

        Blur.draw(8, ColorUtility.getColorComps(new Color(0x717171)));

        String text = "История покупок";

        mc.textRenderer.draw(matrices, text, x + (145 - mc.textRenderer.getWidth(text)) / 2f, y + 3, -1);
        button.render(matrices);
        historyManager.render(matrices, x, offset, 145, this.backgroundHeight);
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
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        HistoryManager.getInstance().scroll += (float) (amount * 16);

        return super.mouseScrolled(mouseX, mouseY, amount);
    }
}
