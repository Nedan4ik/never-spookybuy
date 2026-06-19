package ru.nedan.spookybuy.autobuy.history;

import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import ru.nedan.neverapi.gl.Scissor;
import ru.nedan.neverapi.math.FloatRectangle;
import ru.nedan.neverapi.math.MathUtils;
import ru.nedan.neverapi.shader.Rounds;

import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public class SalesHistoryManager extends ArrayList<SalesHistoryItem> {
    @Getter
    private static final SalesHistoryManager instance;

    private final MinecraftClient mc;
    public float scroll, animatedScroll;
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.US);

    static {
        instance = new SalesHistoryManager();
    }

    private SalesHistoryManager() {
        this.mc = MinecraftClient.getInstance();
    }

    public void render(MatrixStack matrixStack, float x, float y, float width, float height) {
        Scissor.push();
        Scissor.setFromComponentCoordinates(x, y, width, height - 16);

        float offset = y + animatedScroll;
        float scissorBottom = y + height - 16;

        for (SalesHistoryItem item : this) {
            float itemBottom = offset + 29;

            if (itemBottom >= y && offset <= scissorBottom) {
                renderItem(matrixStack, item, x, offset);
            }

            offset += 38;
        }

        Scissor.pop();

        AtomicReference<Float> offsetRef = new AtomicReference<>(y + animatedScroll);

        this.forEach(item -> {
            if (MathUtils.isHovered(new FloatRectangle(x, offsetRef.get(), 140, 34))) {
                Vec2f mousePos = MathUtils.getMousePos();
            }
            offsetRef.updateAndGet(v -> v + 38);
        });

        animatedScroll = MathUtils.lerp(animatedScroll, scroll, 8);
        scroll = MathHelper.clamp(scroll, -(size() * 40 + 10), 0);
    }

    private void renderItem(MatrixStack matrixStack, SalesHistoryItem item, float x, float y) {
        Rounds.drawRound(x, y, 140, 34, 4, new Color(0x1C1C1C));

        String formattedPrice = "$" + NUMBER_FORMAT.format((long) item.getPrice());

        mc.textRenderer.draw(matrixStack, "§f" + item.getItemName(), x + 8, y + 4, -1);
        mc.textRenderer.draw(matrixStack, "§aПродано! §6" + formattedPrice, x + 8, y + 14, -1);
        mc.textRenderer.draw(matrixStack, "§7" + item.getDate(), x + 8, y + 24, -1);
    }

    public void addSale(String itemName, double price) {
        add(new SalesHistoryItem(itemName, price));

        while (size() > 50) {
            remove(0);
        }
    }

    public void clear() {
        super.clear();
    }
}