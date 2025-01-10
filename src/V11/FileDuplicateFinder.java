package V11;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

public class FileDuplicateFinder {

    private final CheckValid checkValid;

    /* HashMap filesBySize - для хранения файлов, сгруппированных по размеру */
    private final Map<Long, Set<Path>> filesBySize;
    public Map<Long, Set<Path>> getFilesBySize() {
        return filesBySize;
    }

    /* Список для хранения результата - групп дубликатов файлов */
    private final List<List<Path>> duplicates;
    public List<List<Path>> getDuplicates() {
        return duplicates;
    }

    //private final ExecutorService executor;


    /* Конструктор */
    public FileDuplicateFinder() {
        this.checkValid = new CheckValid();
        this.filesBySize = new HashMap<>();
        //duplicates = new ArrayList<>();
        this.duplicates = Collections.synchronizedList(new ArrayList<>());
        //this.executor = Executors.newVirtualThreadPerTaskExecutor();
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


    /* Метод для рекурсивного обхода директории выполняет рекурсивный обход файловой системы,
     * начиная с указанного пути (path). Все файлы, найденные в процессе обхода, группируются по их размеру в HasyMap filesBySize.
     * @param path - путь к директории, с которой начинается обход файловой системы
     */
    public void walkFileTree(String path) {

        // Создаем объект File(директорий) для указанного пути
        File directory = new File(path);

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


    /*  Находитгруппы побайтно одинаковых файлов из карты файлов, ключ которой — размер файла.*/
    /**
    * @throws IOException при возникновении ошибки ввода-вывода*/
    public void findDuplicateGroups() throws IOException {

        // Создаем ExecutorService с фиксированным пулом потоков
        //int numThreads = Runtime.getRuntime().availableProcessors();
        //ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

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
        try {
            // Ждем завершения всех задач в течение 60 секунд
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Принудительно завершаем все активные задачи
            }
        } catch (InterruptedException e) {
            executor.shutdownNow(); // Принудительно завершаем все активные задачи
            Thread.currentThread().interrupt(); // Восстанавливаем статус прерывания
        }
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
//        System.out.println("---------------------------------------------------------------");
//        System.out.println(" Проверяем группу файлов - " + Arrays.toString(files.toArray()));

        // Создаем ExecutorService с виртуальными потоками
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        while (files.size() > 1) {
            // Извлекаем первый файл
            Iterator<Path> iterator = files.iterator();
            Path file = iterator.next();
            iterator.remove();   // Удаляем первый файл из списка

            System.out.println("Проверка файла: " + file);
            //List<String> group = Collections.synchronizedList(new ArrayList<>());
            List<Path> group = new CopyOnWriteArrayList<>();   // потокобезопасный список для добавления путей к дубликатам
            group.add(file);   // Добавляем путь к первому файлу в группу дубликатов

            List<Path> toRemove = new CopyOnWriteArrayList<>();   // потокобезопасный список для удаления дубликатов из files

            // Список задач для параллельного выполнения
            List<Future<Boolean>> futures = new ArrayList<>();

            // Перебираем оставшиеся файлы в списке
            for (Path anotherFile : files) {
                if (file.equals(anotherFile)) {  // Если пути к файлам равны, пропускаем
                    continue;
                }

                // Отправляем задачу на сравнение файлов в пул потоков
                futures.add(executor.submit(() -> {
                    if (V11.FileComparator.areFilesEqual(file, anotherFile)) {
                            group.add(anotherFile);  // Добавляем путь к дубликату в группу дубликатов
                            toRemove.add(anotherFile);  // Добавляем путь к дубликату в список для удаления
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

            files.removeAll(toRemove);  // Удаляем дубликаты из списка файлов одинакового размера
            // Добавляем группу дубликатов в список дубликатов (если группа содержит более одного файла)
            if (group.size() > 1) {
                duplicates.add(group);
            }
        }

        // Завершаем работу ExecutorService
        executor.shutdown();
        try {
            // Ждем завершения всех задач в течение 60 секунд
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Принудительно завершаем все активные задачи
            }
        } catch (InterruptedException e) {
            executor.shutdownNow(); // Принудительно завершаем все активные задачи
            Thread.currentThread().interrupt(); // Восстанавливаем статус прерывания
        }
    }

}