package V12;

import V11.FileComparator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;


public class FileDuplicateFinder {

    private final CheckValid checkValid;

    private final Map<Long, Set<File>> fileBySize;   // HashMap fileBySize - для хранения файлов, сгруппированных по размеру

    private final Map<FileKeyHash, Set<File>> filesByKey;  /* HashMap filesBySize - для хранения файлов, сгруппированных по размеру */
    //Map<FileKeyHash, Set<File>> getFilesByKey() {return filesByKey;}

    private final Map<String, Set<File>> filesByContent;  /* HashMap filesBySize - для хранения файлов, сгруппированных по одинаковому контенту */

    // Создаем TreeMap для хранения групп файлов, отсортированных по размеру
    private final Map<String, Set<File>> duplicates;
    Map<String, Set<File>> getDuplicates() {return duplicates;}

    /* Конструктор */
    public FileDuplicateFinder() {
        this.checkValid = new CheckValid();
        this.fileBySize = new HashMap<>();
        this.filesByKey = new ConcurrentSkipListMap<>();
        this.filesByContent = new ConcurrentSkipListMap<>();
        this.duplicates = new TreeMap<>();
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
        removeSingleFiles();  // Удаляем списки по одному файлу из filesByKey

        printSortedFileGroups();  // Вывод групп дубликатов файлов в консоль
        //printDuplicateResults();  // Вывод групп дубликатов файлов в консоль
        
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
//        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(); // Виртуальные потоки
//        List<CompletableFuture<Void>> futures = new ArrayList<>();

        fileBySize.entrySet().forEach(entry -> {
            if (entry.getValue().size() < 2) {  // Пропускаем списки файлов, которых меньше 2
                return;
            }
            processGroupFiles(entry.getValue());
        });

//        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
//        allOf.join(); // Блокируем текущий поток до завершения всех задач
//
//        executor.shutdown();
//        try {
//            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
//                executor.shutdownNow();
//            }
//        } catch (InterruptedException e) {
//            executor.shutdownNow();
//            Thread.currentThread().interrupt();
//        }
    }


    // Обработка файлов из fileBySize по размеру
    private void processGroupFiles(Set<File> files) {
        long numFiles = files.size();   // Количество файлов в списке
        long sizeFiles = files.iterator().next().length();  // Размер файлов

        if (false) {   // если ...
            // группировка дубликатов по хешу в filesByKey
            groupByHesh(files);
        }  else {
            // Группировка дубликатов по содержимому в filesByKey
            try {
                groupByContent(files);
            } catch (IOException e) {
                System.err.println("Ошибка при группировке файлов по содержимому");
                throw new RuntimeException(e);
            }
        }


    }

    // Групировка файлов по хешу и добавление в filesByKey - группы дубликатов
    private void groupByHesh(Set<File> files) {
        files.forEach(file -> {
            //CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    FileKeyHash key = new FileKeyHash(file);
                    filesByKey.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(file);
                } catch (IOException | NoSuchAlgorithmException e) {
                    System.out.println("Ошибка при вычислении хеша файла: " + file.getAbsolutePath());
                    e.printStackTrace();
                }
//            }, executor);
//            futures.add(future);
        });
    }


    private void groupByContent(Set<File> files) throws IOException {
        while (files.size() > 1) {  // Пока в списке файлов одинакового размера есть хотя бы два файла
            Iterator<File> iterator = files.iterator();  // Извлекаем первый файл из списка
            File file = iterator.next();
            iterator.remove();   // Удаляем первый файл из списка
            System.out.println(" Поиск дубликатов файла: " + file.getAbsolutePath());

            Set<File> group = new HashSet<>();
            group.add(file);   // Добавляем первый файл в группу дубликатов
            String key = file.getAbsolutePath();  // Ключ для группы дубликатов по содержимому - путь к файлу
            Set<File> toRemove = ConcurrentHashMap.newKeySet();   // Список для удаления дубликатов из files

            //List<CompletableFuture<Void>> fileComparisons = new ArrayList<>(); // Список для хранения CompletableFuture

            for (File anotherFile : files) {  // Перебираем оставшиеся файлы в списке
                if (file.equals(anotherFile)) {  // Если пути к файлам равны, пропускаем
                    continue;
                }
                // Отправляем задачу на сравнение файлов в пул потоков
                //CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        if (FileComparator.areFilesEqual(file.toPath(), anotherFile.toPath())) {
                            //synchronized (group) { // Синхронизация при добавлении в группу
                                group.add(anotherFile);  // Добавляем путь к дубликату в группу дубликатов
                            //}
                            toRemove.add(anotherFile);  // Добавляем путь к дубликату в список для удаления
                        }
                    } catch (IOException e) {
                        System.err.println("Ошибка при сравнении файлов: " + file.getAbsolutePath() + " и " + anotherFile.getAbsolutePath());
                    }
//                }, executor);
//                fileComparisons.add(future); // Добавляем CompletableFuture в список
            }

