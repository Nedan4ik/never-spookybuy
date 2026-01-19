package ru.nedan.spookybuy;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;
import ru.nedan.neverapi.NeverAPI;
import ru.nedan.neverapi.etc.*;
import ru.nedan.neverapi.event.api.Event;
import ru.nedan.neverapi.event.impl.EventPress;
import ru.nedan.neverapi.event.impl.EventWindowOpen;
import ru.nedan.neverapi.shader.ColorUtility;
import ru.nedan.spookybuy.autobuy.AutoBuy;
import ru.nedan.spookybuy.autobuy.GenericContainerScreenHook;
import ru.nedan.spookybuy.autobuy.autoparse.AutoParser;
import ru.nedan.spookybuy.autobuy.autoparse.coefficient.Coefficient;
import ru.nedan.spookybuy.autobuy.functional.inst.AutoBuyFunction;
import ru.nedan.spookybuy.screen.configs.ConfigScreen;
import ru.nedan.spookybuy.screen.setting.SpookyBuyGui;
import ru.nedan.spookybuy.util.script.ScriptStorage;
import ru.nedan.spookybuy.util.CommandRegister;
import ru.nedan.spookybuy.util.Discord;
import ru.nedan.spookybuy.util.autojoin.AutoJoin;
import ru.nedan.spookybuy.util.autojoin.AutoJoinConfiguration;
import ru.nedan.spookybuy.util.telegram.TelegramAPI;
import ru.nedan.spookybuy.util.telegram.command.api.TelegramCommandExecutor;
import ru.nedan.spookybuy.util.ws.Client;

