package ru.nedan.spookybuy.autobuy.functional;

import ru.nedan.neverapi.event.impl.EventMessage;

public interface ABMessageListener {
    void message(EventMessage message);
}
