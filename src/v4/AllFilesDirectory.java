package v4;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AllFilesDirectory {

    // Метод для поиска всех файлов в директориях
    public List<File> findAllFilesInDirectories(List<String> paths){

        Set<File> fileSet = new HashSet<>();

        for (String path : paths) {
            File dir = new File(path);
            fileSet.addAll(findFilesInDirectory(dir));
        }
        System.out.println(" number of all files in directories - " + fileSet.size());

        return new ArrayList<>(fileSet);
    }

    // Метод для поиска всех файлов в директории - выдает Set<File>
    public Set<File> findFilesInDirectory(File path) {
        Set<File> fileSet = new HashSet<>();
        File[] files = path.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    fileSet.addAll(findFilesInDirectory(file));
                } else {
                    fileSet.add(file);
                }
            }
        }
        return fileSet;
    }
}
