package ru.nedan.spookybuy.autobuy.functional;

import ru.nedan.neverapi.event.impl.EventPlayerTick;

public interface ABTicker {
    void tick(EventPlayerTick e);
}
