import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class DuplicateFilePrinter {

    // Метод для вывода групп дубликатов файлов в консоль
    public void printDuplicates(List<List<String>> duplicates) {
        // Создаем карту для хранения групп файлов по их размеру
        Map<Long, List<List<String>>> filesBySize = new HashMap<>();

        // Заполняем карту, вычисляя размер каждого файла
        for (List<String> group : duplicates) {
            if (!group.isEmpty()) {
                try {
                    long size = Files.size(Path.of(group.get(0)));
                    filesBySize.computeIfAbsent(size, k -> new ArrayList<>()).add(group);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Сортируем размеры файлов по убыванию
        List<Long> sortedSizes = new ArrayList<>(filesBySize.keySet());
        sortedSizes.sort(Collections.reverseOrder());

        // Выводим группы файлов в консоль, начиная с самых больших
        for (Long size : sortedSizes) {
            for (List<String> group : filesBySize.get(size)) {
                System.out.println("Группа файлов типа: " + Path.of(group.get(0)).getFileName() + " размера: " + size + " байт");
                for (String filePath : group) {
                    System.out.println(filePath);
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

        // Пример использования
        //String path = "/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder";
        String path = "/home/alek7ey";


        FileDuplicateFinder finder = new FileDuplicateFinder();
        List<List<String>> duplicates = finder.findDuplicates(path);

        DuplicateFilePrinter printer = new DuplicateFilePrinter();
        printer.printDuplicates(duplicates);

        long endTime = System.currentTimeMillis();
        long duration = (long) ((endTime - startTime) / 1000.0);
        System.out.println("Время выполнения поиска дубликатов файлов в директории " + path + " --- " + duration + " секунд       ");

    }
}
