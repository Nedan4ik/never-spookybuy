package ru.nedan.spookybuy.util;

import net.fabricmc.loader.api.FabricLoader;
import ru.nedan.neverapi.etc.discord.DiscordEventHandlers;
import ru.nedan.neverapi.etc.discord.DiscordRPC;
import ru.nedan.neverapi.etc.discord.DiscordRichPresence;
import ru.nedan.spookybuy.SpookyBuy;
import ru.nedan.neverapi.etc.discord.helpers.RPCButton;


import java.math.BigDecimal;

public class Discord {
    public boolean running;
    private final DiscordRichPresence.Builder discordRichPresence = new DiscordRichPresence.Builder();



    public void run() {
        running = true;

        RPCButton button = RPCButton.create("Дискорд", "https://dsc.gg/nvrbuy");
        RPCButton button2 = RPCButton.create("Телеграм", "https://t.me/neverproduct");
        discordRichPresence
                .setStartTimestamp(System.currentTimeMillis() / 1000)
                .setSmallImage("https://iili.io/B1aw1V4.png", "dsc.gg/nvrbuy")
                .setLargeImage("https://iili.io/Bw6ToNf.jpg", "dsc.gg/nvrbuy")
                .setState("Version: " + getModVersion())
                .setButtons(button, button2);

        DiscordEventHandlers eventHandlers = new DiscordEventHandlers.Builder()
                .build();

        DiscordRPC.INSTANCE.Discord_Initialize("1376119257166778378", eventHandlers, true, null);

        new Thread(() -> {
            try {
                while (isRunning()) {
                    DiscordRPC.INSTANCE.Discord_RunCallbacks();

                    BigDecimal balance = SpookyBuy.getInstance().getAutoBuy().getBalance();
                    String balanceStr = balance.toPlainString();

                    discordRichPresence
                            .setDetails("Баланс: " + balanceStr);

                    DiscordRPC.INSTANCE.Discord_UpdatePresence(discordRichPresence.build());
                    Thread.sleep(15 * 1000);
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }).start();
    }

    public void stop() {
        DiscordRPC.INSTANCE.Discord_Shutdown();
        this.running = false;
    }

    private boolean isRunning() {
        return this.running;
    }

    public static String getModVersion() {
        return FabricLoader.getInstance()
                .getModContainer("spookybuy")
                .map((container) -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }

}
