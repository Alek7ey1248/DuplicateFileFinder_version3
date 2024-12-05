package v3;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/*
 * LARGE_FILE_THRESHOLD: Определяем порог для больших файлов на основе доступной памяти.
 * findDuplicates: Основной метод для поиска дубликатов файлов в указанных директориях.
 * walkFileTree: Рекурсивный обход директорий для группировки файлов по их размеру или хешу.
 * findLargeDuplicates: Метод для поиска дубликатов среди больших файлов.
 * createDuplicatesList: Метод для создания списка дубликатов из карт filesByHash и largeFilesBySize.
 * getDuplicates: Метод для получения списка дубликатов.
 * */

public class FileDuplicateFinder3 {

    private final Map<Long, Set<File>> fileBySize = new HashMap<>();   // HashMap fileBySize - для хранения файлов, сгруппированных по размеру
    private final TreeMap<FileKey, Set<File>> fileByHash;    // HashMap fileByHash - для хранения файлов, сгруппированных по хешу. Ключ FileKey хранит размер и хеш файла
    private final ExecutorService executorService;

    /* Конструктор */
    public FileDuplicateFinder3() {
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
    public void addFilesToTreeMap() {
        // Проходим по всем записям в HashMap fileBySize
        for (Map.Entry<Long, Set<File>> entry : fileBySize.entrySet()) {
            // Получаем значение (Set<File>) для текущей записи
            Set<File> files = entry.getValue();
            // Проверяем, что в группе есть файлы больше одного
            if (files.size() > 1) {
                // Проходим по всем файлам в группе и добавляем их в TreeMap fileByHash
                for (File file : files) {
                    addFileToTreeMap(file);
                }
            }
        }
    }


    /* Метод для добавления файла в мапу по ключу - хэшу
    * @param file - файл, который нужно добавить в мапу
     */
    public void addFileToTreeMap(File file) {
        System.out.println("обрабатывается - : " + file.getName());

        // Вычисляем хэш файла
        FileKey fileKey = new FileKey(file, executorService);

        fileByHash.computeIfAbsent(fileKey, k -> new HashSet<>()).add(file);
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

}