import java.awt.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class SpookyBuy implements ModInitializer {
    @Getter
    private static SpookyBuy instance;

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    @Setter
    private boolean state, autoSetupState;

    @Setter
    private long autoSetupTime;

    private final SpookyBuyGui gui = new SpookyBuyGui();
    private final AutoBuy autoBuy;
    private final TelegramCommandExecutor commandExecutor;

    @Setter
    private int abKey;

    @Setter
    private int autoSetupKey;

    @Getter
    private static final TextBuilder spookyBuyAppender = new TextBuilder()
            .append(gradient())
            .append(" » ", Formatting.BLUE);

    private final Executor executor = Executors.newCachedThreadPool();

    @Setter
    private String anarchy;

    @Override
    public void onInitialize() {
    }

    private final Discord discordRPC;

    public SpookyBuy() {
        if (!NeverAPI.isInit()) new NeverAPI();

        instance = this;
        autoBuy = new AutoBuy();
        commandExecutor = new TelegramCommandExecutor();

        TelegramAPI.start();
        CommandRegister.registerAll();
        new AutoParser();
        new AutoJoin();
        new Client("ws://5.83.140.208:25975").startClient();

        discordRPC = new Discord();
        discordRPC.run();

        TextVisitors.putConsumer((string, reference) -> {
            if (string.contains(mc.getSession().getUsername())) {
                reference.set(string.replaceAll(mc.getSession().getUsername(), "t.me/neverproduct"));
            }
        });

        Pattern donatePattern = Pattern.compile("\\b(Игрок|Барон|Страж|Герой|Глава|Аспид|Сквид|Элита|Титан|Принц|Князь|Герцог)\\b");

        TextVisitors.putConsumer(((string, style, reference) -> {
            Matcher matcher = donatePattern.matcher(string);

            /* замена любого доната на Staff
            if (matcher.find()) {
                String foundDonate = matcher.group(1);
                reference.set(new Pair<>(string.replaceAll(foundDonate, "Staff"), style.withFormatting(Formatting.RED)));
            }*/

            if (string.contains(mc.getSession().getUsername())) {
                reference.set(new Pair<>(string.replaceAll(mc.getSession().getUsername(), "t.me/neverproduct"), reference.get().getRight()));
            }
        }));

        Event.addListener(EventWindowOpen.class, e -> {
            if (e.getScreen() instanceof GenericContainerScreen) {
                e.setScreen(new GenericContainerScreenHook((GenericContainerScreen) e.getScreen()));
            }
        });

        Event.addListener(EventPress.class, e -> {
            if (e.getAction() == 1) {
                if (mc.currentScreen == null || mc.currentScreen instanceof GenericContainerScreen) {
                    if (e.getKey() == abKey) {
                        state = !state;
                        ChatUtility.sendMessage(spookyBuyAppender
                                .copy()
                                .append("АвтоБай успешно ")
                                .append(state ? "включен!" : "выключен!", state ? Formatting.GREEN : Formatting.RED)
                                .build());
                    }

                    if (e.getKey() == autoSetupKey) {
                        autoSetupState = !autoSetupState;
                        AutoBuyFunction.onAutoSetupToggle(false);
                    }

                    if (e.getKey() == GLFW.GLFW_KEY_G) {
                        assert mc.player != null;
                        ConfigScreen sc = new ConfigScreen(mc.player.inventory, (slot, button) -> {
                            if (slot == -999) return;

                            ConfigScreen configScreen = (ConfigScreen) mc.currentScreen;

                            assert configScreen != null;
                            ItemStack stack = configScreen.getScreenHandler().getSlot(slot).getStack();

                            if (stack.isEmpty()) return;
                            NbtCompound compound = stack.getTag();

                            assert compound != null;

                            if (button == 0) {
                                String key = compound.getString("key");
                                String cloudKey = configScreen.keys.get(key);
                                Client.getInstance().sendLoadConfigResponse(cloudKey);
                            } else if (button == 1) {
                                Util.getOperatingSystem().open(compound.getString("discordLink"));
                            } else if (button == 2) {
                                String itemName = compound.getString("authorName");
                                ConfigScreen.toggleAuthor(itemName);

                                NbtCompound display = compound.getCompound("display");

                                NbtList lore = display.getList("Lore", 8);

                                if (!lore.isEmpty()) {
                                    Style defaultStyle = Style.EMPTY.withItalic(false);
                                    Style actionStyle = defaultStyle.withColor(Formatting.GOLD);

                                    lore.set(3, NbtString.of(Text.Serializer.toJson(new LiteralText("СКМ Авто-загрузка: ").setStyle(actionStyle).append(new LiteralText(ConfigScreen.autoInject(itemName) ? "§aвключено" : "§cвыключено")))));
                                }

                                display.put("Lore", lore);
                                compound.put("display", display);
                                stack.setTag(compound);
                            }
                        });

                        mc.openScreen(sc);
                        mc.player.currentScreenHandler = sc.getScreenHandler();
                    }
                }

                if (mc.currentScreen != null) return;

                if (e.getKey() == GLFW.GLFW_KEY_RIGHT_SHIFT) {
                    mc.openScreen(gui);
                }
            }
        });

        {
/*            JsonElement element = Config.readData("./config/default-spookybuy.nvr");

            if (element != null) {
                loadConfig(element);
            }*/

            JsonElement telegramElement = Config.readData("./config/telegram-spookybuy.nvr");

            if (telegramElement != null) {
                TelegramAPI.readFromConfig(telegramElement);
            }

            AutoJoinConfiguration.deserialize();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            saveConfig("default-spookybuy");
            saveTelegram();
            AutoJoinConfiguration.serialize();
        }));

        if (!Libraries.hasLibrary("org.openjdk.nashorn.api.scripting.ScriptUtils")) {
            try {
                Libraries.setToDownload("https://repo1.maven.org/maven2/", "org.openjdk.nashorn", "nashorn-core", "15.6");
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }

        try {
            ScriptStorage.reloadScripts();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public static void saveConfig(String file) {
        Config.saveData("./config/" + file + ".nvr", saveConfig());
    }

    public static JsonObject saveConfig() {
        JsonObject mai = new JsonObject();
        JsonObject main = new JsonObject();
        JsonObject settings = new JsonObject();

        settings.addProperty("abBind", instance.abKey);
        settings.addProperty("autoSetupBind", instance.autoSetupKey);

        main.add("settings", settings);

        JsonObject items = new JsonObject();
        instance.getAutoBuy().getPriceMap().saveInConfig(items);

        main.add("items", items);

        JsonArray coefficients = new JsonArray();
        for (Coefficient coefficient : Coefficient.getAll()) coefficient.serialize(coefficients);

        main.add("coefficients", coefficients);

        mai.add("main", main);

        return mai;
    }

    private void saveTelegram() {
        String path = "./config/telegram-spookybuy.nvr";
        JsonObject main = new JsonObject();

        TelegramAPI.saveInConfig(main);

        Config.saveData(path, main);
    }

    public static void loadConfig(JsonElement element) {
        if (!element.isJsonObject()) return;

        JsonObject mai = element.getAsJsonObject();

        if (!mai.has("main")) return;

        JsonObject main = mai.getAsJsonObject("main");

        if (main.has("settings")) {
            JsonObject settings = main.getAsJsonObject("settings");

            if (settings.has("abBind")) {
                instance.abKey = settings.get("abBind").getAsInt();
            }

            if (settings.has("autoSetupBind")) {
                instance.autoSetupKey = settings.get("autoSetupBind").getAsInt();
            }
        }

        if (main.has("items")) {
            instance.getAutoBuy().getPriceMap().readFromConfig(main);
        }

        if (main.has("coefficients")) {
            Coefficient.deserialize(main.getAsJsonArray("coefficients"));
        }
    }

    private static LiteralText gradient() {
        int first = new Color(0x4344D6).getRGB();
        int end = new Color(0x0642C6).getRGB();

        LiteralText text = new LiteralText("");
        for (int i = 0; i < "Never SpookyBuy".length(); i++)
            text.append(new LiteralText(String.valueOf("Never SpookyBuy".charAt(i))).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(ColorUtility.interpolateColor(first, end, (float) i / "NeverBuy".length())))));

        text.append(new LiteralText("").formatted(Formatting.RESET));

        return text;
    }
}
