package v2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CheckValid2 {

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
        File directory = new File(path);
        if (!directory.exists()) {
            System.err.println("Directory " + directory.getAbsolutePath() + " не существует");
            return false;
        }
        if (!directory.isDirectory()) {
            System.err.println("Directory " + directory.getAbsolutePath() + " это не каталог");
            return false;
        }
        if (directory.listFiles() == null) {
            System.err.println("Directory " + directory.getAbsolutePath() + " недоступна для чтения");
            return false;
        }
        return true;
    }


    // проверка файла
    public boolean isValidFile(File file) {

        if (!file.exists()) {
            System.err.println("method isValidFile.    File " + file.getAbsolutePath() + " не существует");
            return false;
        }

        if (!file.isFile()) {
            System.err.println("method isValidFile.    File " + file.getAbsolutePath() + " это не файл");
            return false;
        }

        if (!file.canRead()) {
            System.err.println("method isValidFile.     File " + file.getAbsolutePath() + " поврежден или нет прав на чтение");
            return false;
        }

        return true;
    }

}
