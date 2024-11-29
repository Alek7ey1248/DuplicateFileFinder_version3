package v3;

import v1.CheckValid;
import v1.FileComparator;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;


/*
 * LARGE_FILE_THRESHOLD: Определяем порог для больших файлов на основе доступной памяти.
 * findDuplicates: Основной метод для поиска дубликатов файлов в указанных директориях.
 * walkFileTree: Рекурсивный обход директорий для группировки файлов по их размеру или хешу.
 * findLargeDuplicates: Метод для поиска дубликатов среди больших файлов.
 * createDuplicatesList: Метод для создания списка дубликатов из карт filesByHash и largeFilesBySize.
 * getDuplicates: Метод для получения списка дубликатов.
 * */

public class FileDuplicateFinder3 {

    /* HashMap fileByHash - для хранения файлов, сгруппированных по хешу */
    private final Map<Long, Set<Path>> fileByHash;

    /* геттер для получения списка дубликатов */
    public Map<Long, Set<Path>> getFilesByHash() {
        return fileByHash;
    }

    /* Список для хранения результата - групп дубликатов файлов */
    private final List<List<String>> duplicates;

    /* геттер для получения списка дубликатов */
    public List<List<String>> getDuplicates() {
        return duplicates;
    }

    /* класс вычисления хеша */
    private Hashing hashing;

    /* класс проверки валидности */
    CheckValid checkValid;


    /* Конструктор */
    public FileDuplicateFinder3() {
        fileByHash = new HashMap<>();
        hashing = new Hashing();
        duplicates = new ArrayList<>();
        checkValid = new CheckValid();
    }


    /* Основной метод для поиска групп дубликатов файлов
     * @return duplicates - список групп дубликатов файлов
     * @return path - путь к директории, в которой нужно найти дубликаты
     * */
    public void findDuplicates(String[] paths) throws IOException {

        // Рекурсивный обход директорий для группировки файлов по их хешу в карту filesByHash
        for (String path : paths) {
            walkFileTree(path);
        }

        // Поиск групп дубликатов файлов из карты filesByHash в список duplicates
        findDuplicatesGroup();

        // Вывод групп дубликатов файлов в консоль
        printDuplicateResults();
    }


    /* Метод для рекурсивного обхода директории выполняет рекурсивный обход файловой системы,
     * начиная с указанного пути (path). Все файлы, найденные в процессе обхода, группируются по их хешу в HasyMap filesByHash.
     * @param path - путь к директории, с которой начинается обход файловой системы
     */
    public void walkFileTree(String path) {

        // Для проверки валидности папки или файла
        //CheckValid checkValid = new CheckValid();

        // Создаем объект File(директорий) для указанного пути
        File directory = new File(path);

        // Проверка валидности директории
//        if (!checkValid.isValidDirectoryPath(directory.getAbsolutePath())) {
//            return;
//        }

        // Получаем список всех файлов и директорий в указанной директории
        File[] files = directory.listFiles();

        // Проверяем, что массив не пустой
        if (files != null) {
            // Создаем список потоков для параллельной обработки
            List<Thread> threads = new ArrayList<>();

            // Перебираем каждый файл и директорию в текущей директории
            for (File file : files) {
                // Если текущий файл является директорией, создаем новый поток для рекурсивного вызова walkFileTree
                if (file.isDirectory()) {
                    Thread thread = new Thread(() -> walkFileTree(file.getAbsolutePath()));
                    threads.add(thread);
                    thread.start();

                } else {
                    // Проверка валидности файла
                    if (checkValid.isValidFile(file)) {
                        // Создаем новый поток для вычисления хеша и добавления файла в карту
                        //Thread thread = new Thread(() -> {
                            // Вычисляем хеш файла с учетом его размера
                            long fileHash = hashing.calculateHashWithSize(file);
                            // Группируем файлы по их хешу
                            synchronized (fileByHash) {
                                fileByHash.computeIfAbsent(fileHash, k -> new HashSet<>()).add(file.toPath());
                            }
                        //});
//                        threads.add(thread);
//                        thread.start();
                    }
                }
            }

// Ожидаем завершения всех потоков
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /*
    * Метод для поиска групп дубликатов файлов из карты filesByHash в список duplicates
    * и сортировки списка по размеру файлов в каждой группе
    * */
    public void findDuplicatesGroup() {

        // Перебираем все файлы, сгруппированные по хешу
        for (Map.Entry<Long, Set<Path>> entry : fileByHash.entrySet()) {
            // Если в группе больше одного файла, добавляем их в список duplicates
            if (entry.getValue().size() > 1) {
                List<String> group = new ArrayList<>();
                for (Path path : entry.getValue()) {
                    group.add(path.toString());
                }
                duplicates.add(group);
            }
        }


        // Сортируем список duplicates по размеру файлов в каждом списке
        duplicates.sort((group1, group2) -> {
            try {
                long size1 = Files.size(Paths.get(group1.get(0)));
                long size2 = Files.size(Paths.get(group2.get(0)));
                return Long.compare(size1, size2); // Сортировка по убыванию размера
                //return Long.compare(size2, size1); // Сортировка по возрастанию размера
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }
        });
    }



    // выводит группы дубликатов файлов в консоль из списка дубликатов duplicates, результат поиска дубликатов основным классом FileDuplicateFinder
    public void printDuplicateResults() throws IOException {
        for (List<String> group : duplicates) {
            System.out.println();
            System.out.println("Группа дубликатов тмпа файла: '" + Paths.get(group.get(0)).getFileName() + "     размера - " + Files.size(Paths.get(group.get(0))) + " байт -------------------------------");
            for (String filePath : group) {
                System.out.println(filePath);
            }
            System.out.println();
            System.out.println("--------------------");
        }
    }

}