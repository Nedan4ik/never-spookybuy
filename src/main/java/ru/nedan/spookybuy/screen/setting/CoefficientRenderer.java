package ru.nedan.spookybuy.screen.setting;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import ru.nedan.neverapi.animation.Animation;
import ru.nedan.neverapi.animation.util.Easings;
import ru.nedan.neverapi.gl.Scissor;
import ru.nedan.neverapi.gui.Button;
import ru.nedan.neverapi.gui.TextField;
import ru.nedan.neverapi.math.FloatRectangle;
import ru.nedan.neverapi.math.MathUtils;
import ru.nedan.neverapi.shader.Rounds;
import ru.nedan.spookybuy.SpookyBuy;
import ru.nedan.spookybuy.autobuy.autoparse.coefficient.Coefficient;
import ru.nedan.spookybuy.autobuy.autoparse.coefficient.CoefficientType;
import ru.nedan.spookybuy.util.ImageRenderer;

import java.awt.*;
import java.math.BigDecimal;

public class CoefficientRenderer {
    @Getter
    private final Coefficient coefficient;

    @Setter
    @Getter
    private FloatRectangle positions;
    public float offset, scroll;

    final TextField priceField;
    final TextField buyPriceCoefficient;
    final TextField sellPriceCoefficient;

    final Button left, right, delete;

    public CoefficientRenderer(FloatRectangle positions, Coefficient coefficient, float offset) {
        this.positions = positions;
        this.coefficient = coefficient;
        this.offset = offset;

        height.setToValue(positions.height).setValue(positions.height);

        priceField = new TextField(positions.x + 3, positions.y + offset + 20, positions.width - 6, 12, "Для цены", (string) -> {
            if (string.isEmpty()) string = "0";

            BigDecimal bigDecimal = new BigDecimal(string);

            coefficient.edit(bigDecimal, null, null, null);
        }, Character::isDigit);

        buyPriceCoefficient = new TextField(positions.x + 3, positions.y + offset + 38, positions.width - 6, 12, "Коэффициент покупки", (string) -> {
            BigDecimal buyCoef;

            try {
                if (string.isEmpty()) string = "0";

                buyCoef = new BigDecimal(string);
            } catch (NumberFormatException e) {
                buyCoef = BigDecimal.ZERO;
            }

            coefficient.edit(null, buyCoef.doubleValue(), null,null);
        }, (chr) -> Character.isDigit(chr) || chr == '.');

        sellPriceCoefficient = new TextField(positions.x + 3, positions.y + offset + 56, positions.width - 6, 12, "Коэффициент продажи", (string) -> {
            BigDecimal sellCoef;

            try {
                if (string.isEmpty()) string = "0";

                sellCoef = new BigDecimal(string);
            } catch (NumberFormatException e) {
                sellCoef = BigDecimal.ZERO;
            }

            coefficient.edit(null, null, sellCoef.doubleValue(), null);
        }, (chr) -> Character.isDigit(chr) || chr == '.');

        left = new Button(Text.of(""), new FloatRectangle(positions.x + 3, positions.y + offset + 20, 12, 12), (button) -> {
            coefficient.edit(null, null, null, CoefficientType.previous(coefficient.getType()));
        }, null) {
            @Override
            public void render(MatrixStack matrices) {
//                super.render(matrices);
                FloatRectangle positions = getPositions();

//                Rounds.drawRound((double)positions.x, (double)positions.y, (double)positions.width, (double)positions.height, (double)4.0F, Color.BLACK);

                Identifier left = ImageRenderer.loadOrGetTexture(null, "left");
                ImageRenderer.render(left, positions.x, positions.y, positions.width, positions.height, -1);
            }
        };

        right = new Button(Text.of(""), new FloatRectangle(positions.x + 3, positions.y + offset + 20, 12, 12), (button) -> {
            coefficient.edit(null, null, null, CoefficientType.next(coefficient.getType()));
        }, null) {
            @Override
            public void render(MatrixStack matrices) {
//                super.render(matrices);
                FloatRectangle positions = getPositions();

//                Rounds.drawRound((double)positions.x, (double)positions.y, (double)positions.width, (double)positions.height, (double)4.0F, Color.BLACK);

                Identifier right = ImageRenderer.loadOrGetTexture(null, "right");
                ImageRenderer.render(right, positions.x, positions.y, positions.width, positions.height, -1);
            }
        };

        delete = new Button(Text.of("Удалить"), new FloatRectangle(positions.x + 3, positions.y + offset + 20, positions.width - 6, 12), (button) -> {
            MinecraftClient.getInstance().openScreen(new AcceptScreen(SpookyBuy.getInstance().getGui(), Text.of("Вы уверены, что хотите удалить коэффициент " + CoefficientType.toShortString(coefficient.getType()) + " " + coefficient.getForPrice().toPlainString()), new Runnable[]{
                    () -> {
                        Coefficient.getAll().removeIf(coef -> coef == coefficient);
                    },
                    () -> {

                    }
            }));
        }, null);

        priceField.setText(coefficient.getForPrice().toPlainString());
        buyPriceCoefficient.setText(BigDecimal.valueOf(coefficient.getDecimalPair().getLeft()).toPlainString());
        sellPriceCoefficient.setText(BigDecimal.valueOf(coefficient.getDecimalPair().getRight()).toPlainString());
    }

