package ru.nedan.spookybuy.screen.setting.page.inst;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import ru.nedan.neverapi.animation.util.Easings;
import ru.nedan.neverapi.etc.KeyUtil;
import ru.nedan.neverapi.etc.TextBuilder;
import ru.nedan.neverapi.gl.Scale;
import ru.nedan.neverapi.gl.Scissor;
import ru.nedan.neverapi.gui.Button;
import ru.nedan.neverapi.gui.TextField;
import ru.nedan.neverapi.math.FloatRectangle;
import ru.nedan.neverapi.math.MathUtils;
import ru.nedan.neverapi.shader.Blur;
import ru.nedan.neverapi.shader.ColorUtility;
import ru.nedan.neverapi.shader.Rounds;
import ru.nedan.spookybuy.SpookyBuy;
import ru.nedan.spookybuy.items.CollectItem;
import ru.nedan.spookybuy.items.ItemStorage;
import ru.nedan.spookybuy.screen.setting.ItemRenderer;
import ru.nedan.spookybuy.screen.setting.allocation.Allocation;
import ru.nedan.spookybuy.screen.setting.page.Page;
import ru.nedan.spookybuy.screen.setting.page.Pages;
import ru.nedan.spookybuy.screen.setting.page.icon.IconRenderer;

import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ItemsPage extends Page {

    private static ItemsPage instance;

    private FloatRectangle positions, original;
    private List<ItemRenderer> renderers;
    private Button abBindButton, autoSetupBindButton, timedAutoSetup;

    private boolean abBinding, autoSetupBinding;

    private TextField buyPriceField, sellPrice;

    public ItemsPage() {
        instance = this;
    }

    public static Page getInstance() {
        if (instance == null) new ItemsPage();

        return instance;
    }

    @Override
    public void init() {
        float width = 200;
        float height = 220;

        positions = MathUtils.getCenteredPosition(width, height);
        original = MathUtils.getCenteredPosition(width, height);
        renderers = new ArrayList<>();

        float offset = 22;

        for (CollectItem collectItem : ItemStorage.ALL) {
            ItemRenderer itemRenderer = new ItemRenderer(new FloatRectangle(positions.x + 3, positions.y, positions.width - 6, 18), collectItem, offset);
            renderers.add(itemRenderer);
            offset += itemRenderer.getPosition().height + 3;
        }

        abBindButton = new ru.nedan.neverapi.gui.Button(Text.of("Автобай: " + KeyUtil.getKey(SpookyBuy.getInstance().getAbKey())), new FloatRectangle(positions.x, positions.y + positions.height + 4, positions.width, 16), (button) -> {
            abBinding = true;
            button.setTitle(Text.of("Клик..."));
        }, Collections.singletonList(Text.of("Клик чтобы забиндить автобай")));

        autoSetupBindButton = new Button(Text.of("АвтоСетап: " + KeyUtil.getKey(SpookyBuy.getInstance().getAutoSetupKey())), new FloatRectangle(positions.x, positions.y + positions.height + 24, positions.width, 16), (button) -> {
            autoSetupBinding = true;
            button.setTitle(Text.of("Клик..."));
        }, Collections.singletonList(Text.of("Клик чтобы забиндить автосетап")));

        timedAutoSetup = new Button(Text.of("Время автонастройки: " + SpookyBuy.getInstance().getAutoSetupTime()), new FloatRectangle(positions.x, positions.y + positions.height + 44, positions.width, 16), (button) -> {

        }, Arrays.asList(Text.of("Миллисекунды"), new TextBuilder().append("Со значением ").append("менее", Formatting.RED).append(" 300000").append(" не работает", Formatting.RED).build(), Text.of("ЛКМ - увеличить на 1000 (c CTRL на 10000)"), Text.of("ПКМ - уменьшить на 1000 (c CTRL на 10000)"))) {
            @Override
            public void mouseClicked(double mouseX, double mouseY, int button) {
                if (this.getPositions().contains(mouseX, mouseY)) {
                    long time = SpookyBuy.getInstance().getAutoSetupTime();
                    boolean hasCtrl = Screen.hasControlDown();

                    switch (button) {
                        case 0: {
                            // INCREASE
                            SpookyBuy.getInstance().setAutoSetupTime(time + (hasCtrl ? 10000 : 1000));
                            this.setTitle(Text.of("Время автонастройки: " + SpookyBuy.getInstance().getAutoSetupTime()));
                            break;
                        }
                        case 1: {
                            long decrement = hasCtrl ? 10000 : 1000;

                            if (time <= 0) break;

                            long newTime = Math.max(0, time - decrement);

                            SpookyBuy.getInstance().setAutoSetupTime(newTime);
                            this.setTitle(Text.of("Время автонастройки: " + newTime));
                            break;
                        }

                        default:
                            break;
                    }
                }
            }
        };

        FloatRectangle position = new FloatRectangle(this.positions.x + this.positions.width + 30, this.positions.y, 180, 120);

        buyPriceField = new ru.nedan.neverapi.gui.TextField(position.x + 3, position.y + 20, position.width - 6, 12, "Цена покупки всех предметов", (string) -> {
            if (string.isEmpty()) string = "0";

            BigDecimal bigDecimal = new BigDecimal(string);

            List<ItemRenderer> allocatedRenderers = renderers.stream()
                    .filter(itemRenderer1 -> itemRenderer1.allocated)
                    .toList();

            for (ItemRenderer renderer : allocatedRenderers) {
                SpookyBuy.getInstance().getAutoBuy().getPriceMap().putPrice(renderer.getCollectItem(), bigDecimal, false);
//                SpookyBuy.getInstance().getAutoBuy().getPriceMap().putCoefficient(renderer.getCollectItem(), bigDecimal, true);
            }

//            SpookyBuy.getInstance().getAutoBuy().getPriceMap().putPrice(collectItem, bigDecimal, false);
        }, Character::isDigit);

        sellPrice = new ru.nedan.neverapi.gui.TextField(position.x + 3, position.y + 38, position.width - 6, 12, "Цена продажи всех предметов", (string) -> {
            if (string.isEmpty()) string = "0";

            BigDecimal bigDecimal = new BigDecimal(string);

            List<ItemRenderer> allocatedRenderers = renderers.stream()
                    .filter(itemRenderer1 -> itemRenderer1.allocated)
                    .toList();

            for (ItemRenderer renderer : allocatedRenderers) {
                SpookyBuy.getInstance().getAutoBuy().getPriceMap().putPrice(renderer.getCollectItem(), bigDecimal, true);
            }
        }, Character::isDigit);
    }

    public float scroll, animatedScroll;

    @Override
    public void render(MatrixStack matrixStack) {
        translateAnimation.update();
        positions = original.offset((float) translateAnimation.getValue(), 0);

        abBindButton.updatePositions(positions.x, positions.y + positions.height + 4, positions.width, 16);
        autoSetupBindButton.updatePositions(positions.x, positions.y + positions.height + 24, positions.width, 16);
        timedAutoSetup.updatePositions(positions.x, positions.y + positions.height + 44, positions.width, 16);

        Blur.register(() -> Rounds.drawRound(positions.x, positions.y, positions.width, positions.height, 8, Color.BLACK));
        Blur.draw(4, ColorUtility.getColorComps(new Color(0x717171)));

        Text text = Text.of("Never SpookyBuy");
        Vec2f textPos = MathUtils.getCenteredTextPosition(text, new FloatRectangle(positions.x, positions.y + 4, positions.width, client.textRenderer.fontHeight));

        animatedScroll = MathUtils.lerp(animatedScroll, scroll, 8);

        Scale.renderScaled(() -> {
            client.textRenderer.draw(matrixStack, text, textPos.x, textPos.y, -1);
        }, new FloatRectangle(textPos.x, textPos.y, client.textRenderer.getWidth(text), client.textRenderer.fontHeight), 1.25f);

        float offset = 4;

        Scissor.push();

        FloatRectangle rectPos = new FloatRectangle(0, positions.y + offset, client.getWindow().getScaledWidth(), positions.height - 10);

        Scissor.setFromComponentCoordinates(rectPos.x, positions.y + 18, rectPos.width, rectPos.height - 14);

        offset += 12;

        for (ItemRenderer renderer : renderers) {
            renderer.setPosition(new FloatRectangle(positions.x + 3, positions.y, positions.width - 6, 18));
            offset -= renderer.getHeight() + 3;

            renderer.scroll = animatedScroll;
            renderer.render(matrixStack);
        }

        Scissor.pop();

        abBindButton.render(matrixStack);
        autoSetupBindButton.render(matrixStack);
        timedAutoSetup.render(matrixStack);

        updateHeight();

        { // RENDER ALLOCATED
            List<ItemRenderer> allocatedRenderers = renderers.stream()
                    .filter(itemRenderer1 -> itemRenderer1.allocated)
                    .toList();

            if (!allocatedRenderers.isEmpty()) {
                FloatRectangle renderPosition = new FloatRectangle(this.positions.x + this.positions.width + 30, this.positions.y, 180, 55);

                Blur.register(() -> Rounds.drawRound(renderPosition.x, renderPosition.y, renderPosition.width, renderPosition.height, 8, Color.BLACK));
                Blur.draw(4, ColorUtility.getColorComps(new Color(0x717171)));

                LiteralText allocatedText = new LiteralText("Выделено предметов: " + allocatedRenderers.size() + "/" + renderers.size());
                Vec2f textPosition = MathUtils.getCenteredTextPosition(allocatedText, new FloatRectangle(renderPosition.x, renderPosition.y, renderPosition.width, client.textRenderer.fontHeight + 4));

                client.textRenderer.draw(matrixStack, allocatedText, textPosition.x, textPosition.y, -1);
                buyPriceField.render(matrixStack);
                sellPrice.render(matrixStack);
            }
        }

        scroll = MathHelper.clamp(scroll, offset, 0);
    }

    private void updateHeight() {
        float offset = 22;

        for (ItemRenderer renderer : renderers) {
            renderer.offset = offset;
            offset += renderer.getHeight() + 3;
        }
    }

    @Override
    public void mouseClicked(int button) {
        Vec2f mousePos = MathUtils.getMousePos();
        double mouseX = mousePos.x;
        double mouseY = mousePos.y;

        FloatRectangle renderPosition = new FloatRectangle(this.positions.x + this.positions.width + 30, this.positions.y, 180, 120);

        List<ItemRenderer> allocatedRenderers = renderers.stream()
                .filter(itemRenderer1 -> itemRenderer1.allocated)
                .toList();

        if (renderPosition.contains(mouseX, mouseY) && !allocatedRenderers.isEmpty()) {
            buyPriceField.mouseClicked(mouseX, mouseY, button);
            sellPrice.mouseClicked(mouseX, mouseY, button);

            return;
        }

        for (ItemRenderer renderer : renderers) {
            renderer.allocated = false;
        }

        FloatRectangle rectPos = new FloatRectangle(0, positions.y + 4, client.getWindow().getScaledWidth(), positions.height - 10);

        for (ItemRenderer renderer : renderers) {
            FloatRectangle rendererPos = renderer.getPosition().offset(0, renderer.offset + animatedScroll);

            if (!rectPos.contains(rendererPos.x, rendererPos.y)) continue;

            renderer.mouseClicked(mouseX, mouseY, button);
        }

        abBindButton.mouseClicked(mouseX, mouseY, button);
        autoSetupBindButton.mouseClicked(mouseX, mouseY, button);
        timedAutoSetup.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased(int button) {

    }

    @Override
    public void charTyped(char codePoint) {
        buyPriceField.charTyped(codePoint);
        sellPrice.charTyped(codePoint);

        FloatRectangle rectPos = new FloatRectangle(0, positions.y + 4, client.getWindow().getScaledWidth(), positions.height - 10);

        for (ItemRenderer renderer : renderers) {
            FloatRectangle rendererPos = renderer.getPosition().offset(0, renderer.offset + animatedScroll);

            if (!rectPos.contains(rendererPos.x, rendererPos.y)) {
                continue;
            }

            renderer.charTyped(codePoint);
        }
    }

    @Override
    public void mouseScrolled(double amount) {
        scroll += (float) (amount * 16);
    }

    @Override
    public void keyPressed(int keyCode) {
        buyPriceField.keyPressed(keyCode);
        sellPrice.keyPressed(keyCode);

        FloatRectangle rectPos = new FloatRectangle(0, positions.y + 4, client.getWindow().getScaledWidth(), positions.height - 10);

        for (ItemRenderer renderer : renderers) {
            FloatRectangle rendererPos = renderer.getPosition().offset(0, renderer.offset + animatedScroll);

            if (!rectPos.contains(rendererPos.x, rendererPos.y)) {
                continue;
            }

            renderer.keyPressed(keyCode);
        }

        if (abBinding) {
            abBinding = false;
            SpookyBuy.getInstance().setAbKey(keyCode);
            abBindButton.setTitle(Text.of("Автобай: " + KeyUtil.getKey(SpookyBuy.getInstance().getAbKey())));
        }

        if (autoSetupBinding) {
            autoSetupBinding = false;
            SpookyBuy.getInstance().setAutoSetupKey(keyCode);
            autoSetupBindButton.setTitle(Text.of("АвтоСетап: " + KeyUtil.getKey(SpookyBuy.getInstance().getAutoSetupKey())));
        }
    }

    private final IconRenderer iconRenderer = new IconRenderer("АвтоБай") {
        @Override
        public void render(MatrixStack matrixStack) {
            FloatRectangle positions = this.getPositions();

            Rounds.drawRound(positions.x, positions.y, positions.width, positions.height, 4, Color.BLACK);

            Vec2f center = MathUtils.getCenteredTextPosition(Text.of(this.getTitle()), positions);

            client.textRenderer.drawWithShadow(matrixStack, this.getTitle(), center.x + 0.5f, center.y + 0.5f, Pages.getCurrent() == Pages.ITEMS.getPage() ? Color.GREEN.getRGB() : -1);
        }

        @Override
        public void mouseClicked(int button) {
            Vec2f mousePos = MathUtils.getMousePos();

            if (getPositions().contains(mousePos.x, mousePos.y)) {
                Pages.setCurrent(Pages.ITEMS.getPage());
                Pages.ITEMS.getPage().translateAnimation.animate(0, 0.8, Easings.BACK_OUT);
                Pages.PARSER.getPage().translateAnimation.animate(client.getWindow().getScaledWidth() + 500, 0.8, Easings.BACK_IN);
            }
        }
    };

    @Override
    public IconRenderer getIconRenderer() {
        return iconRenderer;
    }
}
