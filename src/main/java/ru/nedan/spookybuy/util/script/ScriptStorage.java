package ru.nedan.spookybuy.util.script;

import lombok.Getter;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.Window;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.util.Supplier;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;
import ru.nedan.neverapi.event.api.Event;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ScriptStorage {
    @Getter
    private static final ScriptEngine engine = new ScriptEngineManager().getEngineByName("Nashorn");

    @Getter
    private static final List<Script> scripts = new ArrayList<>();
    private static final List<Object> listeners = new ArrayList<>();
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final Path scriptFolder = Paths.get("scripts/");
    private static final Map<String, Thread> runningThreads = new ConcurrentHashMap<>();

    static {
        engine.put("on", (BiConsumer<String, ScriptObjectMirror>) ScriptStorage::registerEvent);
        engine.put("minecraft", mc);
        engine.put("player", ((Supplier<ClientPlayerEntity>) () -> mc.player).get());
        engine.put("chat", (Consumer<String>) cons -> mc.player.sendChatMessage(cons));
        engine.put("print", (Consumer<Object>) System.out::println);
        engine.put("runScript", (BiConsumer<String, Boolean>) (string, isFile) -> {
            if (isFile) {
                runScript(string);
            } else {
                try {
                    Script script = new Script(engine, RandomStringUtils.randomAlphabetic(10), string);
                    Thread scriptThread = new Thread(() -> {
                        try {
                            script.eval();
                        } catch (ScriptException e) {
                            e.printStackTrace();
                        }
                    }, script.getName());

                    runningThreads.put(script.getName(), scriptThread);
                    scriptThread.start();
                } catch (ScriptException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        engine.put("window", (Supplier<Window>) mc::getWindow);
        engine.put("keyboard", ((Supplier<Keyboard>) () -> mc.keyboard).get());

        engine.put("repeat", (BiConsumer<ScriptObjectMirror, Integer>) (task, periodMillis) -> {
            Thread t = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    task.call(null);
                    try {
                        Thread.sleep(periodMillis);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            });
            t.start();
        });
    }

    public static Script getScript(String name) {
        return scripts.stream()
                .filter(script -> script.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    private static void registerEvent(String eventClassName, ScriptObjectMirror handler) {
        try {
            Class<?> clazz = Class.forName(eventClassName);
            Object listener = Event.addListener(clazz, (e) -> handler.call(null, e));
            
            listeners.add(listener);
        } catch (ClassNotFoundException e) {
            System.out.println("Событие " + eventClassName + " не найдено.");
        }
    }

    public static void reloadScripts() {
        scripts.forEach(Script::stop);
//        listeners.forEach(Event::removeListener);
        scripts.clear();

        File scriptsFolder = scriptFolder.toFile();

        if (!scriptsFolder.exists()) {
            scriptsFolder.mkdirs();
        }

        File onLoad = new File("./scripts/onLoad.js");

        if (!onLoad.exists()) {
            try {
                Files.writeString(onLoad.toPath(), "print(\"Привет с onLoad скрипта!\")");
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }

        for (File file : Objects.requireNonNull(scriptsFolder.listFiles((dir, name) -> name.endsWith(".js")))) {
            loadScript(file.getName(), file.toPath());
        }

        runScript("onLoad.js");
    }

    public static void runScript(String scriptName) {
        Script script = scripts.stream().filter(sc -> sc.getName().equalsIgnoreCase(scriptName)).findFirst().orElse(null);
        if (script != null) {
            stopScript(scriptName);

            Thread scriptThread = new Thread(() -> {
                try {
                    script.eval();
                } catch (ScriptException e) {
                    e.printStackTrace();
                }
            }, scriptName);

            runningThreads.put(scriptName, scriptThread);
            scriptThread.start();
        }
    }

    public static void stopScript(String scriptName) {
        Thread thread = runningThreads.remove(scriptName);
        if (thread != null) {
            thread.interrupt();
        }
    }

    public static void loadScript(String fileName, Path path) {
        try {
            String[] scriptContent = new String[]{new String(Files.readAllBytes(path))};

            Script compiledScript = new Script(engine, fileName, scriptContent[0]);
            scripts.add(compiledScript);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
