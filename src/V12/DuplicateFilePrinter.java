package V12;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class DuplicateFilePrinter {

    // Метод для вывода групп дубликатов файлов в консоль
//    public static void printDuplicates(Map<FileKey, Set<File>> filesByKey) {
//
//        Collections.sort(filesByKey);
//        // Выводим сгруппированные списки в консоль
//        for (Map.Entry<FileKey, Set<File>> entry : filesByKey.entrySet()) {
//            FileKey key = entry.getKey();
//            List<Set<File>> fileSets = entry.getValue();
//
//            System.out.println("Размер: " + size + " байт");
//            for (Set<File> fileSet : fileSets) {
//                System.out.println("  Дубликаты: " + fileSet);
//            }
//        }
//    }



    public static void main(String[] args) throws IOException {

        long startTime = System.currentTimeMillis();


        long endTime = System.currentTimeMillis();
        long duration = (long) (endTime - startTime);
        System.out.println("Время выполнения поиска дубликатов файлов в директориях " + duration + " ms       ");

    }
}
