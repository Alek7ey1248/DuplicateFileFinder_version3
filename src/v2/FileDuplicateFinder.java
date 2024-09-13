package v2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FileDuplicateFinder {

    // Основной метод для поиска дубликатов файлов
    public List<List<String>> findDuplicates(String path) throws IOException {
        // HashMap для хранения файлов, сгруппированных по размеру
        Map<Long, List<Path>> filesBySize = new HashMap<>();

        // Рекурсивный обход директорий для группировки файлов по их размеру в карту filesBySize
        walkFileTree(path, filesBySize);

        // Параллельная обработка файлов одинакового размера для поиска дубликатов
        List<List<String>> duplicates = findDuplicateGroups(filesBySize);

        return duplicates;
    }


    /*  Находитгруппы побайтно одинаковых файлов из карты файлов, ключ которой — размер файла.*/
    /**
    * @param filesBySize — карта, где ключом является размер файла, а значением — список путей к файлам этого размера.
    * @return список групп повторяющихся файлов
    * @throws IOException при возникновении ошибки ввода-вывода*/
    public List<List<String>> findDuplicateGroups(Map<Long, List<Path>> filesBySize) throws IOException {
        // Результат - Список для хранения дубликатов файлов
        List<List<String>> duplicates = new ArrayList<>();
        FileComparator comparator = new FileComparator();

        // Создаем ExecutorService с фиксированным пулом потоков
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // Список задач для параллельного выполнения
        List<Future<Void>> futures = new ArrayList<>();

        // перебираю ключи (размеры файлов)
        for (Long size : filesBySize.keySet()) {
            // Получаю список файлов для текущего размера
            List<Path> files = filesBySize.get(size);

            // Отправляем задачу на обработку файлов в пул потоков
            futures.add(executor.submit(() -> {
                findDuplicatesInSameSizeFiles(files, duplicates, comparator);
                return null;
            }));
        }

        // Ожидаем завершения всех задач
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // Завершаем работу ExecutorService
        executor.shutdown();

        // Возвращаем список групп дубликатов
        return duplicates;
    }


    /**
     * Находит дубликаты файлов в списке файлов одинакового размера.
     *
     * @param files — список путей к файлам одинакового размера. Из HashMap filesBySize.
     * @param duplicates -  список для хранения групп побайтно одинаковых файлов.
     * @param comparator — компаратор для сравнения файлов
     * @throws IOException при возникновении ошибки ввода-вывода
     */
    public void findDuplicatesInSameSizeFiles(List<Path> files, List<List<String>> duplicates, FileComparator comparator) throws IOException {
        if (files.isEmpty()) {
            return;
        }

        // Преобразуем список файлов в очередь
        Queue<Path> fileQueue = new ArrayDeque<>(files);

        // Создаем ExecutorService с фиксированным пулом потоков
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        while (!fileQueue.isEmpty()) {
            // Извлекаем первый файл из очереди
            Path file = fileQueue.poll();
            List<String> group = new ArrayList<>();
            group.add(file.toString());

            // Временный список для хранения дубликатов
            List<Path> toRemove = new ArrayList<>();

            // Список задач для параллельного выполнения
            List<Future<Boolean>> futures = new ArrayList<>();

            for (Path anotherFile : fileQueue) {
                if (file.equals(anotherFile)) {
                    continue;
                }

                // Отправляем задачу на сравнение файлов в пул потоков
                futures.add(executor.submit(() -> {
                    if (comparator.areFilesEqual(file, anotherFile)) {
                        synchronized (group) {
                            group.add(anotherFile.toString());
                        }
                        synchronized (toRemove) {
                            toRemove.add(anotherFile);
                        }
                        return true;
                    }
                    return false;
                }));
            }

            // Ожидаем завершения всех задач
            for (Future<Boolean> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            // Удаляем найденные дубликаты из очереди
            fileQueue.removeAll(toRemove);
            // Добавляем группу дубликатов в список дубликатов (если группа содержит более одного файла)
            if (group.size() > 1) {
                duplicates.add(group);
            }
        }

        // Завершаем работу ExecutorService
        executor.shutdown();
    }

    // Метод для рекурсивного обхода директории выполняет рекурсивный обход файловой системы,
    // начиная с указанного пути (path). Все файлы, найденные в процессе обхода,
    // группируются по их размеру в HasyMap filesBySize.
    public void walkFileTree(String path, Map<Long, List<Path>> filesBySize) {
        // Создаем объект File для указанного пути
        File directory = new File(path);
        // для проверки валидности файла
        CheckValid checkValid = new CheckValid();

        // Получаем список всех файлов и директорий в указанной директории
        File[] files = directory.listFiles();

        // Проверяем, что массив не пустой
        if (files != null) {
            // Перебираем каждый файл и директорию в текущей директории
            for (File file : files) {
                // Если текущий файл является директорией, рекурсивно вызываем walkFileTree
                if (file.isDirectory()) {
                    walkFileTree(file.getAbsolutePath(), filesBySize);
                } else {
                    // Проверка валидности файла
                    if (checkValid.isValidFile(file)) {
                        // Если текущий файл не является директорией, добавляем его в карту
                        // Группируем файлы по их размеру
                        filesBySize.computeIfAbsent(file.length(), k -> new ArrayList<>()).add(file.toPath());
                    }
                }
            }
        }
    }



    public static void main(String[] args) throws IOException {

        long startTime = System.currentTimeMillis();

        // Создание экземпляра класса FileDuplicateFinder
        FileDuplicateFinder finder = new FileDuplicateFinder();

        Map<Long, List<Path>> filesBySize = new HashMap<>();

        finder.walkFileTree("/home/alek7ey/Рабочий стол/TestsDuplicateFileFinder", filesBySize);
        //finder.walkFileTree("/home/alek7ey/Рабочий стол", filesBySize);
        //finder.walkFileTree("/home/alek7ey", filesBySize);
        //finder.walkFileTree("/home", filesBySize);


        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime);

        int countFiles = 0;
        for (Map.Entry<Long, List<Path>> entry : filesBySize.entrySet()) {
            System.out.println("");
            System.out.println("размер: " + entry.getKey() + " ------------------");
            for (Path path : entry.getValue()) {
                countFiles++;
                System.out.println(path);
            }
        }

        System.out.println();
        System.out.println("Время выполнения: " + duration + " милисекунд       " + (long)duration/1000.0 + " секунд");
        System.out.println();
        System.out.println("Количество файлов: " + countFiles);
    }

}