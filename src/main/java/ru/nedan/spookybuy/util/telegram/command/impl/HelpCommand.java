package ru.nedan.spookybuy.util.telegram.command.impl;

import ru.nedan.spookybuy.SpookyBuy;
import ru.nedan.spookybuy.event.EventTelegramMessage;
import ru.nedan.spookybuy.util.telegram.command.api.TelegramCommand;

import java.util.List;

public class HelpCommand extends TelegramCommand {

    public HelpCommand() {
        super("Помощь", "помощь", "Отправляет этот список команд");
    }

    @Override
    public void execute(EventTelegramMessage event) {
        StringBuilder message = new StringBuilder("Список всех команд\n");
        message.append("Название: использование - описание\n");

        List<TelegramCommand> commands = SpookyBuy.getInstance().getCommandExecutor().getCommands();

        for (TelegramCommand command : commands) {
            message.append(commands.indexOf(command) + 1).append(". ").append(command.getName()).append(": ").append(command.getUsage()).append(" - ").append(command.getDesc()).append("\n");
        }

        event.reply(message.toString());
    }
}
