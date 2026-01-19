package ru.nedan.spookybuy.event;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.nedan.neverapi.event.api.Event;

@EqualsAndHashCode(callSuper = true)
@Getter
@RequiredArgsConstructor
@Data
public class EventServerResponse extends Event {
    private final String message;
    private boolean used;

    public JsonObject asJson() {
        return new JsonParser().parse(message).getAsJsonObject();
    }
}
