package ru.nedan.spookybuy.screen.setting;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec2f;
import ru.nedan.neverapi.animation.Animation;
import ru.nedan.neverapi.animation.util.Easings;
import ru.nedan.neverapi.gl.Render2D;
import ru.nedan.neverapi.gl.Scissor;
import ru.nedan.neverapi.gui.TextField;
import ru.nedan.neverapi.math.FloatRectangle;
import ru.nedan.neverapi.math.MathUtils;
import ru.nedan.neverapi.math.TimerUtility;
import ru.nedan.neverapi.shader.Rounds;
import ru.nedan.spookybuy.SpookyBuy;
import ru.nedan.spookybuy.items.CollectItem;

import java.awt.*;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class ItemRenderer {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private final TimerUtility hoverTimer = new TimerUtility();
    private float alpha = 0.05f;
    private boolean fadeOut = false;

    @Getter
    @Setter
    private FloatRectangle position;

    public float offset, scroll;

    @Getter
    private final CollectItem collectItem;

    boolean extended;
    private final Animation height = new Animation();
    private final TextField buyPriceField, sellPriceField;

    public boolean allocated = false;

    public ItemRenderer(FloatRectangle position, CollectItem collectItem, float offset) {
        this.position = position;
        this.collectItem = collectItem;
        this.offset = offset;

        height.setToValue(position.height).setValue(position.height);

        buyPriceField = new TextField(position.x + 3, position.y + offset + 20, position.width - 6, 12, "Цена покупки", (string) -> {
            if (string.isEmpty()) string = "0";

            BigDecimal bigDecimal = new BigDecimal(string);

            SpookyBuy.getInstance().getAutoBuy().getPriceMap().putPrice(collectItem, bigDecimal, false);
        }, Character::isDigit);

        sellPriceField = new TextField(position.x + 3, position.y + offset + 38, position.width - 6, 12, "Цена продажи", (string) -> {
            if (string.isEmpty()) string = "0";

            BigDecimal bigDecimal = new BigDecimal(string);

            SpookyBuy.getInstance().getAutoBuy().getPriceMap().putPrice(collectItem, bigDecimal, true);
        }, Character::isDigit);

        BigDecimal buyPrice = SpookyBuy.getInstance().getAutoBuy().getPriceMap().getPrice(collectItem, false);
        BigDecimal sellPrice = SpookyBuy.getInstance().getAutoBuy().getPriceMap().getPrice(collectItem, true);

        buyPriceField.setText(buyPrice.toString());
        sellPriceField.setText(sellPrice.toString());
    }

    public float getHeight() {
        return (float) height.getValue();
    }

    public void render(MatrixStack matrixStack) {
        height.update();

        FloatRectangle position = this.position.offset(0, offset + scroll);

        boolean hovered = MathUtils.isHovered(position);

        if (!hovered) {
            hoverTimer.updateLast();
        }

        if (hovered) {
            if (fadeOut) {
                alpha -= 0.03f;
                if (alpha <= 0.1f) {
                    alpha = 0.1f;
                }
            } else {
                alpha += 0.03f;
                if (alpha >= 1.0f) {
                    alpha = 1.0f;
                }
            }
        } else {
            fadeOut = false;

            alpha -= 0.03f;
            if (alpha <= 0.1f) {
                alpha = 0.1f;
            }
        }

        if (alpha > 0.1f) {
            List<String> list = Arrays.asList(
                    collectItem.getName(),
                    "Цена покупки: " + SpookyBuy.getInstance().getAutoBuy().getPriceMap().getPrice(collectItem, false),
                    "Цена продажи: " + SpookyBuy.getInstance().getAutoBuy().getPriceMap().getPrice(collectItem, true),
                    "ПКМ чтобы открыть"
            );

            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

            float defaultWidth = Math.max(90, list.stream()
                    .mapToInt(textRenderer::getWidth)
                    .max()
                    .orElse(95)) + 5;

            float defaultHeight = list.stream()
                    .mapToInt(string -> textRenderer.fontHeight + 3)
                    .sum();

            Vec2f mousePos = MathUtils.getMousePos();
            boolean renderLeft = mousePos.x < position.x + position.width / 2f;
            float tooltipX = renderLeft
                    ? position.x - defaultWidth - 6
                    : position.x + position.width + 6;

            int alphaByte = (int) (alpha * 255);
            Color injectedColor = new Color(0, 0, 0, alphaByte);
            Rounds.drawRound(tooltipX, position.y, defaultWidth, defaultHeight, 4, injectedColor);

            int color = (int) (alpha * 255) << 24 | 0xFFFFFF;

            float offset = 2;
            for (String string : list) {
                float textX = tooltipX + 3;
                float textY = position.y + offset;

                textRenderer.draw(matrixStack, string, textX, textY, color);
                offset += textRenderer.fontHeight + 3;
            }
        }

        Rounds.drawRound(position.x, position.y, position.width, getHeight(), 8, allocated ? new Color(0x434343) : new Color(0x272727));
        Render2D.renderItem(collectItem.getStack(), position.x + 5, position.y + 1);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        textRenderer.draw(matrixStack, collectItem.getName(), position.x + 25, position.y + 6, -1);

        buyPriceField.updatePositions(position.x + 3, position.y + (getHeight() - /*87*/51), position.width - 6, 12);
        sellPriceField.updatePositions(position.x + 3, position.y + (getHeight() - /*69*/33), position.width - 6, 12);

        Scissor.push();

        Scissor.setFromComponentCoordinates(position.x, position.y + 17, position.width, height.getValue());

        buyPriceField.render(matrixStack);
        sellPriceField.render(matrixStack);

        mc.textRenderer.draw(matrixStack, "Учитывать в AutoSetup", position.x + 20, position.y + (getHeight() - 14), -1);

        Rounds.drawRound(position.x + 3, position.y + (getHeight() - 16), 12, 12, 4, Color.BLACK);

        if (SpookyBuy.getInstance().getAutoBuy().getPriceMap().getAutoSetupFlag(collectItem)) {
            Rounds.drawRound(position.x + 7, position.y + (getHeight() - 12), 4f, 4f, 1.5f, Color.WHITE);
        }

        Scissor.pop();
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (extended) {
            FloatRectangle position = this.position.offset(0, offset + scroll);
            if (MathUtils.isHovered(new FloatRectangle(position.x + 3, position.y + (getHeight() - 16), 12, 12))) {
                boolean flag = SpookyBuy.getInstance().getAutoBuy().getPriceMap().getAutoSetupFlag(collectItem);
                SpookyBuy.getInstance().getAutoBuy().getPriceMap().putFlag(collectItem, !flag);
            }
        }

        if (extended) {
            buyPriceField.mouseClicked(mouseX, mouseY, button);
            sellPriceField.mouseClicked(mouseX, mouseY, button);
        }

        if (button == 1) {
            if (position.offset(0, scroll + offset).contains((float) mouseX, (float) mouseY)) {
                extended = !extended;

                if (extended)
                    height.animate(position.height + /*90*/56, 0.2, Easings.QUAD_OUT);
                else
                    height.animate(position.height, 0.2, Easings.QUAD_OUT);
            }
        }
    }

    public void charTyped(char chr) {
        buyPriceField.charTyped(chr);
        sellPriceField.charTyped(chr);
    }

    public void keyPressed(int keyCode) {
        buyPriceField.keyPressed(keyCode);
        sellPriceField.keyPressed(keyCode);
    }
}
