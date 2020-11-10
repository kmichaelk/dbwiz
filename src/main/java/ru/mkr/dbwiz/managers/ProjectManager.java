package ru.mkr.dbwiz.managers;

import ru.mkr.dbwiz.Main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProjectManager {

    public static final Set<String> RUNNING_PROJECTS = new HashSet<>();

    public static List<String> findProjects() {
        List<String> projects = new ArrayList<>();

        File[] directoryListing = new File(Main.PROJECTS_DIRECTORY).listFiles();
        if (directoryListing != null) {
            for (File file : directoryListing) {
                String name = file.getName();
                if(name.endsWith(".js")) {
                    projects.add(name.substring(0, name.length()-3));
                }
            }
        }

        return projects;
    }

    public static String getProjectNameFromPath(String path) {
        return path.contains(File.separator) ? path.substring(path.indexOf(File.separator)+1) : path;
    }
}
