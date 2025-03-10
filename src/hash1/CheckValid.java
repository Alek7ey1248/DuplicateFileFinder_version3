package hash1;

import java.io.File;
import java.util.*;

public class CheckValid {

    public List<String> getValidDirectoryPaths(String[] paths) {
        List<String> validPaths = new ArrayList<>();
        for (String path : paths) {
            if (isValidDirectoryPath(path)) {
                validPaths.add(new File(path).getAbsolutePath());
            }
        }
        if (validPaths.isEmpty()) {
            System.err.println("There are no correct arguments");
        }
        return validPaths;
    }


    public boolean isValidDirectoryPath(String path) {
        File file = new File(path);
        if (!file.exists()) {
            System.err.println("path " + path + " path does not exist");
            return false;
        }
        if (!file.isDirectory()) {
            System.err.println("path " + path + " is not a directory");
            return false;
        }
        return true;
    }


    // проверка файла
    public boolean isValidFile(File file) {

        if (!file.canRead()) {
            System.err.println("method isValidFile.     File " + file.getAbsolutePath() + " damaged.");
            return false;
        }
        if (!file.isFile()) {
            System.err.println("method isValidFile.    File " + file.getAbsolutePath() + " is not a file");
            return false;
        }
        return true;
    }

}
