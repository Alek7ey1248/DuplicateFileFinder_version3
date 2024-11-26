package v1;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class FileDuplicateFinder {

    /* HashMap filesBySize - для хранения файлов, сгруппированных по размеру */
    private final Map<Long, Set<Path>> filesBySize;

    public Map<Long, Set<Path>> getFilesBySize() {
        return filesBySize;
    }

    /* Список для хранения результата - групп дубликатов файлов */
    private final List<List<String>> duplicates;

    public List<List<String>> getDuplicates() {
        return duplicates;
    }

    /* Конструктор */
    public FileDuplicateFinder() {
        filesBySize = new HashMap<>();
        duplicates = new ArrayList<>();
    }



    /* Основной метод для поиска групп дубликатов файлов
     * @return duplicates - список групп дубликатов файлов
     * @return path - путь к директории, в которой нужно найти дубликаты
    * */
    public void findDuplicates(String[] paths) throws IOException {

        // Рекурсивный обход директорий для группировки файлов по их размеру в карту filesBySize
        for(String path : paths) {
            walkFileTree(path);
        }

        // Параллельная обработка файлов одинакового размера из карты filesBySize для поиска дубликатов в список duplicates
        findDuplicateGroups();
    }


    /*  Находитгруппы побайтно одинаковых файлов из карты файлов, ключ которой — размер файла.*/
    /**
    * @throws IOException при возникновении ошибки ввода-вывода*/
    public void findDuplicateGroups() throws IOException {

        // Создаем ExecutorService с фиксированным пулом потоков
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // Список задач для параллельного выполнения
        List<Future<Void>> futures = new ArrayList<>();

        // перебираю ключи (размеры файлов)
        for (Long size : filesBySize.keySet()) {
            // Получаю список файлов для текущего размера
            Set<Path> files = filesBySize.get(size);

            // Отправляем задачу на обработку файлов в пул потоков
            futures.add(executor.submit(() -> {
                // Находим дубликаты в группе файлов одинакового размера и добавляем их в список дубликатов duplicates 
                findDuplicatesInSameSizeFiles(files);
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

        // Завершаем работу пула потоков - на скорость вроде повлияло
        executor.shutdown();
//        try {
//            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {    // метод блокирует выполнение текущего потока до тех пор, пока все задачи не завершат выполнение, либо не истечет указанный тайм-аут (60 секунд), либо текущий поток не будет прерван, в зависимости от того, что произойдет первым. Если метод возвращает false, это означает, что тайм-аут истек до завершения всех задач.
//                executor.shutdownNow();    // метод инициирует упорядоченное завершение работы пула потоков. Новые задачи больше не принимаются, но уже запущенные задачи продолжают выполняться.
//            }
//        } catch (InterruptedException e) {  // Этот блок catch перехватывает исключение InterruptedException, которое может быть выброшено, если текущий поток был прерван во время ожидания завершения задач.
//            executor.shutdownNow();      // В случае прерывания текущего потока, этот метод также принудительно завершает работу пула потоков.
//            Thread.currentThread().interrupt();   // Восстанавливает статус прерывания текущего потока, чтобы другие части кода могли корректно обработать факт прерывания.
//        }
    }



    /**
     * Находит дубликаты файлов в списке файлов одинакового размера.
     *
     * @param files — список путей к файлам одинакового размера. Из HashMap filesBySize.
     * @throws IOException при возникновении ошибки ввода-вывода
     */
    public void findDuplicatesInSameSizeFiles(Set<Path> files) throws IOException {
        if (files.size() < 2) {
            return;
        }

        // Создаем ExecutorService с фиксированным пулом потоков
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        while (files.size() > 1) {
            // Извлекаем первый файл
            //Path file = files.removeFirst();

            Iterator<Path> iterator = files.iterator();
            Path file = iterator.next();
            iterator.remove();


            System.out.println("Проверка файла: " + file);
            List<String> group = new ArrayList<>();
            group.add(file.toString());

            // Временный список для хранения найденных дубликатов, что бы потом удалить их из списка files
            List<Path> toRemove = new ArrayList<>();

            // Список задач для параллельного выполнения
            List<Future<Boolean>> futures = new ArrayList<>();

            // Перебираем оставшиеся файлы в списке
            for (Path anotherFile : files) {
                if (file.equals(anotherFile)) {
                    continue;
                }

                // Отправляем задачу на сравнение файлов в пул потоков
                futures.add(executor.submit(() -> {
                    if (FileComparator.areFilesEqual(file, anotherFile)) {
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
            files.removeAll(toRemove);
            // Добавляем группу дубликатов в список дубликатов (если группа содержит более одного файла)
            if (group.size() > 1) {
                duplicates.add(group);
            }
        }

        // Завершаем работу ExecutorService
        executor.shutdown();
//        try {
//            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {    // метод блокирует выполнение текущего потока до тех пор, пока все задачи не завершат выполнение, либо не истечет указанный тайм-аут (60 секунд), либо текущий поток не будет прерван, в зависимости от того, что произойдет первым. Если метод возвращает false, это означает, что тайм-аут истек до завершения всех задач.
//                executor.shutdownNow();    // метод инициирует упорядоченное завершение работы пула потоков. Новые задачи больше не принимаются, но уже запущенные задачи продолжают выполняться.
//            }
//        } catch (InterruptedException e) {  // Этот блок catch перехватывает исключение InterruptedException, которое может быть выброшено, если текущий поток был прерван во время ожидания завершения задач.
//            executor.shutdownNow();      // В случае прерывания текущего потока, этот метод также принудительно завершает работу пула потоков.
//            Thread.currentThread().interrupt();   // Восстанавливает статус прерывания текущего потока, чтобы другие части кода могли корректно обработать факт прерывания.
//        }
    }



    /* Метод для рекурсивного обхода директории выполняет рекурсивный обход файловой системы,
    * начиная с указанного пути (path). Все файлы, найденные в процессе обхода, группируются по их размеру в HasyMap filesBySize.
    * @param path - путь к директории, с которой начинается обход файловой системы
     */
    public void walkFileTree(String path) {

        // Для проверки валидности папки или файла
        CheckValid checkValid = new CheckValid();

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
                        filesBySize.computeIfAbsent(file.length(), k -> new HashSet<>()).add(file.toPath());
                    }
                }
            }
        }
    }

}