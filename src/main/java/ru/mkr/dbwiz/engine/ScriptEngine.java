package ru.mkr.dbwiz.engine;

import ru.mkr.dbwiz.modules.Module;

import java.util.HashSet;
import java.util.Set;

/*
 * Базовая реализация обертки скриптового движка
 *
 * Поддерживаемые движки:
 *  [+] Chromium V8   / J2V8        / (Windows x64)
 *  [ ] Mozilla Rhino / Встроенный / (Universal)    TODO
 *
 * Created on 11/10/20
 * (c) Mikhail K., 2020
 */
public abstract class ScriptEngine {

    protected final Set<Module> MODULES = new HashSet<>();
    protected String LIBRARIES_CODE = "";

    public abstract void initialize();
    public abstract boolean isRunning();

    public abstract void shutdown();
    public abstract void kill();

    public abstract Object getRuntime();

    public void bindFunction(String function, ScriptCallback callback) {
        bindFunction(function, callback, 0);
    }
    public abstract void bindFunction(String function, ScriptCallback callback, int minArgs);
    public abstract Object execute(String code);

    public void loadLibrary(String code) {
        LIBRARIES_CODE += code;
    }

    public void loadModule(Module module) {
        MODULES.add(module);
        module.inject(this);
    }
    protected void unloadModules() {
        for (Module module : MODULES) {
            module.unload();
        }
        MODULES.clear();
    }
}
