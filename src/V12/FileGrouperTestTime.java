package V12;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

// FileGrouperTestTime: Класс для тестирования времени выполнения методов класса FileGrouper.
// Генерирует наборы файлов разного размера и тестирует методы класса FileGrouper на них.
public class FileGrouperTestTime {
    public static void main(String[] args) {

   // ОДИНАКОВЫЕ ФАЙЛЫ------------------------------------------------
        // Генерация тестовых данных c 2 файлами каждого размера
        Set<File> smallFiles2Id = generateIdenticalFiles(2, 2); // Файлы почти 0 байт
        Set<File> mediumFiles2Id = generateIdenticalFiles(10, 2); // Файлыдо 10 байт
        Set<File> largeFiles2Id = generateIdenticalFiles(1000, 2); // Файлы до 1 Кб
        Set<File> hugeFiles2Id = generateIdenticalFiles(10000, 2); // Файлы до 100 Кб
        Set<File> giganticFiles2Id = generateIdenticalFiles(300000000, 2); // Файлы до 300 Мб

        // Генерация тестовых данных c 4 файлами каждого размера
        Set<File> smallFiles4Id = generateIdenticalFiles(2, 4); // Файлы почти 0 байт
        Set<File> mediumFiles4Id = generateIdenticalFiles(10, 4); // Файлыдо 10 байт
        Set<File> largeFiles4Id = generateIdenticalFiles(1000, 4); // Файлы до 1 Кб
        Set<File> hugeFiles4Id = generateIdenticalFiles(10000, 4); // Файлы до 100 Кб
        Set<File> giganticFiles4Id = generateIdenticalFiles(300000000, 4); // Файлы до 300 Мб

        // Генерация тестовых данных c 8 файлами каждого размера
        Set<File> smallFiles8Id = generateIdenticalFiles(2, 8); // Файлы почти 0 байт
        Set<File> mediumFiles8Id = generateIdenticalFiles(10, 8); // Файлыдо 10 байт
        Set<File> largeFiles8Id = generateIdenticalFiles(1000, 8); // Файлы до 1 Кб
        Set<File> hugeFiles8Id = generateIdenticalFiles(10000, 8); // Файлы до 100 Кб
        Set<File> giganticFiles8Id = generateIdenticalFiles(300000000, 8); // Файлы до 300 Мб


        // РАЗНЫЕ ФАЙЛЫ------------------------------------------------
        // Генерация тестовых данных c 2 файлами каждого размера
        Set<File> smallFiles2Di = generateDifferentFiles(2, 2); // Файлы почти 0 байт
        Set<File> mediumFiles2Di = generateDifferentFiles(10, 2); // Файлыдо 10 байт
        Set<File> largeFiles2Di = generateDifferentFiles(1000, 2); // Файлы до 1 Кб
        Set<File> hugeFiles2Di = generateDifferentFiles(10000, 2); // Файлы до 100 Кб
        Set<File> giganticFiles2Di = generateDifferentFiles(300000000, 2); // Файлы до 300 Мб

        // Генерация тестовых данных c 4 файлами каждого размера
        Set<File> smallFiles4Di = generateDifferentFiles(2, 4); // Файлы почти 0 байт
        Set<File> mediumFiles4Di = generateDifferentFiles(10, 4); // Файлыдо 10 байт
        Set<File> largeFiles4Di = generateDifferentFiles(1000, 4); // Файлы до 1 Кб
        Set<File> hugeFiles4Di = generateDifferentFiles(10000, 4); // Файлы до 100 Кб
        Set<File> giganticFiles4Di = generateDifferentFiles(300000000, 4); // Файлы до 300 Мб

        // Генерация тестовых данных c 8 файлами каждого размера
        Set<File> smallFiles8Di = generateDifferentFiles(2, 8); // Файлы почти 0 байт
        Set<File> mediumFiles8Di = generateDifferentFiles(10, 8); // Файлыдо 10 байт
        Set<File> largeFiles8Di = generateDifferentFiles(1000, 8); // Файлы до 1 Кб
        Set<File> hugeFiles8Di = generateDifferentFiles(10000, 8); // Файлы до 100 Кб
        Set<File> giganticFiles8Di = generateDifferentFiles(300000000, 8); // Файлы до 300 Мб



        FileGrouper fileGrouper = new FileGrouper();

        // Список для хранения результатов тестов
        List<String> results = new ArrayList<>();

        // ТЕСТ РАЗНЫХ ФАЙЛОВ
        // Тестирование методов с наборами по 2 файлов каждого размера
        results.add("Testing Small Files 2 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, smallFiles2Di, results);
        results.add("Testing Medium Files 2 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, mediumFiles2Di, results);
        results.add("Testing Large Files 2 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, largeFiles2Di, results);
        results.add("Testing Huge Files 2 -РАЗНЫЕ ФАЙЛЫ-----------------------");
        testFileGroup(fileGrouper, hugeFiles2Di, results);
        results.add("Testing Gigantic Files 2 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, giganticFiles2Di, results);

        // Тестирование методов с наборами по 4 файлов каждого размера
        results.add("Testing Small Files 4 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, smallFiles4Di, results);
        results.add("Testing Medium Files 4 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, mediumFiles4Di, results);
        results.add("Testing Large Files 4 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, largeFiles4Di, results);
        results.add("Testing Huge Files 4 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, hugeFiles4Di, results);
        results.add("Testing Gigantic Files 4 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, giganticFiles4Di, results);

        // Тестирование методов с наборами по 8 файлов каждого размера
        results.add("Testing Small Files 8 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, smallFiles8Di, results);
        results.add("Testing Medium Files 8 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, mediumFiles8Di, results);
        results.add("Testing Large Files 8 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, largeFiles8Di, results);
        results.add("Testing Huge Files 8 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, hugeFiles8Di, results);
        results.add("Testing Gigantic Files 8 РАЗНЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, giganticFiles8Di, results);


        // ТЕСТ ОДИНАКОВЫХ ФАЙЛОВ
        // Тестирование методов с наборами по 2 файлов каждого размера
        results.add("Testing Small Files 2 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, smallFiles2Id, results);
        results.add("Testing Medium Files 2 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, mediumFiles2Id, results);
        results.add("Testing Large Files 2 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, largeFiles2Id, results);
        results.add("Testing Huge Files 2 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, hugeFiles2Id, results);
        results.add("Testing Gigantic Files 2 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, giganticFiles2Id, results);

        // Тестирование методов с наборами по 4 файлов каждого размера
        results.add("Testing Small Files 4 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, smallFiles4Id, results);
        results.add("Testing Medium Files 4 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, mediumFiles4Id, results);
        results.add("Testing Large Files 4 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, largeFiles4Id, results);
        results.add("Testing Huge Files 4 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, hugeFiles4Id, results);
        results.add("Testing Gigantic Files 4 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, giganticFiles4Id, results);

        // Тестирование методов с наборами по 8 файлов каждого размера
        results.add("Testing Small Files 8 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, smallFiles8Id, results);
        results.add("Testing Medium Files 8 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, mediumFiles8Id, results);
        results.add("Testing Large Files 8 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, largeFiles8Id, results);
        results.add("Testing Huge Files 8 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, hugeFiles8Id, results);
        results.add("Testing Gigantic Files 8 ОДИНАКОВЫЕ ФАЙЛЫ------------------------");
        testFileGroup(fileGrouper, giganticFiles8Id, results);


        // Вывод результатов всех тестов
        for (String result : results) {
            System.out.println(result);
        }
    }

    // Генерация набора одинаковых файлов заданного размера и количества
    private static Set<File> generateIdenticalFiles(int fileSize, int numberOfFiles) {
        Set<File> fileSet = new HashSet<>();
        String content = generateRandomString(fileSize); // Генерируем строку для содержимого

        for (int i = 0; i < numberOfFiles; i++) {
            try {
                // Создаем временный файл
                Path tempFile = Files.createTempFile("identicalFile" + i + "_", ".txt");
                Files.write(tempFile, content.getBytes());
                fileSet.add(tempFile.toFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileSet;
    }

    // Генерация набора разных файлов заданного размера и количества
    private static Set<File> generateDifferentFiles(int fileSize, int numberOfFiles) {
        Set<File> fileSet = new HashSet<>();

        for (int i = 0; i < numberOfFiles; i++) {
            try {
                // Создаем временный файл
                Path tempFile = Files.createTempFile("differentFile" + i + "_", ".txt");
                String content = generateRandomString(fileSize); // Генерируем строку для содержимого
                Files.write(tempFile, content.getBytes());
                fileSet.add(tempFile.toFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileSet;
    }

    // Вспомогательный метод для генерации строки заданной длины
    private static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()"; // Символы для генерации
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }


    // Тестирование методов класса FileGrouper на наборе файлов и запись результатов в список
    private static void testFileGroup(FileGrouper fileGrouper, Set<File> files, List<String> results) {
        testMethodExecutionTime("groupByHesh", () -> fileGrouper.groupByHesh(files), results);
        testMethodExecutionTime("groupByHeshParallel", () -> fileGrouper.groupByHeshParallel(files), results);
        testMethodExecutionTime("groupByContent", () -> {
            try {
                fileGrouper.groupByContent(files);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, results);
        testMethodExecutionTime("groupByContentParallel", () -> {
            try {
                fileGrouper.groupByContentParallel(files);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, results);
    }

    // Тестирование времени выполнения метода
    private static void testMethodExecutionTime(String methodName, Runnable method, List<String> results) {
        long startTime = System.nanoTime();
        for (int i = 0; i < 10; i++){
            method.run();
        }
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        results.add(methodName + " executed in " + duration/1000000 + " ms");
    }
}
