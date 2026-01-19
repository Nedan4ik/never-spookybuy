package ru.nedan.spookybuy.mixin;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotUtils;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.nedan.spookybuy.util.telegram.TelegramAPI;
import ru.nedan.spookybuy.util.telegram.command.api.TelegramCommandExecutor;

import java.io.File;
import java.util.function.Consumer;

@Mixin(ScreenshotUtils.class)
public abstract class MixinScreenshotUtils {

    @Shadow
    public static NativeImage takeScreenshot(int width, int height, Framebuffer framebuffer) {
        return null;
    }

    @Shadow
    private static File getScreenshotFilename(File directory) {
        return null;
    }

    @Shadow @Final private static Logger LOGGER;

    /**
     * @author nedan4ik
     * @reason для отправки в телеграм при необходимости
     */
    @Overwrite
    private static void saveScreenshotInner(File gameDirectory, @Nullable String fileName, int framebufferWidth, int framebufferHeight, Framebuffer framebuffer, Consumer<Text> messageReceiver) {
        NativeImage nativeImage = takeScreenshot(framebufferWidth, framebufferHeight, framebuffer);
        File file = new File(gameDirectory, "screenshots");
        boolean dir = file.mkdir();
        File file2;
        if (fileName == null) {
            file2 = getScreenshotFilename(file);
        } else {
            file2 = new File(file, fileName);
        }

        assert nativeImage != null;
        assert file2 != null;

        Util.getIoWorkerExecutor().execute(() -> {
            try {
                nativeImage.writeFile(file2);
                Text text = (new LiteralText(file2.getName())).formatted(Formatting.UNDERLINE).styled((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file2.getAbsolutePath())));

                if (TelegramCommandExecutor.ACTIVE_SCREENSHOT_COMMAND) {
                    new Thread(() -> {
                        try {
                            Thread.sleep(500);

                            TelegramAPI.sendPhoto(file2.getAbsolutePath(), "Скрин из игры:");

                            TelegramCommandExecutor.ACTIVE_SCREENSHOT_COMMAND = false;

                            Thread.sleep(500);
                            boolean del = file2.delete();
                        } catch (Exception e) {
                            e.printStackTrace(System.err);
                        }
                    }).start();
                }

                messageReceiver.accept(new TranslatableText("screenshot.success", new Object[]{text}));
            } catch (Exception exception) {
                LOGGER.warn("Couldn't save screenshot", exception);
                messageReceiver.accept(new TranslatableText("screenshot.failure", new Object[]{exception.getMessage()}));
            } finally {
                nativeImage.close();
            }

        });
    }
}
