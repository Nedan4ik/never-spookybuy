package ru.nedan.spookybuy.screen.setting.page;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import ru.nedan.neverapi.animation.Animation;
import ru.nedan.spookybuy.SpookyBuy;
import ru.nedan.spookybuy.screen.setting.SpookyBuyGui;
import ru.nedan.spookybuy.screen.setting.page.icon.IconRenderer;

public abstract class Page {

    protected static final MinecraftClient client = MinecraftClient.getInstance();
    protected static final SpookyBuyGui gui = SpookyBuy.getInstance().getGui();

    public Animation translateAnimation = new Animation();

    public abstract void init();
    public abstract void render(MatrixStack matrixStack);
    public abstract void mouseClicked(int button);
    public abstract void mouseReleased(int button);
    public abstract void charTyped(char codePoint);
    public abstract void keyPressed(int keyCode);
    public abstract void mouseScrolled(double amount);

    public abstract IconRenderer getIconRenderer();

}
