package ru.nedan.spookybuy.util.telegram.command.impl;

import ru.nedan.spookybuy.SpookyBuy;
import ru.nedan.spookybuy.event.EventTelegramMessage;
import ru.nedan.spookybuy.util.telegram.command.api.TelegramCommand;

import java.text.NumberFormat;
import java.util.Locale;

public class BalanceCommand extends TelegramCommand {

    public BalanceCommand() {
        super("баланс", "Баланс", "Отправляет ваш баланс в чат телеграм");
    }

    @Override
    public void execute(EventTelegramMessage event) {
        NumberFormat format = NumberFormat.getInstance(Locale.US);
        event.reply("Ваш баланс: " + format.format(SpookyBuy.getInstance().getAutoBuy().balance).replaceAll(",", "."));
    }
}
