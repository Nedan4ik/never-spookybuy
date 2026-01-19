package ru.nedan.spookybuy.util.telegram.command.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import ru.nedan.spookybuy.event.EventTelegramMessage;

@Getter
@AllArgsConstructor
public abstract class TelegramCommand {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    private final String name, usage, desc;

    public abstract void execute(EventTelegramMessage event);
}
