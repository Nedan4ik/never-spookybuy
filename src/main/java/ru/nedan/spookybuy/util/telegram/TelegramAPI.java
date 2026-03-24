package ru.nedan.spookybuy.util.telegram;

import com.google.gson.*;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import ru.nedan.neverapi.NeverAPI;
import ru.nedan.neverapi.etc.ChatUtility;
import ru.nedan.neverapi.http.HttpUtil;
import ru.nedan.spookybuy.SpookyBuy;
import ru.nedan.spookybuy.event.EventTelegramMessage;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TelegramAPI {
    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    public static String token = "";
    public static String chatId = "";
    private static int lastOffset = 0;

    public static final JsonArray START_KEYBOARD = buildInlineKeyboard(new String[][]{
            {"Баланс", "Статистика общ"},
            {"Скрин", "Помощь"}
    });

    public static void start() {
        executor.scheduleWithFixedDelay(() -> {
            if (token == null || token.isEmpty()) return;

            try {
                JsonArray array = pollMessage();

                for (JsonElement object : array) {
                    JsonObject update = object.getAsJsonObject();

                    if (update.has("update_id")) {
                        lastOffset = update.get("update_id").getAsInt() + 1;
                    }

                    if (!update.has("message")) continue;

                    JsonObject message = update.get("message").getAsJsonObject();

                    if (!message.has("from")) continue;
                    JsonObject from = message.get("from").getAsJsonObject();

                    if (from.has("is_bot") && from.get("is_bot").getAsBoolean()) continue;
                    if (!message.has("text")) continue;

                    String text = message.get("text").getAsString();
                    String senderId = from.get("id").getAsString();

                    if (text.equalsIgnoreCase("/start")) {
                        if (chatId == null || chatId.isEmpty()) {
                            chatId = senderId;
                        }

                        continue;
                    }

                    if (senderId.equalsIgnoreCase(chatId)) {
                        NeverAPI.getApi().getEventBus().post(new EventTelegramMessage(update));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    public static void stop() {
        executor.shutdown();
    }

    public static String getURL() {
        return "https://api.telegram.org/bot" + token;
    }

    public static void sendMessage(String message, JsonArray keyboard) {
        sendMessage(message, chatId, keyboard);
    }

    public static void saveInConfig(JsonObject main) {
        JsonObject telegramObject = new JsonObject();
        telegramObject.addProperty("token", token);
        telegramObject.addProperty("chatId", chatId);
        telegramObject.addProperty("sendBuy", SpookyBuy.getInstance().getAutoBuy().getAb().isSendBuy());
        telegramObject.addProperty("sendSell", SpookyBuy.getInstance().getAutoBuy().getAb().isSendSell());
        main.add("telegram", telegramObject);
    }

    public static void readFromConfig(JsonElement el) {
        if (el == null || el instanceof JsonNull) return;
        JsonObject main = el.getAsJsonObject();
        if (main.has("telegram")) {
            JsonObject obj = main.getAsJsonObject("telegram");
            if (obj.has("token")) token = obj.get("token").getAsString();
            if (obj.has("chatId")) chatId = obj.get("chatId").getAsString();
        }
    }

    public static void sendMessage(String message, String targetChatId, JsonArray keyboard) {
        if (targetChatId == null || targetChatId.isEmpty()) return;
        CompletableFuture.runAsync(() -> {
            try {
                String encodedText = URLEncoder.encode(message, "UTF-8");
                StringBuilder url = new StringBuilder(getURL() + "/sendMessage?chat_id=" + targetChatId + "&text=" + encodedText + "&parse_mode=Markdown");

                if (keyboard != null) {
                    JsonObject markup = new JsonObject();
                    markup.add("keyboard", keyboard);
                    markup.addProperty("resize_keyboard", true);
                    url.append("&reply_markup=").append(URLEncoder.encode(markup.toString(), "UTF-8"));
                }

                new HttpUtil.RequestBuilder().setUrl(url.toString()).setMethod("POST").setBody("").build().execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void reply(JsonObject messageObj, String text, JsonArray keyboard) {
        if (!messageObj.has("from")) return;
        String senderId = messageObj.get("from").getAsJsonObject().get("id").getAsString();
        sendMessage(text, senderId, keyboard);
    }

    public static JsonArray buildInlineKeyboard(String[][] buttons) {
        JsonArray keyboard = new JsonArray();
        for (String[] row : buttons) {
            JsonArray rowArr = new JsonArray();
            for (String btnText : row) {
                JsonObject btn = new JsonObject();
                btn.addProperty("text", btnText);
                rowArr.add(btn);
            }
            keyboard.add(rowArr);
        }
        return keyboard;
    }

    public static void sendPhoto(String imgPath, String caption) {
        try {
            File file = new File(imgPath);
            if (!file.exists()) return;

            String url = getURL() + "/sendPhoto?chat_id=" + chatId + "&caption=" + URLEncoder.encode(caption, "UTF-8");
            HttpUtil.RequestBuilder builder = new HttpUtil.RequestBuilder()
                    .setUrl(url)
                    .setMethod("POST")
                    .setConsumer(conn -> {
                        try {
                            String boundary = "---" + System.currentTimeMillis();
                            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                            conn.setDoOutput(true);
                            try (OutputStream out = conn.getOutputStream();
                                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"), true);
                                 FileInputStream fis = new FileInputStream(file)) {
                                writer.println("--" + boundary);
                                writer.println("Content-Disposition: form-data; name=\"photo\"; filename=\"" + file.getName() + "\"");
                                writer.println("Content-Type: image/jpeg");
                                writer.println();
                                writer.flush();
                                byte[] buffer = new byte[4096];
                                int read;
                                while ((read = fis.read(buffer)) != -1) out.write(buffer, 0, read);
                                out.flush();
                                writer.println();
                                writer.println("--" + boundary + "--");
                            }
                        } catch (Exception e) { e.printStackTrace(); }
                    })
                    .setBody("");
            builder.build().execute();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static JsonArray pollMessage() {
        try {
            String url = getURL() + "/getUpdates?offset=" + lastOffset + "&timeout=1";
            String response = new HttpUtil.RequestBuilder().setUrl(url).setMethod("GET").build().execute();
            JsonObject obj = new JsonParser().parse(response).getAsJsonObject();
            if (obj.has("ok") && obj.get("ok").getAsBoolean()) {
                return obj.getAsJsonArray("result");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JsonArray();
    }
}