package hash2;

import processing.CheckValid;
import processing.FileKeyHash;
import processing.Printer;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;


public class FileDuplicateFinder {

    private final CheckValid checkValid;

    // Хранит группы файлов по хешу
    private final Map<FileKeyHash, Set<File>> filesByKeyHash = new ConcurrentSkipListMap<>();;
    private final List<String> verifiedDirectories = new ArrayList<>();

    /* Конструктор */
    public FileDuplicateFinder() {
        this.checkValid = new CheckValid();
    }


    /* Основной метод для поиска групп дубликатов файлов
     * @return duplicates - список групп дубликатов файлов
     * @return path - путь к директории, в которой нужно найти дубликаты
    * */
    public void findDuplicates(String[] paths) throws IOException {
        for(String path : paths) {  // Рекурсивный обход директорий для группировки файлов по их размеру в карту filesBySize
            walkFileTree(path);
        }

        // Вывод групп дубликатов файлов в консоль
        Printer.duplicatesByHash(getFilesByKeyHash());
    }



    /* Ускореный метод для рекурсивного обхода директории выполняет рекурсивный обход файловой системы,
     * начиная с указанного пути (path). Все файлы, найденные в процессе обхода, группируются по их размеру в HashMap filesBySize.
     * @param path - путь к директории, с которой начинается обход файловой системы
     */
    public void walkFileTree(String path) throws IOException {
        if (!checkValid.isValidDirectoryPath(path) || verifiedDirectories.contains(path)) {
            System.err.println("Невалидная директория или проверенная уже: " + path);
            return;
        }

        verifiedDirectories.add(path); // Добавляем проверенную директорию в список

        File directory = new File(path); // Создаем объект File(директорий) для указанного пути
        File[] files = directory.listFiles();  // Получаем список всех файлов и директорий в указанной директории

        if (files == null || files.length == 0) { // Проверяем, что массив не пустой
            System.err.println(" В директории " + path + " нет файлов");
            return;
        }

        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        //ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2);
        for (int i = 0; i < files.length; i++) {  // Перебираем каждый файл и директорию в текущей директории
            final File file = files[i]; // Сохраняем ссылку на текущий файл в локальной переменной

            if (file.isDirectory()) {  // Если текущий файл является директорией, рекурсивно вызываем walkFileTree
                walkFileTree(file.getAbsolutePath());
            } else {
                // Если файл валиден, то добавляем его в карту
                if (checkValid.isValidFile(file)) {
                    // ---------------------------------------------
                    executorService.submit(() -> {
                        processFileByHash(file);
                    });
                    // ---------------------------------------------
                }
            }
        }
        executorService.shutdown(); // Завершаем работу пула потоков
        while (!executorService.isTerminated()) {
            // Ожидаем завершения всех задач
        }
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


    // Геттер
    public Map<FileKeyHash, Set<File>> getFilesByKeyHash() {
        return filesByKeyHash;
    }

    // для тестов TesterUnit
    public List<Set<File>> getDuplicates() {
        List<Set<File>> duplicates = new ArrayList<>();
        for (Set<File> fileSet : filesByKeyHash.values()) {
            if (fileSet.size() > 1) {
                duplicates.add(fileSet);
            }
        }
        return duplicates;
    }

}