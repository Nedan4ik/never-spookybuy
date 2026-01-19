package ru.nedan.spookybuy.util.ws;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import ru.nedan.neverapi.NeverAPI;
import ru.nedan.neverapi.etc.ChatUtility;
import ru.nedan.neverapi.etc.TextBuilder;
import ru.nedan.spookybuy.SpookyBuy;
import ru.nedan.spookybuy.event.EventServerResponse;
import ru.nedan.spookybuy.screen.configs.ConfigScreen;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Handler extends SimpleChannelInboundHandler<Object> {

    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;

    public Handler(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    private void applyMessage(TextWebSocketFrame text) {
        String msg = text.text();
        JsonObject object = (new JsonParser()).parse(msg).getAsJsonObject();
        EventServerResponse response = new EventServerResponse(msg);

        NeverAPI.getApi().getEventBus().post(response);

        if (response.isUsed()) return;

        if (object.has("type")) {
            String type = object.get("type").getAsString();
            String mes = object.get("message").getAsString();

            if (type.equalsIgnoreCase("irc")) {
                String input = mes;
                MutableText result = new LiteralText("");
                Style currentStyle = Style.EMPTY;
                int i = 0;

                while (i < input.length()) {
                    if (input.startsWith("§x", i) && i + 13 <= input.length()) {
                        StringBuilder hexBuilder = new StringBuilder();
                        try {
                            for (int j = 2; j < 13; j += 2) {
                                if (input.charAt(i + j) == '§') {
                                    hexBuilder.append(input.charAt(i + j + 1));
                                } else {
                                    break;
                                }
                            }

                            String hex = hexBuilder.toString();
                            int color = Integer.parseInt(hex, 16);
                            i += 14;

                            if (i < input.length()) {
                                char c = input.charAt(i);
                                result.append(new LiteralText(String.valueOf(c)).setStyle(currentStyle.withColor(TextColor.fromRgb(color))));
                                i++;
                            }
                        } catch (Exception e) {
                            result.append(new LiteralText("§x"));
                            i += 2;
                        }
                    } else if (input.charAt(i) == '§' && i + 1 < input.length()) {
                        char code = input.charAt(i + 1);
                        Formatting formatting = Formatting.byCode(code);
                        if (formatting != null) {
                            currentStyle = currentStyle.withFormatting(formatting);
                        }
                        i += 2;
                    } else {
                        result.append(new LiteralText(String.valueOf(input.charAt(i))).setStyle(currentStyle));
                        i++;
                    }
                }

                ChatUtility.sendMessage(result);
            } else if (type.equalsIgnoreCase("config")) {
                JsonElement configData = new JsonParser().parse(mes);
                SpookyBuy.loadConfig(configData);
            } else if (type.equalsIgnoreCase("saveConfig")) {
                ChatUtility.sendMessage(new TextBuilder()
                        .append("Ваш конфиг ")
                        .append(" успешно", Formatting.GREEN)
                        .append(" сохранён на ключ: ")
                        .append(mes, Formatting.GOLD)
                        .append(" (скопирован в буфер обмена)")
                        .build());

                MinecraftClient.getInstance().keyboard.setClipboard(mes);
            } else if (type.equalsIgnoreCase("getConfig")) {
                if (object.has("author")) {
                    String author = object.get("author").getAsString();
                    if (!ConfigScreen.autoInject(author)) {
                        return;
                    }
                }

                JsonElement element = (new JsonParser()).parse(mes);
                SpookyBuy.loadConfig(element);
                ChatUtility.sendMessage(new TextBuilder()
                        .append("Конфиг ")
                        .append("успешно", Formatting.GREEN)
                        .append(" загружен!")
                        .build());
            } else if (type.equalsIgnoreCase("whisper")) {
                Pattern pattern = Pattern.compile("Конфиг от автора (.+) обновлён, загружаю!");
                Matcher matcher = pattern.matcher(mes);

                if (matcher.find()) {
                    String authorName = matcher.group(1);
                    if (!ConfigScreen.autoInject(authorName)) return;
                }

                ChatUtility.sendMessage(mes);
            }
        } else if (object.has("status")) {
            String status = object.get("status").getAsString();
            String message = object.get("message").getAsString();

            if (status.equalsIgnoreCase("error")) {
                ChatUtility.sendMessage(new TextBuilder()
                        .append("Ошибка!", Formatting.RED)
                        .append(" " + message)
                        .build());
            }
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
        Channel ch = ctx.channel();

        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (FullHttpResponse) msg);
            handshakeFuture.setSuccess();

            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException("Unexpected FullHttpResponse: " + response);
        }

        WebSocketFrame frame = (WebSocketFrame) msg;

        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;

            applyMessage(textFrame);
        } else if (frame instanceof CloseWebSocketFrame) {
            ch.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }
}
