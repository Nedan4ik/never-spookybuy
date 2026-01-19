package ru.nedan.spookybuy.screen.setting.allocation;

import lombok.Getter;
import net.minecraft.util.math.Vec2f;
import ru.nedan.neverapi.math.FloatRectangle;
import ru.nedan.neverapi.math.MathUtils;

public class Allocation {
    @Getter
    private FloatRectangle rectangle;

    private Vec2f start;

    public void init() {
        start = MathUtils.getMousePos();

        rectangle = new FloatRectangle(start.x, start.y, 0, 0);
    }

    public void mouseMoved() {
        if (start == null) return;

        Vec2f current = MathUtils.getMousePos();

        float x1 = Math.min(start.x, current.x);
        float y1 = Math.min(start.y, current.y);
        float x2 = Math.max(start.x, current.x);
        float y2 = Math.max(start.y, current.y);

        rectangle = new FloatRectangle(x1, y1, x2 - x1, y2 - y1);
    }

    public void stop() {
        start = null;
        rectangle = null;
    }
}