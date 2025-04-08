package hashNew;

import processing.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;


public class FileDuplicateFinder {

    private final List<String> verifiedDirectories;  // Список всех абсолютных путей проверенных директорий

    private final CheckValid checkValid;  // класс для проверки валидности файлов и директорий
    private final FileGrouperNew fileGrouperNew;  // класс для группировки файлов по хешу и контенту

    private final Map<Long, Set<File>> filesBySize;   // HashMap fileBySize - для хранения файлов, сгруппированных по размеру

    private final List<Set<File>> duplicates; // Список групп дубликатов файлов - результат работы программы

    /* Конструктор */
    public FileDuplicateFinder() {
        this.checkValid = new CheckValid();
        this.verifiedDirectories = new ArrayList<>();
        this.filesBySize = new ConcurrentHashMap<>();
        this.fileGrouperNew = new FileGrouperNew();
        this.duplicates = new ArrayList<>();
    }


    /* Основной метод для поиска групп дубликатов файлов
     * @return path - путь к директории, в которой нужно найти дубликаты
    */
    public void findDuplicates(String[] paths) throws IOException {

        for(String path : paths) {  // Рекурсивный обход директорий для группировки файлов по их размеру в карту filesBySize
            walkFileTree(path);
        }

        processGroupFiles();  // Добавляем файлы в карту fileByKey из HashMap fileBySize

        // Получаем список групп дубликатов файлов, сортируем и выводим результат
        Printer.duplicatesByContent1(duplicates);
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

        if (files != null) {  // Проверяем, что массив не пустой
            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(); // Виртуальные потоки
            //CompletableFuture[] futures = new CompletableFuture[files.length]; // Массив для хранения CompletableFuture
            var futures = new CompletableFuture[files.length];

            for (int i = 0; i < files.length; i++) {  // Перебираем каждый файл и директорию в текущей директории
                final File file = files[i]; // Сохраняем ссылку на текущий файл в локальной переменной
                futures[i] = CompletableFuture.runAsync(() -> {
                        if (file.isDirectory()) {  // Если текущий файл является директорией, рекурсивно вызываем walkFileTree
                            walkFileTree(file.getAbsolutePath());
                        } else {
                            if (!checkValid.isValidFile(file)) {  // Проверяем, что текущий файл является валидным
                                return;
                            }
                            // Добавляем файл в карту fileBySize по его размеру
                            long fileSize = file.length();
                            filesBySize.computeIfAbsent(fileSize, k -> ConcurrentHashMap.newKeySet()).add(file);
                        }
                }, executor);
            }

            // Ожидаем завершения всех CompletableFuture
            CompletableFuture.allOf(futures).join();
            executor.shutdown();
        }
    }


    /* Добавление файлов в карту fileByKey или filesByContent в зависимости от логики метода processGroupFiles
    */
//    public void processGroupFiles() {
//        for (Map.Entry<Long, Set<File>> entry : filesBySize.entrySet()) {
//            Set<File> files = entry.getValue(); // Получаем набор файлов с данным размером
//            if (files.size() < 2) { // Если в наборе файлов меньше 2-х, пропускаем его
//                continue;
//            }
//
//            System.out.println("Обработка группы файлов типа " + files.iterator().next().getAbsolutePath() + " размером: " + entry.getKey() + " байт");
//
//            // Группируем файлы по контенту в fileGroups
//            List<Set<File>> fileGroups = fileGrouperNew.groupByContent(files);
//            if (fileGroups.isEmpty()) { // Если не удалось сгруппировать файлы, пропускаем
//                continue;
//            }
//            // доюавляем список групп в список дубликатов duplicates
//            duplicates.addAll(fileGroups);
//        }
//    }

    public void processGroupFiles() {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        List<Future<?>> futures = new ArrayList<>();
        for (Map.Entry<Long, Set<File>> entry : filesBySize.entrySet()) {
            Set<File> files = entry.getValue();
            if (files.size() < 2) {
                continue;
            }
            futures.add(executor.submit(() -> {
                System.out.println("Обработка группы файлов типа " + files.iterator().next().getAbsolutePath() + " размером: " + entry.getKey() + " байт");
                List<Set<File>> fileGroups = fileGrouperNew.groupByContent(files);
                if (!fileGroups.isEmpty()) {
                    duplicates.addAll(fileGroups);
                }
            }));
        }
        // Ждем завершения всех задач
        for (Future<?> future : futures) {
            try {
                future.get(); // Можно обработать исключения, если необходимо
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown(); // Завершаем работу пула потоков
    }


    /* Метод для получения порога для больших файлов
        * @return порог для больших файлов
     */
    private static long getLargeFileThreshold() {
        long maxMemory = Runtime.getRuntime().maxMemory(); // Доступная память
        int availableProcessors = Runtime.getRuntime().availableProcessors(); // Количество доступных процессоров
        return maxMemory / (availableProcessors * 4L); // Возвращаем порог
    }

    // Возвращает карту файлов, сгруппированных по размеру - гетер
    Map<Long, Set<File>> getFilesBySize() {return filesBySize;}

    // Возвращает список файлов, сгруппированных по одинаковому содержимому - гетер
    List<Set<File>> getDuplicates() {return duplicates;}

}