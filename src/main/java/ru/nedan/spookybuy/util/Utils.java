package ru.nedan.spookybuy.util;

import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class Utils {
    private final MinecraftClient mc = MinecraftClient.getInstance();

    public String getSeller(ItemStack stack) {
        List<Text> itemTooltip = getTooltip(stack);

        for (Text line : itemTooltip) {
            try {
                if (line.getString().contains("☤ Продавец:")) {
                    return line.getString().replace("☤ Продавец: ", "").replaceAll("\\$", "").replaceAll(" ", "");
                }
            } catch (Exception ignored) {
            }
        }

        return "";
    }

    public int getPrice(ItemStack stack) {
        List<Text> itemTooltip = getTooltip(stack);

        for (Text line : itemTooltip) {
            try {
                boolean hasByOne = itemTooltip.stream()
                        .anyMatch(text -> text.getString().startsWith("$ Цена за 1 шт."));

                if (line.getString().contains("$ Цена: $") && itemTooltip.get(itemTooltip.indexOf(line) + (hasByOne ? 2 : 1)).getString().contains("☤")) {
                    String part = line.getString().replace("$ Цена: $", "").replaceAll("\\$", "").replaceAll(" ", "").replaceAll(",", "");
                    return Integer.parseInt(part);
                }
            } catch (Exception ignored) {
            }
        }

        return -1;
    }

    public static String getCurrentAnarchy() {
        PlayerListHud playerListHud = mc.inGameHud.getPlayerListHud();
        String anString = "";

        List<Field> textFields = new ArrayList<>();

        for (Field field : playerListHud.getClass().getDeclaredFields()) {
            if (field.getType().equals(Text.class)) {
                field.setAccessible(true);
                textFields.add(field);
            }
        }

        for (Field textField : textFields) {
            try {
                Text text = (Text) textField.get(playerListHud);

                if (text == null) return "none";

                if (text.getString().contains("Режим: Анархия"))
                    anString = text.getString();

            } catch (IllegalAccessException ignored) {

            }
        }

        if (anString.isEmpty())
            return "none";

        String[] split = anString.split("Режим: ");

        if (split.length < 2)
            return "none";

        return split[1].replaceAll("Анархия-", "");
    }

    public List<Text> getTooltip(ItemStack stack) {
        return stack.getTooltip(mc.player, mc.options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.NORMAL);
    }

}
