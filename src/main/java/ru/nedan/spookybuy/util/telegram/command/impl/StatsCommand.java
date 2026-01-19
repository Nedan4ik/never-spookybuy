package ru.nedan.spookybuy.util.telegram.command.impl;

import ru.nedan.spookybuy.SpookyBuy;
import ru.nedan.spookybuy.event.EventTelegramMessage;
import ru.nedan.spookybuy.util.telegram.command.api.TelegramCommand;

import java.math.BigDecimal;

public class StatsCommand extends TelegramCommand {

    public StatsCommand() {
        super("Статистика", "статистика [общ/продаж/покупок]", "Выводит статистику в чат телеграм");
    }

    @Override
    public void execute(EventTelegramMessage event) {
        String argument = event.getText().split(" ")[1];

        BigDecimal buy = SpookyBuy.getInstance().getAutoBuy().getAb().getBuy(), sell = SpookyBuy.getInstance().getAutoBuy().getAb().getSell();

        switch (argument.toLowerCase()) {
            case "общ": {
                String message = "Общая статистика:\n" +
                        "Покупки: " + buy.toPlainString() + " монет\n" +
                        "Продажи: " + sell.toPlainString() + " монет\n" +
                        "Прибыль: " + sell.subtract(buy).toPlainString() + " монет\n";

                event.reply(message);
                break;
            }
            case "продаж": {
                event.reply("Продажи: " + sell.toPlainString() + " монет");
                break;
            }
            case "покупок": {
                event.reply("Покупки: " + buy.toPlainString() + " монет");
                break;
            }
            default:
                break;
        }
    }
}
