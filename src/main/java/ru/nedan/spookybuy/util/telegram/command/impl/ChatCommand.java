package ru.nedan.spookybuy.util.telegram.command.impl;

import ru.nedan.spookybuy.event.EventTelegramMessage;
import ru.nedan.spookybuy.util.telegram.command.api.TelegramCommand;

public class ChatCommand extends TelegramCommand {

    public ChatCommand() {
        super("Чат", "чат [сообщение]", "Отправляет сообщение в чат майнкрафта");
    }

    @Override
    public void execute(EventTelegramMessage event) {
        String message = event.getMessage().getAsJsonObject("message").get("text").getAsString();
        mc.player.sendChatMessage(message.substring(4));
    }
}
