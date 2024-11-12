package v2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

public class FileDuplicateFinder2 {

    // HashMap для хранения файлов, сгруппированных по размеру
    Map<Long, ConcurrentLinkedQueue<Path>> filesBySize;

    // для проверки валидности файла или папки
    CheckValid2 checkValid;


    public FileDuplicateFinder2() {
        filesBySize = new HashMap<>();
        checkValid = new CheckValid2();
    }

    // Основной метод для поиска дубликатов файлов
    public List<List<String>> findDuplicates(String path) throws IOException {

        // Обход директорий для группировки файлов по их размеру в карту filesBySize
        walkFileTree(path);

        // Параллельная обработка файлов одинакового размера для поиска дубликатов
        List<List<String>> duplicates = findDuplicateGroups();

        return duplicates;
    }


    /*  Находитгруппы побайтно одинаковых файлов из карты файлов, ключ которой — размер файла.*/
    /**
    * @return список групп повторяющихся файлов
    * @throws IOException при возникновении ошибки ввода-вывода*/
    public List<List<String>> findDuplicateGroups() throws IOException {
        // Результат - Список для хранения дубликатов файлов
        List<List<String>> duplicates = new ArrayList<>();

        // Создаем ExecutorService с фиксированным пулом потоков
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // Список задач для параллельного выполнения
        List<Future<Void>> futures = new ArrayList<>();

        // перебираю ключи (размеры файлов)
        for (Long size : filesBySize.keySet()) {
            // Получаю список файлов для текущего размера
            ConcurrentLinkedQueue<Path> files = filesBySize.get(size);

            // Отправляем задачу на обработку файлов в пул потоков
            futures.add(executor.submit(() -> {
                findDuplicatesInSameSizeFiles(files, duplicates);

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
     * @param fileQueue — список(очередь) путей к файлам одинакового размера. Из HashMap filesBySize.
     * @param duplicates -  список для хранения групп побайтно одинаковых файлов.
     * @throws IOException при возникновении ошибки ввода-вывода
     */
    public void findDuplicatesInSameSizeFiles(ConcurrentLinkedQueue<Path> fileQueue, List<List<String>> duplicates) throws IOException {
        // Если файлов меньше двух, выходим из метода. Нет смысла сравнивать один файл сам с собой.
        if (fileQueue.size() < 2) {
            return;
        }

        // Создаем ExecutorService с фиксированным пулом потоков
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        while (!fileQueue.isEmpty()) {
            // Извлекаем первый файл из очереди , если очередь не пуста после удаления файла, то продолжаем
            Path file = fileQueue.remove();
            System.out.println("Проверка файла: " + file);
            if (fileQueue.isEmpty()) { continue;}

            List<String> group = new ArrayList<>();
            group.add(file.toString());

            // Список задач для параллельного выполнения
            List<Future<Boolean>> futures = new ArrayList<>();

            for (Path anotherFile : fileQueue) {
                // Отправляем задачу на сравнение файлов в пул потоков
                futures.add(executor.submit(() -> {
                    if (FileComparator2.areFilesEqual(file, anotherFile)) {
                        synchronized (group) {
                            group.add(anotherFile.toString());
                        }
                        // Синхронизируем доступ к fileQueue и удаляем anotherFile из очереди
                        synchronized (fileQueue) {
                            fileQueue.remove(anotherFile);
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
    public void walkFileTree(String path) {

        // Создаем объект File(директорий) для указанного пути
        File directory = new File(path);
        // Проверка валидности директории
        if (!checkValid.isValidDirectoryPath(directory.getAbsolutePath())) {
            return;
        }

        // Получаем список всех файлов и директорий в указанной директории
        File[] files = directory.listFiles();

        // Проверяем, что массив не пустой
        if (files != null) {
            // Перебираем каждый файл и директорию в текущей директории
            for (File file : files) {
                // Если текущий файл является директорией, рекурсивно вызываем walkFileTree
                if (file.isDirectory()) {
                    walkFileTree(file.getAbsolutePath());
                } else {
                    // Проверка валидности файла
                    if (checkValid.isValidFile(file)) {
                        // Если текущий файл не является директорией, добавляем его в карту
                        // Группируем файлы по их размеру
                        filesBySize.computeIfAbsent(file.length(), k -> new ConcurrentLinkedQueue<>()).add(file.toPath());
                    }
                }
            }
        }
    }



    public static void main(String[] args) throws IOException {

        long startTime = System.currentTimeMillis();

        // Создание экземпляра класса FileDuplicateFinder
        FileDuplicateFinder2 finder = new FileDuplicateFinder2();

        Map<Long, ConcurrentLinkedQueue<Path>> filesBySize = new HashMap<>();

        //finder.walkFileTree("/home/alek7ey/Рабочий стол/TestsDFF/TestsDuplicateFileFinder");
        finder.walkFileTree("/home/alek7ey/Рабочий стол");
        //finder.walkFileTree("/home/alek7ey");
        //finder.walkFileTree("/home");


        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime);

        int countFiles = 0;
        for (Map.Entry<Long, ConcurrentLinkedQueue<Path>> entry : filesBySize.entrySet()) {
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