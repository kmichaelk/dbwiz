package ru.mkr.dbwiz.utilities;

import java.io.File;

public class NativeUtility {

    public static final String LIBRARIES_DIRECTORY = "";//""libs";

    public static void loadLibrary(String libraryName) {
        libraryName = "lib" + System.mapLibraryName(libraryName + "_" + ARCHITECTURE);
        String fileName = (LIBRARIES_DIRECTORY.isEmpty() ? "" : LIBRARIES_DIRECTORY + File.separator) + libraryName;
        File file = Utility.extractResource(fileName, "libs/" + libraryName);
        try {
            System.load(file.getAbsolutePath());
        } catch (UnsatisfiedLinkError ex) {
            throw new RuntimeException("Ошибка при загрузке библиотеки '" + libraryName + "': " + ex.getMessage(), ex);
        }
    }

    public static final String ARCHITECTURE = System.getProperty("os.arch").contains("64") ? "x64" : "x86";
    static {
        File libs = new File("libs");
        if(!libs.exists() && libs.mkdir()) {
            System.setProperty("java.library.path", libs.getAbsolutePath());
        }
    }
}