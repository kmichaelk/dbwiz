package ru.mkr.dbwiz.utilities;

import java.io.*;
import java.util.function.Consumer;

public class HookedOutputStream extends FilterOutputStream {

    private final PrintStream source;
    private final Consumer<String> listener;

    public HookedOutputStream(OutputStream out, PrintStream baseOutput, Consumer<String> listener) {
        super(out);
        source = baseOutput;
        this.listener = listener;
    }

    @Override
    public void write(byte[] b) throws IOException {
        String s = new String(b);
        source.write(b);
        listener.accept(s);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        String s = new String(b, off, len);
        source.write(b, off, len);
        listener.accept(s);
    }

    public static PrintStream newPrintStream(Consumer<String> listener) {
        return new PrintStream(new HookedOutputStream(new ByteArrayOutputStream(), System.out, listener));
    }
}
