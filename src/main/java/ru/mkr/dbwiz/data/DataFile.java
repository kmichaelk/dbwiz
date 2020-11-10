package ru.mkr.dbwiz.data;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DataFile {

    private final String fileName;
    protected List<String> lines;

    public DataFile(String fileName) {
        this.fileName = fileName;
    }

    public void load() throws IOException {
        lines = new ArrayList<>();

        File file = getFile();
        if (!file.exists()) {
            write(file, new ArrayList<>());
            return;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
    }

    public void write(String line) throws IOException {
        write(getFile(), Collections.singletonList(line));
    }

    public void write(Collection<String> lines) throws IOException {
        write(getFile(), lines);
    }

    public void write(File file, Collection<String> lines) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String line : lines) {
                writer.write(line);
                writer.write("\r\n");
            }
            writer.flush();
        }
    }

    public String getFileExtension() {
        return "txt";
    }

    public List<String> getLines() {
        return lines;
    }

    public String inline(String delimiter) {
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            builder.append(line);
            if(delimiter != null) builder.append(delimiter);
        }
        return builder.toString();
    }

    public String inline() {
        return inline("\n");
    }

    public File getFile() {
        return new File(getFileName() + "." + getFileExtension());
    }

    public String getFileName() {
        return fileName;
    }
}