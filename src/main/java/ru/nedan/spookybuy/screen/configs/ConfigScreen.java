package ru.nedan.spookybuy.screen.configs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.RandomStringUtils;
import ru.nedan.neverapi.async.AsyncRunManager;
import ru.nedan.spookybuy.event.EventServerResponse;
import ru.nedan.spookybuy.util.ws.Client;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class ConfigScreen extends GenericContainerScreen {

    public final Map<String, String> keys = new HashMap<>();

    public ConfigScreen(PlayerInventory inventory, BiConsumer<Integer, Integer> ijConsumer) {
        super(createCustomContainerHandler(inventory, -1, 6, ijConsumer), inventory, new LiteralText("Never SpookyBuy — Конфиги"));
    }

    @Override
    protected void init() {
        super.init();

        AsyncRunManager.once(EventServerResponse.class, e -> {
            e.setUsed(true);
            JsonArray configs = e.asJson().getAsJsonArray("message");
            GenericContainerScreenHandler screenHandler = this.getScreenHandler();

            int i = 0;
            for (JsonElement element : configs) {
                JsonObject object = element.getAsJsonObject();

                String itemName = object.get("itemName").getAsString();
                String description = object.get("description").getAsString();
                String discordLink = object.get("discordLink").getAsString();
                String cloudKey = object.get("cloudKey").getAsString();
                int usersSize = object.get("usersSize").getAsInt();

                ItemStack stack = new ItemStack(Items.NETHER_STAR);
                NbtCompound nbt = stack.getOrCreateTag();

                NbtCompound display = new NbtCompound();

                Style defaultStyle = Style.EMPTY.withItalic(false);

                Style nameStyle = defaultStyle.withColor(Formatting.AQUA).withBold(true);
                Style descStyle = defaultStyle.withColor(Formatting.GRAY);
                Style actionStyle = defaultStyle.withColor(Formatting.GOLD);

                display.putString("Name", Text.Serializer.toJson(new LiteralText(itemName).setStyle(nameStyle)));

                NbtList loreList = new NbtList();
                loreList.add(NbtString.of(Text.Serializer.toJson(new LiteralText(description).setStyle(descStyle))));
                loreList.add(NbtString.of(Text.Serializer.toJson(new LiteralText("ЛКМ — загрузить").setStyle(actionStyle))));
                loreList.add(NbtString.of(Text.Serializer.toJson(new LiteralText("ПКМ — перейти в Discord").setStyle(actionStyle))));
                loreList.add(NbtString.of(Text.Serializer.toJson(new LiteralText("СКМ Авто-загрузка: ").setStyle(actionStyle).append(new LiteralText(autoInject(itemName) ? "§aвключено" : "§cвыключено")))));
                loreList.add(NbtString.of(Text.Serializer.toJson(new LiteralText("Пользователей: ").setStyle(actionStyle).append(new LiteralText("" + usersSize).setStyle(Style.EMPTY.withFormatting(Formatting.AQUA))))));
                display.put("Lore", loreList);
                display.put("Lore", loreList);
                nbt.put("display", display);

                String randomString = RandomStringUtils.randomAlphabetic(15);

                nbt.putString("discordLink", discordLink);
                nbt.putString("key", randomString);
                nbt.putString("authorName", itemName);
                stack.setTag(nbt);
                screenHandler.setStackInSlot(i, stack);
                i++;
                keys.put(randomString, cloudKey);
            }
        }, e -> e.asJson().isJsonObject() && e.asJson().has("type") && e.asJson().get("type").getAsString().equalsIgnoreCase("configs"));

        JsonObject object = new JsonObject();
        object.addProperty("action", "askConfigs");
        Client.getInstance().sendMessage(object.toString());
    }

    public static GenericContainerScreenHandler createCustomContainerHandler(PlayerInventory playerInventory, int syncId, int rows, BiConsumer<Integer, Integer> ijConsumer) {
        Inventory containerInventory = new SimpleInventory(9 * rows);
        return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, containerInventory, rows) {
            @Override
            public ItemStack onSlotClick(int i, int j, SlotActionType actionType, PlayerEntity playerEntity) {
                ijConsumer.accept(i, j);

                return ItemStack.EMPTY;
            }
        };
    }

    private static final HashMap<String, Boolean> flags = new HashMap<>();

    public static void toggleAuthor(String s) {
        if (!flags.containsKey(s)) flags.put(s, false);
        else flags.replace(s, !flags.get(s));
    }

    public static boolean autoInject(String authorName) {
        return flags.getOrDefault(authorName, true);
    }
}
