package ru.nedan.spookybuy.util;

import lombok.experimental.UtilityClass;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import ru.nedan.neverapi.NeverAPI;
import ru.nedan.neverapi.command.Argument;
import ru.nedan.neverapi.command.Command;
import ru.nedan.neverapi.etc.ChatUtility;
import ru.nedan.neverapi.etc.TextBuilder;
import ru.nedan.spookybuy.SpookyBuy;
import ru.nedan.spookybuy.util.script.Script;
import ru.nedan.spookybuy.util.script.ScriptStorage;
import ru.nedan.spookybuy.util.telegram.TelegramAPI;
import ru.nedan.spookybuy.util.ws.Client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@UtilityClass
public class CommandRegister {

    public static void registerAll() {
        TextBuilder textBuilder = SpookyBuy.getSpookyBuyAppender();

        NeverAPI.getApi().getCommandManager().registerCommand(new Command("irc", ".irc [message]",
                new Argument("message", s -> s.equalsIgnoreCase(".irc "))) {
            @Override
            public void execute(String... strings) {
                if (strings.length < 1) return;
                if (strings.length < 2) {
                    Client.getInstance().sendIRCResponse("&a&n&lЯ сын хуйни!!!!");
                    return;
                }

                String msg = String.join(" ", Arrays.copyOfRange(strings, 1, strings.length));
                Client.getInstance().sendIRCResponse(msg);
            }
        });

        NeverAPI.getApi().getCommandManager().registerCommand(new Command("server", ".server disconnect",
                new Argument("disconnect", s -> s.equalsIgnoreCase(".server "))) {
            @Override
            public void execute(String... strings) {
                if (strings.length >= 2 && strings[1].equalsIgnoreCase("disconnect")) {
                    Client.getInstance().disconnect();
                }
            }
        });

        NeverAPI.getApi().getCommandManager().registerCommand(new Command("cloud", ".cloud [save/load] [key]",
                new Argument("save", s -> s.equalsIgnoreCase(".cloud ")),
                new Argument("load", s -> s.equalsIgnoreCase(".cloud ")),
                new Argument("key", s -> s.equalsIgnoreCase(".cloud load "))) {
            @Override
            public void execute(String... strings) {
                String action = strings[1];

                switch (action.toLowerCase()) {
                    case "save": {
                        Client.getInstance().sendConfigSaveResponse();
                        break;
                    }
                    case "load": {
                        String key = strings[2];
                        Client.getInstance().sendLoadConfigResponse(key);
                    }
                    default:
                        break;
                }
            }
        });

        NeverAPI.getApi().getCommandManager().registerCommand(new Command("telegram", ".telegram [test/setToken/setChatId] [message/token/chatId]",
                new Argument("test", string -> string.equalsIgnoreCase(".telegram ")),
                new Argument("tutorial", string -> string.equalsIgnoreCase(".telegram ")),
                new Argument("setToken", string -> string.equalsIgnoreCase(".telegram ")),
                new Argument("setChatId", string -> string.equalsIgnoreCase(".telegram ")),
                new Argument("message", string -> string.equalsIgnoreCase(".telegram test ")),
                new Argument("token", string -> string.equalsIgnoreCase(".telegram setToken ")),
                new Argument("chatId", string -> string.equalsIgnoreCase(".telegram setChatId "))) {
            @Override
            public void execute(String... strings) {
                if (strings.length >= 3) {
                    String action = strings[1];
                    String message = String.join(" ", Arrays.copyOfRange(strings, 2, strings.length));

                    switch (action.toLowerCase()) {
                        case "test": {
                            Text text = textBuilder.copy()
                                    .append("Отправил сообщение \"")
                                    .append(message, Formatting.GREEN)
                                    .append("\" вам в телеграм.")
                                    .build();

                            TelegramAPI.sendMessage(message, null);
                            ChatUtility.sendMessage(textBuilder.copy().append(text).build());
                            break;
                        }
                        case "settoken": {
                            TelegramAPI.token = message;

                            Text text = textBuilder.copy()
                                    .append("Успешно установил токен \"")
                                    .append(message, Formatting.GREEN)
                                    .append("\"")
                                    .build();

                            ChatUtility.sendMessage(textBuilder.copy().append(text).build());
                            break;
                        }
                        case "setchatid": {
                            TelegramAPI.chatId = message;

                            Text text = textBuilder.copy()
                                    .append("Успешно установил чат айди \"")
                                    .append(message, Formatting.GREEN)
                                    .append("\"")
                                    .build();

                            ChatUtility.sendMessage(textBuilder.copy().append(text).build());
                            break;
                        }
                        default:
                            break;
                    }
                } else if (strings.length == 2) {
                    String action = strings[1];

                    if (action.equalsIgnoreCase("tutorial")) {
                        Util.getOperatingSystem().open("https://www.youtube.com/watch?v=TiPVtm5p-ng");
                    } else {
                        ChatUtility.sendMessage(textBuilder.copy().append(this.getUsage()).build());
                    }
                }
            }
        });

        NeverAPI.getApi().getCommandManager().registerCommand(new Command(
                "script",
                ".script [reload/run/stop] [name]",
                new Argument("reload", s -> s.equalsIgnoreCase(".script ")),
                new Argument("run", s -> s.equalsIgnoreCase(".script ")),
                new Argument("stop", s -> s.equalsIgnoreCase(".script "))
        ) {
            @Override
            public void execute(String... strings) {
                if (strings.length < 2) {
                    ChatUtility.sendMessage(textBuilder.copy().append(this.getUsage()).build());
                    return;
                }

                String action = strings[1];

                switch (action.toLowerCase()) {
                    case "reload" -> {
                        ScriptStorage.reloadScripts();
                        ChatUtility.sendMessage(textBuilder.copy().append("Скрипты успешно перезагружены!").build());
                    }
                    case "run" -> {
                        if (strings.length < 3) {
                            ChatUtility.sendMessage(textBuilder.copy().append(this.getUsage()).build());
                            return;
                        }

                        String argument = strings[2];
                        Script script = ScriptStorage.getScript(argument);

                        if (script == null) {
                            ChatUtility.sendMessage(textBuilder.copy().append("Скрипта \"" + argument + "\" не существует!").build());
                            return;
                        }

                        ScriptStorage.runScript(argument);
                    }
                    case "stop" -> {
                        if (strings.length < 3) {
                            ChatUtility.sendMessage(textBuilder.copy().append(this.getUsage()).build());
                            return;
                        }

                        String argument = strings[2];
                        Script script = ScriptStorage.getScript(argument);

                        if (script == null) {
                            ChatUtility.sendMessage(textBuilder.copy().append("Скрипта \"" + argument + "\" не существует!").build());
                            return;
                        }

                        if (!script.isRunning()) {
                            ChatUtility.sendMessage(textBuilder.copy().append("Скрипт \"" + argument + "\" не включен").build());
                        }

                        ScriptStorage.stopScript(argument);
                    }
                }
            }

            @Override
            public List<Argument> getDynamicArguments() {
                List<Argument> arguments = new ArrayList<>();

                for (Script script : ScriptStorage.getScripts()) {
                    arguments.add(new Argument(script.getName(), s -> s.equalsIgnoreCase(".script run ") || s.equalsIgnoreCase(".script stop ")));
                }

                return arguments;
            }
        });
    }

}
