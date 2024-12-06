package v3;

import java.io.File;
import java.io.IOException;
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

    private final Map<Long, Set<File>> fileBySize;   // HashMap fileBySize - для хранения файлов, сгруппированных по размеру
    private final TreeMap<FileKey, Set<File>> fileByHash;    // HashMap fileByHash - для хранения файлов, сгруппированных по хешу. Ключ FileKey хранит размер и хеш файла
    private final ExecutorService executorService;
    private static final int FILES_SIZE_THRESHOLD = getOptimalFilesSize() * 30;; // Порог для больших файлов взят из Hashing. Тут порог кол-ва файлов в одном потоке в методе addFilesToTreeMap

    /* Конструктор */
    public FileDuplicateFinder3() {
        fileBySize = new HashMap<>();
        fileByHash = new TreeMap<>();
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());  // Создаем пул потоков с количеством равным количеству доступных процессоров
    }


    /* Основной метод для поиска групп дубликатов файлов
     * @return duplicates - список групп дубликатов файлов
     * @return path - путь к директории, в которой нужно найти дубликаты
     * */
    public void findDuplicates(String[] paths) throws IOException {

        for (String path : paths) { // Рекурсивный обход директорий для группировки файлов по их пазмеру в карту filesByHash

            walkFileTree(path);
        }

        addFilesToTreeMap(); // Добавляем файлы в TreeMap fileByHash из HashMap fileBySize
        shutdown(); // Завершаем работу ExecutorService
        printDuplicateResults();  // Вывод групп дубликатов файлов в консоль

    }


    /* Метод для рекурсивного обхода директории выполняет рекурсивный обход файловой системы,
     * начиная с указанного пути (path). Все файлы, найденные в процессе обхода, группируются по их размеру в HasyMap filesBySize.
     * @param path - путь к директории, с которой начинается обход файловой системы
     */
    public void walkFileTree(String path) {

        File directory = new File(path); // Создаем объект File(директорий) для указанного пути
        File[] files = directory.listFiles();  // Получаем список всех файлов и директорий в указанной директории

        if (files != null) {  // Проверяем, что массив не пустой
            for (File file : files) {  // Перебираем каждый файл и директорию в текущей директории
                if (file.isDirectory()) {  // Если текущий файл является директорией, создаем новый поток для рекурсивного вызова walkFileTree
                    walkFileTree(file.getAbsolutePath());
                } else {
                    // Добавляем файл в карту fileBySize по его размеру
                    long fileSize = file.length();
                    fileBySize.computeIfAbsent(fileSize, k -> new HashSet<>()).add(file);
                }
            }
        }
    }

    /*
     * Метод добавления файлов в fileByHash из fileBySize для файлов, количество которых одного размера больше одного
     */
//    public void addFilesToTreeMap() {
//        // Проходим по всем записям в HashMap fileBySize
//        for (Map.Entry<Long, Set<File>> entry : fileBySize.entrySet()) {
//            // Получаем значение (Set<File>) для текущей записи
//            Set<File> files = entry.getValue();
//            // Проверяем, что в группе есть файлы больше одного
//            if (files.size() > 1) {
//                // Проходим по всем файлам в группе и добавляем их в TreeMap fileByHash
//                for (File file : files) {
//                    addFileToTreeMap(file);
//                }
//            }
//        }
//    }


    public void addFilesToTreeMap() {
        // Список для хранения задач Future
        List<Future<Void>> futures = new ArrayList<>();
        // Список для текущей группы файлов
        List<File> currentBatch = new ArrayList<>();
        // Переменная для хранения текущего суммарного размера группы файлов
        long currentBatchSize = 0;

        // Проходим по всем записям в HashMap fileBySize
        for (Map.Entry<Long, Set<File>> entry : fileBySize.entrySet()) {
            // Получаем значение (Set<File>) для текущей записи
            Set<File> files = entry.getValue();
            // Проверяем, что в группе есть файлы больше одного
            if (files.size() > 1) {
                // Проходим по всем файлам в группе
                for (File file : files) {
                    // Добавляем файл в текущую группу
                    currentBatch.add(file);
                    // Увеличиваем текущий суммарный размер группы файлов
                    currentBatchSize += file.length();

                    // Если текущий суммарный размер группы файлов превышает LARGE_FILE_THRESHOLD
                    if (currentBatchSize >= FILES_SIZE_THRESHOLD) {
                        // Создаем копию текущей группы файлов для обработки в параллельном потоке
                        final List<File> batchToProcess = new ArrayList<>(currentBatch);
                        // Создаем задачу для обработки группы файлов
                        Future<Void> future = executorService.submit(() -> {
                            for (File f : batchToProcess) {
                                addFileToTreeMap(f);
                            }
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
            }
        }

        // Если остались файлы в текущей группе, обрабатываем их
        if (!currentBatch.isEmpty()) {
            final List<File> batchToProcess = new ArrayList<>(currentBatch);
            Future<Void> future = executorService.submit(() -> {
                for (File f : batchToProcess) {
                    addFileToTreeMap(f);
                }
                return null;
            });
            futures.add(future);
        }

        // Ожидаем завершения всех задач
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }


    /* Метод для добавления файла в мапу по ключу - хэшу
    * @param file - файл, который нужно добавить в мапу
     */
    public void addFileToTreeMap(File file) {
        System.out.println("обрабатывается - : " + file.getName());
        FileKey fileKey = new FileKey(file, executorService); // Вычисляем хэш файла
        fileByHash.computeIfAbsent(fileKey, k -> new HashSet<>()).add(file); // Добавляем файл в мапу по хэшу и размеру
    }


    // выводит группы дубликатов файлов
    public void printDuplicateResults() throws IOException {
        // Проходим по всем записям в TreeMap fileByHash
        for (Map.Entry<FileKey, Set<File>> entry : fileByHash.entrySet()) {
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


    /* геттер для получения карты файлов по хешу */
    public TreeMap<FileKey, Set<File>> getFilesByHash() {
        return fileByHash;
    }

    /* Метод для получения оптимального порога величины суммарного
    * размера группы файлов запущеных в одном потоке
    */
    private static int getOptimalFilesSize() {
        // Получаем максимальное количество доступной памяти
        long maxMemory = Runtime.getRuntime().maxMemory();
        // Получаем количество доступных процессоров
        int availableProcessors = Runtime.getRuntime().availableProcessors();

        // Устанавливаем оптимальный размер файла как 1/4 от максимальной памяти, деленной на количество процессоров
        return (int) (maxMemory / (availableProcessors * 4));
    }

    /**
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

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

       System.out.println("FILES_SIZE_THRESHOLD: " + FILES_SIZE_THRESHOLD);


    }

}