//            // Ждем завершения всех задач
//            CompletableFuture<Void> allOf = CompletableFuture.allOf(fileComparisons.toArray(new CompletableFuture[0]));
//            allOf.join(); // Блокируем до завершения всех задач

            files.removeAll(toRemove);  // Удаляем дубликаты из списка файлов одинакового размера

            if (group.size() > 1) { // Добавляем группу дубликатов в список дубликатов по ключу (если группа содержит более одного файла)
                filesByContent.put(key, group);
                System.out.println(" группа дубликатов : ----------" );
                for (File f : group) {
                    System.out.println(f.getAbsolutePath() + "    ");
                }
                System.out.println("----------" );
            }
        }
    }



    // Удаление списков по одному файлу
    public void removeSingleFiles() {
        for (Iterator<Map.Entry<FileKeyHash, Set<File>>> iterator = filesByKey.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<FileKeyHash, Set<File>> entry = iterator.next();
            if (entry.getValue().size() < 2) {
                iterator.remove();
            }
        }
    }


    public void printSortedFileGroups() {

        // Добавляем все Set<File> из filesByKey с уникальными ключами
        int keyIndex = 0;
        for (Set<File> fileSet : filesByKey.values()) {
            addFileSetToMap(fileSet, "key_", keyIndex++);
        }

        // Добавляем все Set<File> из filesByContent с уникальными ключами
        int contentIndex = 0;
        for (Set<File> fileSet : filesByContent.values()) {
            addFileSetToMap(fileSet, "content_", contentIndex++);
        }

        // Выводим отсортированные группы в консоль
        for (Map.Entry<String, Set<File>> entry : duplicates.entrySet()) {
            String key = entry.getKey();
            Set<File> fileSet = entry.getValue();

            // Извлекаем размер первого файла для вывода
            File firstFile = fileSet.iterator().next();
            System.out.println("-------------------------------------------------");
            System.out.println("Группа дубликатов (" + key + ") размером: " + firstFile.length() + " байт");
            for (File file : fileSet) {
                System.out.println("    " + file.getAbsolutePath());
            }
            System.out.println("-------------------------------------------------");
        }
    }

    // Вспомогательный метод для добавления Set<File> в TreeMap
    private void addFileSetToMap(Set<File> fileSet, String prefix, int index) {
        // Получаем первый файл из Set<File>
        File firstFile = fileSet.stream().findFirst().orElse(null);
        if (firstFile != null) {
            long size = firstFile.length();
            // Создаем уникальный ключ с префиксом, размером и индексом
            String key = prefix + size + "_" + index;
            // Добавляем в TreeMap
            duplicates.computeIfAbsent(key, k -> new HashSet<>()).addAll(fileSet);
        }
    }

    // выводит группы дубликатов файлов
    public void printDuplicateResults() {
        // Проходим по всем записям в TreeMap fileByHash
        for (Map.Entry<FileKeyHash, Set<File>> entry : filesByKey.entrySet()) {
            // Получаем ключ (FileKey) и значение (Set<File>) для текущей записи
            FileKeyHash key = entry.getKey();
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