package ru.nedan.spookybuy.event;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import ru.nedan.neverapi.event.api.Event;
import ru.nedan.spookybuy.util.telegram.TelegramAPI;

@Getter
public class EventTelegramMessage extends Event {
    private final JsonObject message;
    private final String text;

    public EventTelegramMessage(JsonObject message) {
        this.message = message;

        text = message.getAsJsonObject("message").get("text").getAsString();;
    }

    public void reply(String text) {
        JsonObject message = this.message.get("message").getAsJsonObject();
        TelegramAPI.reply(message, text, null);
    }

    public void reply(String text, JsonArray keyboard) {
        JsonObject message = this.message.get("message").getAsJsonObject();
        TelegramAPI.reply(message, text, keyboard);
    }
}
