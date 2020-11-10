package ru.mkr.dbwiz.utilities;

import ru.mkr.dbwiz.Main;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Utility {

    public static void safeOpenFile(File file) {
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException ex) {
            System.out.println("При открытии файла произошла ошибка: " + ex.getMessage());
            ex.printStackTrace();
            UIUtility.showMessage("Ошибка", "При открытии файла произошла ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static File extractResource(String res) {
        return extractResource(res, true);
    }

    public static File extractResource(String res, boolean override) {
        return extractResource(res, res, override);
    }

    public static File extractResource(String dst, String src) {
        return extractResource(dst, src, true);
    }

    public static File extractResource(String dst, String src, boolean override) {
        dst = dst.replace("/", File.separator);
        File file = new File(dst);
        if (file.exists() && !override) return file;

        src = "/" + src.replace(File.separator, "/");
        InputStream link = Main.class.getResourceAsStream(src);
        try {
            Files.copy(link, file.getAbsoluteFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            System.out.println("Ошибка при распаковке ресурса: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
        return file;
    }

    public static String loadStringResource(String path) throws IOException {
        try (InputStream resource = Main.class.getResourceAsStream(path)) {
            List<String> list = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.toList());
            return String.join("\n", list);
        }
    }

    public static String millisecondsToString(long timeMs) {
        long hours = TimeUnit.MILLISECONDS.toHours(timeMs);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeMs - TimeUnit.HOURS.toMillis(hours));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeMs - TimeUnit.HOURS.toMillis(hours) - TimeUnit.MINUTES.toMillis(minutes));
        long milliseconds = timeMs - TimeUnit.HOURS.toMillis(hours) - TimeUnit.MINUTES.toMillis(minutes) - TimeUnit.SECONDS.toMillis(seconds);

        return String.format("%02d:%02d:%02d.%d", hours, minutes, seconds, milliseconds);
    }
}
