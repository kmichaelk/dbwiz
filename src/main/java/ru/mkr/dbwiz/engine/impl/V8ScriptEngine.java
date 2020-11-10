package ru.mkr.dbwiz.engine.impl;

import com.eclipsesource.v8.Releasable;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import ru.mkr.dbwiz.engine.ScriptEngine;
import ru.mkr.dbwiz.engine.ScriptCallback;
import ru.mkr.dbwiz.utilities.NativeUtility;

import java.io.File;

/*
 * Движок JavaScript
 * V8 из проекта Chromium
 *
 * Powered by J2V8
 * https://github.com/eclipsesource/J2V8
 * Eclipse Public License v1.0
 *
 * Created on 11/10/20
 * (c) Mikhail K., 2020
 */
public class V8ScriptEngine extends ScriptEngine {

    private static final String V8_SCRIPT_NAME = "line";
    private V8 runtime = null;

    @Override
    public void initialize() {
        runtime = V8.createV8Runtime(null,
                new File(".").getAbsolutePath() + File.separator + NativeUtility.LIBRARIES_DIRECTORY
        );
    }

    @Override
    public boolean isRunning() {
        return runtime != null && !runtime.isReleased();
    }

    @Override
    public void shutdown() {
        // при вызове из параллельного потока крашится вся JVM
        runtime.release();
        //runtime = null;
        unloadModules();
    }

    @Override
    public void kill() {
        runtime.terminateExecution();
    }

    @Override
    public V8 getRuntime() {
        return runtime;
    }

    @Override
    public void bindFunction(String function, ScriptCallback callback, int minArgs) {
        runtime.registerJavaMethod((v8Object, v8Array) -> {
            Object[] args = readV8Array(v8Array);
            if(args.length < minArgs) {
                throw new IllegalArgumentException("Недостаточно аргументов для вызова функции");
            }
            return callback.call(args);
        }, function);
    }

    @Override
    public Object execute(String code) {
        return runtime.executeScript(
                LIBRARIES_CODE + "\n" + code,
                V8_SCRIPT_NAME,
                -LIBRARIES_CODE.split("\r\n|\r|\n").length
        );
    }

    private Object[] readV8Array(V8Array v8Array) {
        Object[] arr = new Object[v8Array.length()];
        for(int i = 0; i < arr.length; i++) {
            Object arg = v8Array.get(i);
            if(arg instanceof V8Array) {
                arr[i] = readV8Array(((V8Array) arg));
            } else {
                arr[i] = arg;
            }
            if(arg instanceof Releasable) {
                ((Releasable) arg).release();
            }
        }
        return arr;
    }
}
