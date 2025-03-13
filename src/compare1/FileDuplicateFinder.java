package compare1;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class FileDuplicateFinder {

    private final CheckValid checkValid;
    private final Map<Long, Set<File>> filesBySize;  /* HashMap filesBySize - для хранения файлов, сгруппированных по размеру */
    final Map<Long, Set<File>> getFilesBySize() {return filesBySize;}
    private final List<List<File>> duplicates;  /* Список для хранения результата - групп дубликатов файлов */
    List<List<File>> getDuplicates() {return duplicates;}
    private final List<String> verifiedDirectories;  // Множество для хранения проверенных директорий

    /* Конструктор */
    public FileDuplicateFinder() {
        this.checkValid = new CheckValid();
        this.filesBySize = new ConcurrentHashMap<>();
        this.duplicates = new CopyOnWriteArrayList<>();
        this.verifiedDirectories = new ArrayList<>();
    }


    /* Основной метод для поиска групп дубликатов файлов
     * @return duplicates - список групп дубликатов файлов
     * @return path - путь к директории, в которой нужно найти дубликаты
    * */
    public void findDuplicates(String[] paths) throws IOException {
        for(String path : paths) {  // Рекурсивный обход директорий для группировки файлов по их размеру в карту filesBySize
            walkFileTree(path);
        }
        findDuplicateGroups();  // Параллельная обработка файлов одинакового размера из карты filesBySize для поиска дубликатов в список duplicates
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
    }


    /*  Находит группы побайтно одинаковых файлов из карты файлов, ключ которой — размер файла.*/
    public void findDuplicateGroups() throws IOException {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();  // Создаем ExecutorService с виртуальными потоками
        //List<Future<Void>> futures = new ArrayList<>();  // Список задач для параллельного выполнения

        for (Long size : filesBySize.keySet()) {  // перебираю ключи (размеры файлов)
            Set<File> files = filesBySize.get(size);  // Получаю список файлов для текущего размера
            //futures.add(executor.submit(() -> {  // Отправляем задачу на обработку файлов в пул потоков
            executor.submit(() -> {
                // Находим дубликаты в группе файлов одинакового размера и добавляем их в список дубликатов duplicates
                findDuplicatesInSameSizeFiles(files);
                return null;
            //}));
            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            // Ждем завершения всех задач
        }

//        // Ожидаем завершения всех задач
//        for (Future<Void> future : futures) {
//            try {
//                future.get();
//            } catch (InterruptedException | ExecutionException e) {
//                e.printStackTrace();
//            }
//        }
//        // Завершаем работу пула потоков - на скорость вроде повлияло
//        executor.shutdown();

//        try {
//            // Ждем завершения всех задач в течение 60 секунд
//            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
//                executor.shutdownNow(); // Принудительно завершаем все активные задачи
//            }
//        } catch (InterruptedException e) {
//            executor.shutdownNow(); // Принудительно завершаем все активные задачи
//            Thread.currentThread().interrupt(); // Восстанавливаем статус прерывания
//        }
    }



    /**
     * Находит дубликаты файлов в списке файлов одинакового размера.
     *
     * @param files — список путей к файлам одинакового размера. Из HashMap filesBySize.
     */
    public void findDuplicatesInSameSizeFiles(Set<File> files) throws IOException {
        if (files.size() < 2) {  // Если файлов меньше двух, выходим из метода
            return;
        }

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();   // Создаем ExecutorService с виртуальными потоками

        while (files.size() > 1) {  // Пока в списке файлов одинакового размера есть хотя бы два файла
            Iterator<File> iterator = files.iterator();  // Извлекаем первый файл из списка
            File file = iterator.next();
            iterator.remove();   // Удаляем первый файл из списка

            System.out.println(" Поиск дубликатов файла: " + file);
            //List<String> group = Collections.synchronizedList(new ArrayList<>());
            List<File> group = new CopyOnWriteArrayList<>();   // потокобезопасный список для добавления путей к дубликатам
            group.add(file);   // Добавляем путь к первому файлу в группу дубликатов

            List<File> toRemove = new CopyOnWriteArrayList<>();   // потокобезопасный список для удаления дубликатов из files

            List<Future<Boolean>> futures = new ArrayList<>();  // Список задач для параллельного выполнения

            for (File anotherFile : files) {  // Перебираем оставшиеся файлы в списке

                if (file.equals(anotherFile)) {  // Если пути к файлам равны, пропускаем
                    //continue;
                    return;
                }

                // Отправляем задачу на сравнение файлов в пул потоков
                futures.add(executor.submit(() -> {
                    try {
                        if (FileComparator.areFilesEqual(file, anotherFile)) {
                                group.add(anotherFile);  // Добавляем путь к дубликату в группу дубликатов
                                toRemove.add(anotherFile);  // Добавляем путь к дубликату в список для удаления
                                return true;
                        }
                    } catch (IOException e) {
                        System.err.println("Ошибка при сравнении файлов: " + file + " и " + anotherFile);
                    }
                    return false;
                }));
            }

            for (Future<Boolean> future : futures) { // Ожидаем завершения всех задач
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            files.removeAll(toRemove);  // Удаляем дубликаты из списка файлов одинакового размера

            if (group.size() > 1) { // Добавляем группу дубликатов в список дубликатов (если группа содержит более одного файла)
                duplicates.add(group);
            }
        }

        // Завершаем работу ExecutorService
        executor.shutdown();
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

}