package V12;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;


public class FileDuplicateFinder {

    private final CheckValid checkValid;

    private final Map<Long, Set<File>> fileBySize;   // HashMap fileBySize - для хранения файлов, сгруппированных по размеру

    private final Map<FileKey, Set<File>> filesByKey;  /* HashMap filesBySize - для хранения файлов, сгруппированных по размеру */
    Map<FileKey, Set<File>> getFilesByKey() {return filesByKey;}

    /* Конструктор */
    public FileDuplicateFinder() {
        this.checkValid = new CheckValid();
        this.fileBySize = new HashMap<>();
        this.filesByKey = new ConcurrentSkipListMap<>();
    }


    /* Основной метод для поиска групп дубликатов файлов
     * @return duplicates - список групп дубликатов файлов
     * @return path - путь к директории, в которой нужно найти дубликаты
    * */
    public void findDuplicates(String[] paths) throws IOException {
        for(String path : paths) {  // Рекурсивный обход директорий для группировки файлов по их размеру в карту filesBySize
            walkFileTree(path);
        }

        addFilesToMap();  // Добавляем файлы в карту fileByKey из HashMap fileBySize
        removeSingleFiles();  // Удаляем списки по одному файлу

        printDuplicateResults();  // Вывод групп дубликатов файлов в консоль
        
    }


    /* Метод для рекурсивного обхода директории выполняет рекурсивный обход файловой системы,
     * начиная с указанного пути (path). Все файлы, найденные в процессе обхода, группируются по их размеру в HashMap filesBySize.
     * @param path - путь к директории, с которой начинается обход файловой системы
     */
    public void walkFileTree(String path) {

        if (!checkValid.isValidDirectoryPath(path)) {
            System.out.println("Невалидная директория: " + path);
            return;
        }

        File directory = new File(path); // Создаем объект File(директорий) для указанного пути
        File[] files = directory.listFiles();  // Получаем список всех файлов и директорий в указанной директории

        if (files != null) {  // Проверяем, что массив не пустой
            for (File file : files) {  // Перебираем каждый файл и директорию в текущей директории
                if (file.isDirectory()) {  // Если текущий файл является директорией, создаем новый поток для рекурсивного вызова walkFileTree
                    walkFileTree(file.getAbsolutePath());
                } else {
                    if (!checkValid.isValidFile(file)) {  // Проверяем, что текущий файл является валидным
                        continue;
                    }
                    // Добавляем файл в карту fileBySize по его размеру
                    long fileSize = file.length();
                    fileBySize.computeIfAbsent(fileSize, k -> new HashSet<>()).add(file);
                }
            }
        }
    }


    // Добавляем файлы в Map fileByKey из HashMap fileBySize
//    public void addFilesToMap() {
//        // Используем поле класса filesByKey для потокобезопасности
//        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor(); // Виртуальные потоки
//
//        // Список для хранения Future
//        List<Future<Void>> futures = new ArrayList<>();
//
//        fileBySize.entrySet().forEach(entry -> {
//            if (entry.getValue().size() < 2) {  // Пропускаем списки файлов, которых меньше 2
//                return;
//            }
//            entry.getValue().forEach(file -> {
//                Future<Void> future = executorService.submit(() -> {
//                    try {
//                        FileKey key = new FileKey(file);
//                        filesByKey.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(file);
//                    } catch (IOException | NoSuchAlgorithmException e) {
//                        System.out.println("Ошибка при вычислении хеша файла: " + file.getAbsolutePath());
//                        e.printStackTrace();
//                    }
//                    return null;
//                });
//                futures.add(future);
//            });
//        });
//
//        // Ожидаем завершения всех задач
//        for (Future<Void> future : futures) {
//            try {
//                future.get();
//            } catch (InterruptedException | ExecutionException e) {
//                System.out.println("Ошибка при ожидании завершения задачи");
//                e.printStackTrace();
//            }
//        }
//        executorService.shutdown();
//    }

    public void addFilesToMap() {
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor(); // Виртуальные потоки
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        fileBySize.entrySet().forEach(entry -> {
            if (entry.getValue().size() < 2) {  // Пропускаем списки файлов, которых меньше 2
                return;
            }
            processFilesBySize(entry.getValue(), executorService, futures);
        });

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.join(); // Блокируем текущий поток до завершения всех задач

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // Обработка файлов из fileBySize по размеру
    private void processFilesBySize(Set<File> files, ExecutorService executorService, List<CompletableFuture<Void>> futures) {
        long numFiles = files.size();   // Количество файлов в списке
        long sizeFiles = files.iterator().next().length();  // Размер файлов


        filesByHesh( files, executorService, futures);
    }


    private void filesByHesh(Set<File> files, ExecutorService executorService, List<CompletableFuture<Void>> futures) {
        files.forEach(file -> {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    FileKey key = new FileKey(file);
                    filesByKey.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(file);
                } catch (IOException | NoSuchAlgorithmException e) {
                    System.out.println("Ошибка при вычислении хеша файла: " + file.getAbsolutePath());
                    e.printStackTrace();
                }
            }, executorService);
            futures.add(future);
        });
    }


    // Удаление списков по одному файлу
    public void removeSingleFiles() {
        for (Iterator<Map.Entry<FileKey, Set<File>>> iterator = filesByKey.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<FileKey, Set<File>> entry = iterator.next();
            if (entry.getValue().size() < 2) {
                iterator.remove();
            }
        }
    }




    // выводит группы дубликатов файлов
    public void printDuplicateResults() {
        // Проходим по всем записям в TreeMap fileByHash
        for (Map.Entry<FileKey, Set<File>> entry : filesByKey.entrySet()) {
            // Получаем ключ (FileKey) и значение (Set<File>) для текущей записи
            FileKey key = entry.getKey();
            Set<File> files = entry.getValue();
            // Проверяем, что в группе есть файлы
            if (files.size() > 1) {
                System.out.println();
                // Выводим информацию о группе дубликатов
                System.out.println("Группа дубликатов файла: '" + files.iterator().next().getName() + "' размера - " + key.getSize() + " байт -------------------------------");
                // Проходим по всем файлам в группе и выводим их пути
                for (File file : files) {
                    System.out.println(file.getAbsolutePath());
                }
                System.out.println();
                System.out.println("--------------------");
            }
        }
    }



}