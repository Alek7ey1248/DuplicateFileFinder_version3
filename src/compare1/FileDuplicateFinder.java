package compare1;

import processing.CheckValid;
import processing.FileGrouper;
import processing.Printer;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class FileDuplicateFinder {

    private final CheckValid checkValid;  // Проверка валидности файлов и директорий
    private final FileGrouper fileGrouper;  // Группировка файлов по содержимому
    private final Map<Long, Set<File>> filesBySize;  /* HashMap filesBySize - для хранения файлов, сгруппированных по размеру */
    private final List<String> verifiedDirectories;  // Для хранения проверенных директорий

    /* Конструктор */
    public FileDuplicateFinder() {
        this.checkValid = new CheckValid();
        this.fileGrouper = new FileGrouper();
        this.filesBySize = new ConcurrentHashMap<>();
        this.verifiedDirectories = new ArrayList<>();
    }


    /* Основной метод - поиск дубликатов файлов в директории и вывод результатов
        * @param paths - массив путей к директориям, в которых нужно найти дубликаты файлов
        * @throws IOException - исключение ввода-вывода
    */
    public void findDuplicates(String[] paths) throws IOException {
        for(String path : paths) {  // Рекурсивный обход директорий для группировки файлов по их размеру в карту filesBySize
            walkFileTree(path);
        }
        processGroupFiles();  // Группировка файлов по содержимому
        Printer.duplicatesByContent1(fileGrouper.getFilesByContent());
    }


    /* Ускореный метод для рекурсивного обхода директории выполняет рекурсивный обход файловой системы,
     * начиная с указанного пути (path). Все файлы, найденные в процессе обхода, группируются по их размеру в HashMap filesBySize.
     * @param path - путь к директории, с которой начинается обход файловой системы
     */
    public void walkFileTree(String path) {
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
            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(); // Виртуальные потоки
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (File file : files) {  // Перебираем каждый файл и директорию в текущей директории
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    if (file.isDirectory()) {  // Если текущий файл является директорией, рекурсивно вызываем walkFileTree
                        walkFileTree(file.getAbsolutePath());
                    } else {
                        if (checkValid.isValidFile(file)) { // Проверяем, что файл является валидным
                            // Добавляем файл в карту fileBySize по его размеру
                            long fileSize = file.length();
                            filesBySize.computeIfAbsent(fileSize, k -> ConcurrentHashMap.newKeySet()).add(file);
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


    // Добавление файлов в карту fileByKey или filesByContent в зависимости от логики метода processGroupFiles
    // Перед этим смотрим если файлов меньше 2, то пропускаем
    public void processGroupFiles() {

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(); // Виртуальные потоки
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Список файлов одинакового размера
        filesBySize.forEach((key, files) -> {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {

                if (files.size() < 2) {  // Пропускаем списки файлов, которых меньше 2
                    return;
                }
                // Группировка файлов по содержимому
                fileGrouper.groupByContentParallel(files);
                //fileGrouper.groupByContent(files);

            }, executor);
            futures.add(future);
        });
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.join(); // Блокируем текущий поток до завершения всех задач

        executor.shutdown();
        awaitTermination(executor);
    }


    // Ожидание завершения работы пула потоков
    private void awaitTermination(ExecutorService executor) {
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Принудительное завершение
            }
        } catch (InterruptedException e) {
            executor.shutdownNow(); // Принудительное завершение
            Thread.currentThread().interrupt(); // Восстанавливаем статус прерывания
        }
    }

    // геттеры
    Map<Long, Set<File>> getFilesBySize() {return filesBySize;}

    // Список групп дубликатов файлов для тестов TesterUnit
    List<Set<File>> getDuplicates() {
        return fileGrouper.getFilesByContent();
    }


}