package ru.nedan.spookybuy.screen.setting;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import ru.nedan.neverapi.math.FloatRectangle;
import ru.nedan.neverapi.math.MathUtils;

public class AcceptScreen extends Screen {

    final Screen prev;
    final Text acceptString;
    final Runnable[] actions;

    public AcceptScreen(Screen prev, Text acceptString, Runnable[] actions) {
        super(Text.of("AcceptScreen"));
        this.prev = prev;
        this.acceptString = acceptString;
        this.actions = actions;
    }

    @Override
    protected void init() {
        super.init();

        FloatRectangle centerPosition = MathUtils.getCenteredPosition(200, 20);

        addButton(new ButtonWidget((int) centerPosition.x, (int) centerPosition.y, 100, 20, Text.of("Да"), (butt) -> {
            actions[0].run();
            client.openScreen(prev);
        }));

        addButton(new ButtonWidget((int) centerPosition.x + 110, (int) centerPosition.y, 100, 20, Text.of("Нет"), (butt) -> {
            actions[1].run();
            client.openScreen(prev);
        }));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        Vec2f textPos = MathUtils.getCenteredTextPosition(acceptString, new FloatRectangle(0, 0, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight()).offset(0, -40));

        client.textRenderer.drawWithShadow(matrices, acceptString, textPos.x, textPos.y, -1);
    }
}
