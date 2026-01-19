package ru.nedan.spookybuy.util.telegram.command.impl;

import net.minecraft.client.util.ScreenshotUtils;
import ru.nedan.neverapi.etc.ChatUtility;
import ru.nedan.spookybuy.event.EventTelegramMessage;
import ru.nedan.spookybuy.util.telegram.command.api.TelegramCommand;
import ru.nedan.spookybuy.util.telegram.command.api.TelegramCommandExecutor;

public class ScreenCommand extends TelegramCommand {

    public ScreenCommand() {
        super("Скрин", "скрин", "Скринит игру и отправляет его в чат телеграм");
    }

    @Override
    public void execute(EventTelegramMessage e) {
        TelegramCommandExecutor.ACTIVE_SCREENSHOT_COMMAND = true;
        ScreenshotUtils.saveScreenshot(mc.runDirectory, mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight(), mc.getFramebuffer(), (p_lambda$onKeyEvent$3_1_) -> ChatUtility.sendMessage("Успешно заскринил!"));
    }

}
