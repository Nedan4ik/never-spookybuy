package ru.nedan.spookybuy.screen.setting.page.icon;

import lombok.Getter;
import net.minecraft.client.util.math.MatrixStack;
import ru.nedan.neverapi.math.FloatRectangle;

@Getter
public class IconRenderer {
    private final String title;
    private FloatRectangle positions;

    public IconRenderer(String title) {
        this.title = title;
    }

    public void updatePosition(FloatRectangle position) {
        this.positions = position;
    }

    public void render(MatrixStack matrixStack) {

    }

    public void mouseClicked(int button) {

    }
}
