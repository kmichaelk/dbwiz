package ru.mkr.dbwiz.exceptions;

public class NativeLibraryException extends Exception {

    public NativeLibraryException(String message) {
        super("Ошибка при выполнении нативной библиотеки: " + message);
    }
}
