package compare;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class DuplicateFilePrinter {

    // Метод для вывода групп дубликатов файлов в консоль
    public static void printDuplicates(List<List<File>> duplicates) {
        // Создаем карту для хранения групп файлов по их размеру
        // Ключ - размер файла, значение - списки групп одинаковых файлов
        Map<Long, List<List<File>>> filesBySize = new HashMap<>();

        // Заполняем карту, вычисляя размер каждого файла
        for (List<File> group : duplicates) {
            if (!group.isEmpty()) {
                long size = group.get(0).length();
                filesBySize.computeIfAbsent(size, k -> new ArrayList<>()).add(group);
            }
        }

        // Сортируем размеры файлов
        List<Long> sortedSizes = new ArrayList<>(filesBySize.keySet());
        // по убыванию
        //sortedSizes.sort(Collections.reverseOrder());
        // по возрастанию
        Collections.sort(sortedSizes);


        // Выводим группы файлов в консоль, начиная с самых больших
        for (Long size : sortedSizes) {
            for (List<File> group : filesBySize.get(size)) {
                System.out.println("Группа файлов типа: '" + group.get(0).getName() + "';     размера: " + size + " байт");
                System.out.println();
                for (File file : group) {
                    System.out.println("     " + file.getAbsolutePath());
                }
                System.out.println();
                System.out.println("--------------------");
            }
        }
    }


    // выводит группы дубликатов файлов в консоль из списка дубликатов duplicates, результат поиска дубликатов основным классом FileDuplicateFinder
    public void printDuplicateResults(List<List<String>> duplicates) {
        for (List<String> group : duplicates) {
            System.out.println("Группа дубликатов:");
            for (String filePath : group) {
                System.out.println(filePath);
            }
            System.out.println();
            System.out.println("--------------------");
        }
    }

    public static void main(String[] args) throws IOException {

        long startTime = System.currentTimeMillis();

        //String[] paths = {"/home/alek7ey/Рабочий стол"};
        //String[] paths = "/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder";
        String[] paths = {"/home/alek7ey"};
        //String[] paths = "/home/alek7ey/Рабочий стол/TestsDFF/Большие файлы";
        //String[] paths = "/home/alek7ey/.cache";
        //String[] paths = "/home/alek7ey/snap/flutter";
        //String path = "/home/alek7ey/Рабочий стол";


        FileDuplicateFinder finder = new FileDuplicateFinder();

        // ищем дубликаты файлов в директории
        finder.findDuplicates(paths);

        // выводим отсортированные результаты поиска дубликатов
        printDuplicates(finder.getDuplicates());

        long endTime = System.currentTimeMillis();
        long duration = (long) (endTime - startTime);
        System.out.println("Время выполнения поиска дубликатов файлов в директориях " + Arrays.toString(paths) + " --- " + duration + " ms       ");

    }
}
