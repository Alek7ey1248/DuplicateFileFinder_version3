package V12;

import helperClasses.InsertZerosInMiddle;
import helperClasses.LargeFileCreator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

// Тестирование времени сравнения 2 файлов методами compareFiles, compareLargeFiles, хеширования
public class TestTimeCompare {

    File file1;
    File file2;

    // конструктор
    public TestTimeCompare(File file1, File file2) {
        this.file1 = file1;
        this.file2 = file2;
    }


    //  метод для определения списков файлов одинакового размера для последующего тестирования
    private void testWalkFileTree() {
        FileDuplicateFinder finder = new FileDuplicateFinder();
        finder.walkFileTree("/home/alek7ey/snap");
        Set<File> files = finder.getFileBySize().get(10376644L);
        for (File file : files) {
            System.out.println(file);
        }
    }


    // тест времени сравнения 2 файлов методом compareFiles
    private String testCompareFiles() throws IOException {
        System.out.println(" Тестируем метод compareFiles");
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 20; i++) {
            FileComparator.compareFiles(file1, file2, file1.length());
        }
        boolean result = FileComparator.compareFiles(file1, file2, file1.length());
        System.out.println(result);
        long endTime = System.currentTimeMillis();
        long duration = (long) (endTime - startTime);
        if (result) {
            System.out.println("файлы одинаковы");
        } else {
            System.out.println("файлы разные");
        }
        System.out.println("Время выполнения сравнения 2 файлов методом compareFiles - " + duration + " ms       ");
        return " compareFiles - " + duration + " ms";
    }

    // тест времени сравнения 2 файлов методом compareLargeFiles
    private String testcompareLargeFiles() throws IOException, InterruptedException {
        System.out.println(" Тестируем метод compareLargeFiles");
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 20; i++) {
            FileComparator.compareLargeFiles(file1, file2, file1.length());
        }
        boolean result = FileComparator.compareLargeFiles(file1, file2, file1.length());
        System.out.println(result);
        long endTime = System.currentTimeMillis();
        long duration = (long) (endTime - startTime);
        if (result) {
            System.out.println("файлы одинаковы");
        } else {
            System.out.println("файлы разные");
        }
        System.out.println("Время выполнения сравнения 2 файлов методом compareLargeFiles - " + duration + " ms       ");
        return " compareLargeFiles - " + duration + " ms";
    }

    // тест времени сравнения 2 небольших файлов методом хеширования
    private String testHashSmallFiles() throws IOException, NoSuchAlgorithmException {
        System.out.println(" Тестируем метод хеширования calculateHashSmallFile");
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 20; i++) {  // одновременно запускаем 2 потока
            // Создаем CompletableFuture для асинхронного выполнения
            CompletableFuture<String> hashFuture1 = CompletableFuture.supplyAsync(() -> {
                return FileKeyHash.calculateHashSmallFile(file1);
            });

            CompletableFuture<String> hashFuture2 = CompletableFuture.supplyAsync(() -> {
                return FileKeyHash.calculateHashSmallFile(file2);
            });

            // Ожидаем завершения обоих вычислений
            String hash1 = hashFuture1.join();
            String hash2 = hashFuture2.join();
        }
        long endTime = System.currentTimeMillis();
        long duration = (long) (endTime - startTime);
        if (new FileKeyHash(file1).equals(new FileKeyHash(file2))) {
            System.out.println("файлы одинаковы");
        } else {
            System.out.println("файлы разные");
        }
        System.out.println("Время выполнения сравнения 2 файлов методом хеширования calculateHashSmallFile - " + duration + " ms       ");
        return " calculateHashSmallFile - " + duration + " ms";
    }

    // тест времени сравнения 2 больших файлов методом хеширования
    private String testHashLardeFiles() throws IOException, NoSuchAlgorithmException {
        System.out.println(" Тестируем метод хеширования calculateHashLargeFile");
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 20; i++) { // одновременно запускаем 2 потока
            // Создаем CompletableFuture для асинхронного выполнения
            CompletableFuture<String> hashFuture1 = CompletableFuture.supplyAsync(() -> {
                return FileKeyHash.calculateHashLargeFile(file1);
            });

            CompletableFuture<String> hashFuture2 = CompletableFuture.supplyAsync(() -> {
                return FileKeyHash.calculateHashLargeFile(file2);
            });

            // Ожидаем завершения обоих вычислений
            String hash1 = hashFuture1.join();
            String hash2 = hashFuture2.join();        }
        long endTime = System.currentTimeMillis();
        long duration = (long) (endTime - startTime);
        if (new FileKeyHash(file1).equals(new FileKeyHash(file2))) {
            System.out.println("файлы одинаковы");
        } else {
            System.out.println("файлы разные");
        }
        System.out.println("Время выполнения сравнения 2 файлов методом хеширования calculateHashLargeFile - " + duration + " ms       ");
        return " calculateHashLargeFile - " + duration + " ms";
    }


    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InterruptedException {

        // класс для создания файлов
        LargeFileCreator largeFileCreator = new LargeFileCreator();
        // Класс для вставки в середину файла нулей
        InsertZerosInMiddle insertZerosInMiddle = new InsertZerosInMiddle();

        // Список для хранения результатов тестов сравнения одинаковых файлов
        List<String> resultsID = new ArrayList<>();
        // Список для хранения результатов тестов сравнения разных файлов
        List<String> resultsDif = new ArrayList<>();

        Long[] sizes = new Long[10]; // Размеры файлов для тестирования
        sizes[0] = 1024L; // 1 КБ
        sizes[1] = 102400L; // 100 КБ
        sizes[2] = 1048576L; // 1 МБ = 1024 КБ
        sizes[3] = 10485760L; // 10 МБ
        sizes[4] = 104857600L; // 100 МБ
        sizes[5] = 314572800L; // 300 МБ
        sizes[6] = 524288000L; // 500 МБ
        sizes[7] = 734003200L; // 700 МБ
        sizes[8] = 943718400L; // 900 МБ
        sizes[9] = 1073741824L; // 1 ГБ

// тестирование времени сравнения 2 одинаковых файлов  - делаем их разных размеров
        for (int i = 0; i < 10; i++) {
            long size = sizes[i];
            // создаем 2 одинаковых файла размером sizes[i]
            System.out.println("------------------  Создаем 2 одинаковых файла размером - " + size + " байт ------------------");
            String directoryPath = "/home/alek7ey/Рабочий стол/";
            String fileName1 = "file1.txt";
            String fileName2 = "file2.txt";
            largeFileCreator.createTwoFiles(directoryPath, fileName1, fileName2, size);
            File file1 = new File(directoryPath + fileName1);
            File file2 = new File(directoryPath + fileName2);

            // тестируем время сравнения 2 файлов всеми методами
            TestTimeCompare testTimeCompare = new TestTimeCompare(file1, file2);
            resultsID.add("---------------------------------------------------------");
            resultsID.add(" ОДИНАКОВЫЕ 2 файла размер - " + sizes[i]);
            resultsID.add("  - " + testTimeCompare.testCompareFiles());
            resultsID.add("  - " + testTimeCompare.testcompareLargeFiles());
            resultsID.add("  - " + testTimeCompare.testHashSmallFiles());
            resultsID.add("  - " +  testTimeCompare.testHashLardeFiles());
            resultsID.add("---------------------------------------------------------");

            // удаляем созданные файлы
            System.out.println("Удаляем созданные файлы");
            try {
                if (Files.deleteIfExists(file1.toPath())) {
                    System.out.println("Файл " + file1.getName() + " успешно удален.");
                } else {
                    System.out.println("Файл " + file1.getName() + " не существует.");
                }

                if (Files.deleteIfExists(file2.toPath())) {
                    System.out.println("Файл " + file2.getName() + " успешно удален.");
                } else {
                    System.out.println("Файл " + file2.getName() + " не существует.");
                }
            } catch (IOException e) {
                System.err.println("Ошибка при удалении файла: " + e.getMessage());
                e.printStackTrace(); // Выводим стек вызовов для диагностики
            }
        }

        // Результаты тестирования сравнения одинаковых файлов
        System.out.println();
        System.out.println("  Результаты тестирования сравнения одинаковых файлов:");
        for (String result : resultsID) {
            System.out.println(result);
        }

    }

}
