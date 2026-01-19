package ru.nedan.spookybuy.screen.setting.page.inst;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import ru.nedan.neverapi.animation.util.Easings;
import ru.nedan.neverapi.gl.Scale;
import ru.nedan.neverapi.gl.Scissor;
import ru.nedan.neverapi.gui.Button;
import ru.nedan.neverapi.math.FloatRectangle;
import ru.nedan.neverapi.math.MathUtils;
import ru.nedan.neverapi.shader.Blur;
import ru.nedan.neverapi.shader.ColorUtility;
import ru.nedan.neverapi.shader.Rounds;
import ru.nedan.spookybuy.autobuy.autoparse.coefficient.Coefficient;
import ru.nedan.spookybuy.screen.setting.CoefficientRenderer;
import ru.nedan.spookybuy.screen.setting.page.Page;
import ru.nedan.spookybuy.screen.setting.page.Pages;
import ru.nedan.spookybuy.screen.setting.page.icon.IconRenderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParserPage extends Page {

    private static ParserPage instance;

    private FloatRectangle positions, original;
    private final List<CoefficientRenderer> renderers = new ArrayList<>();

    public ParserPage() {
        instance = this;
    }

    public static Page getInstance() {
        if (instance == null) new ParserPage();

        return instance;
    }

    public float scroll, animatedScroll;

    private Button add;

    @Override
    public void init() {
        float width = 200;
        float height = 220;

        positions = MathUtils.getCenteredPosition(width, height);
        original = MathUtils.getCenteredPosition(width, height);

        if (Pages.getCurrent() != this) {
            translateAnimation.setToValue(client.getWindow().getScaledWidth() + 500).setValue(client.getWindow().getScaledWidth() + 500);
        }

        add = new Button(Text.of("Добавить"), new FloatRectangle(positions.x, positions.y + positions.height + 4, positions.width, 16), (b) -> {
            Coefficient.getAll().add(Coefficient.createDefault());
            updateCoefficients();
        }, Collections.singletonList(
                Text.of("Добавить новый коэффициент")
        ));

        updateCoefficients();
    }

    @Override
    public void render(MatrixStack matrixStack) {
        translateAnimation.update();
        positions = original.offset((float) translateAnimation.getValue(), 0);

        add.updatePositions(positions.x, positions.y + positions.height + 4, positions.width, 16);
        Blur.register(() -> Rounds.drawRound(positions.x, positions.y, positions.width, positions.height, 8, Color.BLACK));
        Blur.draw(4, ColorUtility.getColorComps(new Color(0x717171)));

        animatedScroll = MathUtils.lerp(animatedScroll, scroll, 8);
        Text text = Text.of("Авто Сетап");
        Vec2f textPos = MathUtils.getCenteredTextPosition(text, new FloatRectangle(positions.x, positions.y + 4, positions.width, client.textRenderer.fontHeight));

        float offset = 4;

        Scale.renderScaled(() -> client.textRenderer.draw(matrixStack, text, textPos.x, textPos.y, -1), new FloatRectangle(textPos.x, textPos.y, client.textRenderer.getWidth(text), client.textRenderer.fontHeight), 1.25f);

        FloatRectangle rectPos = new FloatRectangle(0, positions.y + offset, client.getWindow().getScaledWidth(), positions.height - 10);

        add.render(matrixStack);

        Scissor.push();
        Scissor.setFromComponentCoordinates(rectPos.x, positions.y + 18, rectPos.width, rectPos.height - 14);

        offset += 12;

        for (CoefficientRenderer renderer : renderers) {
            renderer.setPositions(new FloatRectangle(positions.x + 3, positions.y, positions.width - 6, 18));
            offset -= renderer.getHeight() + 3;
            renderer.render(matrixStack);

            renderer.scroll = animatedScroll;
        }

        updateHeight();
        Scissor.pop();

        scroll = MathHelper.clamp(scroll, offset, 0);
    }

    private void updateHeight() {
        float offset = 22;

        for (CoefficientRenderer renderer : renderers) {
            renderer.offset = offset;
            offset += renderer.getHeight() + 3;
        }
    }

    @Override
    public void mouseClicked(int button) {
        for (CoefficientRenderer renderer : renderers) renderer.mouseClicked(button);

        Vec2f mouse = MathUtils.getMousePos();
        add.mouseClicked(mouse.x, mouse.y, button);
    }

    @Override
    public void mouseReleased(int button) {

    }

    @Override
    public void charTyped(char codePoint) {
        for (CoefficientRenderer renderer : renderers) renderer.charTyped(codePoint);
    }

    @Override
    public void keyPressed(int keyCode) {
        for (CoefficientRenderer renderer : renderers) renderer.keyPressed(keyCode);
    }

    @Override
    public void mouseScrolled(double amount) {
        scroll += (float) (amount * 16);
    }

    private void updateCoefficients() {
        renderers.clear();

        float offset = 22;
        for (Coefficient coefficient : Coefficient.getAll()) {
            CoefficientRenderer renderer = new CoefficientRenderer(new FloatRectangle(positions.x + 3, positions.y, positions.width - 6, 18), coefficient, offset);
            renderers.add(renderer);
            offset += renderer.getHeight() + 3;
        }
    }

    private final IconRenderer iconRenderer = new IconRenderer("АвтоСетап") {
        @Override
        public void render(MatrixStack matrixStack) {
            FloatRectangle positions = this.getPositions();

            Rounds.drawRound(positions.x, positions.y, positions.width, positions.height, 4, Color.BLACK);

            Vec2f center = MathUtils.getCenteredTextPosition(Text.of(this.getTitle()), positions);

            client.textRenderer.drawWithShadow(matrixStack, this.getTitle(), center.x + 0.5f, center.y + 0.5f, Pages.getCurrent() == Pages.PARSER.getPage() ? Color.GREEN.getRGB() : -1);
        }

        @Override
        public void mouseClicked(int button) {
            Vec2f mousePos = MathUtils.getMousePos();

            if (getPositions().contains(mousePos.x, mousePos.y)) {
                Pages.setCurrent(Pages.PARSER.getPage());
                Pages.PARSER.getPage().translateAnimation.animate(0, 0.8, Easings.BACK_OUT);
                Pages.ITEMS.getPage().translateAnimation.animate(client.getWindow().getScaledWidth() + 500, 0.8, Easings.BACK_IN);
            }
        }
    };

    @Override
    public IconRenderer getIconRenderer() {
        return iconRenderer;
    }
}
