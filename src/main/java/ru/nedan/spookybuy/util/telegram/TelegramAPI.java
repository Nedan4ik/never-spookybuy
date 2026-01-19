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
    public static String token = "7883995093:AAH7tpclGxTkiN_GV710TTuNP11B8Pf9E2U";
    public static String chatId  = /*""*//*"5712188508"*/"5100999758";
    private static int lastOffset = 0;
    public static final JsonArray DEFAULT_KEYBOARD = buildInlineKeyboard(new String[][]{
            {"Баланс", "Помощь"},
            {"Скрин"}
    });

    public static void start() {
        executor.scheduleWithFixedDelay(() -> {
            try {
                JsonArray array = pollMessage();

                for (JsonElement object : array) {
                    JsonObject message = object.getAsJsonObject().get("message").getAsJsonObject();
                    if (message.get("is_bot").getAsBoolean()) continue;

                    lastOffset = object.getAsJsonObject().get("update_id").getAsInt() + 1;

                    EventTelegramMessage event = new EventTelegramMessage(object.getAsJsonObject());

                    if (message.get("text").getAsString().equalsIgnoreCase("/start")) {
                        event.reply("Привет " + message.getAsJsonObject().get("from").getAsJsonObject().get("first_name").getAsString() + "! Напиши \"помощь\" чтобы узнать список комманд!", DEFAULT_KEYBOARD);
                        continue;
                    }

                    NeverAPI.getApi().getEventBus().post(event);
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }, 0, 3500, TimeUnit.MILLISECONDS);
    }

    public static void stop() {
        executor.shutdown();
    }

    public static String getURL() {
        return "api.telegram.org/bot" + token;
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
        if (el instanceof JsonNull) return;

        JsonObject main = el.getAsJsonObject();

        if (main.has("telegram") && main.get("telegram").isJsonObject()) {
            JsonObject telegramObject = main.getAsJsonObject("telegram");

            if (telegramObject.has("token")) {
                token = telegramObject.get("token").getAsString();
            }

            if (telegramObject.has("chatId")) {
                chatId = telegramObject.get("chatId").getAsString();
            }

            if (telegramObject.has("sendBuy")) {
                SpookyBuy.getInstance().getAutoBuy().getAb().setSendBuy(telegramObject.get("sendBuy").getAsBoolean());
            }

            if (telegramObject.has("sendSell")) {
                SpookyBuy.getInstance().getAutoBuy().getAb().setSendBuy(telegramObject.get("sendSell").getAsBoolean());
            }
        }
    }

    public static void sendMessage(String message, String chatId, JsonArray keyboard) {
        CompletableFuture.runAsync(() -> {
            try {
                String encodedMessage = URLEncoder.encode(message, "UTF-8");
                String encodedChatId = URLEncoder.encode(chatId, "UTF-8");

                StringBuilder urlBuilder = new StringBuilder(getURL() + "/sendMessage?chat_id=" + encodedChatId + "&text=" + encodedMessage);

                if (keyboard != null && keyboard.size() > 0) {
                    String keyboardJson = keyboard.toString();
                    urlBuilder.append("&reply_markup={\"keyboard\":").append(keyboardJson).append(", \"resize_keyboard\": true, \"one_time_keyboard\": false}");
                }

                HttpUtil.RequestBuilder builder = new HttpUtil.RequestBuilder()
                        .setBody("")
                        .setMethod("POST")
                        .setUrl(urlBuilder.toString());

                String obj = builder.build().execute();
                JsonObject object = new JsonParser().parse(obj).getAsJsonObject();

                if (!object.get("ok").getAsBoolean()) {
                    MutableText mutableText = new LiteralText("")
                            .append(new LiteralText("Ошибка при отправке сообщение в телеграм!").formatted(Formatting.RED))
                            .append(" ")
                            .append(new LiteralText(object.get("description").getAsString()).formatted(Formatting.YELLOW));

                    ChatUtility.sendMessage(mutableText);
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        });
    }

    public static void reply(JsonObject message, String text, JsonArray keyboard) {
        try {
            String chatId = message.get("from").getAsJsonObject().get("id").getAsString();
            String messageId = message.get("message_id").getAsString();

            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);

            StringBuilder urlBuilder = new StringBuilder("https://api.telegram.org/bot" + token + "/sendMessage?chat_id=" + chatId + "&text=" + encodedText + "&reply_to_message_id=" + messageId);

            if (keyboard != null && keyboard.size() > 0) {
                String keyboardJson = keyboard.toString();
                urlBuilder.append("&reply_markup={\"keyboard\":").append(keyboardJson).append(", \"resize_keyboard\": true, \"one_time_keyboard\": false}");
            }

            HttpUtil.RequestBuilder requestBuilder = new HttpUtil.RequestBuilder()
                    .setUrl(urlBuilder.toString())
                    .setMethod("POST")
                    .setBody("");

            requestBuilder.build().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JsonArray buildInlineKeyboard(String[][] buttons) {
        JsonArray keyboard = new JsonArray();

        for (String[] row : buttons) {
            JsonArray buttonRow = new JsonArray();
            for (String buttonText : row) {
                JsonObject button = new JsonObject();
                button.addProperty("text", buttonText);
                button.addProperty("callback_data", buttonText);
                buttonRow.add(button);
            }
            keyboard.add(buttonRow);
        }

        return keyboard;
    }

    public static void sendPhoto(String imgPath, String caption) {
        try {
            String encodedCaption = URLEncoder.encode(caption, StandardCharsets.UTF_8);

            System.out.println("Sending photo: " + imgPath + " Caption: " + caption);

            HttpUtil.RequestBuilder requestBuilder = new HttpUtil.RequestBuilder()
                    .setUrl(getURL() + "/sendPhoto?chat_id=" + chatId + "&caption=" + encodedCaption)
                    .setMethod("POST")
                    .setConsumer(connection -> {
                        try {
                            File imgFile = new File(imgPath);
                            String boundary = Long.toHexString(System.currentTimeMillis());
                            String CRLF = "\r\n";
                            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                            connection.setDoOutput(true);

                            try (
                                    OutputStream output = new BufferedOutputStream(connection.getOutputStream());
                                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8), true);
                                    FileInputStream inputStream = new FileInputStream(imgFile)
                            ) {
                                writer.append("--").append(boundary).append(CRLF);
                                writer.append("Content-Disposition: form-data; name=\"photo\"; filename=\"").append(imgFile.getName()).append("\"").append(CRLF);
                                writer.append("Content-Type: image/jpeg").append(CRLF);
                                writer.append(CRLF).flush();

                                byte[] buffer = new byte[8192];
                                int bytesRead;
                                while ((bytesRead = inputStream.read(buffer)) != -1) {
                                    output.write(buffer, 0, bytesRead);
                                }
                                output.flush();

                                writer.append(CRLF).flush();
                                writer.append("--").append(boundary).append("--").append(CRLF).flush();
                            }

                        } catch (Exception e) {
                            e.printStackTrace(System.err);
                        }
                    })
                    .setBody("");

            String obj = requestBuilder.build().execute();
            JsonObject object = new JsonParser().parse(obj).getAsJsonObject();

            if (!object.get("ok").getAsBoolean()) {
                MutableText mutableText = new LiteralText("")
                        .append(new LiteralText("Ошибка при отправке сообщение в телеграм!").formatted(Formatting.RED))
                        .append(" ")
                        .append(new LiteralText(object.get("description").getAsString()).formatted(Formatting.YELLOW));

                System.out.println(mutableText.getString());
                ChatUtility.sendMessage(mutableText);
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    private static JsonArray pollMessage() {
        try {
            HttpUtil.RequestBuilder requestBuilder = new HttpUtil.RequestBuilder()
                    .setUrl(getURL() + "/getUpdates")
                    .setMethod("POST")
                    .setBody(String.format("{\"offset\": %s, \"timeout\": 1, \"allowed_updates\": [\"message\"]}", lastOffset));

            JsonObject object = new JsonParser().parse(requestBuilder.build().execute()).getAsJsonObject();

            JsonArray filtered = new JsonArray();
            JsonArray array = object.getAsJsonArray("result");

            if (array == null) return filtered;

            for (JsonElement element : array) {
                JsonObject jsonObject = element.getAsJsonObject();
                JsonObject message = jsonObject.get("message").getAsJsonObject();

                if (message.getAsJsonObject().get("from").getAsJsonObject().get("id").getAsString().equalsIgnoreCase(chatId) && !message.get("from").getAsJsonObject().get("is_bot").getAsBoolean()) {
                    filtered.add(jsonObject);
                }
            }

            return filtered;
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

        return new JsonArray();
    }

    public static void main(String[] args) {
        start();
    }

}