    public float getHeight() {
        return (float) height.getValue();
    }

    boolean extended;
    private final Animation height = new Animation();

    public void render(MatrixStack matrixStack) {
        height.update();

        FloatRectangle position = this.positions.offset(0, this.offset + this.scroll);

        Rounds.drawRound(position.x, position.y, position.width, getHeight(), 8, new Color(0x272727));
        String fullyString = "Если отличие " + CoefficientType.toShortString(coefficient.getType()) + " " + coefficient.getForPrice().toPlainString();

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        textRenderer.draw(matrixStack, fullyString, position.x + 10, position.y + 6, -1);

        left.updatePositions(position.x + 3, position.y + (getHeight() - /*87*/88), 10, 14);
        right.updatePositions(position.x + (position.width - 12), position.y + (getHeight() - /*87*/88), 10, 14);
        priceField.updatePositions(position.x + 3, position.y + (getHeight() - /*87*/69), position.width - 6, 12);
        buyPriceCoefficient.updatePositions(position.x + 3, position.y + (getHeight() - /*69*/51), position.width - 6, 12);
        sellPriceCoefficient.updatePositions(position.x + 3, position.y + (getHeight() - /*69*/33), position.width - 6, 12);
        delete.updatePositions(position.x + 3, position.y + (getHeight() - /*69*/15), position.width - 6, 12);

        Scissor.push();
        Scissor.setFromComponentCoordinates(position.x, position.y + 17, position.width, height.getValue());

        priceField.render(matrixStack);
        buyPriceCoefficient.render(matrixStack);
        sellPriceCoefficient.render(matrixStack);
        left.render(matrixStack);
        right.render(matrixStack);
        delete.render(matrixStack);

        TextRenderer t = MinecraftClient.getInstance().textRenderer;

        Text text = Text.of("Режим: " + coefficient.getType());
        Vec2f textPos = MathUtils.getCenteredTextPosition(text, positions);

        t.drawWithShadow(matrixStack, "Режим: " + coefficient.getType(), textPos.x, position.y + (getHeight() - 86), -1);

        Scissor.pop();
    }

    public void mouseClicked(int button) {
        Vec2f mousePos = MathUtils.getMousePos();

        if (extended) {
            left.mouseClicked(mousePos.x, mousePos.y, button);
            right.mouseClicked(mousePos.x, mousePos.y, button);
            priceField.mouseClicked(mousePos.x, mousePos.y, button);
            buyPriceCoefficient.mouseClicked(mousePos.x, mousePos.y, button);
            sellPriceCoefficient.mouseClicked(mousePos.x, mousePos.y, button);
            delete.mouseClicked(mousePos.x, mousePos.y, button);
        }

        if (positions.offset(0, offset + scroll).contains(mousePos.x, mousePos.y)) {
            extended = !extended;

            if (extended) height.animate(positions.height + 90, 0.2, Easings.QUAD_OUT);
            else height.animate(positions.height, 0.2, Easings.QUAD_OUT);
        }
    }

    public void charTyped(char codePoint) {
        if (extended) {
            priceField.charTyped(codePoint);
            buyPriceCoefficient.charTyped(codePoint);
            sellPriceCoefficient.charTyped(codePoint);
        }
    }

    public void keyPressed(int keyCode) {
        if (extended) {
            priceField.keyPressed(keyCode);
            buyPriceCoefficient.keyPressed(keyCode);
            sellPriceCoefficient.keyPressed(keyCode);
        }
    }
}
