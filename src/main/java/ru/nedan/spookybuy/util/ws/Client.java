package ru.nedan.spookybuy.util.ws;

import com.google.gson.JsonObject;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import ru.nedan.neverapi.async.AsyncRunManager;
import ru.nedan.neverapi.etc.ChatUtility;
import ru.nedan.spookybuy.Authentication;
import ru.nedan.spookybuy.SpookyBuy;
import ru.nedan.spookybuy.event.EventServerResponse;
import ru.nedan.spookybuy.util.Discord;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Client {
    @Getter
    private static Client instance;

    private Channel channel;
    private EventLoopGroup group;
    private final ScheduledExecutorService checker = Executors.newSingleThreadScheduledExecutor();
    private final String url;

    public Client(String url) {
        instance = this;
        this.url = url;
    }

    public void startClient() {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            int port = uri.getPort();

            group = new NioEventLoopGroup();
            final Handler handler =
                    new Handler(
                            WebSocketClientHandshakerFactory.newHandshaker(
                                    uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders()
                            )
                    );

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpClientCodec());
                            pipeline.addLast(new HttpObjectAggregator(8192));
                            pipeline.addLast(handler);
                        }
                    });

            channel = bootstrap.connect(host, port).sync().channel();
            handler.handshakeFuture().sync();

            Authentication.auth();

            if (!stated)
                startChecker();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public boolean isConnected() {
        return channel != null && channel.isOpen() && channel.isActive();
    }

    public void sendIRCResponse(String msg) {
        JsonObject json = new JsonObject();
        json.addProperty("action", "irc");
        json.addProperty("hwid", Authentication.getHWID());
        json.addProperty("data", msg);

        sendMessage(json.toString());
    }

    public void sendConfigSaveResponse() {
        JsonObject json = new JsonObject();
        String data = SpookyBuy.saveConfig().toString();

        json.addProperty("action", "saveConfig");
        json.addProperty("hwid", Authentication.getHWID());
        json.addProperty("data", data);

        sendMessage(json.toString());
    }

    public void sendLoadConfigResponse(String key) {
        JsonObject object = new JsonObject();

        object.addProperty("action", "readConfig");
        object.addProperty("hwid", Authentication.getHWID());
        object.addProperty("key", key);

        sendMessage(object.toString());
    }

    public void sendMessage(String msg) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(new TextWebSocketFrame(msg));
        }
    }

    public void sendAuthResponse(String hardwareId) {
        JsonObject object = new JsonObject();

        object.addProperty("action", "auth");
        object.addProperty("hwid", hardwareId);
        object.addProperty("version", Discord.getModVersion());

        AsyncRunManager.once(EventServerResponse.class, e -> {
            e.setUsed(true);
            String username = e.asJson().get("message").getAsString();
            Authentication.setUsername(username);

            if (username.equalsIgnoreCase("unknown")) {
                while (MinecraftClient.getInstance().keyboard == null) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                MinecraftClient.getInstance().keyboard.setClipboard(hardwareId);
            }
        }, e -> e.asJson().has("type") && e.asJson().get("type").getAsString().equalsIgnoreCase("authResult"));

        sendMessage(object.toString());
    }

    boolean stated = false;

    public void sendPing() {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(new PingWebSocketFrame());
        }
    }

    public void disconnect() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close().sync();
            }

            if (group != null && !group.isShuttingDown()) {
                group.shutdownGracefully().sync();
            }

            checker.shutdownNow();

            stated = false;
            ChatUtility.sendMessage("Вы успешно отключились от сервера!");
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }


    private void startChecker() {
        stated = true;
        checker.scheduleAtFixedRate(() -> {
            if (!isConnected()) {
                instance.startClient();
            } else {
                sendPing();
            }
        }, 0, 3, TimeUnit.SECONDS);
    }
}
