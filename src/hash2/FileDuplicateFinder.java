package hash2;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;


public class FileDuplicateFinder {

    private final CheckValid checkValid;

    // Хранит группы файлов по хешу
    private final Map<FileKeyHash, Set<File>> filesByKey = new ConcurrentSkipListMap<>();;
    public Map<FileKeyHash, Set<File>> getFilesByKey() {
        return filesByKey;
    }

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
        printSortedFileGroups();
    }



    /* Ускореный метод для рекурсивного обхода директории выполняет рекурсивный обход файловой системы,
     * начиная с указанного пути (path). Все файлы, найденные в процессе обхода, группируются по их размеру в HashMap filesBySize.
     * @param path - путь к директории, с которой начинается обход файловой системы
     */
    public void walkFileTree(String path) throws IOException {
        if (!checkValid.isValidDirectoryPath(path)) {
            System.err.println("Невалидная директория: " + path);
            return;
        }

        File directory = new File(path); // Создаем объект File(директорий) для указанного пути
        File[] files = directory.listFiles();  // Получаем список всех файлов и директорий в указанной директории

        if (files == null) return;  // Проверяем, что массив не пустой

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
                        System.out.println(" вычислениеа хеш - " + file.getAbsolutePath());
                        try {
                            FileKeyHash key = new FileKeyHash(file);
                            filesByKey.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(file);
                        } catch (IOException | NoSuchAlgorithmException e) {
                            System.out.println("Ошибка при вычислении хеша файла: " + file.getAbsolutePath());
                            e.printStackTrace();
                        }
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


    // Сортирровка и Вывод групп дубликатов файлов в консоль
    public void printSortedFileGroups() {

        // Выводим отсортированные группы в консоль
        for (Map.Entry<FileKeyHash, Set<File>> fileSet : filesByKey.entrySet()) {             // Перебираем все группы файлов

            if (fileSet.getValue().size() < 2) {                    // Если в группе только один файл, переходим к следующей группе
                continue;
            }

            System.out.println("-------------------------------------------------");
            System.out.println("Группа дубликатов размером: " + fileSet.getKey().getSize() + " байт");
            for (File file : fileSet.getValue()) {                  // Перебираем все файлы в группе
                System.out.println("    " + file.getAbsolutePath());
            }
            System.out.println("-------------------------------------------------");
        }
    }


    public List<Set<File>> getDuplicates() {
        List<Set<File>> duplicates = new ArrayList<>();
        for (Set<File> fileSet : filesByKey.values()) {
            if (fileSet.size() > 1) {
                duplicates.add(fileSet);
            }
        }
        return duplicates;
    }

}