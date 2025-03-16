package hash1;

import processing.CheckValid;
import processing.FileKeyHash;
import processing.Printer;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;

public class FileDuplicateFinder {

    private  final CheckValid checkValid;

    private final Map<FileKeyHash, Set<File>> filesByKeyHash;

    private final List<String> verifiedDirectories;

    /* Конструктор */
    public FileDuplicateFinder() {
        this.checkValid = new CheckValid();
        this.filesByKeyHash = new ConcurrentSkipListMap<>();
        this.verifiedDirectories = new ArrayList<>();
    }


    /* Основной метод для поиска групп дубликатов файлов
     * @return duplicates - список групп дубликатов файлов
     * @return path - путь к директории, в которой нужно найти дубликаты
     * */
    public void findDuplicates(String[] paths) {

        for (String path : paths) { // Рекурсивный обход директорий для группировки файлов по их пазмеру в карту filesByHash
            walkFileTree(path);
        }

        Printer.duplicatesByHash(getFilesByKeyHash());  // Вывод групп дубликатов файлов в консоль
    }


    /* Метод для рекурсивного обхода директории выполняет рекурсивный обход файловой системы,
     * начиная с указанного пути (path).
     * Все файлы, найденные в процессе обхода, группируются по по хешу в fileByHash.
     * @param path - путь к директории, с которой начинается обход файловой системы
     */
    public void walkFileTree(String path) {
        if (!checkValid.isValidDirectoryPath(path) || verifiedDirectories.contains(path)) {
            System.err.println("Невалидная директория или проверенная уже: " + path);
            return;
        }

        verifiedDirectories.add(path); // Добавляем проверенную директорию в список

        File directory = new File(path); // Создаем объект File(рут директория) для указанного пути
        File[] files = directory.listFiles(); // Получаем список всех файлов и директорий в текущей директории

        if (files == null || files.length == 0) { // Проверяем, что массив не пустой
            System.err.println(" В директории " + path + " нет файлов");
            return;
        }

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(); // Виртуальные потоки
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for(File file : files) { // Перебираем каждый файл и директорию в текущей директории
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                if (file.isDirectory()) {
                    walkFileTree(file.getAbsolutePath()); // Если текущий файл является валидной директорией, вставляем рекурсивно в walkFileTree
                } else {
                    // Если файл валиден, то добавляем его в fileByContent
                    if(checkValid.isValidFile(file)) {
                            processFileByHash(file);
                    }
                }
            }, executor);
            futures.add(future);
        }
        // Ожидаем завершения всех CompletableFuture
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.join(); // Блокируем текущий поток до завершения всех задач
        executor.shutdown();
    }



    /* Метод для добавления файла в мап fileByHash по ключу - хэшу
    * @param file - файл, который нужно добавить в мапу
     */
    public void processFileByHash(File file) {
        System.out.println(file.getAbsolutePath());
        FileKeyHash key = null; // Вычисляем хэш файла
        try {
            key = new FileKeyHash(file);
        } catch (IOException e) {
            System.err.println("Ошибка IOException при обработке файла: " + file.getAbsolutePath());
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Ошибка NoSuchAlgorithmException при обработке файла: " + file.getAbsolutePath());
            e.printStackTrace();
        }
        filesByKeyHash.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(file);
    }


    // метод для тестов JUnit - TesterUnit
    // Возвращает список групп дубликатов файлов переделывая
    // Map<FileKeyHash, Set<File>> в List<Set<File>>
    public List<Set<File>> getDuplicates() {
        List<Set<File>> duplicates = new ArrayList<>();
        for (Map.Entry<FileKeyHash, Set<File>> entry : filesByKeyHash.entrySet()) {
            Set<File> files = entry.getValue();
            if (files.size() > 1) {
                duplicates.add(files);
            }
        }
        return duplicates;
    }

    // геттер filesByKeyHash
    public Map<FileKeyHash, Set<File>> getFilesByKeyHash() {
        return filesByKeyHash;
    }

}

