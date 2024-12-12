package v2;

import v1.FileComparator;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.file.*;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class FileDuplicateFinder {

    private final Map<Long, Set<Path>> filesBySize;   /* HashMap filesBySize - для хранения файлов, сгруппированных по размеру */
    private final List<List<String>> duplicates;     /* Список для хранения результата - групп дубликатов файлов */
    public static long FILES_SIZE_THRESHOLD = calculateMemoryPerThread() / 36; //getOptimalFilesSize() * 30; // Порог для больших файлов взят из Hashing. Тут порог кол-ва файлов в одном потоке в методе addFilesToTreeMap
    private static long SIZE_PATH = 0;
    private final ExecutorService executorService;
    private final Semaphore semaphore;

    /* Конструктор */
    public FileDuplicateFinder() {
        filesBySize = new HashMap<>();
        duplicates = new ArrayList<>();
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());  // Создаем пул потоков с количеством равным количеству доступных процессоров
        this.semaphore = new Semaphore(Runtime.getRuntime().availableProcessors());  // Создаем семафор с количеством разрешений равным количеству доступных процессоров
    }

    /* Основной метод для поиска групп дубликатов файлов
     * @return duplicates - список групп дубликатов файлов
     * @return path - путь к директории, в которой нужно найти дубликаты
    * */
    public void findDuplicates(String[] paths) throws IOException {
        //System.out.println("метод findDuplicates---------------------------- ");
        for(String path : paths) { // Рекурсивный обход директорий для группировки файлов по их размеру в карту filesBySize
            walkFileTree(path);
        }
        FILES_SIZE_THRESHOLD = SIZE_PATH  / (Runtime.getRuntime().availableProcessors()); // порог кол-ва файлов в одном потоке в методе addFilesToTreeMap зависит от размера папки деленного на кол-во процессоров
        findDuplicateGroups(); // Параллельная обработка файлов одинакового размера из карты filesBySize для поиска дубликатов в список duplicates
        shutdown(); // Завершение работы ExecutorService
    }


    /*  Находитгруппы побайтно одинаковых файлов из карты файлов, ключ которой — размер файла.*/
    /**
    * @throws IOException при возникновении ошибки ввода-вывода*/
    public void findDuplicateGroups() throws IOException {

        // список файлов с ключем ноль добавляем в список дубликатов и удаляем из filesBySize
        Set<Path> zeroSizeFiles = filesBySize.remove(0L);
        if (zeroSizeFiles != null) {
            duplicates.add(zeroSizeFiles.stream().map(Path::toString).toList());
        }

        // Список для хранения задач Future
        List<Future<Void>> futures = new ArrayList<>();
        // список для текущей группы спиcков (Set)  файлов
        List<Set<Path>> currentBatch = new ArrayList<>();
        // Переменная для хранения текущего суммарного размера групп файлов
        long currentBatchSize = 0;

        // перебираю ключи (размеры файлов)
        for (Long size : filesBySize.keySet()) {
            // Получаю список файлов для текущего размера
            Set<Path> files = filesBySize.get(size);
            if (files.size() < 2) {  // Если файлов меньше двух, перехожу к следующему размеру
                continue;
            }
            // Добавляем список файлов в текущую группу
            currentBatch.add(files);
            // Увеличиваем текущий суммарный размер групп файлов
            currentBatchSize += filesSize(files);
            // Если текущий суммарный размер группы файлов превышает LARGE_FILE_THRESHOLD
            if (currentBatchSize >= FILES_SIZE_THRESHOLD) {
                // Создаем копию текущей группы файлов для обработки в параллельном потоке
                final List<Set<Path>> batchToProcess = new ArrayList<>(currentBatch);
                // Создаем задачу для обработки группы файлов
                Future<Void> future = executorService.submit(() -> {
//                    // Захватываем семафор перед выполнением задачи
//                    semaphore.acquire();
//                    try {
                        for (Set<Path> sf : batchToProcess) {
                            findDuplicatesInSameSizeFiles(sf);  // выявляем дубликаты в группе файлов одинакового размера в список дубликатов duplicates
                        }
//                    } finally {
//                        // Освобождаем семафор после завершения задачи
//                        semaphore.release();
//                    }
                    return null;
                });
                // Добавляем задачу в список задач Future
                futures.add(future);
                // Очищаем текущую группу файлов
                currentBatch.clear();
                // Сбрасываем текущий суммарный размер группы файлов
                currentBatchSize = 0;
            }
        }
            // Если остались группы файлов в текущей группе, обрабатываем их
            if (!currentBatch.isEmpty()) {
                final List<Set<Path>> batchToProcess = new ArrayList<>(currentBatch);
                Future<Void> future = executorService.submit(() -> {
//                    // Захватываем семафор перед выполнением задачи
//                    semaphore.acquire();
//                    try {
                        for (Set<Path> sf : batchToProcess) {
                            findDuplicatesInSameSizeFiles(sf);  // Добавляем файл в fileByHash
                        }
//                    } finally {
//                        // Освобождаем семафор после завершения задачи
//                        semaphore.release();
//                    }
                    return null;
                });
                futures.add(future);
            }

            // Ожидаем завершения всех задач
            for (Future<Void> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!  Ошибка при обработке группы файлов: " + e.getMessage());
                }
            }

    }



    /**
     * Находит дубликаты файлов в списке файлов одинакового размера.
     *
     * @param files — список путей к файлам одинакового размера. Из HashMap filesBySize.
     * @throws IOException при возникновении ошибки ввода-вывода
     */
    public void findDuplicatesInSameSizeFiles(Set<Path> files) throws IOException {

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

            // Перебираем оставшиеся файлы в списке
            for (Path anotherFile : files) {
                // сравнение файлов
                if (FileComparator.areFilesEqual(file, anotherFile)) {
                    group.add(anotherFile.toString());
                    toRemove.add(anotherFile);
                }
            }

            // Удаляем найденные дубликаты из очереди
            files.removeAll(toRemove);
            // Добавляем группу дубликатов в список дубликатов (если группа содержит более одного файла)
            if (group.size() > 1) {
                duplicates.add(group);
            }
        }
    }



    /* Метод для рекурсивного обхода директории выполняет рекурсивный обход файловой системы,
    * начиная с указанного пути (path). Все файлы, найденные в процессе обхода, группируются по их размеру в HasyMap filesBySize.
    * @param path - путь к директории, с которой начинается обход файловой системы
     */
    public void walkFileTree(String path) {
        //System.out.println("метод walkFileTree---------------------------- ");
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
                        // накапливаем размер папки
                        SIZE_PATH += file.length();
                        // Если текущий файл не является директорией, добавляем его в карту
                        // Группируем файлы по их размеру
                        filesBySize.computeIfAbsent(file.length(), k -> new HashSet<>()).add(file.toPath());
                    }
                }
            }
        }
    }


    /* Геттер для получения карты файлов, сгруппированных по размеру */
    public Map<Long, Set<Path>> getFilesBySize() {
        return filesBySize;
    }
    /* Геттер для получения списка групп дубликатов файлов */
    public List<List<String>> getDuplicates() {
        return duplicates;
    }

    /* Метод для расчета объема памяти на один поток
     * альтернативный метод предыдущему
     */
    private static long calculateMemoryPerThread() {
        // Получаем количество доступных процессоров
        long availableProcessors = Runtime.getRuntime().availableProcessors();
        // Рассчитываем объем памяти на один поток
        return getTotalMemory() / availableProcessors;
    }

    /* Метод для получения общего объема памяти - вспомогательный метод для calculateMemoryPerThread */
    private static long getTotalMemory() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            return ((com.sun.management.OperatingSystemMXBean) osBean).getTotalMemorySize();
        } else {
            throw new UnsupportedOperationException("Unsupported OperatingSystemMXBean implementation");
        }
    }

    /* Метод вычисления суммарного размера файлов в списке */
    private static long filesSize(Set<Path> files) {
        long sum = 0;
        for (Path file : files) {
            sum += file.toFile().length();
        }
        return sum;
    }

    /*
    * Метод для корректного завершения работы ExecutorService
    */
    private void shutdown() {
        executorService.shutdown(); // Останавливаем прием новых задач
        try {
            // Ждем завершения текущих задач в течение 60 секунд
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow(); // Принудительно останавливаем все задачи
                // Ждем завершения принудительно остановленных задач в течение 60 секунд
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("ExecutorService не завершился корректно");
                }
            }
        } catch (InterruptedException ie) {
            executorService.shutdownNow(); // Принудительно останавливаем все задачи
            Thread.currentThread().interrupt(); // Восстанавливаем статус прерывания
        }
    }


}