package ru.nedan.spookybuy.autobuy.history;

import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import ru.nedan.neverapi.gl.Scale;
import ru.nedan.neverapi.gl.Scissor;
import ru.nedan.neverapi.math.FloatRectangle;
import ru.nedan.neverapi.math.MathUtils;
import ru.nedan.neverapi.shader.Rounds;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class HistoryManager extends ArrayList<HistoryItem> {
    @Getter
    private static final HistoryManager instance;

    static {
        instance = new HistoryManager();
    }

    public float scroll, animatedScroll;
    MinecraftClient mc = MinecraftClient.getInstance();

    public void render(MatrixStack matrixStack, float x, float y, float width, float height) {
        Scissor.push();
        Scissor.setFromComponentCoordinates(x, y, width, height - 16);

        float offset = y + animatedScroll;
        float scissorBottom = y + height - 16;

        for (HistoryItem item : this) {
            float itemBottom = offset + 29;

            if (itemBottom >= y && offset <= scissorBottom) {
                renderItem(matrixStack, item, x, offset);
            }

            offset += 38;
        }

        Scissor.pop();

        AtomicReference<Float> offset1 = new AtomicReference<>(y + animatedScroll);

        assert mc.currentScreen != null;

        this.forEach(historyItem -> {
            if (MathUtils.isHovered(new FloatRectangle(x, offset1.get(), 140, 34))) {
                Vec2f mousePos = MathUtils.getMousePos();
                mc.currentScreen.renderTooltip(matrixStack, historyItem.getStack().getTooltip(mc.player, TooltipContext.Default.NORMAL), (int) mousePos.x, (int) mousePos.y);
            }

            offset1.updateAndGet(v -> v + 38);
        });

        animatedScroll = MathUtils.lerp(animatedScroll, scroll, 8);
        scroll = MathHelper.clamp(scroll, -(size() * 40 + 10), 0);
    }

    private void renderItem(MatrixStack matrixStack, HistoryItem item, float x, float y) {
        Rounds.drawRound(x, y, 140, 34, 4, new Color(0x1C1C1C));

        // RENDER ITEM

        Scale.renderScaled(() -> {
            mc.getItemRenderer().renderGuiItemIcon(item.getStack(), (int) (x + 8), (int) (y + 7));
            mc.getItemRenderer().renderGuiItemOverlay(mc.textRenderer, item.getStack(),  (int) (x + 8), (int) (y + 7));
        }, new FloatRectangle(x + 8, y + 8, 25, 25), 1.35f);

        mc.textRenderer.draw(matrixStack, item.getCollectItem().getName(), x + 30, y + 4, -1);
        mc.textRenderer.draw(matrixStack, item.getStatus().getStatusText(), x + 30, y + 14, -1);
        mc.textRenderer.draw(matrixStack, "Время: " + item.getDate(), x + 30, y + 24, -1);
    }
}
