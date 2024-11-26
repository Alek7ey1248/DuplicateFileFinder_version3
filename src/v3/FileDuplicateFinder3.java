package v3;

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

    private static final long LARGE_FILE_THRESHOLD = getLargeFileThreshold();  // Порог для больших файлов
    private final Map<Long, Set<Path>> filesByHash = new HashMap<>();      // Файлы, сгруппированные по хешу
    private final Map<Long, Set<Path>> largeFilesBySize = new HashMap<>();   // Большие файлы, сгруппированные по размеру
    private final List<List<File>> duplicates = new ArrayList<>();    // Список дубликатов

    // Определяем порог для больших файлов на основе доступной памяти
    private static long getLargeFileThreshold() {
        long availableMemory = Runtime.getRuntime().maxMemory();
        return availableMemory / 3;
    }

    // Метод для поиска дубликатов файлов в указанных директориях
    public void findDuplicates(String[] paths) throws IOException, InterruptedException, ExecutionException {
        for (String path : paths) {
            walkFileTree(path);
        }
        findLargeDuplicates();
        createDuplicatesList();
    }

    // Рекурсивный обход директорий для группировки файлов по их размеру или хешу
    private void walkFileTree(String path) throws IOException {
        Files.walk(Paths.get(path))
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        long fileSize = Files.size(file);
                        if (fileSize <= LARGE_FILE_THRESHOLD) {
                            long fileHash = new Hashing().calculateHashWithSize(file.toFile());
                            filesByHash.computeIfAbsent((long) fileHash, k -> new HashSet<>()).add(file);
                        } else {
                            largeFilesBySize.computeIfAbsent(fileSize, k -> new HashSet<>()).add(file);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    // Метод для поиска дубликатов среди больших файлов
    private void findLargeDuplicates() throws IOException, InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<Map<Long, Set<Path>>>> futures = new ArrayList<>();

        for (Map.Entry<Long, Set<Path>> entry : largeFilesBySize.entrySet()) {
            futures.add(executor.submit(() -> {
                Map<Long, Set<Path>> result = new HashMap<>();
                Set<Path> files = entry.getValue();
                for (Path file1 : files) {
                    for (Path file2 : files) {
                        if (!file1.equals(file2) && FileComparator3.compareLargeFiles(file1, file2)) {
                            result.computeIfAbsent(entry.getKey(), k -> new HashSet<>()).add(file1);
                            result.get(entry.getKey()).add(file2);
                        }
                    }
                }
                return result;
            }));
        }

        for (Future<Map<Long, Set<Path>>> future : futures) {
            Map<Long, Set<Path>> result = future.get();
            result.forEach((key, value) -> largeFilesBySize.merge(key, value, (oldSet, newSet) -> {
                oldSet.addAll(newSet);
                return oldSet;
            }));
        }

        executor.shutdown();
    }

    // Метод для создания списка дубликатов из карт filesByHash и largeFilesBySize
    private void createDuplicatesList() {
        filesByHash.values().forEach(set -> {
            if (set.size() > 1) {
                List<File> group = new ArrayList<>();
                set.forEach(path -> group.add(path.toFile()));
                duplicates.add(group);
            }
        });

        largeFilesBySize.values().forEach(set -> {
            if (set.size() > 1) {
                List<File> group = new ArrayList<>();
                set.forEach(path -> group.add(path.toFile()));
                duplicates.add(group);
            }
        });

        duplicates.sort((list1, list2) -> {
            try {
                long size1 = Files.size(list1.get(0).toPath());
                long size2 = Files.size(list2.get(0).toPath());
                return Long.compare(size2, size1);
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }
        });
    }

    // Метод для получения списка дубликатов
    public List<List<File>> getDuplicates() {
        return duplicates;
    }
}