package ru.nedan.spookybuy.util.telegram.command.api;

import lombok.Getter;
import ru.nedan.neverapi.event.api.Event;
import ru.nedan.spookybuy.event.EventTelegramMessage;
import ru.nedan.spookybuy.util.telegram.command.impl.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class TelegramCommandExecutor {
    public static boolean ACTIVE_SCREENSHOT_COMMAND = false;
    private final List<TelegramCommand> commands = new ArrayList<>();

    public TelegramCommandExecutor() {
        Event.addListener(EventTelegramMessage.class, event -> {
            String text = event.getMessage().getAsJsonObject("message").get("text").getAsString();
            String commandName = text.split(" ")[0];
            for (TelegramCommand command : commands) {
                if (command.getName().equalsIgnoreCase(commandName)) {
                    command.execute(event);
                    break;
                }
            }
        });

        commands.addAll(Arrays.asList(
                new BalanceCommand(),
                new HelpCommand(),
                new ChatCommand(),
                new ScreenCommand(),
                new StatsCommand(),
                new NotifyCommand()
        ));
    }
}
