package ru.mkr.dbwiz.engine;

import com.eclipsesource.v8.*;
import com.eclipsesource.v8.utils.V8ObjectUtils;
import ru.mkr.dbwiz.engine.impl.V8ScriptEngine;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScriptEngineCompat {

    public static Object convertMapToRuntimeFormat(ScriptEngine engine, Map<String, ?> map) {
        if(engine instanceof V8ScriptEngine) {
            V8 v8 = ((V8ScriptEngine) engine).getRuntime();
            V8Object result = V8ObjectUtils.toV8Object(v8, map);
            try {
                return result;
            } finally {
                // TODO: Check this
                //if(!result.isReleased()) {
                //    result.release();
                //}
            }
        } else {
            throw newUnsupportedEngineException();
        }
    }

    public static Object convertListToRuntimeFormat(ScriptEngine engine, List<?> list) {
        if(engine instanceof V8ScriptEngine) {
            V8 v8 = ((V8ScriptEngine) engine).getRuntime();
            V8Array result = V8ObjectUtils.toV8Array(v8, list);
            try {
                return result;
            } finally {
                // TODO: Check this
                //if(!result.isReleased()) {
                //    result.release();
                //}
            }
        } else {
            throw newUnsupportedEngineException();
        }
    }

    public static Object instantiateObject(ScriptEngine engine, String className, Object... args) {
        if(engine instanceof V8ScriptEngine) {
            V8 v8 = ((V8ScriptEngine) engine).getRuntime();
            String argsString = Stream.of(args).map(Object::toString).collect(Collectors.joining(", "));
            return v8.executeObjectScript(String.format("new %s(%s)", className, argsString));
        } else {
            throw newUnsupportedEngineException();
        }
    }

    public static Object callFunction(ScriptEngine engine, String name, Object... args) {
        if(engine instanceof V8ScriptEngine) {
            V8 v8 = ((V8ScriptEngine) engine).getRuntime();
            V8Array v8Array = V8ObjectUtils.toV8Array(v8, Arrays.asList(args));
            Object result = v8.executeObjectFunction(name, v8Array);
            v8Array.release();
            return result;
        } else {
            throw newUnsupportedEngineException();
        }
    }

    public static boolean isEngineException(Exception ex) {
        return ex instanceof V8ScriptExecutionException || ex instanceof V8ScriptCompilationException;
    }

    private static UnsupportedOperationException newUnsupportedEngineException() {
        return new UnsupportedOperationException("Only V8 engine is supported at the moment");
    }
}
