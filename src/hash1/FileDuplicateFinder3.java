package hash1;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
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

    private  final CheckValid checkValid;
    private final Map<Long, Set<File>> fileBySize;   // HashMap fileBySize - для хранения файлов, сгруппированных по размеру
    //private final TreeMap<FileKey, Set<File>> fileByHash;    // HashMap fileByHash - для хранения файлов, сгруппированных по хешу. Ключ FileKey хранит размер и хеш файла
    private final ConcurrentSkipListMap<FileKey, Set<File>> fileByHash;  // вместо TreeMap используем ConcurrentSkipListMap для безопасности в многопоточной среде
    private final ExecutorService executorAddFilesToTreeMap;

    /* Конструктор */
    public FileDuplicateFinder3() {
        this.checkValid = new CheckValid();
        this.fileBySize = new HashMap<>();
        this.fileByHash = new ConcurrentSkipListMap<>();
        this.executorAddFilesToTreeMap = Executors.newVirtualThreadPerTaskExecutor();
    }


    /* Основной метод для поиска групп дубликатов файлов
     * @return duplicates - список групп дубликатов файлов
     * @return path - путь к директории, в которой нужно найти дубликаты
     * */
    public void findDuplicates(String[] paths) {

        for (String path : paths) { // Рекурсивный обход директорий для группировки файлов по их пазмеру в карту filesByHash
            walkFileTree(path);
        }

        //shutdown(executorWalkFileTree); // Завершаем работу ExecutorService
        addFilesToTreeMap(); // Добавляем файлы в TreeMap fileByHash из HashMap fileBySize
        shutdown(executorAddFilesToTreeMap); // Завершаем работу ExecutorService
        printDuplicateResults();  // Вывод групп дубликатов файлов в консоль

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
            //shutdown(executorWalkFileTree); // Завершаем работу ExecutorService
            //System.out.println("Обработана директория: " + directory.getAbsolutePath());
        }
    }


    /* Метод для добавления файлов в TreeMap fileByHash из HashMap fileBySize
        * При добавлении файлов в TreeMap fileByHash, файлы группируются по их хешу и размеру
        *   файлы подаются на обработку в параллельные потоки
     */
    public void addFilesToTreeMap() {

        // Проходим по всем записям в HashMap fileBySize
        for (Map.Entry<Long, Set<File>> entry : fileBySize.entrySet()) {
            // Получаем значение (Set<File>) для текущей записи
            Set<File> files = entry.getValue();
            // Проверяем, что в группе есть файлы больше одного
            if (files.size() > 1) {
                // Проходим по всем файлам в группе
                for (File file : files) {
                    executorAddFilesToTreeMap.submit(() -> {  // запускаем виртуальный поток
                        addFileToTreeMap(file);  // Добавляем файл в fileByHash
                    });
                }
            }
        }
    }


    /* Метод для добавления файла в мап fileByHash по ключу - хэшу
    * @param file - файл, который нужно добавить в мапу
     */
    public void addFileToTreeMap(File file) {
        FileKey fileKey = new FileKey(file); // Вычисляем хэш файла
        fileByHash.computeIfAbsent(fileKey, k -> new HashSet<>()).add(file); // Добавляем файл в мапу по хэшу и размеру
    }


    // выводит группы дубликатов файлов
    public void printDuplicateResults() {
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
    public static long getTotalMemory() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            return ((com.sun.management.OperatingSystemMXBean) osBean).getTotalMemorySize();
        } else {
            throw new UnsupportedOperationException("Unsupported OperatingSystemMXBean implementation");
        }
    }

    /**
     * Метод для корректного завершения работы ExecutorService
     */
    private void shutdown(ExecutorService executor) {
        executor.shutdown(); // Останавливаем прием новых задач
        try {
            // Ждем завершения текущих задач в течение 120 секунд
            if (!executor.awaitTermination(120, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Принудительно останавливаем все задачи
                // Ждем завершения принудительно остановленных задач в течение 60 секунд
                if (!executor.awaitTermination(120, TimeUnit.SECONDS)) {
                    System.err.println("ExecutorService не завершился корректно");
                }
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow(); // Принудительно останавливаем все задачи
            Thread.currentThread().interrupt(); // Восстанавливаем статус прерывания
        }
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {



    }

}



//        executorService.submit(() -> {
//        for (File f : batchToProcess) {
//addFileToTreeMap(f);  // Добавляем файл в fileByHash
//                                }
//                                        return null;
//                                        });
//                                        // Очищаем текущую группу файлов
//                                        currentBatch.clear();
//                    }