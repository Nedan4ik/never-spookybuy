package ru.nedan.spookybuy.util.autojoin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import ru.nedan.neverapi.async.AsyncRunManager;
import ru.nedan.neverapi.event.impl.EventMessage;
import ru.nedan.neverapi.event.impl.EventWindowOpen;

public class AutoJoin {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public AutoJoin() {
        EventWindowOpen.addListener(EventWindowOpen.class, e -> {
            if (!AutoJoinConfiguration.enabled) return;

            if (e.getScreen() instanceof DisconnectedScreen) {
                AsyncRunManager.waitForClientTicks(AutoJoinConfiguration.longToTicks()).thenRun(() -> {
                    if (mc.currentScreen instanceof DownloadingTerrainScreen || mc.currentScreen instanceof ConnectScreen || mc.player != null) return;

                    mc.openScreen(new ConnectScreen(e.getScreen(), mc, "spookytime.net", 25565));
                });
            }
        });

        EventMessage.addListener(EventMessage.class, e -> {
            if (!AutoJoinConfiguration.enabled) return;
            if (e.isSend()) return;

            if (e.getMessage().equalsIgnoreCase("[✾] Войдите в игру ⇝ /login <Пароль>")) {
                AsyncRunManager.waitForTicks(20).thenRun(() -> mc.player.sendChatMessage("/l " + AutoJoinConfiguration.PASSWORD));
            } else if (e.getMessage().equalsIgnoreCase("[✾] Успешная авторизация! Приятной игры!")) {
                AsyncRunManager.waitForTicks(20).thenRun(() -> mc.player.sendChatMessage("/an" + AutoJoinConfiguration.ANARCHY_TO_JOIN));
            }
        });


    }
}
