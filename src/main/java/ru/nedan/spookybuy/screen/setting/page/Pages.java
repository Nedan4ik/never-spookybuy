package ru.nedan.spookybuy.screen.setting.page;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.util.math.MatrixStack;
import ru.nedan.spookybuy.screen.setting.page.inst.ItemsPage;
import ru.nedan.spookybuy.screen.setting.page.inst.ParserPage;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum Pages {
    ITEMS(ItemsPage.getInstance()),
    PARSER(ParserPage.getInstance());

    public static final List<Page> ALL = Arrays.stream(values()).map(Pages::getPage).collect(Collectors.toList());

    final Page page;

    @Getter
    @Setter
    static Page current = ITEMS.getPage();

    public static boolean isCurrent(Page page) {
        return current == page;
    }

    public static void init() {
        ALL.forEach(Page::init);
    }

    public static void render(MatrixStack matrices) {
        ALL.forEach(page -> page.render(matrices));
    }

    public static void mouseReleased(int button) {
        ALL.forEach(page -> page.mouseReleased(button));
    }

    public static void mouseClicked(int button) {
        ALL.forEach(page -> page.mouseClicked(button));
    }

    public static void charTyped(char chr) {
        ALL.forEach(page -> page.charTyped(chr));
    }

    public static void keyPressed(int keyCode) {
        ALL.forEach(page -> page.keyPressed(keyCode));
    }

    public static void mouseScrolled(double amount) {
        ALL.forEach(page -> page.mouseScrolled(amount));
    }
}
