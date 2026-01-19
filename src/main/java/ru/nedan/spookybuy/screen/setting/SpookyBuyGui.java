package ru.nedan.spookybuy.screen.setting;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import ru.nedan.neverapi.math.FloatRectangle;
import ru.nedan.neverapi.math.MathUtils;
import ru.nedan.spookybuy.screen.setting.page.Page;
import ru.nedan.spookybuy.screen.setting.page.Pages;
import ru.nedan.spookybuy.screen.setting.page.icon.IconRenderer;
import ru.nedan.spookybuy.screen.setting.page.inst.ItemsPage;

public class SpookyBuyGui extends Screen {

    public SpookyBuyGui() {
        super(new LiteralText("SpookyBuyGui"));
    }

    @Override
    protected void init() {
        super.init();

        float width = (float) Pages.ALL.stream()
                .map(Page::getIconRenderer)
                .map(IconRenderer::getTitle)
                .mapToInt(title -> client.textRenderer.getWidth(title) + 10)
                .sum();

        float height = 20;

        FloatRectangle iconPositions = MathUtils.getCenteredPosition(width, height).offset(0, 200);

        for (Page page : Pages.ALL) {
            page.getIconRenderer().updatePosition(new FloatRectangle(iconPositions.x, iconPositions.y, client.textRenderer.getWidth(page.getIconRenderer().getTitle()) + 5, 13));
            iconPositions = iconPositions.offset(client.textRenderer.getWidth(page.getIconRenderer().getTitle()) + 10, 0);
        }

        Pages.init();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        Pages.render(matrices);

        for (Page page : Pages.ALL) {
            page.getIconRenderer().render(matrices);
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Pages.mouseReleased(button);

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Pages.mouseClicked(button);

        for (Page page : Pages.ALL) {
            page.getIconRenderer().mouseClicked(button);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        Pages.charTyped(chr);

        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        Pages.keyPressed(keyCode);

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        Pages.mouseScrolled(amount);

        return super.mouseScrolled(mouseX, mouseY, amount);
    }
}
