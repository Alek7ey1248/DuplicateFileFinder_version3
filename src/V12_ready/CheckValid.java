package V12_ready;

import java.io.File;

public class CheckValid {

    public boolean getValidDirectoryPaths(String[] paths) {
        if (paths.length == 0) {
            System.err.println("Укажите пути для поиска дубликатов.");
            return false;
        }
        for (String path : paths) {
            if (!isValidDirectoryPath(path)) {
                return false;
            }
        }
        return true;
    }


    public boolean isValidDirectoryPath(String path) {

        if (path == null || path.isEmpty()) {
            System.err.println("     '"+ path + "'   - Путь к директории не указан");
            return false;
        }
        File directory = new File(path);
        if (!directory.exists()) {
            System.err.println("Directory - '" + directory.getAbsolutePath() + "'  -  не существует");
            return false;
        }
        if (!directory.isDirectory()) {
            System.err.println("Directory '" + directory.getAbsolutePath() + "'   -  это не каталог");
            return false;
        }
        if (directory.listFiles() == null) {
            System.err.println("Directory '" + directory.getAbsolutePath() + "'   -  недоступен для чтения");
            return false;
        }
        return true;
    }


    // проверка файла
    public boolean isValidFile(File file) {

        if (!file.exists()) {
            System.err.println(" File " + file.getAbsolutePath() + " не существует");
            return false;
        }

        if (!file.isFile()) {
            System.err.println(" File " + file.getAbsolutePath() + " это не файл");
            return false;
        }

        if (!file.canRead()) {
            System.err.println(" File " + file.getAbsolutePath() + " поврежден или нет прав на чтение");
            return false;
        }

        return true;
    }

}
