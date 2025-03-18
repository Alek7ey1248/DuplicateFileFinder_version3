package compareAndHash2;

import processing.CheckValid;
import processing.FileGrouper;
import processing.Printer;
import processing.FileNameSimilarityChecker;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;


public class FileDuplicateFinder {

    private static final int NUM_PROCESSORS = Runtime.getRuntime().availableProcessors();  // Количество процессоров
    private static final long LARGE_FILE_THRESHOLD = getLargeFileThreshold()/428L; // Порог для больших файлов; // порог для больших файлов
    private final List<String> verifiedDirectories;  // Список всех абсолютных путей проверенных директорий

    private final CheckValid checkValid;  // класс для проверки валидности файлов и директорий
    private final FileNameSimilarityChecker fileNameSimilarityChecker;    // класс для проверки схожести имен файлов
    private final FileGrouper fileGrouper;  // класс для группировки файлов по хешу и контенту

    private final Map<Long, Set<File>> fileBySize;   // HashMap fileBySize - для хранения файлов, сгруппированных по размеру

    private List<Set<File>> duplicates; // Список групп дубликатов файлов - результат поиска дубликатов


    /* Конструктор */
    public FileDuplicateFinder() {
        this.checkValid = new CheckValid();
        this.verifiedDirectories = new ArrayList<>();
        this.fileNameSimilarityChecker = new FileNameSimilarityChecker();
        this.fileBySize = new ConcurrentHashMap<>();
        this.fileGrouper = new FileGrouper();
        this.duplicates = new ArrayList<>();
    }


    /* Основной метод для поиска групп дубликатов файлов
     * path - путь к директории, в которой нужно найти дубликаты
     */
    public void findDuplicates(String[] paths) throws IOException {
        for(String path : paths) {  // Рекурсивный обход директорий для группировки файлов по их размеру в карту filesBySize
            walkFileTree(path);
        }
        // Добавляем файлы в карту fileByKey и fileByContent из HashMap fileBySize
        processGroupFiles();
        // Получаем список групп дубликатов файлов, сортируем и выводим результат
        duplicates = Printer.duplicatesByHashAndContent(fileGrouper.getFilesByKey(), fileGrouper.getFilesByContent());
    }



    /* Ускореный метод для рекурсивного обхода директории выполняет рекурсивный обход файловой системы,
     * начиная с указанного пути (path). Все файлы, найденные в процессе обхода, группируются по их размеру в HashMap filesBySize.
     * @param path - путь к директории, с которой начинается обход файловой системы
     */
    public void walkFileTree(String path) {
        if (!checkValid.isValidDirectoryPath(path) || verifiedDirectories.contains(path)) {
            System.err.println("Невалидная директория или проверенная уже: " + path);
            return;
        }

        verifiedDirectories.add(path); // Добавляем проверенную директорию в список

        File directory = new File(path); // Создаем объект File(директорий) для указанного пути
        File[] files = directory.listFiles();  // Получаем список всех файлов и директорий в указанной директории

        if (files != null) {  // Проверяем, что массив не пустой
            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(); // Виртуальные потоки
            //CompletableFuture[] futures = new CompletableFuture[files.length]; // Массив для хранения CompletableFuture
            var futures = new CompletableFuture[files.length];

            for (int i = 0; i < files.length; i++) {  // Перебираем каждый файл и директорию в текущей директории
                final File file = files[i]; // Сохраняем ссылку на текущий файл в локальной переменной
                // ----------------------------------------------------------------------------------------
                futures[i] = CompletableFuture.runAsync(() -> {
                        if (file.isDirectory()) {  // Если текущий файл является директорией, рекурсивно вызываем walkFileTree
                            walkFileTree(file.getAbsolutePath());
                        } else {
                            if (!checkValid.isValidFile(file)) {  // Проверяем, что текущий файл является валидным
                                return;
                            }
                            // Добавляем файл в карту fileBySize по его размеру
                            long fileSize = file.length();
                            fileBySize.computeIfAbsent(fileSize, k -> ConcurrentHashMap.newKeySet()).add(file);
                        }
                }, executor);
                // ----------------------------------------------------------------------------------------
            }
            // Ожидаем завершения всех CompletableFuture
            CompletableFuture.allOf(futures).join();
            executor.shutdown();
        }
    }


    /* Добавление файлов в карту fileByKey или filesByContent в зависимости от логики метода processGroupFiles
     */
    public void processGroupFiles() {

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(); // Виртуальные потоки
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Список файлов одинакового размера
//        fileBySize.entrySet().forEach(entry -> {
        fileBySize.forEach((key, files) -> {

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {

                long sizeFile = key;     // Размер файла
                long numFiles = files.size();       // Количество файлов одинакового размера
                if (numFiles < 2) {  // Пропускаем списки файлов, которых меньше 2
                    return;
                }

                //----------------------------------------------------------------------------------
                switch ((numFiles == 2 || numFiles < NUM_PROCESSORS / 2.5  ? 1 :
                        ((numFiles > NUM_PROCESSORS / 2.5 && numFiles <= NUM_PROCESSORS * 1.8) ? 2 : 3))) {

                    case 1: // 2 - 6 файлов на большом компе  - numFiles >= 2 && numFiles < NUM_PROCESSORS / 2.5
                            // 2 - 4 на среднем(12 процессоров)
                            // 2 - на маленьком ноуте
                        if (sizeFile <= LARGE_FILE_THRESHOLD) {     // на большом до 300_000 байт = LARGE_FILE_THRESHOLD = 306549
                            fileGrouper.groupByContent(files);
                        } else {
                            boolean areFileNamesSimilar = fileNameSimilarityChecker.areFileNamesSimilar(files);
                            if (areFileNamesSimilar) {
                                fileGrouper.groupByHeshParallel(files);
                            } else {
                                fileGrouper.groupByContentParallel(files);
                            }
                        }
                        break;

                    case 2: // 8 - 30 файлов на большом компе - (numFiles > NUM_PROCESSORS / 2.5 && numFiles <= NUM_PROCESSORS * 1.8)
                            // 5 - 21 на среднем(12 процессоров)
                            // 2 - 7 на маленьком ноуте
                        fileGrouper.groupByHeshParallel(files);
                        break;

                    case 3: // 30+ файлов на большом компе
                        if (sizeFile < LARGE_FILE_THRESHOLD*30) {     // на большом до 9_196_470 байт(почти 10_000_000) = LARGE_FILE_THRESHOLD = 306549
                            fileGrouper.groupByHeshParallel(files);
                        } else {
                            fileGrouper.groupByContentParallel(files);
                        }
                        break;
                }
//-----------------------------------------------------------------------------------

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


    /* Метод для получения порога для больших файлов
        * @return порог для больших файлов
     */
    private static long getLargeFileThreshold() {
        long maxMemory = Runtime.getRuntime().maxMemory(); // Доступная память
        int availableProcessors = Runtime.getRuntime().availableProcessors(); // Количество доступных процессоров
        return maxMemory / (availableProcessors * 4L); // Возвращаем порог
    }

    // Возвращает карту файлов, сгруппированных по размеру - геттер
    Map<Long, Set<File>> getFilesBySize() {return fileBySize;}

    // Возвращает список групп дубликатов файлов - геттер
    List<Set<File>> getDuplicates() {return duplicates;}

}