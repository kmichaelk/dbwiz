package ru.mkr.dbwiz.utilities;

import ru.mkr.dbwiz.exceptions.ModuleInitializationException;

public class ModuleUtility {

    public static ModuleInitializationException newScriptInitializationException() {
        return new ModuleInitializationException("Failed to load bundled script resource");
    }

    public static String loadBundledScript(String path) {
        try {
            return Utility.loadStringResource(path);
        } catch (Exception ex) {
            System.out.printf("Failed to load bundled script resource: '%s': %s\n", path, ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }
}
