package ru.nedan.spookybuy.util.telegram.command.impl;

import ru.nedan.spookybuy.util.Pair;
import ru.nedan.spookybuy.SpookyBuy;
import ru.nedan.spookybuy.event.EventTelegramMessage;
import ru.nedan.spookybuy.util.telegram.command.api.TelegramCommand;

public class NotifyCommand extends TelegramCommand {

    public NotifyCommand() {
        super("Уведомления", "уведомления [покупка/продажа] [выкл/вкл]", "Выключает/включает сообщения о продаже/покупке");
    }

    @Override
    public void execute(EventTelegramMessage event) {
        String[] strings = event.getText().split(" ");

        if (strings.length < 3) {
            event.reply("Вы неправильно написали команду! Правильный синтаксис: " + this.getUsage());
            return;
        }

        String what = strings[1].toLowerCase();
        Pair<Boolean, String> bool = parseBoolean(strings[2]);

        switch (what) {
            case "покупка": {
                SpookyBuy.getInstance().getAutoBuy().getAb().setSendBuy(bool.getLeft());
                event.reply("Уведомления о покупке успешно " + bool.getRight() + "!");
                break;
            }
            case "продажа": {
                SpookyBuy.getInstance().getAutoBuy().getAb().setSendSell(bool.getLeft());
                event.reply("Уведомления о продаже успешно " + bool.getRight() + "!");
                break;
            }
            default: break;
        }
    }

    Pair<Boolean, String> parseBoolean(String str) {
        boolean bool = str.toLowerCase().startsWith("вкл");

        return new Pair<>(bool, bool ? "включены" : "выключены");
    }
}
