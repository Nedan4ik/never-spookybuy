package ru.nedan.spookybuy.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.nedan.neverapi.event.api.Event;
import ru.nedan.spookybuy.items.CollectItem;

@Getter
@AllArgsConstructor
public class EventPreSell extends Event {
    CollectItem collectItem;
}
