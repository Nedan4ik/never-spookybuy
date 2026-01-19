package ru.nedan.spookybuy.util.script;

import lombok.Getter;

import javax.script.*;

public class Script extends CompiledScript {
    private final ScriptEngine engine;
    private final CompiledScript compiledScript;

    @Getter
    private boolean running;
    @Getter
    private final String name;

    public Script(ScriptEngine engine, String name, String scriptSource) throws ScriptException {
        this.engine = engine;
        if (this.engine == null) {
            throw new ScriptException("Nashorn engine not found. Make sure you added it as a dependency.");
        }

        if (!(engine instanceof Compilable)) {
            throw new ScriptException("Engine does not support compilation.");
        }

        this.compiledScript = ((Compilable) engine).compile(scriptSource);
        this.running = true;
        this.name = name;
    }

    public void stop() {
        ScriptStorage.stopScript(this.getName());
        this.running = false;
    }

    @Override
    public Object eval(ScriptContext context) throws ScriptException {
        if (!running) {
            throw new ScriptException("Script is not running.");
        }
        return compiledScript.eval(context);
    }

    @Override
    public ScriptEngine getEngine() {
        return engine;
    }
}
