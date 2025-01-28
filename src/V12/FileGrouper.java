package V12;
import V11.FileComparator;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;

public class FileGrouper {
    // Хранит группы файлов по хешу
    private final Map<FileKeyHash, Set<File>> filesByKey;
    public Map<FileKeyHash, Set<File>> getFilesByKey() {
        return filesByKey;
    }

    // Хранит группы файлов по содержимому
    private final Map<String, Set<File>> filesByContent;
    public Map<String, Set<File>> getFilesByContent() {
        return filesByContent;
    }

    // Конструктор
    public FileGrouper() {
        filesByKey = new ConcurrentHashMap<>();
        filesByContent = new ConcurrentHashMap<>();
    }


    // Групировка файлов по хешу и добавление в filesByKey - группы дубликатов
    void groupByHesh(Set<File> files) {

        files.forEach(file -> {
                try {
                    FileKeyHash key = new FileKeyHash(file);
                    filesByKey.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(file);
                } catch (IOException | NoSuchAlgorithmException e) {
                    System.out.println("Ошибка при вычислении хеша файла: " + file.getAbsolutePath());
                    e.printStackTrace();
                }
        });
    }


    // Групировка файлов по хешу и добавление в filesByKey- группы дубликатов
    // (Ускоренный потоками)
    void groupByHeshParallel(Set<File> files) {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(); // Виртуальные потоки
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        files.forEach(file -> {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    FileKeyHash key = new FileKeyHash(file);
                    filesByKey.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(file);
                } catch (IOException | NoSuchAlgorithmException e) {
                    System.out.println("Ошибка при вычислении хеша файла: " + file.getAbsolutePath());
                    e.printStackTrace();
                }
            }, executor);
            futures.add(future);
        });

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.join(); // Блокируем текущий поток до завершения всех задач

        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }


    // Групировка файлов по содержимому методом сравнения файлов
    // и добавление в filesByContent - группы дубликатов
    void groupByContent(Set<File> files) throws IOException {
        while (files.size() > 1) {  // Пока в списке файлов одинакового размера есть хотя бы два файла
            Iterator<File> iterator = files.iterator();  // Извлекаем первый файл из списка
            File file = iterator.next();
            iterator.remove();   // Удаляем первый файл из списка
            System.out.println(" Поиск дубликатов файла: " + file.getAbsolutePath());

            Set<File> group = new HashSet<>();
            group.add(file);   // Добавляем первый файл в группу дубликатов
            String key = file.getAbsolutePath();  // Ключ для группы дубликатов по содержимому - путь к файлу
            Set<File> toRemove = ConcurrentHashMap.newKeySet();   // Список для удаления дубликатов из files

            for (File anotherFile : files) {  // Перебираем оставшиеся файлы в списке
                if (file.equals(anotherFile)) {  // Если пути к файлам равны, пропускаем
                    continue;
                }
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
            }

            files.removeAll(toRemove);  // Удаляем дубликаты из списка файлов одинакового размера

            if (group.size() > 1) { // Добавляем группу дубликатов в список дубликатов по ключу (если группа содержит более одного файла)
                filesByContent.put(key, group);
            }
        }
    }


    // Групировка файлов по содержимому методом сравнения файлов
    // и добавление в filesByContent - группы дубликатов
    // (Ускоренный потоками)
    void groupByContentParallel(Set<File> files) throws IOException {
        while (files.size() > 1) {  // Пока в списке файлов одинакового размера есть хотя бы два файла
            Iterator<File> iterator = files.iterator();  // Извлекаем первый файл из списка
            File file = iterator.next();
            iterator.remove();   // Удаляем первый файл из списка
            System.out.println(" Поиск дубликатов файла: " + file.getAbsolutePath());

            Set<File> group = new HashSet<>();
            group.add(file);   // Добавляем первый файл в группу дубликатов
            String key = file.getAbsolutePath();  // Ключ для группы дубликатов по содержимому - путь к файлу
            Set<File> toRemove = ConcurrentHashMap.newKeySet();   // Список для удаления дубликатов из files

            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(); // Виртуальные потоки
            List<CompletableFuture<Void>> fileComparisons = new ArrayList<>(); // Список для хранения CompletableFuture

            for (File anotherFile : files) {  // Перебираем оставшиеся файлы в списке
                if (file.equals(anotherFile)) {  // Если пути к файлам равны, пропускаем
                    continue;
                }
                // Отправляем задачу на сравнение файлов в пул потоков
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
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
                }, executor);
                fileComparisons.add(future); // Добавляем CompletableFuture в список
            }

            // Ждем завершения всех задач
            CompletableFuture<Void> allOf = CompletableFuture.allOf(fileComparisons.toArray(new CompletableFuture[0]));
            allOf.join(); // Блокируем до завершения всех задач

            files.removeAll(toRemove);  // Удаляем дубликаты из списка файлов одинакового размера

            if (group.size() > 1) { // Добавляем группу дубликатов в список дубликатов по ключу (если группа содержит более одного файла)
                filesByContent.put(key, group);
            }
        }
    }

}
